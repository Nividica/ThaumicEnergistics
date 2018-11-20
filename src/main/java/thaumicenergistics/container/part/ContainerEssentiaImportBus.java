package thaumicenergistics.container.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;

import thaumicenergistics.container.slot.SlotGhostEssentia;
import thaumicenergistics.part.PartEssentiaImportBus;
import thaumicenergistics.util.EssentiaFilter;

/**
 * @author BrockWS
 */
public class ContainerEssentiaImportBus extends ContainerSharedEssentiaBus {
    public ContainerEssentiaImportBus(EntityPlayer player, PartEssentiaImportBus part) {
        super(player, part);
        this.bindContainerInventory(this.getEssentiaFilter(), new InventoryBasic("null", false, 9), 80, 40, 3, 3);
        this.bindUpgradesInventory(this.part.getInventoryByName("upgrades"), 187, 8, 4);
    }
}
