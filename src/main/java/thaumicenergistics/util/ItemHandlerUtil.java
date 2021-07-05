package thaumicenergistics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

/**
 * @author BrockWS
 * @author Alex811
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
    public static ItemStack insert(IItemHandler handler, ItemStack stack, boolean simulate) {
        return ItemHandlerUtil.insert(handler, stack, simulate, 0, handler.getSlots());
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
    public static ItemStack extract(IItemHandler handler, ItemStack stack, boolean simulate) {
        return ItemHandlerUtil.extract(handler, stack, simulate, 0, handler.getSlots());
    }

    @Nonnull
    public static ItemStack quickMoveSlot(IItemHandler handler, Slot slot){
        return quickMoveSlot(handler, slot, false);
    }

    @Nonnull
    public static ItemStack quickMoveSlotSim(IItemHandler handler, Slot slot){
        return quickMoveSlot(handler, slot, true);
    }

    @Nonnull
    public static ItemStack quickMoveSlot(IItemHandler handler, Slot slot, boolean simulate){
        return quickMoveSlot(handler, slot, simulate, false);
    }

    @Nonnull
    public static ItemStack insert(IItemHandler handler, ItemStack original, boolean simulate, int minValidSlot, int maxValidSlot) {
        if (original == null || original.isEmpty())
            return ItemStack.EMPTY;

        ItemStack copy = original.copy();
        List<Integer> emptySlots = new ArrayList<>();

        for (int slot = minValidSlot; slot < maxValidSlot; slot++) {   // insert into matching stacks
            ItemStack existing = handler.getStackInSlot(slot);
            if (ThEUtil.areItemStacksEqual(existing, copy)) {
                copy = handler.insertItem(slot, copy, simulate);
                if (copy.isEmpty())
                    return ItemStack.EMPTY;
            }else if (existing.isEmpty() && handler.isItemValid(slot, copy))
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
    public static ItemStack extract(IItemHandler handler, ItemStack original, boolean simulate, int minValidSlot, int maxValidSlot) {
        if (original == null || original.isEmpty())
            return ItemStack.EMPTY;

        ItemStack extracted = null;

        for (int slot = minValidSlot; slot < maxValidSlot; slot++) {
            if (extracted != null && original.getCount() == extracted.getCount()) {
                return extracted;
            }
            ItemStack inSlot = handler.getStackInSlot(slot);
            if (inSlot.isEmpty() || !ThEUtil.areItemStacksEqual(original, inSlot))
                continue;
            if (extracted == null) {
                extracted = handler.extractItem(slot, original.getCount(), simulate);
                continue;
            }
            ItemStack s = handler.extractItem(slot, original.getCount() - extracted.getCount(), simulate);
            if (s.isEmpty() || !ThEUtil.areItemStacksEqual(original, s))
                continue;
            extracted.grow(s.getCount());
        }
        return extracted == null || extracted.isEmpty() ? ItemStack.EMPTY : extracted;
    }

    /**
     * Perform a quick-move.
     * @param handler destination inventory's handler
     * @param slot source slot
     * @param simulate true to simulate instead of actually moving
     * @param skipArmorSlots true to disallow quick-moving into an armor slot, meant for player inventories (destination)
     * @return the leftover stack, will be an empty stack if everything was moved
     */
    @Nonnull
    public static ItemStack quickMoveSlot(IItemHandler handler, Slot slot, boolean simulate, boolean skipArmorSlots){
        ItemStack left = ItemHandlerUtil.insert(handler, slot.getStack(), simulate, 0, skipArmorSlots ? 36 : handler.getSlots());
        if(!simulate)
            slot.putStack(left);
        return left;
    }

    /**
     * Get all the stacks in the inventory, <b>except empty ones</b>!
     * @param handler inventory's handler
     * @return inventory's non-empty stacks
     */
    @Nonnull
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
