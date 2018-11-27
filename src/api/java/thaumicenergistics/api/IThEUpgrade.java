package thaumicenergistics.api;

import java.util.Map;

import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;

/**
 * @author BrockWS
 */
public interface IThEUpgrade {

    void registerItem(IItemDefinition item, int max);

    void registerItem(ItemStack item, int max);

    IItemDefinition getDefinition();

    Map<ItemStack, Integer> getSupported();

    int getSupported(ItemStack stack);

    boolean isSupported(ItemStack stack);
}
