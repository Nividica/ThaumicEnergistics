package thaumicenergistics.container.part;

import appeng.container.implementations.ContainerPriority;
import net.minecraft.entity.player.InventoryPlayer;
import thaumicenergistics.part.PartEssentiaStorageBus;

/**
 * @author Alex811
 */
public class ContainerPriorityBridge extends ContainerPriority {
    public ContainerPriorityBridge(InventoryPlayer ip, PartEssentiaStorageBus te) {
        super(ip, te);
    }
}
