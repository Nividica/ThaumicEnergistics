package thaumicenergistics.common.tiles.abstraction;

import appeng.api.config.Actionable;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.api.tiles.IEssentiaTransportWithSimulate;
import thaumicenergistics.common.storage.AspectStack;
import thaumicenergistics.common.utils.EffectiveSide;

/**
 * Essentia Vibration Chamber Base
 * Handles most of the mod-interface functionality.
 *
 * @author Nividica
 *
 */
public abstract class TileEVCBase extends AENetworkTile implements IEssentiaTransportWithSimulate, IAspectSource {
    /**
     * NBT Key for the stored aspect stack.
     */
    public static final String NBTKEY_STORED = "StoredEssentia";

    /**
     * The maximum amount of stored essentia.
     */
    public static final int MAX_ESSENTIA_STORED = 64;

    /**
     * Maximum reciprocal
     */
    public static final float MAX_ESSENTIA_STORED_RECIPROCAL = 1.0f / MAX_ESSENTIA_STORED;

    /**
     * Stored Essentia
     */
    protected IAspectStack storedEssentia = null;

    /**
     * Returns true if the EVC accepts the specified aspect.
     *
     * @param aspect
     * @return
     */
    public static boolean acceptsAspect(final Aspect aspect) {
        return ((aspect == Aspect.FIRE) || (aspect == Aspect.ENERGY));
    }

    /**
     * Add essentia to the EVC.
     *
     * @param aspect
     * @param amount
     * @param mode
     * @return
     */
    protected abstract int addEssentia(final Aspect aspect, final int amount, final Actionable mode);

    @Override
    protected ItemStack getItemFromTile(final Object obj) {
        // Return the itemstack that visually represents this tile
        return ThEApi.instance().blocks().EssentiaVibrationChamber.getStack();
    }

    /**
     * Returns true if there is any stored essentia.
     *
     * @return
     */
    protected boolean hasStoredEssentia() {
        return (this.storedEssentia != null) && (!this.storedEssentia.isEmpty());
    }

    protected abstract void NBTRead(NBTTagCompound data);

    protected abstract void NBTWrite(NBTTagCompound data);

    @SideOnly(Side.CLIENT)
    protected abstract void networkRead(ByteBuf stream);

    protected abstract void networkWrite(ByteBuf stream);

    @Override
    public int addEssentia(final Aspect aspect, final int amount, final ForgeDirection side) {
        return this.addEssentia(aspect, amount, Actionable.MODULATE);
    }

    @Override
    public int addEssentia(final Aspect aspect, final int amount, final ForgeDirection side, final Actionable mode) {
        return this.addEssentia(aspect, amount, mode);
    }

    @Override
    public int addToContainer(final Aspect aspect, final int amount) {
        return this.addEssentia(aspect, amount, Actionable.MODULATE);
    }

    @Override
    public boolean canInputFrom(final ForgeDirection side) {
        return (side != this.getForward());
    }

    /**
     * Can not output.
     */
    @Override
    public boolean canOutputTo(final ForgeDirection side) {
        return false;
    }

    @Override
    public int containerContains(final Aspect aspect) {
        int storedAmount = 0;

        // Is the aspect stored?
        if ((this.hasStoredEssentia()) && (this.storedEssentia.getAspect() == aspect)) {
            storedAmount = (int) this.storedEssentia.getStackSize();
        }

        return storedAmount;
    }

    @Override
    public boolean doesContainerAccept(final Aspect aspect) {
        // Is there stored essentia?
        if (this.hasStoredEssentia()) {
            // Match to stored essentia
            return aspect == this.storedEssentia.getAspect();
        }

        // Nothing is stored, accepts ignis or potentia
        return TileEVCBase.acceptsAspect(aspect);
    }

    @Deprecated
    @Override
    public boolean doesContainerContain(final AspectList aspectList) {
        // Is there not stored essentia?
        if (!this.hasStoredEssentia()) {
            return false;
        }

        return aspectList.aspects.containsKey(this.storedEssentia.getAspect());
    }

    @Override
    public boolean doesContainerContainAmount(final Aspect aspect, final int amount) {
        // Does the stored essentia match the aspect?
        if ((this.storedEssentia == null) || (this.storedEssentia.getAspect() != aspect)) {
            // Does not match
            return false;
        }

        return (this.storedEssentia.getStackSize() >= amount);
    }

    @Override
    public AspectList getAspects() {
        // Create a new list
        AspectList aspectList = new AspectList();

        // Is there stored essentia?
        if (this.hasStoredEssentia()) {
            // Add the essentia aspect and amount
            aspectList.add(this.storedEssentia.getAspect(), (int) this.storedEssentia.getStackSize());
        }

        return aspectList;
    }

