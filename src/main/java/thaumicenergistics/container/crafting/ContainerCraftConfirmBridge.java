package thaumicenergistics.container.crafting;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftConfirm;

import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketOpenGUI;
import thaumicenergistics.part.PartArcaneTerminal;

/**
 * @author BrockWS
 */
public class ContainerCraftConfirmBridge extends ContainerCraftConfirm {

    private PartArcaneTerminal part;

    public ContainerCraftConfirmBridge(InventoryPlayer ip, PartArcaneTerminal te) {
        super(ip, te);
        this.part = te;
    }

    @Override
    public void startJob() {
        super.startJob();
        PacketHandler.sendToServer(new PacketOpenGUI(ModGUIs.ARCANE_TERMINAL, this.part.getLocation().getPos(), this.part.side));
    }
}
