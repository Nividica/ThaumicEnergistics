package thaumicenergistics.common.parts;

import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.IEssentiaWatcher;
import thaumicenergistics.api.grid.IEssentiaWatcherHost;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.client.gui.GuiEssentiaLevelEmitter;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.container.ContainerPartEssentiaLevelEmitter;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.common.network.IAspectSlotPart;
import thaumicenergistics.common.registries.EnumCache;
import thaumicenergistics.common.utils.EffectiveSide;

/**
 * Emits redstone signals based on networked essentia levels.
 *
 * @author Nividica
 *
 */
public class PartEssentiaLevelEmitter extends ThEPartBase implements IAspectSlotPart, IEssentiaWatcherHost {
    /**
     * How much AE power is required to keep the part active.
     */
    private static final double IDLE_POWER_DRAIN = 0.3D;

    /**
     * NBT keys.
     */
    private static final String NBT_KEY_ASPECT_FILTER = "aspect",
            NBT_KEY_REDSTONE_MODE = "mode",
            NBT_KEY_WANTED_AMOUNT = "wantedAmount",
            NBT_KEY_IS_EMITTING = "emitting";

    /**
     * Default redstone mode the part starts with.
     */
    private static final RedstoneMode DEFAULT_REDSTONE_MODE = RedstoneMode.HIGH_SIGNAL;

    /**
     * Dimensions and offsets
     */
    private static final float Base_XY_Min = 7.0F,
            Base_XY_Max = 9.0F,
            Base_Z_Min = 11.0F,
            Base_Z_Max = 13.0F,
            // Head Z's
            Head_Z_Min = PartEssentiaLevelEmitter.Base_Z_Max,
            Head_Z_Max = PartEssentiaLevelEmitter.Head_Z_Min + 2.0F,
            // Face Z's
            Face_Z_Min = PartEssentiaLevelEmitter.Head_Z_Min - 1.0F,
            FaceZMax = PartEssentiaLevelEmitter.Head_Z_Max + 1.0F,
            // Face offsets
            FaceOffset_XH_YV = 0.001F,
            FaceOffset_XV_YH = 1.0F,
            // Horizontal faces XY
            Face_XH_Min = PartEssentiaLevelEmitter.Base_XY_Min - PartEssentiaLevelEmitter.FaceOffset_XH_YV,
            Face_YH_Min = PartEssentiaLevelEmitter.Base_XY_Min - PartEssentiaLevelEmitter.FaceOffset_XV_YH,
            Face_XH_Max = PartEssentiaLevelEmitter.Base_XY_Max + PartEssentiaLevelEmitter.FaceOffset_XH_YV,
            Face_YH_Max = PartEssentiaLevelEmitter.Base_XY_Max + PartEssentiaLevelEmitter.FaceOffset_XV_YH,
            // Vertical faces XY
            Face_XV_Min = PartEssentiaLevelEmitter.Base_XY_Min - PartEssentiaLevelEmitter.FaceOffset_XV_YH,
            Face_YV_Min = PartEssentiaLevelEmitter.Base_XY_Min - PartEssentiaLevelEmitter.FaceOffset_XH_YV,
            Face_XV_Max = PartEssentiaLevelEmitter.Base_XY_Max + PartEssentiaLevelEmitter.FaceOffset_XV_YH,
            Face_YV_Max = PartEssentiaLevelEmitter.Base_XY_Max + PartEssentiaLevelEmitter.FaceOffset_XH_YV;

    /**
     * Aspect we are watching.
     */
    private Aspect trackedAspect;

    /**
     * Mode the emitter is in
     */
    private RedstoneMode redstoneMode = PartEssentiaLevelEmitter.DEFAULT_REDSTONE_MODE;

    /**
     * Threshold value
     */
    private long thresholdLevel = 0;

    /**
     * Current value
     */
    private long currentLevel;

    /**
     * True if the emitter is emitting a redstone signal.
     */
    private boolean isEmitting = false;

    /**
     * Watches the for changes in the essentia grid.
     */
    private IEssentiaWatcher essentiaWatcher;

