package thaumicenergistics.container.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;

import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import thaumicenergistics.config.AESettings;
import thaumicenergistics.part.PartEssentiaImportBus;

/**
 * @author BrockWS
 */
public class ContainerEssentiaImportBus extends ContainerSharedEssentiaBus {

    public ContainerEssentiaImportBus(EntityPlayer player, PartEssentiaImportBus part) {
        super(player, part);
        this.bindContainerInventory(this.getEssentiaFilter(), new InventoryBasic("null", false, 9), 62, 22, 3, 3);
        this.bindUpgradesInventory(this.part.getInventoryByName("upgrades"), 187, 8, 4);
        this.bindPlayerInventory(new PlayerMainInvWrapper(player.inventory), 0, 100);
    }

    @Override
    protected AESettings.SUBJECT getAESettingSubject() {
        return AESettings.SUBJECT.ESSENTIA_IMPORT_BUS;
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
