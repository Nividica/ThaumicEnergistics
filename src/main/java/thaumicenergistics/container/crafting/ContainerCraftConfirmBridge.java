package thaumicenergistics.container.crafting;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.container.implementations.ContainerCraftConfirm;

import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketOpenGUI;
import thaumicenergistics.part.PartSharedTerminal;

/**
 * @author BrockWS
 */
public class ContainerCraftConfirmBridge extends ContainerCraftConfirm {

    private PartSharedTerminal part;

    public ContainerCraftConfirmBridge(InventoryPlayer ip, PartSharedTerminal te) {
        super(ip, te);
        this.part = te;
    }

    @Override
    public void startJob() {
        super.startJob();
        PacketHandler.sendToServer(new PacketOpenGUI(part.getGui(), this.part.getLocation().getPos(), this.part.side));
    }
}
