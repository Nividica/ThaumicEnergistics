package thaumicenergistics.client.gui.crafting;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.core.localization.GuiText;

import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 */
public class GuiCraftConfirmBridge extends GuiCraftConfirm {

    public GuiCraftConfirmBridge(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn.displayString.equals(GuiText.Cancel.getLocal())) {
            ThELog.info("Cancel");
            return;
        }

        super.actionPerformed(btn);
    }
}
