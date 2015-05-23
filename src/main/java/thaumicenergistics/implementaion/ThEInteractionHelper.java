package thaumicenergistics.implementaion;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.api.IThEInteractionHelper;
import thaumicenergistics.api.IThEWirelessEssentiaTerminal;
import thaumicenergistics.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.gui.ThEGuiHandler;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.inventory.HandlerWirelessEssentiaTerminal;
import thaumicenergistics.network.packet.server.PacketServerArcaneCraftingTerminal;
import appeng.api.AEApi;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.localization.PlayerMessages;
import appeng.tile.misc.TileSecurity;
import appeng.tile.networking.TileWireless;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ThEInteractionHelper
	implements IThEInteractionHelper
{

	@Override
	public long convertEssentiaAmountToFluidAmount( final long essentiaAmount )
	{
		return EssentiaConversionHelper.INSTANCE.convertEssentiaAmountToFluidAmount( essentiaAmount );
	}

	@Override
	public long convertFluidAmountToEssentiaAmount( final long milibuckets )
	{
		return EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( milibuckets );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Class getArcaneCraftingTerminalGUIClass()
	{
		return GuiArcaneCraftingTerminal.class;
	}

	@Override
	public void openWirelessTerminalGui( final EntityPlayer player, final IThEWirelessEssentiaTerminal terminalInterface )
	{
		// Valid player?
		if( ( player == null ) || ( player instanceof FakePlayer ) )
		{
			return;
		}

		// Ignored client side
		if( player.worldObj.isRemote )
		{
			return;
		}

		// Get the item the player is holding.
		ItemStack wirelessTerminal = player.getHeldItem();

		// Ensure the stack is valid
		if( ( wirelessTerminal == null ) )
		{
			// Invalid terminal
			return;
		}

		// Ensure the terminal has power
		if( terminalInterface.getAECurrentPower( wirelessTerminal ) == 0 )
		{
			// Terminal is dead
			player.addChatMessage( PlayerMessages.DeviceNotPowered.get() );
			return;
		}

		// Ensure the terminal is linked
		if( !HandlerWirelessEssentiaTerminal.isTerminalLinked( terminalInterface, wirelessTerminal ) )
		{
			// Unlinked terminal
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
			return;
		}

		// Get the encryption key
		long encryptionKey;
		try
		{
			encryptionKey = Long.parseLong( terminalInterface.getEncryptionKey( wirelessTerminal ) );
		}
		catch( NumberFormatException e )
		{
			// Invalid security key
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
			return;
		}

		// Get the linked source
		Object source = AEApi.instance().registries().locatable().getLocatableBy( encryptionKey );

		// Ensure it is a security terminal
		if( !( source instanceof TileSecurity ) )
		{
			// Invalid security terminal
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
			return;
		}

		// Get the terminal
		TileSecurity securityHost = (TileSecurity)source;

		// Get the grid
		IGrid hostGrid;
		try
		{
			hostGrid = securityHost.getGridNode( ForgeDirection.UNKNOWN ).getGrid();
		}
		catch( Exception e )
		{
			// Can not find the grid
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
			return;
		}

		// Get the AP's
		IMachineSet accessPoints = hostGrid.getMachines( TileWireless.class );

		// Loop over AP's and see if any are close enough to communicate with
		for( IGridNode APNode : accessPoints )
		{
			// Get the AP
			IWirelessAccessPoint AP = (IWirelessAccessPoint)APNode.getMachine();

			// Is the AP active?
			if( AP.isActive() )
			{
				// Is the player close enough to the AP?
				if( HandlerWirelessEssentiaTerminal.isAPInRangeOfPlayer( AP.getLocation(), AP.getRange(), player ) )
				{
					// Launch the gui
					ThEGuiHandler.launchGui( ThEGuiHandler.WIRELESS_TERMINAL_ID, player, player.worldObj, (int)player.posX, (int)player.posY,
						(int)player.posZ, new Object[] { new HandlerWirelessEssentiaTerminal( player, AP, terminalInterface, wirelessTerminal ) } );

					// All done.
					return;
				}
			}
		}

		// No AP's were close enough
		if( accessPoints.isEmpty() )
		{
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
		}
		else
		{
			player.addChatMessage( PlayerMessages.OutOfRange.get() );
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setArcaneCraftingTerminalRecipe( final ItemStack[] itemsVanilla )
	{
		try
		{
			boolean hasItems = false;

			// Ensure the input items array is the correct size
			if( ( itemsVanilla == null ) || ( itemsVanilla.length != 9 ) )
			{
				return;
			}

			// Create the AE items array
			IAEItemStack[] items = new IAEItemStack[9];

			// Get the items and convert them to their AE counterparts.
			for( int slotIndex = 0; slotIndex < 9; ++slotIndex )
			{
				if( itemsVanilla[slotIndex] != null )
				{
					items[slotIndex] = AEApi.instance().storage().createItemStack( itemsVanilla[slotIndex] );
					hasItems = true;
				}
			}

			// Send the list to the server
			if( hasItems )
			{
				new PacketServerArcaneCraftingTerminal().createNEIRequestSetCraftingGrid( Minecraft.getMinecraft().thePlayer, items )
								.sendPacketToServer();
			}
		}
		catch( Exception e )
		{
			// Silently ignore.
		}

	}

}
