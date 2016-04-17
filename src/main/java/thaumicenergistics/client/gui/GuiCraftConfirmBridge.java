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
public class GuiCraftConfirmBridge
	extends GuiCraftConfirm
{

	/**
	 * Cancel button.
	 */
	protected GuiButton buttonCancel;

	/**
	 * Player using this GUI
	 */
	protected EntityPlayer player;

	/**
	 * The thing that issued the crafting request.
	 */
	protected ICraftingIssuerHost host;

	public GuiCraftConfirmBridge( final EntityPlayer player, final ICraftingIssuerHost craftingHost )
	{
		// Call super
		super( player.inventory, craftingHost );

		// Set the player
		this.player = player;

		// Set the host
		this.host = craftingHost;
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		// Sanity check
		if( btn == null )
		{
			return;
		}

		// Call super
		super.actionPerformed( btn );

		// Cancel button or start button?
		if( ( btn == this.buttonCancel ) || ( btn.displayString == GuiText.Start.getLocal() ) )
		{
			this.host.launchGUI( this.player );
		}
	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		// Locate and remove the null cancel button
		for( int i = 0; i < this.buttonList.size(); ++i )
		{
			// Get the button
			Object btn = this.buttonList.get( i );

			// Is it null?
			if( btn == null )
			{
				// Remove it and move i back 1
				this.buttonList.remove( i-- );
			}
		}

		// Create a new cancel button
		this.buttonList.add( this.buttonCancel = new GuiButton( 0, this.guiLeft + 6, ( this.guiTop + this.ySize ) - 25, 50, 20, GuiText.Cancel
						.getLocal() ) );

	}

}