    /**
     * Creates the part
     */
    public PartEssentiaLevelEmitter() {
        super(AEPartsEnum.EssentiaLevelEmitter, SecurityPermissions.BUILD);
    }

    /**
     * Updates the watcher to the tracked essentia.
     */
    private void configureWatcher() {
        boolean didSet = false;

        // this.debugTrackingStage = 0;
        // this.debugMonID = -1;

        if (this.essentiaWatcher != null) {
            // this.debugTrackingStage = 1;

            // Clear any existing watched value
            this.essentiaWatcher.clear();

            // Is there an essentia being tracked?
            if (this.trackedAspect != null) {
                // this.debugTrackingStage = 2;
                // Configure the watcher
                this.essentiaWatcher.add(this.trackedAspect);

                // Get the essentia monitor
                IMEEssentiaMonitor essMon = this.getGridBlock().getEssentiaMonitor();

                // Ensure there is a grid.
                if (essMon != null) {
                    // Update the amount.
                    this.setCurrentLevel(essMon.getEssentiaAmount(this.trackedAspect));
                    didSet = true;
                    // this.debugTrackingStage = 3;
                    // this.debugMonID = essMon.hashCode();

                }
            }
        }

        // Was the current amount set?
        if (!didSet) {
            // Reset
            this.setCurrentLevel(0);
        }
    }

    /**
     * Sets the current amount in the network, of the aspect
     * we are watching/filtering.
     *
     * @param amount
     */
    private void setCurrentLevel(final long amount) {
        // Has the amount changed?
        if (amount != this.currentLevel) {
            // Set the current amount
            this.currentLevel = amount;

            // Check if we should be emitting
            this.updateEmittingState();
        }
    }

    /**
     * Checks if the emitter should be emitting a redstone signal.
     *
     * @return
     */
    private void updateEmittingState() {
        boolean emitting = false;

        // Are we active?
        if (!this.isActive()) {
            // In the event that we have lost activity, do not change state
            return;
        }

        switch (this.redstoneMode) {
            case HIGH_SIGNAL:
                // Is the current amount more than or equal to the wanted amount?
                emitting = (this.currentLevel >= this.thresholdLevel);
                break;

            case LOW_SIGNAL:
                // Is the current amount less than the wanted amount?
                emitting = (this.currentLevel < this.thresholdLevel);
                break;

            case IGNORE:
            case SIGNAL_PULSE:
                break;
        }

        // Did the emitting state change?
        if (emitting != this.isEmitting) {
            // Set the new state
            this.isEmitting = emitting;

            // Mark that we need to be saved and updated
            this.markForSave();
            this.markForUpdate();

            // Get the host tile entity & side
            TileEntity hte = this.getHostTile();
            ForgeDirection side = this.getSide();

            // Update the neighbors
            Platform.notifyBlocksOfNeighbors(hte.getWorldObj(), hte.xCoord, hte.yCoord, hte.zCoord);
            Platform.notifyBlocksOfNeighbors(
                    hte.getWorldObj(), hte.xCoord + side.offsetX, hte.yCoord + side.offsetX, hte.zCoord + side.offsetX);
        }
    }

    /**
     * How far the network cable should extend to meet us.
     */
    @Override
    public int cableConnectionRenderTo() {
        return 8;
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper helper) {
        helper.addBox(
                PartEssentiaLevelEmitter.Base_XY_Min,
                PartEssentiaLevelEmitter.Base_XY_Min,
                PartEssentiaLevelEmitter.Base_Z_Min,
                PartEssentiaLevelEmitter.Base_XY_Max,
                PartEssentiaLevelEmitter.Base_XY_Max,
                PartEssentiaLevelEmitter.Head_Z_Max);
    }

    @Override
    public IIcon getBreakingTexture() {
        return BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[1];
    }

    /**
     * Gets the emitter gui
     */
    @Override
    public Object getClientGuiElement(final EntityPlayer player) {
        return new GuiEssentiaLevelEmitter(this, player);
    }

    @Override
    public double getIdlePowerUsage() {
        return PartEssentiaLevelEmitter.IDLE_POWER_DRAIN;
    }

