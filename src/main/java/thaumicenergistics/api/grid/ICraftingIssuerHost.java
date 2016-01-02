package thaumicenergistics.api.grid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.storage.ITerminalHost;

public interface ICraftingIssuerHost
	extends ITerminalHost
{
	/**
	 * Gets the icon for this terminal.
	 * 
	 * @return
	 */
	public ItemStack getIcon();

	/**
	 * Launches the terminal's GUI.
	 * Used to return to the gui after crafting is confirmed or canceled.
	 */
	public void launchGUI( EntityPlayer player );
}
