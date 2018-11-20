package thaumicenergistics.api;

import java.util.List;
import java.util.Optional;

import net.minecraft.item.ItemStack;

/**
 * @author BrockWS
 */
public interface IThEUpgrades {

    IThEUpgrade arcaneCharger();

    Optional<IThEUpgrade> getUpgrade(ItemStack stack);

    List<IThEUpgrade> getUpgrades();
}
