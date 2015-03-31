package thaumicenergistics.implementaion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.Blocks;
import thaumicenergistics.api.IConfig;
import thaumicenergistics.api.IEssentiaGas;
import thaumicenergistics.api.ITransportPermissions;
import thaumicenergistics.api.IWirelessEssentiaTerminal;
import thaumicenergistics.api.Items;
import thaumicenergistics.api.Parts;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.gui.ThEGuiHandler;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.inventory.HandlerWirelessEssentiaTerminal;
import appeng.api.AEApi;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.core.localization.PlayerMessages;
import appeng.tile.misc.TileSecurity;
import appeng.tile.networking.TileWireless;
import com.google.common.collect.ImmutableList;

public class API
	extends ThEApi
{
	private final ThEBlocks blocks = new ThEBlocks();
	private final ThEItems items = new ThEItems();
	private final ThEParts parts = new ThEParts();
	private final List<IEssentiaGas> essentiaGases = new ArrayList<IEssentiaGas>();
	private final ThETransportPermissions transportPermissions = new ThETransportPermissions();

	/**
	 * Create the API instance.
	 */
	public static final API instance = new API();

	/**
	 * Private constructor
	 */
	private API()
	{
	}

	@Override
	public Blocks blocks()
	{
		return this.blocks;
	}

	@Override
	public IConfig config()
	{
		return ThaumicEnergistics.config;
	}

	@Override
	public long convertEssentiaAmountToFluidAmount( final long essentiaAmount )
	{
		return EssentiaConversionHelper.instance.convertEssentiaAmountToFluidAmount( essentiaAmount );
	}

	@Override
	public long convertFluidAmountToEssentiaAmount( final long milibuckets )
	{
		return EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( milibuckets );
	}

	@Override
	public ImmutableList<List<IEssentiaGas>> essentiaGases()
	{
		// Do we need to update?
		if( this.essentiaGases.size() != GaseousEssentia.gasList.size() )
		{
			// Clear the list
			this.essentiaGases.clear();

			// Get the iterator
			Iterator<Entry<Aspect, GaseousEssentia>> iterator = GaseousEssentia.gasList.entrySet().iterator();

			// Add all gasses
			while( iterator.hasNext() )
			{
				this.essentiaGases.add( iterator.next().getValue() );
			}
		}

		return ImmutableList.of( this.essentiaGases );
	}

	@Override
	public Items items()
	{
		return this.items;
	}

	@Override
	public void openWirelessTerminalGui( final EntityPlayer player, final IWirelessEssentiaTerminal terminalInterface )
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
	public Parts parts()
	{
		return this.parts;
	}

	@Override
	public ITransportPermissions transportPermissions()
	{
		return this.transportPermissions;
	}

}
