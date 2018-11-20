package thaumicenergistics.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.item.ItemStack;

import thaumicenergistics.api.IThEItems;
import thaumicenergistics.api.IThEUpgrade;
import thaumicenergistics.api.IThEUpgrades;

/**
 * @author BrockWS
 */
public class ThEUpgrades implements IThEUpgrades {

    private IThEUpgrade arcaneCharger;
    private List<IThEUpgrade> upgrades;

    public ThEUpgrades(IThEItems items) {
        this.upgrades = new ArrayList<>();

        this.upgrades.add(this.arcaneCharger = new ThEUpgrade(items.upgradeArcane()));
    }

    @Override
    public IThEUpgrade arcaneCharger() {
        return this.arcaneCharger;
    }

    @Override
    public Optional<IThEUpgrade> getUpgrade(ItemStack stack) {
        return this.getUpgrades().stream().filter(upgrade -> upgrade.getDefinition().isSameAs(stack)).findFirst();
    }

    @Override
    public List<IThEUpgrade> getUpgrades() {
        return this.upgrades;
    }
}
