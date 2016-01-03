package thaumicenergistics.common.container;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.ICraftingIssuerHost;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.grid.EssentiaMonitor;
import thaumicenergistics.common.inventory.HandlerItemEssentiaCell;
import thaumicenergistics.common.inventory.PrivateInventory;
import thaumicenergistics.common.items.ItemEssentiaCell;
import thaumicenergistics.common.network.packet.client.Packet_C_EssentiaCellTerminal;
import thaumicenergistics.common.network.packet.server.Packet_S_EssentiaCellTerminal;
import thaumicenergistics.common.storage.AspectStackComparator.AspectStackComparatorMode;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper.AspectItemType;
import appeng.api.config.Settings;
import appeng.api.config.ViewItems;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.tile.storage.TileChest;
import appeng.util.Platform;

/**
 * Inventory container for essentia cells in a ME chest.
 * 
 * @author Nividica
 * 
 */
public class ContainerEssentiaCell
	extends ContainerEssentiaCellTerminalBase
{
	/**
	 * The ME chest the cell is stored in.
	 */
	private TileChest hostChest;

	/**
	 * Network source representing the player who is interacting with the
	 * container.
	 */
	private PlayerSource playerSource = null;

	/**
	 * Compiler safe reference to the TileChest when using the
	 * ISaveProvider interface.
	 */
	private ISaveProvider chestSaveProvider;

	/**
	 * Import and export inventory slots.
	 */
	private PrivateInventory privateInventory = new PrivateInventory( ThaumicEnergistics.MOD_ID + ".item.essentia.cell.inventory", 2, 64 )
	{
		@Override
		public boolean isItemValidForSlot( final int slotID, final ItemStack itemStack )
		{
			// Get the type
			AspectItemType iType = EssentiaItemContainerHelper.INSTANCE.getItemType( itemStack );

			// True if jar or jar label
			return ( iType == AspectItemType.EssentiaContainer ) || ( iType == AspectItemType.JarLabel );
		}
	};

	/**
	 * Creates the container.
	 * 
	 * @param player
	 * The player that owns this container.
	 * @param world
	 * The world the ME chest is in.
	 * @param x
	 * X position of the ME chest.
	 * @param y
	 * Y position of the ME chest.
	 * @param z
	 * Z position of the ME chest.
	 */
	public ContainerEssentiaCell( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Call the super-constructor
		super( player );

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Get the tile entity for the chest
			this.hostChest = (TileChest)world.getTileEntity( x, y, z );

			/*
			 * Note: Casting the hostChest to an object is required to prevent the compiler
			 * from seeing the soft-dependencies of AE2, such a buildcraft, which it attempts
			 * to resolve at compile time.
			 * */
			Object hostObject = this.hostChest;
			this.chestSaveProvider = ( (ISaveProvider)hostObject );

			// Create the action source
			this.playerSource = new PlayerSource( this.player, (IActionHost)hostObject );

			try
			{
				IMEInventoryHandler<IAEFluidStack> handler = null;

				// Get the chest handler
				List<IMEInventoryHandler> hostCellArray = this.hostChest.getCellArray( StorageChannel.FLUIDS );
				if( hostCellArray.size() > 0 )
				{
					handler = hostCellArray.get( 0 );
				}

				// Get the monitor
				if( handler != null )
				{
					// Create the essentia monitor
					this.monitor = new EssentiaMonitor( (IMEMonitor<IAEFluidStack>)handler, this.hostChest.getProxy().getEnergy(), this );

					// Attach to the monitor
					this.attachToMonitor();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			// Request a full update from the server
			Packet_S_EssentiaCellTerminal.sendFullUpdateRequest( player );
			this.hasRequested = true;
		}

		// Bind our inventory
		this.bindToInventory( this.privateInventory );

	}

	/**
	 * Gets a handler for the essentia cell.
	 * 
	 * @return
	 */
	private HandlerItemEssentiaCell getCellHandler()
	{
		// Ensure we have a host
		if( this.hostChest == null )
		{
			return null;
		}

		// Get the cell
		ItemStack essentiaCell = this.hostChest.getStackInSlot( 1 );

		// Ensure we have the cell
		if( ( essentiaCell == null ) || !( essentiaCell.getItem() instanceof ItemEssentiaCell ) )
		{
			return null;
		}

		// Get the handler
		return new HandlerItemEssentiaCell( essentiaCell, this.chestSaveProvider );
	}

	@Override
	protected BaseActionSource getActionSource()
	{
		return this.playerSource;
	}

	/**
	 * Transfers essentia.
	 */
	@Override
	public void doWork( final int elapsedTicks )
	{
		// Transfer essentia if needed.
		this.transferEssentiaFromWorkSlots();
	}

	@Override
	public ICraftingIssuerHost getCraftingHost()
	{
		// Can't craft.
		return null;
	}

	@Override
	public void onClientRequestAutoCraft( final EntityPlayer player, final Aspect aspect )
	{
		// Ignored, can't issue, or even see, craftable essentia from here.
	}

	/**
	 * Gets the current list from the AE monitor and sends
	 * it to the client.
	 */
	@Override
	public void onClientRequestFullUpdate()
	{
		// Get the handler
		HandlerItemEssentiaCell cellHandler = this.getCellHandler();

		// Did we get the handler?
		if( cellHandler != null )
		{
			// Send the viewing mode
			Packet_C_EssentiaCellTerminal.sendViewingModes( this.player, cellHandler.getSortingMode(), cellHandler.getViewMode() );
		}

		// Send the list
		if( ( this.monitor != null ) && ( this.hostChest.isPowered() ) )
		{
			Packet_C_EssentiaCellTerminal.sendFullList( this.player, this.repo.getAll() );
		}
		else
		{
			Packet_C_EssentiaCellTerminal.sendFullList( this.player, new ArrayList<IAspectStack>() );

		}
	}

	@Override
	public void onClientRequestSortModeChange( final EntityPlayer player, final boolean backwards )
	{
		// Get the handler
		HandlerItemEssentiaCell cellHandler = this.getCellHandler();

		// Change the sorting mode
		AspectStackComparatorMode sortingMode;
		if( backwards )
		{
			sortingMode = cellHandler.getSortingMode().previousMode();
		}
		else
		{
			sortingMode = cellHandler.getSortingMode().nextMode();
		}

		// Inform the handler of the change
		cellHandler.setSortingMode( sortingMode );

		// Send confirmation back to client
		Packet_C_EssentiaCellTerminal.sendViewingModes( player, sortingMode, cellHandler.getViewMode() );
	}

	@Override
	public void onClientRequestViewModeChange( final EntityPlayer player, final boolean backwards )
	{
		// Get the handler
		HandlerItemEssentiaCell cellHandler = this.getCellHandler();

		// Change the view mode
		ViewItems viewMode = Platform.rotateEnum( cellHandler.getViewMode(), backwards, Settings.VIEW_MODE.getPossibleValues() );

		// Inform the handler of the change
		cellHandler.setViewMode( viewMode );

		// Send confirmation back to client
		Packet_C_EssentiaCellTerminal.sendViewingModes( player, cellHandler.getSortingMode(), viewMode );
	}

	/**
	 * Drops any items in the import and export inventory.
	 */
	@Override
	public void onContainerClosed( final EntityPlayer player )
	{

		if( EffectiveSide.isServerSide() )
		{
			// Is there a monitor
			if( this.monitor != null )
			{
				// Ensure it gets detached.
				( (EssentiaMonitor)this.monitor ).detach();
			}

			for( int i = 0; i < 2; i++ )
			{
				this.player.dropPlayerItemWithRandomChoice( ( (Slot)this.inventorySlots.get( i ) ).getStack(), false );
			}
		}

		super.onContainerClosed( player );
	}
}
