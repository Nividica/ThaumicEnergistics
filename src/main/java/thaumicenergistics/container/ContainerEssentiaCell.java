package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.integration.tc.EssentiaCellTerminalWorker;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.inventory.HandlerItemEssentiaCell;
import thaumicenergistics.items.ItemEssentiaCell;
import thaumicenergistics.network.packet.client.PacketClientEssentiaCell;
import thaumicenergistics.network.packet.server.PacketServerEssentiaCell;
import thaumicenergistics.util.EffectiveSide;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.tile.storage.TileChest;

/**
 * Inventory container for essentia cells in a ME chest.
 * 
 * @author Nividica
 * 
 */
public class ContainerEssentiaCell
	extends ContainerCellTerminalBase
{
	/**
	 * The aspect the player has selected.
	 */
	private AspectStack selectedAspectStack;

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
			return EssentiaItemContainerHelper.instance.isContainer( itemStack );
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
				// Get the chest handler
				IMEInventoryHandler<IAEFluidStack> handler = this.hostChest.getHandler( StorageChannel.FLUIDS );

				// Get the monitor
				if( handler != null )
				{
					// Get the cell inventory monitor
					this.monitor = (IMEMonitor<IAEFluidStack>)handler;

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
			new PacketServerEssentiaCell().createFullUpdateRequest( player ).sendPacketToServer();
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

	/**
	 * Checks if there is any work to perform.
	 * If there is it does so.
	 */
	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		// Do we have a monitor?
		if( this.monitor != null )
		{
			// Is there work to do?
			if( EssentiaCellTerminalWorker.instance.hasWork( this.inventory ) )
			{

				try
				{
					// Get the energy grid
					IEnergyGrid eGrid = this.hostChest.getProxy().getEnergy();

					// Can we drain energy from the network?
					if( eGrid.extractAEPower( ContainerCellTerminalBase.POWER_PER_TRANSFER, Actionable.SIMULATE, PowerMultiplier.CONFIG ) >= ContainerCellTerminalBase.POWER_PER_TRANSFER )
					{
						// Do the work
						if( EssentiaCellTerminalWorker.instance.doWork( this.inventory, this.monitor, this.playerSource, this.selectedAspect,
							this.player ) )
						{
							// We did work, drain power
							eGrid.extractAEPower( ContainerCellTerminalBase.POWER_PER_TRANSFER, Actionable.MODULATE, PowerMultiplier.CONFIG );
						}
					}
				}
				catch( GridAccessException e )
				{
				}
			}
		}
	}

	/**
	 * Gets the currently selected aspect.
	 * 
	 * @return
	 */
	public AspectStack getSelectedAspectStack()
	{
		return this.selectedAspectStack;
	}

	/**
	 * Gets the current list from the AE monitor and sends
	 * it to the client.
	 */
	@Override
	public void onClientRequestFullUpdate()
	{
		// Call super
		super.onClientRequestFullUpdate();

		// Get the handler
		HandlerItemEssentiaCell cellHandler = this.getCellHandler();

		// Did we get the handler?
		if( cellHandler != null )
		{
			// Send the sorting mode
			new PacketClientEssentiaCell().createSortModeUpdate( this.player, cellHandler.getSortingMode() ).sendPacketToPlayer();
		}

		// Send the list
		if( this.monitor != null )
		{
			new PacketClientEssentiaCell().createUpdateFullList( this.player, this.aspectStackList ).sendPacketToPlayer();
		}
	}

	/**
	 * Called when a client sends a sorting mode request.
	 * 
	 * @param sortingMode
	 */
	public void onClientRequestSortModeChange( final ComparatorMode sortingMode, final EntityPlayer player )
	{
		// Get the handler
		HandlerItemEssentiaCell cellHandler = this.getCellHandler();

		// Inform the handler of the change
		cellHandler.setSortingMode( sortingMode );

		// Send confirmation back to client
		new PacketClientEssentiaCell().createSortModeUpdate( player, sortingMode ).sendPacketToPlayer();
	}

	/**
	 * Drops any items in the import and export inventory.
	 */
	@Override
	public void onContainerClosed( final EntityPlayer player )
	{
		super.onContainerClosed( player );

		if( EffectiveSide.isServerSide() )
		{
			for( int i = 0; i < 2; i++ )
			{
				this.player.dropPlayerItemWithRandomChoice( ( (Slot)this.inventorySlots.get( i ) ).getStack(), false );
			}
		}
	}

	/**
	 * Updates the selected aspect, aspect stack and gui.
	 */
	@Override
	public void onReceiveSelectedAspect( final Aspect selectedAspect )
	{
		this.selectedAspect = selectedAspect;

		if( this.selectedAspect != null )
		{
			for( AspectStack stack : this.aspectStackList )
			{
				if( ( stack != null ) && ( stack.aspect == this.selectedAspect ) )
				{
					this.selectedAspectStack = stack;

					break;
				}
			}
		}

		// Is this the client?
		if( EffectiveSide.isClientSide() )
		{
			// Update the gui
			this.guiBase.updateSelectedAspect();
		}
		else
		{
			// Update the client
			new PacketClientEssentiaCell().createSelectedAspectUpdate( this.player, this.selectedAspect ).sendPacketToPlayer();
		}
	}

	/**
	 * Forwards the change to the client.
	 */
	@Override
	public void postAspectStackChange( final AspectStack change )
	{
		// Send the change
		new PacketClientEssentiaCell().createListChanged( this.player, change ).sendPacketToPlayer();
	}

	/**
	 * Called when the player has clicked the sorting mode button.
	 * 
	 * @param sortingMode
	 */
	public void sendSortModeChangeRequest( final ComparatorMode sortingMode )
	{
		new PacketServerEssentiaCell().createRequestChangeSortMode( this.player, sortingMode ).sendPacketToServer();
	}

	/**
	 * Called when the user has clicked on an aspect.
	 * Sends that change to the server for validation.
	 */
	@Override
	public void setSelectedAspect( final Aspect selectedAspect )
	{
		new PacketServerEssentiaCell().createUpdateSelectedAspect( this.player, selectedAspect ).sendPacketToServer();
	}
}
