package thaumicenergistics.common.inventory;

import appeng.api.storage.data.IAEItemStack;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import thaumicenergistics.common.integration.tc.ArcaneCraftingPattern;
import thaumicenergistics.common.items.ItemKnowledgeCore;

/**
 * Handles an {@link ItemKnowledgeCore}
 *
 * @author Nividica
 *
 */
public class HandlerKnowledgeCore {
    /**
     * NBT Keys.
     */
    private static final String NBTKEY_PATTERNS = "Patterns";

    /**
     * Maximum number of stored patterns.
     */
    public static final int MAXIMUM_STORED_PATTERNS = 21;

    /**
     * Array of stored patterns.
     */
    private final ArrayList<ArcaneCraftingPattern> patterns =
            new ArrayList<ArcaneCraftingPattern>(MAXIMUM_STORED_PATTERNS);

    /**
     * Knowledge core being handled.
     */
    private ItemStack kCore;

    /**
     * Creates a handler without a core.
     * Use open() to set the core in the future.
     */
    public HandlerKnowledgeCore() {}

    /**
     * Creates the handler with the specified core.
     *
     * @param kCore
     */
    public HandlerKnowledgeCore(final ItemStack kCore) {
        this.open(kCore);
    }

    /**
     * Gets or creates the NBT tag for the knowledge core.
     *
     * @return
     */
    private NBTTagCompound getOrCreateNBT() {
        if (!this.kCore.hasTagCompound()) {
            this.kCore.stackTagCompound = new NBTTagCompound();
        }

        return this.kCore.stackTagCompound;
    }

    /**
     * Loads the data from the knowledge core's nbt.
     */
    private void loadKCoreData() {
        // Clear any existing data
        this.patterns.clear();

        // Get the data tag
        NBTTagCompound data = this.getOrCreateNBT();

        // Are there saved patterns?
        if (data.hasKey(NBTKEY_PATTERNS)) {
            // Get the list
            NBTTagList plist = data.getTagList(NBTKEY_PATTERNS, Constants.NBT.TAG_COMPOUND);

            // Read in each pattern
            for (int index = 0; index < plist.tagCount(); index++) {
                try {
                    // Load the pattern
                    ArcaneCraftingPattern pattern =
                            new ArcaneCraftingPattern(this.kCore, plist.getCompoundTagAt(index));

                    // Is the pattern valid?
                    if (pattern.isPatternValid()) {
                        // Add the pattern
                        this.patterns.add(pattern);
                    }
                } catch (Exception e) {
                    // Ignore invalid patterns
                }
            }
        }
    }

    /**
     * Saves the data to the knowldge core's nbt.
     */
    private void saveKCoreData() {
        // Get the data tag
        NBTTagCompound data = this.getOrCreateNBT();

        // Create the pattern list
        NBTTagList plist = new NBTTagList();

        // Save each pattern
        for (ArcaneCraftingPattern pattern : this.patterns) {
            // Ensure the pattern is valid
            if ((pattern == null) || (!pattern.isPatternValid())) {
                continue;
            }

            // Write the pattern
            plist.appendTag(pattern.writeToNBT(new NBTTagCompound()));
        }

        // Write the list to the data
        if (plist.tagCount() > 0) {
            data.setTag(NBTKEY_PATTERNS, plist);
        } else {
            data.removeTag(NBTKEY_PATTERNS);
        }
    }

    /**
     * Adds a pattern to the core.
     */
    public void addPattern(final ArcaneCraftingPattern pattern) {
        // Validate the pattern
        if ((pattern == null) || (!pattern.isPatternValid())) {
            return;
        }

        // Ensure there is room to store the pattern
        if (!this.hasRoomToStorePattern()) {
            return;
        }

        // Check for duplicate patterns
        ArcaneCraftingPattern existingPattern =
                this.getPatternForItem(pattern.getResult().getItemStack());

        if (existingPattern == null) {
            // remove the existing pattern, e.g. if the result changed
            removePatternSameInputs(pattern);
            // Add the pattern
            this.patterns.add(pattern);

            // Save
            this.saveKCoreData();
        }
    }

