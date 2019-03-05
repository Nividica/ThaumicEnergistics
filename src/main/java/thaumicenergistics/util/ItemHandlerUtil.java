package thaumicenergistics.util;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

/**
 * @author BrockWS
 */
public class ItemHandlerUtil {

    @Nonnull
    public static ItemStack insert(IItemHandler handler, ItemStack stack) {
        return ItemHandlerUtil.insert(handler, stack, false);
    }

    @Nonnull
    public static ItemStack insertSim(IItemHandler handler, ItemStack stack) {
        return ItemHandlerUtil.insert(handler, stack, true);
    }

    @Nonnull
    public static ItemStack extract(IItemHandler handler, ItemStack stack) {
        return ItemHandlerUtil.extract(handler, stack, false);
    }

    @Nonnull
    public static ItemStack extractSim(IItemHandler handler, ItemStack stack) {
        return ItemHandlerUtil.extract(handler, stack, true);
    }

    @Nonnull
    public static ItemStack insert(IItemHandler handler, ItemStack original, boolean simulate) {
        if (original == null || original.isEmpty())
            return ItemStack.EMPTY;
        ItemStack copy = original.copy();

        for (int slot = 0; slot < handler.getSlots(); slot++) { // Check existing stacks
            ItemStack existing = handler.getStackInSlot(slot);
            if (ForgeUtil.areItemStacksEqual(existing, copy))
                copy = handler.insertItem(slot, copy, simulate);
            if (copy.isEmpty())
                return ItemStack.EMPTY;
        }

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            copy = handler.insertItem(slot, copy, simulate);
            if (copy.isEmpty())
                return ItemStack.EMPTY;
        }
        return copy;
    }

    @Nonnull
    public static ItemStack extract(IItemHandler handler, ItemStack original, boolean simulate) {
        if (original == null || original.isEmpty())
            return ItemStack.EMPTY;

        ItemStack extracted = null;

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (extracted != null && original.getCount() == extracted.getCount()) {
                return extracted;
            }
            ItemStack inSlot = handler.getStackInSlot(slot);
            if (inSlot.isEmpty() || !ForgeUtil.areItemStacksEqual(original, inSlot))
                continue;
            if (extracted == null) {
                extracted = handler.extractItem(slot, original.getCount(), simulate);
                continue;
            }
            ItemStack s = handler.extractItem(slot, original.getCount() - extracted.getCount(), simulate);
            if (s.isEmpty() || !ForgeUtil.areItemStacksEqual(original, s))
                continue;
            extracted.grow(s.getCount());
        }
        return extracted == null || extracted.isEmpty() ? ItemStack.EMPTY : extracted;
    }

    public static void getInventoryAsList(IItemHandler handler, List<ItemStack> list) {
        if (handler == null)
            return;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty())
                list.add(stack);
        }
    }
}
