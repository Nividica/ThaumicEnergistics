package thaumicenergistics.container.crafting;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftAmount;

/**
 * @author BrockWS
 */
public class ContainerCraftAmountBridge extends ContainerCraftAmount {

    public ContainerCraftAmountBridge(InventoryPlayer ip, ITerminalHost te) {
        super(ip, te);
    }
}
