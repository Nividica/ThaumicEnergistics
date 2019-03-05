package thaumicenergistics.container.crafting;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftingStatus;

/**
 * @author BrockWS
 */
public class ContainerCraftingStatusBridge extends ContainerCraftingStatus {
    public ContainerCraftingStatusBridge(InventoryPlayer ip, ITerminalHost te) {
        super(ip, te);
    }
}
