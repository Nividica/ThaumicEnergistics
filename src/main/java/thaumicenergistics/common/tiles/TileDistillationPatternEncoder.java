package thaumicenergistics.common.tiles;

import appeng.api.AEApi;
import java.util.ArrayList;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumicenergistics.common.tiles.abstraction.ThETileInventory;

/**
 * Encodes recipes whose result is essentia.
 *
 * @author Nividica
 *
 */
public class TileDistillationPatternEncoder extends ThETileInventory implements ISidedInventory {
    /**
     * NBT Keys
     */
    private static String NBTKEY_INVENTORY = "inventory";

    /**
     * Slot counts
     */
    public static int SLOT_PATTERNS_COUNT = 2,
            SLOT_SOURCE_ITEM_COUNT = 1,
            SLOT_TOTAL_COUNT =
                    TileDistillationPatternEncoder.SLOT_SOURCE_ITEM_COUNT
                            + TileDistillationPatternEncoder.SLOT_PATTERNS_COUNT;
    /**
     * Slot ID's
     */
    public static int SLOT_SOURCE_ITEM = 0, SLOT_BLANK_PATTERNS = 1, SLOT_ENCODED_PATTERN = 2;

    /**
     * Default constructor.
     */
    public TileDistillationPatternEncoder() {
        super("distillation.inscriber", TileDistillationPatternEncoder.SLOT_TOTAL_COUNT, 64);
    }

    @Override
    public boolean canExtractItem(final int p_102008_1_, final ItemStack p_102008_2_, final int p_102008_3_) {
        return false;
    }

    @Override
    public boolean canInsertItem(final int p_102007_1_, final ItemStack p_102007_2_, final int p_102007_3_) {
        return false;
    }

    /**
     * Does not need ticks.
     */
    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(final int p_94128_1_) {
        return new int[0];
    }

    /**
     * Returns a list of items to drop when broken.
     *
     * @return
     */
    public ArrayList<ItemStack> getDrops(final ArrayList<ItemStack> drops) {
        // Add encoded
        if (this.internalInventory.getHasStack(TileDistillationPatternEncoder.SLOT_ENCODED_PATTERN)) {
            drops.add(this.internalInventory.getStackInSlot(TileDistillationPatternEncoder.SLOT_ENCODED_PATTERN));
        }

        // Add blank
        if (this.internalInventory.getHasStack(TileDistillationPatternEncoder.SLOT_BLANK_PATTERNS)) {
            drops.add(this.internalInventory.getStackInSlot(TileDistillationPatternEncoder.SLOT_BLANK_PATTERNS));
        }

        return drops;
    }

    /**
     * True if there is a pattern to encode onto.
     *
     * @return
     */
    public boolean hasPatterns() {
        // Is there anything in the pattern slots?
        return this.internalInventory.getHasStack(TileDistillationPatternEncoder.SLOT_ENCODED_PATTERN)
                || this.internalInventory.getHasStack(TileDistillationPatternEncoder.SLOT_BLANK_PATTERNS);
    }

    @Override
    public boolean isItemValidForSlot(final int slotId, final ItemStack itemStack) {
        // Can always clear a slot
        if (itemStack == null) {
            return true;
        }

        // Empty pattern slot?
        if (slotId == TileDistillationPatternEncoder.SLOT_BLANK_PATTERNS) {
            return AEApi.instance().definitions().materials().blankPattern().isSameAs(itemStack);
        }

        // Encoded pattern slot?
        if (slotId == TileDistillationPatternEncoder.SLOT_ENCODED_PATTERN) {
            return AEApi.instance().definitions().items().encodedPattern().isSameAs(itemStack);
        }

        return true;
    }

    /**
     * Read tile state from NBT.
     */
    @Override
    public void readFromNBT(final NBTTagCompound data) {
        // Call super
        super.readFromNBT(data);

        // Has saved inventory?
        if (data.hasKey(TileDistillationPatternEncoder.NBTKEY_INVENTORY)) {
            this.internalInventory.readFromNBT(data, TileDistillationPatternEncoder.NBTKEY_INVENTORY);
        }
    }

    /**
     * Write tile state to NBT.
     */
    @Override
    public void writeToNBT(final NBTTagCompound data) {
        // Call super
        super.writeToNBT(data);

        // Write the inventory
        this.internalInventory.writeToNBT(data, TileDistillationPatternEncoder.NBTKEY_INVENTORY);
    }
}
