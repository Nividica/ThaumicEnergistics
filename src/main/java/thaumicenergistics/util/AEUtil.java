package thaumicenergistics.util;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.integration.appeng.AEEssentiaStack;

/**
 * @author BrockWS
 */
public class AEUtil {

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
}
