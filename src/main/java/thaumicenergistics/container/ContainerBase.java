package thaumicenergistics.container;

import javax.annotation.Nullable;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;

import thaumicenergistics.container.slot.SlotArcaneResult;
import thaumicenergistics.container.slot.SlotGhost;
import thaumicenergistics.container.slot.SlotGhostEssentia;
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

    protected EntityPlayer player;

    public ContainerBase(EntityPlayer player) {
        this.player = player;
    }

    protected void bindPlayerArmour(InventoryPlayer inv, int offsetX, int offsetY) {
        this.addSlotToContainer(new Slot(inv, 36, offsetX, offsetY + 8 + 18 * 3) {
            public int getSlotStackLimit() {
                return 1;
            }

            public boolean isItemValid(ItemStack stack) {
                return stack.getItem().isValidArmor(stack, EntityEquipmentSlot.FEET, inv.player);
            }

            public boolean canTakeStack(EntityPlayer playerIn) {
                return (this.getStack().isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(this.getStack())) && super.canTakeStack(playerIn);
            }

            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return ItemArmor.EMPTY_SLOT_NAMES[EntityEquipmentSlot.FEET.getIndex()];
            }
        });
        this.addSlotToContainer(new Slot(inv, 37, offsetX, offsetY + 8 + 18 * 2) {
            public int getSlotStackLimit() {
                return 1;
            }

            public boolean isItemValid(ItemStack stack) {
                return stack.getItem().isValidArmor(stack, EntityEquipmentSlot.LEGS, inv.player);
            }

            public boolean canTakeStack(EntityPlayer playerIn) {
                return (this.getStack().isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(this.getStack())) && super.canTakeStack(playerIn);
            }

            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return ItemArmor.EMPTY_SLOT_NAMES[EntityEquipmentSlot.LEGS.getIndex()];
            }
        });
        this.addSlotToContainer(new Slot(inv, 38, offsetX, offsetY + 8 + 18) {
            public int getSlotStackLimit() {
                return 1;
            }

            public boolean isItemValid(ItemStack stack) {
                return stack.getItem().isValidArmor(stack, EntityEquipmentSlot.CHEST, inv.player);
            }

            public boolean canTakeStack(EntityPlayer playerIn) {
                return (this.getStack().isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(this.getStack())) && super.canTakeStack(playerIn);
            }

            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return ItemArmor.EMPTY_SLOT_NAMES[EntityEquipmentSlot.CHEST.getIndex()];
            }
        });
        this.addSlotToContainer(new Slot(inv, 39, offsetX, offsetY + 8) {
            public int getSlotStackLimit() {
                return 1;
            }

            public boolean isItemValid(ItemStack stack) {
                return stack.getItem().isValidArmor(stack, EntityEquipmentSlot.HEAD, inv.player);
            }

            public boolean canTakeStack(EntityPlayer playerIn) {
                return (this.getStack().isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(this.getStack())) && super.canTakeStack(playerIn);
            }

            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return ItemArmor.EMPTY_SLOT_NAMES[EntityEquipmentSlot.HEAD.getIndex()];
            }
        });
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

    /**
     * Called when a PacketUIAction is received by the server
     *
     * @param player Player that sent the action
     * @param packet Packet from client
     */
    public void onAction(EntityPlayerMP player, PacketUIAction packet) {
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
            if (ForgeUtil.isServer() && (held.isEmpty() || slot.getStack().isItemEqual(held))) {
                int i = ((ICraftingContainer) this).tryCraft(Math.min(dragType == 1 ? slot.getStack().getMaxStackSize() : 1, slot.getStack().getMaxStackSize() - held.getCount()));
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

    public void handleJEITransfer(EntityPlayer player, NBTTagCompound tag) {

    }
}
