package thaumicenergistics.util.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.item.ItemStack;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;

import thaumicenergistics.api.ThEApi;

/**
 * @author BrockWS
 */
public class ThEUpgradeInventory extends ThEInternalInventory {

    private boolean cached = false;
    private Map<Object, Integer> cachedUpgrades;
    private ItemStack upgradable;

    public ThEUpgradeInventory(String customName, int size, int stackLimit, ItemStack upgradable) {
        this(customName, size, stackLimit);
        this.upgradable = upgradable;
    }

    public ThEUpgradeInventory(String customName, int size, int stackLimit) {
        super(customName, size, stackLimit);
        this.cachedUpgrades = new HashMap<>();
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (!(stack.getItem() instanceof IUpgradeModule) && !ThEApi.instance().upgrades().getUpgrade(stack).isPresent())
            return false;
        if (this.upgradable == null) // If the item/block/part that this is attached to is null, then just allow without checking max allowed
            return true;
        return this.getMaxUpgrades(stack) > 0 && this.getUpgrades(stack) < this.getMaxUpgrades(stack);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.cached = false;
    }

    public int getUpgrades(Object upgrade) {
        if (!this.cached)
            this.calculateUpgrades();

        return this.cachedUpgrades.getOrDefault(upgrade, 0);
    }

    private void calculateUpgrades() {
        this.cachedUpgrades.clear();
        this.iterator().forEachRemaining(stack -> {
            // Check if its a ThEUpgrade, and if it is cache it
            ThEApi.instance().upgrades().getUpgrade(stack).ifPresent(upgrade -> this.cachedUpgrades.put(upgrade, this.cachedUpgrades.getOrDefault(upgrade, 0) + stack.getCount()));

            // Check if its a AE2 Upgrade, and if it is cache it
            if (stack.getItem() instanceof IUpgradeModule) {
                IUpgradeModule item = (IUpgradeModule) stack.getItem();
                Upgrades upgrade = item.getType(stack);
                if (upgrade == null)
                    return;
                this.cachedUpgrades.put(upgrade, this.cachedUpgrades.getOrDefault(upgrade, 0) + stack.getCount());
            }
        });
        this.cached = true;
    }

    private int getMaxUpgrades(ItemStack upgradeStack) {
        AtomicInteger max = new AtomicInteger();
        ThEApi.instance().upgrades().getUpgrade(upgradeStack).ifPresent(upgrade -> max.set(upgrade.getSupported(this.upgradable)));
        if (upgradeStack.getItem() instanceof IUpgradeModule) {
            Upgrades upgrade = ((IUpgradeModule) upgradeStack.getItem()).getType(upgradeStack);
            if (upgrade != null)
                max.set(upgrade.getSupported().getOrDefault(upgradeStack, 0));
        }
        return max.get();
    }
}
