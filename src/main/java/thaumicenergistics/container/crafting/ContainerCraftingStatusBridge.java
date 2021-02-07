package thaumicenergistics.container.crafting;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.container.implementations.ContainerCraftingStatus;
import thaumicenergistics.part.PartSharedTerminal;

/**
 * @author BrockWS
 */
public class ContainerCraftingStatusBridge extends ContainerCraftingStatus {
    public ContainerCraftingStatusBridge(InventoryPlayer ip, PartSharedTerminal te) {
        super(ip, te);
    }
}
