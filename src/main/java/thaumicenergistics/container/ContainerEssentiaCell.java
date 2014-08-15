package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.inventory.HandlerItemEssentiaCell;
import thaumicenergistics.items.ItemEssentiaCell;
import thaumicenergistics.network.packet.client.PacketClientEssentiaCell;
import thaumicenergistics.network.packet.server.PacketServerEssentiaCell;
import thaumicenergistics.util.EffectiveSide;
import thaumicenergistics.util.EssentiaCellTerminalWorker;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
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
	 * Import and export inventory slots.
	 */
	private PrivateInventory privateInventory = new PrivateInventory( ThaumicEnergistics.MOD_ID + ".item.essentia.cell.inventory", 2, 64 )
	{
		@Override
		public boolean isItemValidForSlot( int slotID, ItemStack itemStack )
		{
			return EssentiaItemContainerHelper.isContainer( itemStack );
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
	public ContainerEssentiaCell( EntityPlayer player, World world, int x, int y, int z )
	{
		// Call the super-constructor
		super( player );

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Get the tile entity for the chest
			this.hostChest = (TileChest)world.getTileEntity( x, y, z );

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
	 * Drops any items in the import and export inventory.
	 */
	@Override
	public void onContainerClosed( EntityPlayer player )
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
	 * Forwards the change to the client.
	 */
	@Override
	public void postAspectStackChange( AspectStack change )
	{
		// Send the change
		new PacketClientEssentiaCell().createListChanged( this.player, change ).sendPacketToPlayer();
	}

	/**
	 * Updates the selected aspect, aspect stack and gui.
	 */
	@Override
	public void onReceiveSelectedAspect( Aspect selectedAspect )
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
	 * Gets the currently selected aspect.
	 * 
	 * @return
	 */
	public AspectStack getSelectedAspectStack()
	{
		return this.selectedAspectStack;
	}

	/**
	 * Called when the user has clicked on an aspect.
	 * Sends that change to the server for validation.
	 */
	@Override
	public void setSelectedAspect( Aspect selectedAspect )
	{
		new PacketServerEssentiaCell().createUpdateSelectedAspect( this.player, selectedAspect ).sendPacketToServer();
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
			if( EssentiaCellTerminalWorker.hasWork( this.inventory ) )
			{

				try
				{
					// Get the energy grid
					IEnergyGrid eGrid = this.hostChest.getProxy().getEnergy();

					// Can we drain energy from the network?
					if( eGrid.extractAEPower( ContainerCellTerminalBase.POWER_PER_TRANSFER, Actionable.SIMULATE, PowerMultiplier.CONFIG ) >= ContainerCellTerminalBase.POWER_PER_TRANSFER )
					{
						// Do the work
						if( EssentiaCellTerminalWorker.doWork( this.inventory, this.monitor, null, this.selectedAspect ) )
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
	 * Called when the player has clicked the sorting mode button.
	 * 
	 * @param sortingMode
	 */
	public void sendSortModeChangeRequest( ComparatorMode sortingMode )
	{
		new PacketServerEssentiaCell().createRequestChangeSortMode( this.player, sortingMode ).sendPacketToServer();
	}

	/**
	 * Called when a client sends a sorting mode request.
	 * 
	 * @param sortingMode
	 */
	public void onClientRequestSortModeChange( ComparatorMode sortingMode, EntityPlayer player )
	{
		// Get the handler
		HandlerItemEssentiaCell cellHandler = this.getCellHandler();

		// Inform the handler of the change
		cellHandler.setSortingMode( sortingMode );

		// Send confirmation back to client
		new PacketClientEssentiaCell().createSortModeUpdate( player, sortingMode ).sendPacketToPlayer();
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
		return new HandlerItemEssentiaCell( essentiaCell );
	}
}
