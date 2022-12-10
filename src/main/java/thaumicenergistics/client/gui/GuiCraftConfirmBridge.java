package thaumicenergistics.client.gui;

import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.core.localization.GuiText;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.api.grid.ICraftingIssuerHost;

/**
 * Bridges the AE2 CraftConfirm GUI and the ThE API.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class GuiCraftConfirmBridge extends GuiCraftConfirm {
    /**
     * Player using this GUI
     */
    protected EntityPlayer player;

    /**
     * The thing that issued the crafting request.
     */
    protected ICraftingIssuerHost host;

    public GuiCraftConfirmBridge(final EntityPlayer player, final ICraftingIssuerHost craftingHost) {
        // Call super
        super(player.inventory, craftingHost);

        // Set the player
        this.player = player;

        // Set the host
        this.host = craftingHost;
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        // Sanity check
        if (btn == null) {
            return;
        }

        // Call super
        super.actionPerformed(btn);

        // Cancel button or start button?
        if ((btn == super.getCancelButton()) || (btn.displayString.equals(GuiText.Start.getLocal()))) {
            this.host.launchGUI(this.player);
        }
    }
}
