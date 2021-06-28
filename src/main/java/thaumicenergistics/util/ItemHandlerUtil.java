package thaumicenergistics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        List<Integer> emptySlots = new ArrayList<>();

        for (int slot = 0; slot < handler.getSlots(); slot++) {   // insert into matching stacks
            ItemStack existing = handler.getStackInSlot(slot);
            if (ForgeUtil.areItemStacksEqual(existing, copy)) {
                copy = handler.insertItem(slot, copy, simulate);
                if (copy.isEmpty())
                    return ItemStack.EMPTY;
            }else if (existing.isEmpty())
                emptySlots.add(slot);
        }

        for (int slot : emptySlots) {   // insert the rest into empty slots
            copy = handler.insertItem(slot, copy, simulate);
            if (copy.isEmpty())
                return ItemStack.EMPTY;
        }

        return copy;    // leftover or empty stack
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

    /**
     * Get all the stacks in the inventory, <b>except empty ones</b>!
     * @param handler inventory's handler
     * @return inventory's non-empty stacks
     */
    public static List<ItemStack> getInventoryAsList(IItemHandler handler) {
        if (handler == null)
            return new ArrayList<>();
        return IntStream.range(0, handler.getSlots()).parallel()
                .boxed()
                .map(handler::getStackInSlot)
                .filter(is -> !is.isEmpty())
                .collect(Collectors.toList());
    }

}
