package thaumicenergistics.container.part;

import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.slot.SlotGhost;
import thaumicenergistics.container.slot.SlotGhostEssentia;
import thaumicenergistics.part.PartSharedEssentiaBus;
import thaumicenergistics.util.EssentiaFilter;
import thaumicenergistics.util.FMLUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;

/**
 * @author BrockWS
 */
public abstract class ContainerSharedEssentiaBus extends ContainerBase {

    protected PartSharedEssentiaBus part;

    public ContainerSharedEssentiaBus(EntityPlayer player, PartSharedEssentiaBus part) {
        super(player);
        this.part = part;
        this.bindPlayerInventory(player.inventory, 0, 100);
        this.bindContainerInventory(this.getEssentiaFilter(), new InventoryBasic("null", false, 9), 62, 22);
    }

    private void bindContainerInventory(EssentiaFilter filter, IInventory inventory, int offsetX, int offsetY) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int id = i * 3 + j;
                int group; // TODO: Use a better way
                switch (id) {
                    case 0:
                    case 2:
                    case 6:
                    case 8:
                        group = 2;
                        break;
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                        group = 1;
                        break;
                    default:
                        group = 0;
                }
                this.addSlotToContainer(new SlotGhostEssentia(filter, inventory, id, offsetX + 18 * i, offsetY + 18 * j, group));
            }
        }
        this.recalculateSlots();
    }

    @Override
    public void detectAndSendChanges() {
        if (FMLUtil.isServer()) {

        }
        this.recalculateSlots();
        super.detectAndSendChanges();
    }

    protected void recalculateSlots() {
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
    protected EssentiaFilter getEssentiaFilter() {
        return this.part.getConfig();
    }

    protected int getCapacityUpgrades() {
        // TODO: Check how many upgrades
        return 2;
    }
}
