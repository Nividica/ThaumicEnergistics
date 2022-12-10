package thaumicenergistics.common.inventory;

import appeng.items.misc.ItemEncodedPattern;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import thaumicenergistics.common.items.ItemCraftingAspect;

/**
 * Handles {@link ItemEncodedPattern} items with a distillation encodeing.
 *
 * @author Nividica
 *
 */
public class HandlerDistillationPattern {
    /**
     * NBT Keys
     */
    private static final String NBTKEY_AE_IN = "in",
            NBTKEY_AE_OUT = "out",
            NBTKEY_AE_ISCRAFTING = "crafting",
            NBTKEY_AE_CAN_SUB = "substitute";

    /**
     * Output of the pattern.
     * Must be ItemCraftingAspect patterns.
     */
    protected ItemStack output = null;

    /**
     * Input of the pattern.
     */
    protected ItemStack input = null;

    public void encodePattern(final ItemStack pattern) {
        // Valid pattern?
        if (pattern == null) {
            return;
        }

        // Check the input & output
        if ((this.input == null) || (this.output == null)) {
            // No input or output
            return;
        }

        // Create a new tag
        NBTTagCompound data = new NBTTagCompound();

        // Write the input
        NBTTagList inTags = new NBTTagList();
        inTags.appendTag(this.input.writeToNBT(new NBTTagCompound()));

        // Write the outputs
        NBTTagList outTags = new NBTTagList();
        outTags.appendTag(this.output.writeToNBT(new NBTTagCompound()));

        // Write the basics
        data.setBoolean(NBTKEY_AE_CAN_SUB, false);
        data.setBoolean(NBTKEY_AE_ISCRAFTING, false);

        // Write the lists
        data.setTag(NBTKEY_AE_IN, inTags);
        data.setTag(NBTKEY_AE_OUT, outTags);

        // Save into the item
        pattern.setTagCompound(data);
    }

    /**
     * Returns the input of the pattern.
     * May be null.
     *
     * @return
     */
    public ItemStack getInput() {
        return this.input;
    }

    /**
     * Returns the output of the pattern.
     * May be null.
     *
     * @return
     */
    public ItemStack getOutput() {
        return this.output;
    }

    /**
     * Returns true if the current items are valid.
     *
     * @return
     */
    public boolean isValid() {
        return ((this.output != null) && (this.input != null));
    }

    /**
     * Loads the values from the pattern
     *
     * @param pattern
     */
    public void readPattern(final ItemStack pattern) {
        // Reset
        this.reset();

        // Valid pattern?
        if (pattern == null) {
            return;
        }

        if (!pattern.hasTagCompound()) {
            // Nothing to load
            return;
        }

        // Get the NBT tag
        NBTTagCompound data = pattern.getTagCompound();

        // Get the input and output list
        NBTTagList inTags = data.getTagList(NBTKEY_AE_IN, NBT.TAG_COMPOUND);
        NBTTagList outTags = data.getTagList(NBTKEY_AE_OUT, NBT.TAG_COMPOUND);

        // Empty check
        if ((outTags.tagCount() < 1) || (inTags.tagCount() < 1)) {
            // Nothing to load.
            return;
        }

        // Read the input and output
        this.setOutputItem(ItemStack.loadItemStackFromNBT(outTags.getCompoundTagAt(0)));
        this.setInputItem(ItemStack.loadItemStackFromNBT(inTags.getCompoundTagAt(0)));

        // Null check
        if ((this.input == null) || (this.output == null)) {
            this.reset();
            return;
        }
    }

    /**
     * Resets the helper.
     */
    public void reset() {
        this.output = null;
        this.input = null;
    }

    /**
     * Sets the input.
     *
     * @param inputItem
     */
    public void setInputItem(final ItemStack inputItem) {
        this.input = inputItem.copy();
        this.input.stackSize = 1;
    }

    /**
     * Sets the output.
     * Must be a crafting aspect.
     *
     * @param outputItem
     */
    public void setOutputItem(final ItemStack outputItem) {
        // Not valid crafting aspect?
        if ((outputItem != null) && !(outputItem.getItem() instanceof ItemCraftingAspect)) {
            this.output = null;
        }
        // Aspect null?
        else if (ItemCraftingAspect.getAspect(outputItem) == null) {
            this.output = null;
        } else {
            this.output = outputItem;
        }
    }

    /**
     * Sets the input and output items.
     * Returns if the recipe is valid or not.
     *
     * @param inputItem
     * @param outputItem
     * @return
     */
    public boolean setPatternItems(final ItemStack inputItem, final ItemStack outputItem) {
        this.setInputItem(inputItem);
        this.setOutputItem(outputItem);
        return this.isValid();
    }
}
