package thaumicenergistics.upgrade;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;

import thaumicenergistics.api.IThEUpgrade;
import thaumicenergistics.util.ForgeUtil;

/**
 * @author BrockWS
 */
public class ThEUpgrade implements IThEUpgrade {

    private IItemDefinition definition;
    private Map<ItemStack, Integer> supported;

    public ThEUpgrade(IItemDefinition definition) {
        this.definition = definition;

        this.supported = new HashMap<>();
    }

    @Override
    public void registerItem(IItemDefinition item, int max) {
        item.maybeStack(1).ifPresent(stack -> this.registerItem(stack, max));
    }

    @Override
    public void registerItem(ItemStack item, int max) {
        if (item == null)
            return;
        this.getSupported().put(item, max);
    }

    @Override
    public IItemDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public Map<ItemStack, Integer> getSupported() {
        return this.supported;
    }

    @Override
    public int getSupported(ItemStack upgradeStack) {
        Stream<ItemStack> stream = this.getSupported().keySet().stream().filter(stack -> ForgeUtil.areItemStacksEqual(stack, upgradeStack));
        return this.getSupported().getOrDefault(stream.findFirst().orElse(ItemStack.EMPTY), 0);
    }

    @Override
    public boolean isSupported(ItemStack stack) {
        return this.supported.containsKey(stack);
    }
}
