package thaumicenergistics.common.tiles;

import appeng.api.implementations.tiles.ICrankable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.common.blocks.BlockGolemGearBox;
import thaumicenergistics.common.utils.EffectiveSide;

/**
 * Unbreakable crank.
 *
 * @author Nividica
 *
 */
public class TileGearBox extends TileEntity {
    /**
     * Amount of power generated each time the gearbox is cranked.
     * This power is divided among the connected shafts.
     */
    private static final int BASE_POWER = 6;

    /**
     * How much shaft power is required to apply a turn?
     */
    private static final int REQUIRED_POWER = 18;

    /**
     * Number of valid sides.
     */
    private static final int SIDE_COUNT = ForgeDirection.VALID_DIRECTIONS.length;

    /**
     * The number of ticks required to pass before a sync packet can be sent.
     */
    private static final int MIN_TICKS_PER_SYNC = 6;

    /**
     * One full rotation(PI)
     */
    private static final float FULL_ROTATION = (float) Math.PI;

    /**
     * NBT Keys
     */
    private static final String NBT_KEY_CRANKABLES = "crankables",
            NBT_KEY_ROTATION = "shaftrotation",
            NBT_KEY_ISTHAUMBOX = "isthaumbox";

    /**
     * Tracks the amount of power being sent per side
     */
    private int[] shafts = new int[SIDE_COUNT];

    /**
     * Stores the located crankables.
     */
    private ICrankable[] crankables = new ICrankable[SIDE_COUNT];

    /**
     * Tracks if the crankable can be turned.
     */
    private boolean[] canTurn = new boolean[TileGearBox.SIDE_COUNT];

    /**
     * The number of crankable tiles attached to the gearbox.
     */
    private int crankableCount = -1;

    /**
     * Is this tile a thaumium gearbox?
     */
    private boolean isThaumiumGearbox = false;

    /**
     * Set to true when the tile entity is ready.
     */
    private boolean isReady = false;

    /**
     * Tracks if there have been new cranks since the last tick.
     */
    private boolean hasNewCranks = false;

    /**
     * Counts the number of ticks since the last(potential) sync.
     */
    private int syncTickCount = 0;

    /**
     * True if the side is facing a crankable.
     */
    public boolean[] sideIsFacingCrankable = new boolean[TileGearBox.SIDE_COUNT];

    /**
     * The current amount of shaft rotation.
     */
    public float shaftRotation = 0.0F;

    /**
     * Default constructor.
     */
    public TileGearBox() {
        // Intentionally Empty
    }

    /**
     * Constructor used for item rendering.
     *
     * @param isThaumium
     */
    public TileGearBox(final boolean isThaumium) {
        this.isThaumiumGearbox = isThaumium;
    }

    /**
     * Calculates the amount of power to send to each crank.
     *
     * @return
     */
    private int calculateTransferPower() {
        // Number of crankables that can turn.
        int powerDivisor = 0;

        // Calculate how many can accept a turn
        for (int sideIndex = 0; sideIndex < TileGearBox.SIDE_COUNT; sideIndex++) {
            ICrankable c = this.crankables[sideIndex];
            if ((c != null) && (c.canTurn())) {
                this.canTurn[sideIndex] = true;
                powerDivisor++;
            }
        }

        // Can any turn?
        if (powerDivisor == 0) {
            // None can turn
            return 0;
        }

        // Calculate the amount of power to send to each
        return TileGearBox.BASE_POWER / powerDivisor;
    }

    /**
     * Called when the tile entity is made, placed, and ready to use!
     */
    private void onReady() {
        // Update the crankables
        this.updateCrankables();

        // Is the tile a thaumium gearbox?
        this.isThaumiumGearbox =
                (this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord) instanceof BlockGolemGearBox);

