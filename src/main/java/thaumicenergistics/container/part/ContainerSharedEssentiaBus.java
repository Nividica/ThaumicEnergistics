package thaumicenergistics.container.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;

import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.slot.SlotGhost;
import thaumicenergistics.container.slot.SlotGhostEssentia;
import thaumicenergistics.container.slot.SlotUpgrade;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketEssentiaFilter;
import thaumicenergistics.part.PartSharedEssentiaBus;
import thaumicenergistics.util.EssentiaFilter;
import thaumicenergistics.util.ThEUtil;

/**
 * @author BrockWS
 */
public abstract class ContainerSharedEssentiaBus extends ContainerBase {

    protected PartSharedEssentiaBus part;

    public ContainerSharedEssentiaBus(EntityPlayer player, PartSharedEssentiaBus part) {
        super(player);
        this.part = part;
        this.sendFilter();
        this.bindPlayerInventory(player.inventory, 0, 100);
    }

    protected void bindContainerInventory(EssentiaFilter filter, IInventory inventory, int offsetX, int offsetY, int rows, int columns) {
        this.bindContainerInventory(filter, inventory, offsetX, offsetY, rows, columns, 0);
    }

    protected void bindContainerInventory(EssentiaFilter filter, IInventory inventory, int offsetX, int offsetY, int rows, int columns, int groups) {
        if (rows == 3 && columns == 3) {
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
        } else {
            int i = 0;
            int perGroup = ThEUtil.divide(rows * columns, groups);
            for (int x = 0; x < rows; x++) {
                for (int y = 0; y < columns; y++) {
                    this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, i, offsetX + x * 18, offsetY + y * 18, ThEUtil.divide(i, perGroup)));
                    i++;
                }
            }
        }
        this.recalculateSlots();
    }

    protected void bindUpgradesInventory(IItemHandler handler, int offsetX, int offsetY, int count) {
        for (int i = 0; i < count; i++) {
            this.addSlotToContainer(new SlotUpgrade(handler, i, offsetX, offsetY + 18 * i) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem() instanceof IUpgradeModule && super.isItemValid(stack);
                }
            });
        }
    }

    @Override
    public void detectAndSendChanges() {
        // TODO: Send filter updates?
        this.recalculateSlots();
        super.detectAndSendChanges();
    }

    protected void sendFilter() {
        if (this.getEssentiaFilter() != null && this.player instanceof EntityPlayerMP) {
            PacketHandler.sendToPlayer((EntityPlayerMP) this.player, new PacketEssentiaFilter(this.getEssentiaFilter()));
        }
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
