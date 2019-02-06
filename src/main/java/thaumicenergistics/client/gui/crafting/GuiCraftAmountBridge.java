package thaumicenergistics.client.gui.crafting;

import java.awt.*;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketOpenGUI;
import thaumicenergistics.part.PartArcaneTerminal;
import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 */
public class GuiCraftAmountBridge extends GuiCraftAmount {

    private EntityPlayer player;
    private PartArcaneTerminal part;

    public GuiCraftAmountBridge(EntityPlayer player, PartArcaneTerminal part) {
        super(player.inventory, part);
        this.player = player;
        this.part = part;
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
        this.fontRenderer.drawString("NOT YET IMPLEMENTED", 15, -15, Color.RED.hashCode());
    }

    @Override
    public void initGui() {
        super.initGui();
        ItemStack icon = ThEApi.instance().items().arcaneTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
        if (!icon.isEmpty())
            this.buttonList.add(new GuiTabButton(this.guiLeft + 154, this.guiTop, icon, icon.getDisplayName(), this.itemRender));
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn.displayString.equals(GuiText.Next.getLocal())) {
            ThELog.info("Next");
            //GuiHandler.openGUI(ModGUIs.AE2_CRAFT_CONFIRM, this.player, this.part.getLocation().getPos(), this.part.side);
            //if (this.mc.currentScreen instanceof GuiCraftConfirmBridge) {
            //GuiCraftConfirmBridge ccb = (GuiCraftConfirmBridge) this.mc.currentScreen;
            //}
            return;
        }

        String name = ThEApi.instance().lang().itemArcaneTerminal().getLocalizedKey();
        if (btn instanceof GuiTabButton && ((GuiTabButton) btn).getMessage().equals(name)) {
            ThELog.info("Back");
            PacketHandler.sendToServer(new PacketOpenGUI(ModGUIs.ARCANE_TERMINAL, this.part.getLocation().getPos(), this.part.side));
            return;
        }

        super.actionPerformed(btn);
    }
}
