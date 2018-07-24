package thaumicenergistics.util.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;

/**
 * @author BrockWS
 */
public class ThEUpgradeInventory extends ThEInternalInventory {

    private boolean cached = false;
    private NonNullList<Integer> cachedUpgrades = NonNullList.withSize(Upgrades.values().length, 0);

    public ThEUpgradeInventory(String customName, int size, int stackLimit) {
        super(customName, size, stackLimit);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() instanceof IUpgradeModule;
    }

    public int getUpgrades(Upgrades upgrade) {
        if (!this.cached)
            this.calculateUpgrades();

        return this.cachedUpgrades.get(upgrade.ordinal());
    }

    private void calculateUpgrades() {
        this.cachedUpgrades.clear();
        this.iterator().forEachRemaining(stack -> {
            if (!(stack.getItem() instanceof IUpgradeModule))
                return;
            IUpgradeModule upgrade = (IUpgradeModule) stack.getItem();
            int i = upgrade.getType(stack).ordinal();
            this.cachedUpgrades.set(i, this.cachedUpgrades.get(i) + stack.getCount());
        });
        this.cached = true;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.cached = false;
    }
}
