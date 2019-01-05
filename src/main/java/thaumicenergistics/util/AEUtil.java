package thaumicenergistics.util;

import com.google.common.base.Preconditions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.integration.appeng.AEEssentiaStack;

/**
 * @author BrockWS
 */
public class AEUtil {

    private static KeyBinding focusKeyBinding;

    public static String getDisplayName(Object o) {
        if (o instanceof ItemStack)
            return ((ItemStack) o).getDisplayName();
        if (o instanceof IAEItemStack)
            return ((IAEItemStack) o).getDefinition().getDisplayName();
        if (o instanceof IAEFluidStack)
            return ((IAEFluidStack) o).getFluidStack().getLocalizedName();
        if (o instanceof IAEEssentiaStack)
            return ((IAEEssentiaStack) o).getAspect().getName();
        return "NAMENOTFOUND";
    }

    public static String getModID(Object o) {
        ResourceLocation rl = null;
        if (o instanceof ItemStack)
            rl = ((ItemStack) o).getItem().getRegistryName();
        else if (o instanceof IAEItemStack)
            rl = ((IAEItemStack) o).getDefinition().getItem().getRegistryName();
        else if (o instanceof IAEFluidStack)
            return FluidRegistry.getModId(((IAEFluidStack) o).getFluidStack());
        else if (o instanceof IAEEssentiaStack)
            return "Thaumcraft"; // Probably useless

        return rl != null ? rl.getResourceDomain() : "MODIDNOTFOUND";
    }

    public static long getStackSize(Object o) {
        if (o instanceof ItemStack)
            return ((ItemStack) o).getCount();
        if (o instanceof IAEStack)
            return ((IAEStack) o).getStackSize();
        return 0;
    }

    public static <T extends IAEStack<T>> T inventoryInsert(T input, IMEInventory<T> inv, IActionSource src) {
        return AEUtil.inventoryInsert(input, inv, src, null, Actionable.MODULATE);
    }

    public static <T extends IAEStack<T>> T inventoryInsert(T input, IMEInventory<T> inv, IActionSource src, Actionable mode) {
        return AEUtil.inventoryInsert(input, inv, src, null, mode);
    }

    public static <T extends IAEStack<T>> T inventoryInsert(T input, IMEInventory<T> inv, IActionSource src, IEnergySource energy) {
        return AEUtil.inventoryInsert(input, inv, src, energy, Actionable.MODULATE);
    }

