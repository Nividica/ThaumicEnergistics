package thaumicenergistics.gui;

import java.lang.reflect.Field;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thaumicenergistics.api.ICraftingIssuerTerminalHost;
import thaumicenergistics.network.packet.server.PacketServerConfirmCraftingJob;
import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;

public class GuiCraftAmountBridge
	extends GuiCraftAmount
{
	/**
	 * AE Part that is issued the crafting request.
	 */
	protected ICraftingIssuerTerminalHost terminalHost;

	/**
	 * Button that returns to the terminal's GUI.
	 */
	protected GuiTabButton buttonReturnToTerminalHost;

	/**
	 * The player that is using the GUI.
	 */
	protected EntityPlayer player;

	/**
	 * The next button.
	 */
	protected GuiButton buttonNext;

	/**
	 * The amount-to-craft text box.
	 */
	protected GuiNumberBox amountToCraft;

	public GuiCraftAmountBridge( final EntityPlayer player, final ICraftingIssuerTerminalHost te )
	{
		// Call super
		super( player.inventory, te );

		// Set the terminal host
		this.terminalHost = te;

		// Set the player
		this.player = player;
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		if( btn == this.buttonReturnToTerminalHost )
		{
			// Change back to terminal GUI
			this.terminalHost.launchGUI( this.player );
		}
		else if( btn == this.buttonNext )
		{
			try
			{
				// Parse the amount
				long amount = Long.parseLong( this.amountToCraft.getText() );

				// Ask server to show confirm gui
				new PacketServerConfirmCraftingJob().createRequestConfirmAutoCraft( this.player, amount, isShiftKeyDown() ).sendPacketToServer();
			}
			catch( final NumberFormatException e )
			{
				// Reset amount to 1
				this.amountToCraft.setText( "1" );
			}
		}
		else
		{
			// Call super
			super.actionPerformed( btn );
		}
	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		// Get the icon of the terminal
		ItemStack myIcon = this.terminalHost.getIcon();

		// Create the return button
		this.buttonReturnToTerminalHost = new GuiTabButton( this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), itemRender );
		this.buttonList.add( this.buttonReturnToTerminalHost );

		// Get the next button
		for( Object buttonObj : this.buttonList )
		{
			if( buttonObj instanceof GuiButton )
			{
				GuiButton button = (GuiButton)buttonObj;
				if( button.displayString == GuiText.Next.getLocal() )
				{
					this.buttonNext = button;
					break;
				}
			}
		}

		// Get the amount to craft
		Field atcField;
		try
		{
			atcField = GuiCraftAmount.class.getDeclaredField( "amountToCraft" );
			atcField.setAccessible( true );
			this.amountToCraft = (GuiNumberBox)atcField.get( this );
		}
		catch( Exception e )
		{
		}

	}

}
