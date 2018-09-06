package thaumicenergistics.integration.appeng.cell;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.item.ItemCreativeEssentiaCell;
import thaumicenergistics.item.ItemEssentiaCell;

/**
 * @author BrockWS
 */
public class CreativeEssentiaCellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemCreativeEssentiaCell;
    }

    @Override
    public <T extends IAEStack<T>> ICellInventoryHandler<T> getCellInventory(ItemStack stack, ISaveProvider container, IStorageChannel<T> channel) {
        return channel == this.getEssentiaStorageChannel() ? CreativeEssentiaCellInventory.getCell(stack, container) : null;
    }

    private IStorageChannel getEssentiaStorageChannel() {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class);
    }
}
