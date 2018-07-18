package thaumicenergistics.integration.appeng.cell;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.me.storage.AbstractCellInventoryHandler;

/**
 * FIXME: Don't use any core AE2 core, AKA remove AbstractCellInventoryHandler
 *
 * @author BrockWS
 */
public class EssentiaCellInventoryHandler extends AbstractCellInventoryHandler<IAEEssentiaStack> {

    public EssentiaCellInventoryHandler(IMEInventory c) {
        super(c, AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class));
    }

    @Override
    protected IAEEssentiaStack createConfigStackFromItem(ItemStack is) {
        // TODO: Check if instance of ItemDummyAspect, then convert to AEEssentiaStack
        // Based on ItemCellInventoryHandler
        // AEItemStack.fromItemStack( is );
        return null;
    }
}
