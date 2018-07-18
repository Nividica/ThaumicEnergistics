package thaumicenergistics.container;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumicenergistics.container.slot.SlotGhost;
import thaumicenergistics.container.slot.SlotGhostEssentia;
import thaumicenergistics.util.EssentiaFilter;
import thaumicenergistics.util.ThELog;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * The base container for all containers in Thaumic Energistics
 * <p>
 * TODO: Save inventory and config to part
 * FIXME: Ghost slots
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
        if (slotID >= 40) {
            ThELog.info("slotID {}", slotID);
            Slot slot = this.getSlot(slotID);
            if (slot instanceof SlotGhostEssentia) {
                if (this.getEssentiaFilter() != null) {
                    EssentiaFilter filter = this.getEssentiaFilter();
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

    protected EssentiaFilter getEssentiaFilter() {
        return null;
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
