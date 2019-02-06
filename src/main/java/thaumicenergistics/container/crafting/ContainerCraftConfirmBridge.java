package thaumicenergistics.container.crafting;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftConfirm;

/**
 * @author BrockWS
 */
public class ContainerCraftConfirmBridge extends ContainerCraftConfirm {

    public ContainerCraftConfirmBridge(InventoryPlayer ip, ITerminalHost te) {
        super(ip, te);
    }
}