    /**
     * Light level based on if emitter is emitting.
     */
    @Override
    public int getLightLevel() {
        return (this.isEmitting ? 7 : 0);
    }

    /**
     * Returns the redstone mode.
     *
     * @return
     */
    public RedstoneMode getRedstoneMode() {
        return this.redstoneMode;
    }

    /**
     * Gets the emitter container
     */
    @Override
    public Object getServerGuiElement(final EntityPlayer player) {
        return new ContainerPartEssentiaLevelEmitter(this, player);
    }

    /**
     * Returns the threshold level.
     *
     * @return
     */
    public long getThresholdLevel() {
        return this.thresholdLevel;
    }

    /**
     * Returns the aspect being tracked.
     *
     * @return
     */
    @Nullable
    public Aspect getTrackedAspect() {
        return this.trackedAspect;
    }

    /**
     * Called to see if this is emitting strong redstone power
     */
    @Override
    public int isProvidingStrongPower() {
        return this.isEmitting ? 15 : 0;
    }

    /**
     * Called to see if this is emitting weak redstone power
     */
    @Override
    public int isProvidingWeakPower() {
        return this.isProvidingStrongPower();
    }

    /**
     * Called when a player has adjusted the amount wanted via
     * gui buttons.
     *
     * @param adjustmentAmount
     * @param player
     */
    public void onAdjustThresholdLevel(final int adjustmentAmount, final EntityPlayer player) {
        this.onSetThresholdLevel(this.thresholdLevel + adjustmentAmount, player);
    }

    /**
     * Called when a player has pressed the redstone toggle button in the gui.
     *
     * @param player
     */
    public void onClientToggleRedstoneMode(final EntityPlayer player) {
        switch (this.redstoneMode) {
            case HIGH_SIGNAL:
                this.redstoneMode = RedstoneMode.LOW_SIGNAL;
                break;

            case LOW_SIGNAL:
                this.redstoneMode = RedstoneMode.HIGH_SIGNAL;
                break;

            case IGNORE:
            case SIGNAL_PULSE:
                break;
        }

        // Check if we should be emitting
        this.updateEmittingState();
    }

    /**
     * Called when essentia levels change.
     */
    @Override
    public void onEssentiaChange(final Aspect aspect, final long storedAmount, final long changeAmount) {
        this.setCurrentLevel(storedAmount);
    }

    /**
     * Called when a player has changed the wanted amount
     *
     * @param threshold
     * @param player
     */
    public void onSetThresholdLevel(final long threshold, final EntityPlayer player) {
        // Set the wanted amount
        this.thresholdLevel = threshold;

        // Bounds check it
        if (this.thresholdLevel < 0L) {
            this.thresholdLevel = 0L;
        } else if (this.thresholdLevel > 9999999999L) {
            this.thresholdLevel = 9999999999L;
        }

        // Mark that we need saving
        this.markForSave();

        // Check if we should be emitting
        this.updateEmittingState();
    }

    /**
     * Spawns redstone particles when emitting
     */
    @Override
    public void randomDisplayTick(final World world, final int x, final int y, final int z, final Random r) {
        // Is the emitter, emitting?
        if (this.isEmitting) {
            // Get the side
            ForgeDirection side = this.getSide();

            // Calculate a new random coordinate
            double particleX = (side.offsetX * 0.45F) + ((r.nextFloat() - 0.5F) * 0.2D);
            double particleY = (side.offsetY * 0.45F) + ((r.nextFloat() - 0.5F) * 0.2D);
            double particleZ = (side.offsetZ * 0.45F) + ((r.nextFloat() - 0.5F) * 0.2D);

            world.spawnParticle(
                    "reddust", 0.5D + x + particleX, 0.5D + y + particleY, 0.5D + z + particleZ, 0.0D, 0.0D, 0.0D);
        }
    }

