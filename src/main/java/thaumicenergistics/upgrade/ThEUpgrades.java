package thaumicenergistics.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import appeng.api.definitions.IItemDefinition;
import net.minecraft.item.ItemStack;

import thaumicenergistics.api.IThEItems;
import thaumicenergistics.api.IThEUpgrade;
import thaumicenergistics.api.IThEUpgrades;

/**
 * @author BrockWS
 */
public class ThEUpgrades implements IThEUpgrades {

    private IThEUpgrade arcaneCharger;
    private IThEUpgrade knowledgeCore;
    private IThEUpgrade blankKnowledgeCore;
    private List<IThEUpgrade> upgrades;

    public ThEUpgrades(IThEItems items) {
        this.upgrades = new ArrayList<>();

        this.upgrades.add(this.arcaneCharger = new ThEUpgrade(items.upgradeArcane()));
        this.upgrades.add(this.knowledgeCore = new ThEUpgrade(items.knowledgeCore()));
        this.upgrades.add(this.blankKnowledgeCore = new ThEUpgrade(items.blankKnowledgeCore()));
    }

    @Override
    public IThEUpgrade arcaneCharger() {
        return this.arcaneCharger;
    }

    @Override
    public IThEUpgrade knowledgeCore() {
        return this.knowledgeCore;
    }

    @Override
    public IThEUpgrade blankKnowledgeCore() {
        return this.blankKnowledgeCore;
    }

    @Override
    public Optional<IThEUpgrade> getUpgrade(ItemStack stack) {
        return this.getUpgrades().stream().filter(upgrade -> upgrade.getDefinition().isSameAs(stack)).findFirst();
    }

    @Override
    public List<IThEUpgrade> getUpgrades() {
        return this.upgrades;
    }

    @Override
    public void registerUpgrade(IItemDefinition upgradable, IThEUpgrade upgrade, int max) {
        upgradable.maybeStack(1).ifPresent(stack -> upgrade.registerItem(stack, max));
    }
}
