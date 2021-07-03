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

/**
 * @author BrockWS
 */
public abstract class ContainerSharedEssentiaBus extends ContainerBase {

    protected PartSharedEssentiaBus part;

    public ContainerSharedEssentiaBus(EntityPlayer player, PartSharedEssentiaBus part) {
        super(player);
        this.part = part;
        this.sendFilter();
    }

    protected void bindContainerInventory(EssentiaFilter filter, IInventory inventory, int offsetX, int offsetY, int rows, int columns) {
        int i = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, i, offsetX + x * 18, offsetY + y * 18, this.calculateSlotGroup(i)));
                i++;
            }
        }
        this.recalculateSlots();
    }

    protected int calculateSlotGroup(int index) {
        return 0;
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
        this.sendFilter();
        this.recalculateSlots();
        super.detectAndSendChanges();
    }

    protected void sendFilter() {
        if (this.getEssentiaFilter() != null && this.player instanceof EntityPlayerMP) {
            PacketHandler.sendToPlayer((EntityPlayerMP) this.player, new PacketEssentiaFilter(this.getEssentiaFilter()));
        }
    }

    public void recalculateSlots() {
        for (Slot slot : this.inventorySlots)
            if (slot instanceof SlotGhost)
                ((SlotGhost) slot).setSlotEnabled(((SlotGhost) slot).getSlotGroup() <= this.getCapacityUpgrades());
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
