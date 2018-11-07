package thaumicenergistics.container.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;

import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.slot.SlotGhost;
import thaumicenergistics.container.slot.SlotGhostEssentia;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketEssentiaFilter;
import thaumicenergistics.part.PartSharedEssentiaBus;
import thaumicenergistics.util.EssentiaFilter;

/**
 * @author BrockWS
 */
public abstract class ContainerSharedEssentiaBus extends ContainerBase {

    protected PartSharedEssentiaBus part;

    public ContainerSharedEssentiaBus(EntityPlayer player, PartSharedEssentiaBus part) {
        super(player);
        this.part = part;
        if (this.getEssentiaFilter() != null && this.player instanceof EntityPlayerMP) {
            PacketHandler.sendToPlayer((EntityPlayerMP) this.player, new PacketEssentiaFilter(this.getEssentiaFilter()));
        }
        this.bindPlayerInventory(player.inventory, 0, 100);
        this.bindContainerInventory(this.getEssentiaFilter(), new InventoryBasic("null", false, 9), 80, 40);
        this.bindUpgradesInventory(this.part.upgrades, 187, 8);
    }

    private void bindContainerInventory(EssentiaFilter filter, IInventory inventory, int offsetX, int offsetY) {
        this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, 0, offsetX, offsetY, 0));

        // 1 Upgrade
        this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, 1, offsetX, offsetY - 18, 1));
        this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, 2, offsetX - 18, offsetY, 1));
        this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, 3, offsetX + 18, offsetY, 1));
        this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, 4, offsetX, offsetY + 18, 1));

        // 2 Upgrades
        this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, 5, offsetX - 18, offsetY - 18, 2));
        this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, 6, offsetX + 18, offsetY - 18, 2));
        this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, 7, offsetX - 18, offsetY + 18, 2));
        this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, 8, offsetX + 18, offsetY + 18, 2));
        this.recalculateSlots();
    }

    private void bindUpgradesInventory(IInventory inventory, int offsetX, int offsetY) {
        this.addSlotToContainer(new Slot(inventory, 0, offsetX, offsetY) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof IUpgradeModule;
            }
        });
        this.addSlotToContainer(new Slot(inventory, 1, offsetX, offsetY + 18) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof IUpgradeModule;
            }
        });
        this.addSlotToContainer(new Slot(inventory, 2, offsetX, offsetY + 36) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof IUpgradeModule;
            }
        });
        this.addSlotToContainer(new Slot(inventory, 3, offsetX, offsetY + 54) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof IUpgradeModule;
            }
        });
    }

    @Override
    public void detectAndSendChanges() {
        this.recalculateSlots();
        super.detectAndSendChanges();
    }


    public void recalculateSlots() {
        for (Slot slot : this.inventorySlots) {
            if (slot instanceof SlotGhost) {
                if (((SlotGhost) slot).getSlotGroup() <= this.getCapacityUpgrades())
                    ((SlotGhost) slot).setSlotEnabled(true);
                else
                    ((SlotGhost) slot).setSlotEnabled(false);
            }
        }
    }

    @Override
    public EssentiaFilter getEssentiaFilter() {
        if (this.part == null)
            return null;
        return this.part.getConfig();
    }

    protected int getCapacityUpgrades() {
        if (this.part == null)
            return 0;
        return this.part.getInstalledUpgrades(Upgrades.CAPACITY);
    }
}
