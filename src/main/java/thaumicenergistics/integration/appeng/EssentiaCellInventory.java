package thaumicenergistics.integration.appeng;

import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.me.storage.AbstractCellInventory;

/**
 * @author BrockWS
 */
public class EssentiaCellInventory extends AbstractCellInventory<IAEEssentiaStack> {

    protected EssentiaCellInventory(final NBTTagCompound data, final ISaveProvider container) {
        super(data, container, 8);
    }

    private EssentiaCellInventory(final ItemStack o, final ISaveProvider container) throws AppEngException {
        super(o, container, 8);
    }

    // *******************
    // Based on ItemCellInventory from Applied Energistics 2
    // *******************

    public static IMEInventoryHandler getCell(final ItemStack s, final ISaveProvider c) {
        try {
            return new EssentiaCellInventoryHandler(new EssentiaCellInventory(s, c));
        } catch (AppEngException e) {
            return null;
        }
    }

    private static boolean isStorageCell(ItemStack stack) {
        if (stack == null)
            return false;

        Item item = stack.getItem();
        if (item instanceof IStorageCell)
            return !((IStorageCell) item).storableInStorageCell();

        return false;
    }

    public static boolean isCell(ItemStack stack) {
        if (stack == null)
            return false;

        Item item = stack.getItem();
        if (item instanceof IStorageCell) {
            if (((IStorageCell) item).getChannel() == AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class))
                return ((IStorageCell) item).isStorageCell(stack);
        }

        return false;
    }

    @Override
    protected void loadCellItem(NBTTagCompound tag, int stackSize) {
        EssentiaStack stack = EssentiaStack.readFromNBT(tag);
        // TODO: Probably should catch errors and warn when failing to load it
        if (stack == null) {
            ThaumicEnergistics.LOG.warn("Failed to load EssentiaStack " + tag.toString());
            return;
        }
        stack.setAmount(stackSize);
        if (stack.getAmount() > 0) {
            this.cellItems.add(this.getChannel().createStack(stack));
        }
    }

    @Override
    public IAEEssentiaStack injectItems(IAEEssentiaStack input, Actionable actionable, IActionSource iActionSource) {
        if (input == null)
            return null;
        // TODO
        this.getCellItems();
        this.cellItems.add(input);
        this.saveChanges();
        return input;
    }

    @Override
    public IAEEssentiaStack extractItems(IAEEssentiaStack iaeEssentiaStack, Actionable actionable, IActionSource iActionSource) {
        // TODO
        return null;
    }

    @Override
    public IStorageChannel<IAEEssentiaStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class);
    }
}