    /**
     * Reads the state of the emitter from an NBT tag
     */
    @Override
    public void readFromNBT(final NBTTagCompound data) {
        // Call super
        super.readFromNBT(data);

        // Read the filter
        if (data.hasKey(PartEssentiaLevelEmitter.NBT_KEY_ASPECT_FILTER)) {
            this.trackedAspect = Aspect.aspects.get(data.getString(PartEssentiaLevelEmitter.NBT_KEY_ASPECT_FILTER));
        }

        // Read the redstone mode
        if (data.hasKey(PartEssentiaLevelEmitter.NBT_KEY_REDSTONE_MODE)) {
            this.redstoneMode =
                    EnumCache.AE_REDSTONE_MODES[data.getInteger(PartEssentiaLevelEmitter.NBT_KEY_REDSTONE_MODE)];
        }

        // Read the wanted amount
        if (data.hasKey(PartEssentiaLevelEmitter.NBT_KEY_WANTED_AMOUNT)) {
            this.thresholdLevel = data.getLong(PartEssentiaLevelEmitter.NBT_KEY_WANTED_AMOUNT);
        }

        // Read if emitting
        if (data.hasKey(PartEssentiaLevelEmitter.NBT_KEY_IS_EMITTING)) {
            this.isEmitting = data.getBoolean(PartEssentiaLevelEmitter.NBT_KEY_IS_EMITTING);
        }
    }

    /**
     * Called client side when a sync packet has been received.
     *
     * @throws IOException
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean readFromStream(final ByteBuf stream) throws IOException {
        boolean redraw = false;

        // Cache the old emitting
        boolean oldEmit = this.isEmitting;

        // Call super
        redraw |= super.readFromStream(stream);

        // Read the activity state
        this.isEmitting = stream.readBoolean();

        // Redraw if changed
        redraw |= (this.isEmitting != oldEmit);

        return redraw;
    }

    /**
     * Renders the emitter in the inventory
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventory(final IPartRenderHelper helper, final RenderBlocks renderer) {
        // Set the base texture
        helper.setTexture(BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[0]);
        helper.setBounds(7.0F, 1.0F, 14.0F, 9.0F, 7.0F, 16.0F);
        helper.renderInventoryBox(renderer);

        // Set the active texture
        helper.setTexture(BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[1]);
        helper.setBounds(7.0F, 7.0F, 14.0F, 9.0F, 9.0F, 16.0F);
        helper.renderInventoryBox(renderer);
    }

    /**
     * Renders the emitter in the world
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void renderStatic(
            final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer) {
        // Set the base texture
        helper.setTexture(BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[0]);

        // Set shaft bounds
        helper.setBounds(
                PartEssentiaLevelEmitter.Base_XY_Min,
                PartEssentiaLevelEmitter.Base_XY_Min,
                PartEssentiaLevelEmitter.Base_Z_Min,
                PartEssentiaLevelEmitter.Base_XY_Max,
                PartEssentiaLevelEmitter.Base_XY_Max,
                PartEssentiaLevelEmitter.Base_Z_Max);

        // Render shaft
        helper.renderBlock(x, y, z, renderer);

        // Is the part emitting?
        if (this.isEmitting) {
            // Set the active texture
            IIcon activeTex = BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[1];
            helper.setTexture(activeTex);

            // Set the brightness
            Tessellator.instance.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            Tessellator.instance.setBrightness(0xD000D0);

            // Render horizontal faces
            helper.setBounds(
                    PartEssentiaLevelEmitter.Face_XH_Min,
                    PartEssentiaLevelEmitter.Face_YH_Min,
                    PartEssentiaLevelEmitter.Face_Z_Min,
                    PartEssentiaLevelEmitter.Face_XH_Max,
                    PartEssentiaLevelEmitter.Face_YH_Max,
                    PartEssentiaLevelEmitter.FaceZMax);
            helper.renderFace(x, y, z, activeTex, ForgeDirection.EAST, renderer);
            helper.renderFace(x, y, z, activeTex, ForgeDirection.WEST, renderer);

            // Render vertical faces
            helper.setBounds(
                    PartEssentiaLevelEmitter.Face_XV_Min,
                    PartEssentiaLevelEmitter.Face_YV_Min,
                    PartEssentiaLevelEmitter.Face_Z_Min,
                    PartEssentiaLevelEmitter.Face_XV_Max,
                    PartEssentiaLevelEmitter.Face_YV_Max,
                    PartEssentiaLevelEmitter.FaceZMax);
            helper.renderFace(x, y, z, activeTex, ForgeDirection.UP, renderer);
            helper.renderFace(x, y, z, activeTex, ForgeDirection.DOWN, renderer);

        } else {
            // Set the inactive texture
            helper.setTexture(BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[2]);
        }

        // Set head bounds
        helper.setBounds(
                PartEssentiaLevelEmitter.Base_XY_Min,
                PartEssentiaLevelEmitter.Base_XY_Min,
                PartEssentiaLevelEmitter.Head_Z_Min,
                PartEssentiaLevelEmitter.Base_XY_Max,
                PartEssentiaLevelEmitter.Base_XY_Max,
                PartEssentiaLevelEmitter.Head_Z_Max);

        // Render head
        helper.renderBlock(x, y, z, renderer);
    }

    /**
     * Set's the aspect we are filtering
     */
    @Override
    public void setAspect(final int index, final Aspect aspect, final EntityPlayer player) {
        if (this.trackedAspect != aspect) {
            // Set the filtered aspect
            this.trackedAspect = aspect;

            // Are we client side?
            if (EffectiveSide.isClientSide()) {
                return;
            }

            // Mark that we need to be saved and updated
            this.markForSave();

            // Update the watcher
            this.configureWatcher();
        }
    }

