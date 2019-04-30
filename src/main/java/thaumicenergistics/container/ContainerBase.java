package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.items.IItemHandler;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;

import thaumicenergistics.container.slot.*;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketInvHeldUpdate;
import thaumicenergistics.network.packets.PacketUIAction;
import thaumicenergistics.util.EssentiaFilter;
import thaumicenergistics.util.ForgeUtil;

/**
 * The base container for all containers in Thaumic Energistics
 * <p>
 *
 * @author BrockWS
 */
public abstract class ContainerBase extends Container {

    public EntityPlayer player;

    public ContainerBase(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        // TODO
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack slotClick(int slotID, int dragType, ClickType clickType, EntityPlayer player) {
        if (slotID < 0)
            return super.slotClick(slotID, dragType, clickType, player);
        if (slotID >= this.inventorySlots.size())
            return ItemStack.EMPTY;

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
        if (slot instanceof SlotArcaneResult && this instanceof ICraftingContainer) {
            ItemStack held = this.player.inventory.getItemStack();
            if (ForgeUtil.isServer() && (held.isEmpty() || slot.getStack().isItemEqual(held)) && slot.getStack().getMaxStackSize() - held.getCount() >= slot.getStack().getCount()) {
                int i = ((ICraftingContainer) this).tryCraft(Math.min(dragType == 1 ? slot.getStack().getMaxStackSize() : 1, (slot.getStack().getMaxStackSize() - held.getCount()) / slot.getStack().getCount()));
                if (i > 0) {
                    ItemStack toCraft = slot.getStack().copy();
                    toCraft.setCount(i);
                    ItemStack newHeld = ((ICraftingContainer) this).onCraft(toCraft);
                    newHeld.grow(held.getCount());
                    this.player.inventory.setItemStack(newHeld);
                    PacketHandler.sendToPlayer((EntityPlayerMP) this.player, new PacketInvHeldUpdate(newHeld));
                }
            }
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotID, dragType, clickType, player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    protected void bindPlayerArmour(EntityPlayer player, IItemHandler inv, int offsetX, int offsetY) {
        this.addSlotToContainer(new SlotArmor(player, inv, 0, offsetX, offsetY + 8 + 18 * 3));
        this.addSlotToContainer(new SlotArmor(player, inv, 1, offsetX, offsetY + 8 + 18 * 2));
        this.addSlotToContainer(new SlotArmor(player, inv, 2, offsetX, offsetY + 8 + 18));
        this.addSlotToContainer(new SlotArmor(player, inv, 3, offsetX, offsetY + 8));
    }

    protected void bindPlayerInventory(IItemHandler player, int offsetX, int offsetY) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new ThESlot(player, 9 * i + j + 9, offsetX + 8 + 18 * j, offsetY + 2 + 18 * i));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new ThESlot(player, i, offsetX + 8 + 18 * i, offsetY + 60));
        }
    }

    /**
     * Called when a PacketUIAction is received by the server
     *
     * @param player Player that sent the action
     * @param packet Packet from client
     */
    public void onAction(EntityPlayerMP player, PacketUIAction packet) {
    }

    public EssentiaFilter getEssentiaFilter() {
        return null;
    }

    public void setEssentiaFilter(EssentiaFilter filter) {
        this.getEssentiaFilter().deserializeNBT(filter.serializeNBT());
    }

    public void handleJEITransfer(EntityPlayer player, NBTTagCompound tag) {

    }
}
