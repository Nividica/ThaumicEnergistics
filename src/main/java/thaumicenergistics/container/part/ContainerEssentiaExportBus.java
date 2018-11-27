package thaumicenergistics.container.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;

import thaumicenergistics.part.PartEssentiaExportBus;

/**
 * @author BrockWS
 */
public class ContainerEssentiaExportBus extends ContainerSharedEssentiaBus {

    public ContainerEssentiaExportBus(EntityPlayer player, PartEssentiaExportBus part) {
        super(player, part);
        this.bindContainerInventory(this.getEssentiaFilter(), new InventoryBasic("null", false, 9), 62, 22, 3, 3);
        this.bindUpgradesInventory(this.part.getInventoryByName("upgrades"), 187, 8, 4);
        this.bindPlayerInventory(player.inventory, 0, 100);
    }

    @Override
    protected int calculateSlotGroup(int index) {
        switch (index) {
            case 1:
            case 3:
            case 5:
            case 7:
                return 1;
            case 0:
            case 2:
            case 6:
            case 8:
                return 2;
            default:
                return 0;
        }
    }
}
