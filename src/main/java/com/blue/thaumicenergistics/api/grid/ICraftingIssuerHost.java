package com.blue.thaumicenergistics.api.grid;

import appeng.api.storage.ITerminalHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;



public interface ICraftingIssuerHost
    extends ITerminalHost
{
    /**
     * Gets the icon for this terminal.
     *
     * @return
     */
    ItemStack getIcon();

    /**
     * Launches the terminal's GUI.
     * Used to return to the gui after crafting is confirmed or canceled.
     */
    void launchGUI( EntityPlayer player );
}
