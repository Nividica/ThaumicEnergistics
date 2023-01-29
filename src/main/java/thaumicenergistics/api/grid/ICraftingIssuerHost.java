package thaumicenergistics.api.grid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.storage.ITerminalHost;

/**
 * Defines the requirements for a terminal that wishes to issue AE2 crafting jobs.
 *
 * @author Nividica
 *
 */
public interface ICraftingIssuerHost extends ITerminalHost {

    /**
     * Gets the icon for this terminal.
     *
     * @return
     */
    ItemStack getIcon();

    /**
     * Launches the terminal's GUI. Used to return to the gui after crafting is confirmed or canceled.
     */
    void launchGUI(EntityPlayer player);
}