    /**
     * Called from the container to set the filter based on an itemstack.
     *
     * @param player
     * @param itemStack
     * @return
     */
    public boolean setFilteredAspectFromItemstack(final EntityPlayer player, final ItemStack itemStack) {
        // Get the aspect
        Aspect itemAspect = EssentiaItemContainerHelper.INSTANCE.getFilterAspectFromItem(itemStack);

        // Ensure we got an aspect
        if (itemAspect == null) {
            return false;
        }

        // Set the aspect
        this.setAspect(0, itemAspect, player);

        return true;
    }

    @Override
    public void updateWatcher(final IEssentiaWatcher newWatcher) {
        // Set the watcher
        this.essentiaWatcher = newWatcher;

        // And configure it
        this.configureWatcher();
    }

    /**
     * Writes the state of the emitter to the tag
     */
    @Override
    public void writeToNBT(final NBTTagCompound data, final PartItemStack saveType) {
        // Call super
        super.writeToNBT(data, saveType);

        // Only write NBT data if saving, or wrenched.
        if ((saveType != PartItemStack.World) && (saveType != PartItemStack.Wrench)) {
            return;
        }

        // Is there a filter?
        if (this.trackedAspect != null) {
            // Write the name of the aspect
            data.setString(PartEssentiaLevelEmitter.NBT_KEY_ASPECT_FILTER, this.trackedAspect.getTag());
        }

        // Only save the rest on world save, or if there is a tracked aspect
        if ((saveType == PartItemStack.World) || (this.trackedAspect != null)) {
            // Write the redstone mode ordinal
            if (this.redstoneMode != PartEssentiaLevelEmitter.DEFAULT_REDSTONE_MODE) {
                data.setInteger(PartEssentiaLevelEmitter.NBT_KEY_REDSTONE_MODE, this.redstoneMode.ordinal());
            }

            // Write the threshold amount
            if (this.thresholdLevel > 0) {
                data.setLong(PartEssentiaLevelEmitter.NBT_KEY_WANTED_AMOUNT, this.thresholdLevel);
            }
        }

        // Write if emitting
        if ((saveType == PartItemStack.World) && this.isEmitting) {
            data.setBoolean(PartEssentiaLevelEmitter.NBT_KEY_IS_EMITTING, true);
        }
    }

    /**
     * Called when a packet to sync client and server is being created.
     *
     * @throws IOException
     */
    @Override
    public void writeToStream(final ByteBuf stream) throws IOException {
        // Call super
        super.writeToStream(stream);

        // Write the activity state
        stream.writeBoolean(this.isEmitting);
    }
}
