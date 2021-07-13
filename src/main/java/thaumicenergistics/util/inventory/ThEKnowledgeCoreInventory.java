package thaumicenergistics.util.inventory;

import net.minecraft.item.ItemStack;
import thaumicenergistics.item.ItemKnowledgeCore;

/**
 * @author Alex811
 */
public class ThEKnowledgeCoreInventory extends ThEUpgradeInventory{

    public ThEKnowledgeCoreInventory(String customName, int size, int stackLimit, ItemStack upgradable) {
        super(customName, size, stackLimit, upgradable);
    }

    @Override
    public boolean isKnowledgeCoreSlot() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return super.isItemValidForSlot(index, stack) && (stack.getItem() instanceof ItemKnowledgeCore);
    }
}