        // Mark the tile as ready
        this.isReady = true;
    }

    /**
     * Reads Server->Client sync data from the specified NBT tag.
     *
     * @param data
     */
    private void readSyncData(final NBTTagCompound data) {
        // Does that data have the crankables tag?
        if (data.hasKey(TileGearBox.NBT_KEY_CRANKABLES)) {
            // Read the flags byte
            byte crankableFlags = data.getByte(TileGearBox.NBT_KEY_CRANKABLES);

            // Set the array
            for (int i = 0; i < TileGearBox.SIDE_COUNT; i++) {
                this.sideIsFacingCrankable[i] = ((crankableFlags & ((int) Math.pow(2, i))) != 0);
            }
        }

        // Does the data have the rotation tag?
        if (data.hasKey(TileGearBox.NBT_KEY_ROTATION)) {
            // Should rotation be added?
            if (this.shaftRotation < 1.0F) {
                // Add rotation.
                this.shaftRotation += TileGearBox.FULL_ROTATION * 2;
            }
        }

        // Does the data have the thaum box tag?
        if (data.hasKey(TileGearBox.NBT_KEY_ISTHAUMBOX)) {
            this.isThaumiumGearbox = data.getBoolean(TileGearBox.NBT_KEY_ISTHAUMBOX);
        }
    }

    /**
     * Applies the specified power to each shaft.
     * Once a shaft has enough stored power, it applies a turn to
     * its crankable.
     *
     * @param powerTransfered
     */
    private void updateShafts(final int powerTransfered) {
        for (int sideIndex = 0; sideIndex < TileGearBox.SIDE_COUNT; sideIndex++) {
            // Can this side turn?
            if (this.canTurn[sideIndex] && (this.crankables[sideIndex] != null)) {
                // Does it have enough power to turn the grinder?
                if ((this.shafts[sideIndex] += powerTransfered) >= TileGearBox.REQUIRED_POWER) {
                    // Reset the power
                    this.shafts[sideIndex] = 0;

                    // Turn it
                    this.crankables[sideIndex].applyTurn();
                }
            } else {
                // No power is going to this side.
                this.shafts[sideIndex] = 0;
            }
        }
    }

    /**
     * Writes Server->Client sync data to the specified NBT tag.
     *
     * @param data
     */
    private void writeSyncDataToNBT(final NBTTagCompound data) {
        // Call on ready?
        if (!this.isReady) {
            this.onReady();
        }

        // Create a byte of flags marking if each side has a crankable next to it
        byte crankableFlags = 0;
        for (int i = 0; i < TileGearBox.SIDE_COUNT; i++) {
            if (this.sideIsFacingCrankable[i]) {
                crankableFlags |= (int) Math.pow(2, i);
            }
        }

        // Write the flags
        data.setByte(TileGearBox.NBT_KEY_CRANKABLES, crankableFlags);

        // Write has new cranks
        if (this.hasNewCranks) {
            data.setBoolean(TileGearBox.NBT_KEY_ROTATION, true);

            // Clear the flag
            this.hasNewCranks = false;
        }

        // Write is thaumium gearbox
        data.setBoolean(TileGearBox.NBT_KEY_ISTHAUMBOX, this.isThaumiumGearbox);
    }

    /**
     * Gearbox would like ticks;
     */
    @Override
    public boolean canUpdate() {
        return true;
    }

    /**
     * Cranks the gearbox.
     *
     * @return
     */
    public boolean crankGearbox() {
        // Don't do work on client side
        if (EffectiveSide.isClientSide()) {
            return true;
        }

        // Are there any crankables?
        if (this.crankableCount <= 0) {
            // Nothing to crank.
            return false;
        }

        // Get the power transfer amount
        int powerTransfered = this.calculateTransferPower();

        // Ensure there is some power to transfer
        if (powerTransfered == 0) {
            // Nothing to crank
            return false;
        }

        // Update the shafts
        this.updateShafts(powerTransfered);

        // Mark there are new cranks
        this.hasNewCranks = true;

        // Did work
        return true;
    }

    /**
     * Creates the Server->Client sync data packet.
     */
    @Override
    public Packet getDescriptionPacket() {
        // Create the tag
        NBTTagCompound data = new NBTTagCompound();

        // Write the sync data
        this.writeSyncDataToNBT(data);

        // Send the packet
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, data);
    }

    /**
     * Returns true if this is a thaumium gearbox, false if iron gearbox.
     *
     * @return
     */
    public boolean isThaumiumGearbox() {
        return this.isThaumiumGearbox;
    }

    /**
     * Called when a Server->Client sync data packet arrives.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(final NetworkManager net, final S35PacketUpdateTileEntity packet) {
        // Read the sync data
        this.readSyncData(packet.func_148857_g());
    }

    /*
    @Override
    public void readFromNBT( final NBTTagCompound data )
    {
    	// Call super
    	super.readFromNBT( data );

    	// Read crankables
    	this.readSyncData( data );
    }*/

    /**
     * Locates attached crankables.
     *
     * @return Number of attached crankables found.
     */
    public void updateCrankables() {
        // Reset attached to zero
        this.crankableCount = 0;

        // Check all sides
        for (int sideIndex = 0; sideIndex < TileGearBox.SIDE_COUNT; sideIndex++) {
            // Get the side
            ForgeDirection side = ForgeDirection.VALID_DIRECTIONS[sideIndex];

            // Assume there is not a crankable
            this.crankables[sideIndex] = null;
            this.sideIsFacingCrankable[sideIndex] = false;

            // Get the tile
            TileEntity tile = this.worldObj.getTileEntity(
                    side.offsetX + this.xCoord, side.offsetY + this.yCoord, side.offsetZ + this.zCoord);

            // Is there a crankable?
            if (!(tile instanceof ICrankable)) {
                continue;
            }

            // Get the crankable
            ICrankable crank = (ICrankable) tile;

            // Is it facing the correct direction?
            if (crank.canCrankAttach(side.getOpposite())) {
                // Increment the crankable count
                this.crankableCount++;

                // Mark there is a crankable.
                this.crankables[sideIndex] = crank;
                this.sideIsFacingCrankable[sideIndex] = true;
            }
        }
    }

    /**
     * Called during each tick.
     */
    @Override
    public void updateEntity() {
        // Is this server side?
        if (EffectiveSide.isServerSide()) {
            // Increment the tick count
            this.syncTickCount++;

            // Have enough ticks elapsed?
            if (this.syncTickCount >= TileGearBox.MIN_TICKS_PER_SYNC) {
                // Decrement the tick count
                this.syncTickCount--;

                // Is there rotation to send?
                if (this.hasNewCranks) {
                    // Mark for update
                    this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

                    // Reset the counter
                    this.syncTickCount = 0;
                }
            }
        }
        // Client side
        else {
            // Is there rotation?
            if (this.shaftRotation != 0) {
                // Adjust rotation
                this.shaftRotation -= 0.20F;

                // Has the rotation exceeded epsilon?
                if (this.shaftRotation < 0.001F) {
                    // Stop rotation.
                    this.shaftRotation = 0;
                }
            }
        }
    }

    /*
    @Override
    public void writeToNBT( final NBTTagCompound data )
    {
    	// Call super
    	super.writeToNBT( data );

    	// Write sync data
    	this.writeSyncDataToNBT( data );
    }
    */
}
