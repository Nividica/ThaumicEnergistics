package thaumicenergistics.api;

import java.util.List;
import java.util.Optional;

import appeng.api.definitions.IItemDefinition;
import net.minecraft.item.ItemStack;

/**
 * @author BrockWS
 */
public interface IThEUpgrades {

    IThEUpgrade arcaneCharger();

    IThEUpgrade knowledgeCore();

    IThEUpgrade blankKnowledgeCore();

    IThEUpgrade cardSpeed();

    Optional<IThEUpgrade> getUpgrade(ItemStack stack);

    List<IThEUpgrade> getUpgrades();

    void registerUpgrade(IItemDefinition upgradable, IThEUpgrade upgrade, int max);
}