    public static <T extends IAEStack<T>> T inventoryInsert(T input, IMEInventory<T> inv, IActionSource src, IEnergySource energy, Actionable mode) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(inv);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);

        T notAdded = inv.injectItems(input.copy(), Actionable.SIMULATE, src);

        long toAdd = input.getStackSize();
        if (notAdded != null) {
            toAdd -= notAdded.getStackSize();
        }

        double energyFactor = 0;
        if (energy != null) { // We need to factor in power available
            energyFactor = Math.max(1.0, inv.getChannel().transferFactor());
            double availablePower = energy.extractAEPower(toAdd / energyFactor, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            toAdd = Math.min((long) ((availablePower * energyFactor) + 0.9), toAdd);
        }

        if (toAdd < 1) // We either cannot store one item or don't have enough energy too
            return input;

        if (mode == Actionable.SIMULATE) {
            T s = input.copy().setStackSize(input.getStackSize() - toAdd);
            return s != null && s.getStackSize() > 0 ? s : null;
        }

        if (energy != null)
            energy.extractAEPower(toAdd / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
        if (input.getStackSize() == toAdd) // We have enough power to add everything
            return inv.injectItems(input, Actionable.MODULATE, src);

        T split = input.copy();
        input.setStackSize(toAdd);
        split.decStackSize(toAdd);
        split.add(inv.injectItems(input, Actionable.MODULATE, src));
        return split.getStackSize() > 0 ? split : null;
    }

    public static <T extends IAEStack<T>> T inventoryExtract(T input, IMEInventory<T> inv, IActionSource src) {
        return AEUtil.inventoryExtract(input, inv, src, null, Actionable.MODULATE);
    }

    public static <T extends IAEStack<T>> T inventoryExtract(T input, IMEInventory<T> inv, IActionSource src, Actionable mode) {
        return AEUtil.inventoryExtract(input, inv, src, null, mode);
    }

    public static <T extends IAEStack<T>> T inventoryExtract(T input, IMEInventory<T> inv, IActionSource src, IEnergySource energy) {
        return AEUtil.inventoryExtract(input, inv, src, energy, Actionable.MODULATE);
    }

    public static <T extends IAEStack<T>> T inventoryExtract(T input, IMEInventory<T> inv, IActionSource src, IEnergySource energy, Actionable mode) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(inv);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);

        T canExtract = inv.extractItems(input.copy(), Actionable.SIMULATE, src);

        if (canExtract == null) // There is no item
            return null;

        long toExtract = canExtract.getStackSize();

        double energyFactor = 0;
        if (energy != null) { // We need to factor in power available
            energyFactor = Math.max(1.0, inv.getChannel().transferFactor());
            double availablePower = energy.extractAEPower(toExtract / energyFactor, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            toExtract = Math.min((long) ((availablePower * energyFactor) + 0.9), toExtract);
        }

        if (toExtract < 1)
            return null;

        if (mode == Actionable.SIMULATE)
            return canExtract.setStackSize(toExtract);

        if (energy != null)
            energy.extractAEPower(toExtract / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);

        canExtract.setStackSize(toExtract);
        return inv.extractItems(canExtract, Actionable.MODULATE, src);

    }

    @SuppressWarnings("unchecked")
    public static boolean doesStorageContain(IMEInventory inv, IAEStack stack) {
        return inv.getAvailableItems(inv.getChannel().createList()).findPrecise(stack) != null;
    }

    public static boolean doesStorageContain(IMEInventory inv, Aspect aspect) {
        return AEUtil.doesStorageContain(inv, AEEssentiaStack.fromEssentiaStack(new EssentiaStack(aspect, 1)));
    }

    public static IAEEssentiaStack getAEStackFromAspect(Aspect aspect, int amount) {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class).createStack(new EssentiaStack(aspect, amount));
    }

    public static IAEEssentiaStack getAEStackFromAspect(Aspect aspect) {
        return AEUtil.getAEStackFromAspect(aspect, Integer.MAX_VALUE);
    }

    public static <T extends IAEStack<T>, C extends IStorageChannel<T>> C getStorageChannel(Class<C> clazz) {
        return AEApi.instance().storage().getStorageChannel(clazz);
    }

    public static <T extends IAEStack<T>> IItemList<T> getList(IStorageChannel<T> channel) {
        return channel.createList();
    }

    public static <T extends IAEStack<T>, C extends IStorageChannel<T>> IItemList<T> getList(Class<C> clazz) {
        return AEUtil.getList(AEUtil.getStorageChannel(clazz));
    }

    public static KeyBinding getFocusKeyBinding() {
        if (AEUtil.focusKeyBinding == null) {
            for (KeyBinding key : Minecraft.getMinecraft().gameSettings.keyBindings)
                if (key.getKeyCategory().equalsIgnoreCase("key.appliedenergistics2.category") &&
                        key.getKeyDescription().equalsIgnoreCase("key.toggle_focus.desc")) {
                    AEUtil.focusKeyBinding = key;
                    break;
                }
        }
        return AEUtil.focusKeyBinding;
    }

    public static boolean clearIntoMEInventory(IItemHandler handler, IMEInventory<IAEItemStack> inv, IActionSource src) {
        Preconditions.checkNotNull(handler);
        Preconditions.checkNotNull(inv);
        Preconditions.checkNotNull(src);
        if (handler.getSlots() < 1)
            return true;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot).copy();
            if (stack.isEmpty())
                continue;
            IAEItemStack aeStack = AEUtil.getStorageChannel(IItemStorageChannel.class).createStack(stack);
            if (aeStack == null || aeStack.getStackSize() != stack.getCount()) {
                ThELog.warn("Failed to create IAEItemStack for {}, report to developer!", stack.toString());
                return false;
            }
            IAEItemStack returned = AEUtil.inventoryInsert(aeStack, inv, src);
            if (returned != null && returned.getStackSize() > 0) { // Failed to clear handler
                handler.extractItem(slot, Math.toIntExact(stack.getCount() - returned.getStackSize()), false);
                return false;
            } else {
                handler.extractItem(slot, stack.getCount(), false);
            }
        }
        return true;
    }
}
