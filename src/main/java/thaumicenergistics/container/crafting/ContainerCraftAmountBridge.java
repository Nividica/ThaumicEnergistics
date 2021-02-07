package thaumicenergistics.container.crafting;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.container.implementations.ContainerCraftAmount;
import thaumicenergistics.part.PartSharedTerminal;

/**
 * @author BrockWS
 */
public class ContainerCraftAmountBridge extends ContainerCraftAmount {

    public ContainerCraftAmountBridge(InventoryPlayer ip, PartSharedTerminal te) {
        super(ip, te);
    }
}
