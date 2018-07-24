package thaumicenergistics.integration.appeng.cell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEStack;

import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.item.ItemEssentiaCell;

/**
 * @author BrockWS
 */
public class BasicEssentiaCellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemEssentiaCell;
    }

    @Override
    public <T extends IAEStack<T>> ICellInventoryHandler<T> getCellInventory(ItemStack stack, ISaveProvider container, IStorageChannel<T> channel) {
        return channel == this.getEssentiaStorageChannel() ? EssentiaCellInventory.getCell(stack, container) : null;
    }

    @Override
    public <T extends IAEStack<T>> void openChestGui(EntityPlayer entityPlayer, IChestOrDrive iChestOrDrive, ICellHandler iCellHandler, IMEInventoryHandler<T> imeInventoryHandler, ItemStack itemStack, IStorageChannel<T> iStorageChannel) {
        // TODO
    }

    @Override
    public <T extends IAEStack<T>> int getStatusForCell(ItemStack itemStack, IMEInventory<T> inv) {
        if (inv instanceof EssentiaCellInventoryHandler)
            return ((EssentiaCellInventoryHandler) inv).getStatusForCell();
        return 0;
    }

    @Override
    public <T extends IAEStack<T>> double cellIdleDrain(ItemStack itemStack, IMEInventory<T> inv) {
        return ((ICellInventoryHandler) inv).getCellInv().getIdleDrain();
    }

    private IStorageChannel getEssentiaStorageChannel() {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class);
    }
}
