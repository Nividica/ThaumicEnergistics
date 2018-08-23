package thaumicenergistics.integration.appeng.cell;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.AbstractCellInventory;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.integration.appeng.AEEssentiaStack;
import thaumicenergistics.item.ItemCreativeEssentiaCell;

/**
 * FIXME 23/07: rv6-alpha-4 will have a generic cell inventory, Deprecate
 *
 * @author BrockWS
 */
public class EssentiaCellInventory extends AbstractCellInventory<IAEEssentiaStack> {

    private boolean isCreative;

    private EssentiaCellInventory(final ItemStack o, final ISaveProvider container) throws AppEngException {
        super(o, container, 8);
        isCreative = o.getItem() instanceof ItemCreativeEssentiaCell;
    }

    // *******************
    // Based on ItemCellInventory from Applied Energistics 2
    // *******************

    public static ICellInventoryHandler getCell(final ItemStack s, final ISaveProvider c) {
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
            ThaumicEnergistics.LOGGER.warn("Failed to load EssentiaStack " + tag.toString());
            return;
        }
        stack.setAmount(stackSize);
        if (stack.getAmount() > 0) {
            this.cellItems.add(this.getChannel().createStack(stack));
        }
    }

    @Override
    public IAEEssentiaStack injectItems(IAEEssentiaStack input, Actionable mode, IActionSource src) {
        if (input == null)
            return null;
        if (input.getStackSize() == 0)
            return null;
        if (this.cellType.isBlackListed(this.i, input))
            return input;
        if (this.isCreative) {
            // The cell contains every aspect anyway, so just say we "accepted" all of it
            return null;
        }
        EssentiaStack essentiaStack = input.getStack();

        IAEEssentiaStack a = this.getCellItems().findPrecise(input);
        if (a != null) {
            long remainingItemSlots = this.getRemainingItemCount();
            if (remainingItemSlots <= 0) {
                return input;
            }
            if (input.getStackSize() > remainingItemSlots) {
                IAEEssentiaStack b = input.copy();
                b.setStackSize(b.getStackSize() - remainingItemSlots);
                if (mode == Actionable.MODULATE) {
                    a.setStackSize(a.getStackSize() + remainingItemSlots);
                    this.saveChanges();
                }
                return b;
            } else {
                if (mode == Actionable.MODULATE) {
                    a.setStackSize(a.getStackSize() + input.getStackSize());
                    this.saveChanges();
                }
                return null;
            }
        }

        if (this.canHoldNewItem()) {
            int remainingCount = (int) (this.getRemainingItemCount() - this.getBytesPerType() * itemsPerByte);
            if (remainingCount > 0) {
                if (input.getStackSize() > remainingCount) {
                    EssentiaStack toReturn = essentiaStack.copy();
                    toReturn.setAmount(essentiaStack.getAmount() - remainingCount);
                    if (mode == Actionable.MODULATE) {
                        EssentiaStack toWrite = essentiaStack.copy();
                        toWrite.setAmount(remainingCount);

                        this.cellItems.add(AEEssentiaStack.fromEssentiaStack(toWrite));
                        this.saveChanges();
                    }
                    return AEEssentiaStack.fromEssentiaStack(toReturn);
                }

                if (mode == Actionable.MODULATE) {
                    this.cellItems.add(input);
                    this.saveChanges();
                }

                return null;
            }
        }
        return input;
    }

    @Override
    public IAEEssentiaStack extractItems(IAEEssentiaStack request, Actionable mode, IActionSource src) {
        if (request == null)
            return null;
        if (this.isCreative)
            return request.copy();

        long size = Math.min(Integer.MAX_VALUE, request.getStackSize());
        IAEEssentiaStack result = null;
        IAEEssentiaStack a = this.getCellItems().findPrecise(request);
        if (a != null) {
            result = a.copy();
            if (a.getStackSize() <= size) {
                result.setStackSize(a.getStackSize());
                if (mode == Actionable.MODULATE) {
                    a.setStackSize(0);
                    this.saveChanges();
                }
            } else {
                result.setStackSize(size);
                if (mode == Actionable.MODULATE) {
                    a.setStackSize(a.getStackSize() - size);
                    this.saveChanges();
                }
            }
        }
        return result;
    }

    @Override
    protected IItemList<IAEEssentiaStack> getCellItems() {
        if (!this.isCreative)
            return super.getCellItems();
        if (this.cellItems == null) {
            this.cellItems = this.getChannel().createList();
            Aspect.aspects.forEach((s, aspect) -> this.cellItems.add(this.getChannel().createStack(new EssentiaStack(aspect, 1000))));
        }
        return this.cellItems;
    }

    @Override
    protected void saveChanges() {
        if (!this.isCreative) // We don't want to save infinity stacks
            super.saveChanges();
    }

    @Override
    public IStorageChannel<IAEEssentiaStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class);
    }
}
