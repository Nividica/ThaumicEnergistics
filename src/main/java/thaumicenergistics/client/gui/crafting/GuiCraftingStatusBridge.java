package thaumicenergistics.client.gui.crafting;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketOpenGUI;
import thaumicenergistics.part.PartArcaneTerminal;

/**
 * @author BrockWS
 */
public class GuiCraftingStatusBridge extends GuiCraftingStatus {

    private PartArcaneTerminal part;
    private GuiTabButton backButton;

    public GuiCraftingStatusBridge(InventoryPlayer inventoryPlayer, PartArcaneTerminal part) {
        super(inventoryPlayer, part);
        this.part = part;
        ReflectionHelper.setPrivateValue(GuiCraftingStatus.class, this, ThEApi.instance().items().arcaneTerminal().maybeStack(1).orElse(ItemStack.EMPTY), "myIcon");
    }

    @Override
    public void initGui() {
        super.initGui();
        this.backButton = ReflectionHelper.getPrivateValue(GuiCraftingStatus.class, this, "originalGuiBtn");
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn == this.backButton) {
            PacketHandler.sendToServer(new PacketOpenGUI(ModGUIs.ARCANE_TERMINAL, this.part.getLocation().getPos(), this.part.side));
            return;
        }
        super.actionPerformed(btn);
    }
}
