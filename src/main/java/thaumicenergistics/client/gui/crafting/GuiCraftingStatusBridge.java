package thaumicenergistics.client.gui.crafting;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;

import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketOpenGUI;
import thaumicenergistics.part.PartSharedTerminal;

/**
 * @author BrockWS
 */
public class GuiCraftingStatusBridge extends GuiCraftingStatus {

    private PartSharedTerminal part;
    private GuiTabButton backButton;

    public GuiCraftingStatusBridge(InventoryPlayer inventoryPlayer, PartSharedTerminal part) {
        super(inventoryPlayer, part);
        this.part = part;
        ReflectionHelper.setPrivateValue(GuiCraftingStatus.class, this, part.getRepr(), "myIcon");
    }

    @Override
    public void initGui() {
        super.initGui();
        this.backButton = ReflectionHelper.getPrivateValue(GuiCraftingStatus.class, this, "originalGuiBtn");
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn == this.backButton) {
            PacketHandler.sendToServer(new PacketOpenGUI(this.part.getGui(), this.part.getLocation().getPos(), this.part.side));
            return;
        }
        super.actionPerformed(btn);
    }
}
