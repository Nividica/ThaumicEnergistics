package thaumicenergistics.integration.appeng.cell;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.me.storage.AbstractCellInventoryHandler;

/**
 * FIXME 23/07: rv6-alpha-4 will have a generic cell inventory, Deprecate
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