    @Override
    public AECableType getCableConnectionType(final ForgeDirection dir) {
        return AECableType.COVERED;
    }

    @Override
    public int getEssentiaAmount(final ForgeDirection side) {
        return (this.hasStoredEssentia() ? (int) this.storedEssentia.getStackSize() : 0);
    }

    @Override
    public Aspect getEssentiaType(final ForgeDirection side) {
        return (this.hasStoredEssentia() ? this.storedEssentia.getAspect() : null);
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    /**
     * Can not output.
     */
    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int getSuctionAmount(final ForgeDirection side) {
        // Suction is based on how full the chamber is, as it fills up suction drops

        // Get how much is stored
        float stored = (this.storedEssentia == null ? 0.0f : this.storedEssentia.getStackSize());
        if (stored == MAX_ESSENTIA_STORED) {
            return 0;
        }

        // Calculate the ratio, minimum of 25%, and multiply against maximum suction
        return (int) (128 * (1.0f - ((stored * 0.75f) * MAX_ESSENTIA_STORED_RECIPROCAL)));
    }

    @Override
    public Aspect getSuctionType(final ForgeDirection side) {
        // Is there anything stored?
        if (this.hasStoredEssentia()) {
            // Suction type must match what is stored
            return this.storedEssentia.getAspect();
        }

        // Rotate into Potentia?
        if ((MinecraftServer.getServer().getTickCounter() % 200) > 100) {
            // Set to Potentia
            return Aspect.ENERGY;
        }

        // Default to Ignis
        return Aspect.FIRE;
    }

    @Override
    public boolean isConnectable(final ForgeDirection side) {
        return (side != this.getForward());
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public final void onNBTLoad(final NBTTagCompound data) {
        // Is there essentia stored?
        if (data.hasKey(TileEVCBase.NBTKEY_STORED)) {
            // Load the stack
            this.storedEssentia = AspectStack.loadAspectStackFromNBT(data.getCompoundTag(TileEVCBase.NBTKEY_STORED));
        }

        // Call sub
        this.NBTRead(data);
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public final void onNBTSave(final NBTTagCompound data) {
        // Save storage
        if (this.hasStoredEssentia()) {
            // Save stack
            NBTTagCompound stack = new NBTTagCompound();
            this.storedEssentia.writeToNBT(stack);

            // Write into data
            data.setTag(TileEVCBase.NBTKEY_STORED, stack);
        }

        // Call sub
        this.NBTWrite(data);
    }

    @TileEvent(TileEventType.NETWORK_READ)
    @SideOnly(Side.CLIENT)
    public final boolean onNetworkRead(final ByteBuf stream) {
        // Anything stored?
        if (stream.readBoolean()) {
            // Is the local copy null?
            if (this.storedEssentia == null) {
                // Create the stack from the stream
                this.storedEssentia = AspectStack.loadAspectStackFromStream(stream);
            } else {
                // Update the stack from the stream
                this.storedEssentia.readFromStream(stream);
            }
        } else {
            // Null out the stack
            this.storedEssentia = null;
        }

        // Call sub
        this.networkRead(stream);

        return true;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public final void onNetworkWrite(final ByteBuf stream) throws IOException {
        // Is there anything stored?
        boolean hasStored = this.storedEssentia != null;

        // Write stored
        stream.writeBoolean(hasStored);
        if (hasStored) {
            // Write the stack
            this.storedEssentia.writeToStream(stream);
        }

        // Call sub
        this.networkWrite(stream);
    }

    /**
     * Sets up the chamber
     *
     * @return
     */
    @Override
    public void onReady() {
        // Call super
        super.onReady();

        // Ignored on client side
        if (EffectiveSide.isServerSide()) {
            // Set idle power usage to zero
            this.getProxy().setIdlePowerUsage(0.0D);
        }
    }

    /**
     * Full block, not extension needed.
     */
    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    public void setAspects(final AspectList aspectList) {
        // Ignored
    }

    /**
     * Sets the owner of this tile.
     *
     * @param player
     */
    public void setOwner(final EntityPlayer player) {
        this.getProxy().setOwner(player);
    }

    @Override
    public void setSuction(final Aspect aspect, final int amount) {
        // Ignored
    }

    /**
     * Can not output.
     */
    @Override
    public int takeEssentia(final Aspect aspect, final int amount, final ForgeDirection side) {
        return 0;
    }

    /**
     * Can not output.
     */
    @Override
    public boolean takeFromContainer(final Aspect aspect, final int amount) {
        return false;
    }

    /**
     * Can not output.
     */
    @Deprecated
    @Override
    public boolean takeFromContainer(final AspectList arg0) {
        return false;
    }
}
