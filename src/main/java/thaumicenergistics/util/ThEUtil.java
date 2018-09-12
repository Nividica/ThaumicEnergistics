package thaumicenergistics.util;

import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.item.ItemDummyAspect;

/**
 * @author BrockWS
 */
public class ThEUtil {

    public static ItemStack setAspect(ItemStack stack, Aspect aspect) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemDummyAspect))
            return stack;
        ItemDummyAspect item = (ItemDummyAspect) stack.getItem();
        item.setAspect(stack, aspect);
        return stack;
    }


}
