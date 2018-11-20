package thaumicenergistics.container.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;

import thaumicenergistics.part.PartEssentiaStorageBus;

/**
 * @author BrockWS
 */
public class ContainerEssentiaStorageBus extends ContainerSharedEssentiaBus {

    public ContainerEssentiaStorageBus(EntityPlayer player, PartEssentiaStorageBus part) {
        super(player, part);
        this.bindContainerInventory(this.getEssentiaFilter(), new InventoryBasic("null", false, 25), 80, 40, 5, 5);
        this.bindUpgradesInventory(this.part.getInventoryByName("upgrades"), 187, 8, 4);
    }
}
