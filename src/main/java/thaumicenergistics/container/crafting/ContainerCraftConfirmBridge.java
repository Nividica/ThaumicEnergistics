package thaumicenergistics.container.crafting;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.container.implementations.ContainerCraftConfirm;

import net.minecraft.util.IThreadListener;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.part.PartSharedTerminal;

/**
 * @author BrockWS
 * @author Alex811
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
        ((IThreadListener) part.getLocation().getWorld()).addScheduledTask(() ->
                GuiHandler.openGUI(ModGUIs.values()[this.part.getGui().ordinal()], this.getPlayerInv().player, this.part.getLocation().getPos(), this.part.side));
    }
}
