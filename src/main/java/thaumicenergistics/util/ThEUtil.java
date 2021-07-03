package thaumicenergistics.util;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;

import appeng.api.config.TerminalStyle;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaContainerItem;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.item.ItemDummyAspect;

/**
 * @author BrockWS
 */
public class ThEUtil {

    /**
     * Safe divide
     *
     * @param x amount
     * @param y amount
     * @return if x or y is 0, returns 0, else divides x by y
     */
    public static int divide(int x, int y) {
        return x == 0 || y == 0 ? 0 : x / y;
    }

    public static ItemStack setAspect(ItemStack stack, Aspect aspect) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemDummyAspect))
            return stack;
        ItemDummyAspect item = (ItemDummyAspect) stack.getItem();
        item.setAspect(stack, aspect);
        return stack;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum> T rotateEnum(T e, EnumSet options, boolean backwards) {
        if (e == null || options == null)
            return e;
        int i = e.ordinal();
        if (i == 0)
            i = backwards ? options.size() - 1 : 1;
        else if (i >= options.size() - 1)
            i = backwards ? i - 1 : 0;
        else
            i = i + (backwards ? -1 : 1);
        T next = (T) options.toArray()[i];
        return ThEUtil.isInvalidSetting(next) ? ThEUtil.rotateEnum(next, options, backwards) : next;
    }

    public static boolean isInvalidSetting(Enum e) {
        return e == TerminalStyle.FULL;
    }

    @Deprecated
    public static int getEssentiaCapacity(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof IEssentiaContainerItem) || stack.getItem().getRegistryName() == null)
            return 0;
        return ThEApi.instance().config().essentiaContainerCapacity().getOrDefault(stack.getItem().getRegistryName().toString() + ":" + stack.getMetadata(), 0);
    }
}