    private void removePatternSameInputs(final ArcaneCraftingPattern pattern) {
        IAEItemStack[] newInputs = pattern.getInputs();
        // Loop over all stored patterns
        for (ArcaneCraftingPattern p : this.patterns) {
            IAEItemStack[] oldInputs = p.getInputs();
            if (newInputs.length != oldInputs.length) continue;
            boolean match = true;
            for (int i = 0; i < newInputs.length; ++i) {
                if (oldInputs[i] == null && newInputs[i] == null) continue;
                if (oldInputs[i] == null || newInputs[i] == null) {
                    match = false;
                    break;
                }
                if (!ItemStack.areItemStacksEqual(oldInputs[i].getItemStack(), newInputs[i].getItemStack())) {
                    match = false;
                    break;
                }
            }
            if (match) {
                // can't be more than one duplicate
                this.patterns.remove(p);
                return;
            }
        }
    }

    /**
     * Instructs the handler to close.
     */
    public void close() {
        this.kCore = null;
        this.patterns.clear();
    }

    /**
     * Gets the pattern that produces the result.
     *
     * @param resultStack
     * @return
     */
    public ArcaneCraftingPattern getPatternForItem(final ItemStack resultStack) {
        // Loop over all stored patterns
        for (ArcaneCraftingPattern p : this.patterns) {
            // Does the pattern have a valid output?
            if ((p != null) && (p.getResult() != null)) {
                // Is the output equal to the specified result?
                if (ItemStack.areItemStacksEqual(p.getResult().getItemStack(), resultStack)) {
                    // Found the pattern
                    return p;
                }
            }
        }

        // No matching patterns
        return null;
    }

    /**
     * Gets the list of stored patterns.
     *
     * @return
     */
    public ArrayList<ArcaneCraftingPattern> getPatterns() {
        return this.patterns;
    }

    /**
     * Gets the results of all stored patterns.
     *
     * @return
     */
    public ArrayList<ItemStack> getStoredOutputs() {
        // Create the array
        ArrayList<ItemStack> results = new ArrayList<ItemStack>();

        // Add each stored patterns output
        for (ArcaneCraftingPattern p : this.patterns) {
            if ((p != null) && (p.getResult() != null)) {
                results.add(p.getResult().getItemStack());
            }
        }

        // Return the array
        return results;
    }

    /**
     * Returns true if the handler has a core.
     *
     * @return
     */
    public boolean hasCore() {
        return this.kCore != null;
    }

    /**
     * Returns true if there is a pattern stored that produces the specified
     * result.
     *
     * @param resultStack
     * @return
     */
    public boolean hasPatternFor(final ItemStack resultStack) {
        return (this.getPatternForItem(resultStack) != null);
    }

    /**
     * Returns true if there is room to store a new pattern.
     *
     * @return
     */
    public boolean hasRoomToStorePattern() {
        return (this.patterns.size() < HandlerKnowledgeCore.MAXIMUM_STORED_PATTERNS);
    }

    /**
     * Return's true if this handler is handling the specified knowledge core.
     *
     * @param kCore
     * @return
     */
    public boolean isHandlingCore(final ItemStack kCore) {
        // Is the handler handling a core?
        if (this.kCore == null) {
            // Handler has been closed.
            return false;
        }

        // Is the specified itemstack valid?
        if ((kCore == null) || (kCore.getItem() == null)) {
            // Invalid itemstack
            return false;
        }

        // Is the specified item a knowledge core?
        if (!(kCore.getItem() instanceof ItemKnowledgeCore)) {
            // Invalid core
            return false;
        }

        return (ItemStack.areItemStacksEqual(kCore, this.kCore)
                && (ItemStack.areItemStackTagsEqual(kCore, this.kCore)));
    }

    /**
     * Closes the previous core, and opens the new core.
     *
     * @param kCore
     */
    public void open(final ItemStack kCore) {
        // Close any existing core
        this.close();

        // Set the kCore
        this.kCore = kCore;

        // Load
        this.loadKCoreData();
    }

    /**
     * Removes the specified pattern from the core.
     *
     * @param pattern
     */
    public void removePattern(final ArcaneCraftingPattern pattern) {
        // Attempt to remove the pattern
        if (this.patterns.remove(pattern)) {
            // Save
            this.saveKCoreData();
        }
    }
}
