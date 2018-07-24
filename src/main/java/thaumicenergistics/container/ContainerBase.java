package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;

import thaumicenergistics.container.slot.SlotGhost;
import thaumicenergistics.container.slot.SlotGhostEssentia;
import thaumicenergistics.util.EssentiaFilter;

/**
 * The base container for all containers in Thaumic Energistics
 * <p>
 *
 * @author BrockWS
 */
public abstract class ContainerBase extends Container {

    protected EntityPlayer player;

    public ContainerBase(EntityPlayer player) {
        this.player = player;
    }

    protected void bindPlayerInventory(InventoryPlayer player, int offsetX, int offsetY) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(player, 9 * i + j + 9, offsetX + 8 + 18 * j, offsetY + 2 + 18 * i));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(player, i, offsetX + 8 + 18 * i, offsetY + 60));
        }
    }

    @Override
    public ItemStack slotClick(int slotID, int dragType, ClickType clickType, EntityPlayer player) {
        //ThELog.info("slotID {}", slotID);
        if (slotID >= 36) {
            Slot slot = this.getSlot(slotID);
            if (slot instanceof SlotGhostEssentia) {
                if (((SlotGhostEssentia) slot).getFilter() != null) {
                    EssentiaFilter filter = ((SlotGhostEssentia) slot).getFilter();
                    ItemStack stack = player.inventory.getItemStack().copy();
                    int id = slot.getSlotIndex();

                    if (stack.getItem() instanceof IEssentiaContainerItem) {
                        IEssentiaContainerItem item = (IEssentiaContainerItem) stack.getItem();
                        if (item.getAspects(stack) != null) {
                            AspectList aspects = item.getAspects(stack);
                            filter.setAspect(aspects.getAspects()[0], id);
                        }
                    } else {
                        filter.setAspect(null, id);
                    }
                    return ItemStack.EMPTY;
                }
            }
            if (slot instanceof SlotGhost) {
                ItemStack stack = player.inventory.getItemStack().copy();
                stack.setCount(1);
                slot.putStack(stack);
                return ItemStack.EMPTY;
            }
        }
        return super.slotClick(slotID, dragType, clickType, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        // TODO
        return ItemStack.EMPTY;
    }

    public EssentiaFilter getEssentiaFilter() {
        return null;
    }

    public void setEssentiaFilter(EssentiaFilter filter) {
        this.getEssentiaFilter().deserializeNBT(filter.serializeNBT());
    }

    @Override
    public void detectAndSendChanges() {

        super.detectAndSendChanges();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
