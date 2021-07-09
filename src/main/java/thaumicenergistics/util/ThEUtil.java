package thaumicenergistics.util;

import java.util.EnumSet;
import java.util.stream.IntStream;

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
    public static <T extends Enum<?>> T rotateEnum(T e, EnumSet<? extends T> options, boolean backwards) {
        if (e == null || options == null)
            return e;
        Object[] optArr = options.toArray();
        int mappedOrdinal = IntStream.range(0, options.size())  // find e's index in options
                .parallel()
                .filter(i -> optArr[i] == e)
                .findFirst()
                .orElseThrow(ArrayIndexOutOfBoundsException::new);
        T next = (T) optArr[(mappedOrdinal + (backwards ? -1 : 1) + options.size()) % options.size()];
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

    /**
     * Like {@link ForgeUtil#areItemStacksEqual(ItemStack, ItemStack)}, but safely compares items that were cheated in, that normally would have NBT.
     * @param a 1st stack
     * @param b 2nd stack
     * @return true if they're equal
     */
    public static boolean areItemStacksEqual(ItemStack a, ItemStack b) {
        return a != null && b != null && ItemStack.areItemsEqual(a, b) && (a.hasTagCompound() == b.hasTagCompound()) && ForgeUtil.areNBTTagsEqual(a.getTagCompound(), b.getTagCompound());
    }
}
