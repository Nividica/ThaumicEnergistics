package thaumicenergistics.container;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

/**
 * @author BrockWS
 */
public interface ICraftingContainer {

    void onMatrixChanged();

    /**
     * Calculate the amount we can craft
     *
     * @param amount Amount requested (Normally either 1 or Integer.MAX_VALUE)
     * @return The max amount we can craft
     */
    int tryCraft(int amount);

    ItemStack onCraft(ItemStack crafted);

    IItemHandler getInventory(String name);
}
