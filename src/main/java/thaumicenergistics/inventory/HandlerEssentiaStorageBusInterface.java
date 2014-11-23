package thaumicenergistics.inventory;

import net.minecraft.tileentity.TileEntity;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;

class HandlerEssentiaStorageBusInterface
	extends AbstractHandlerEssentiaStorageBus
	implements IMEMonitorHandlerReceiver<IAEFluidStack>
{
	/**
	 * Interface the storage bus is facing.
	 */
	private TileInterface MEInterface = null;

	/**
	 * Handler to the interfaces ME grid.
	 */
	private MEInventoryHandler<IAEFluidStack> handler;

	/**
	 * Hashcode of the interface.
	 */
	private int handlerHash;

	/**
	 * Prevent infinite loops when sub-grid's sub-grid is hosts grid.
	 * yeah.
	 */
	private boolean canPostUpdate = true;

	/**
	 * Set when inject or extract is called to prevent the change event from
	 * posting. Prevents ghost doubles.
	 */
	private boolean haltChanges = false;

	/**
	 * Creates the interface handler.
	 * 
	 * @param part
	 */
	public HandlerEssentiaStorageBusInterface( final AEPartEssentiaStorageBus part )
	{
		super( part );
	}

	/**
	 * Checks if the sub-grid can accept this gas.
	 */
	@Override
	public boolean canAccept( final IAEFluidStack fluidStack )
	{
		// Is the fluid an essentia gas?
		if( this.isFluidEssentiaGas( fluidStack ) )
		{
			// Pass to handler
			if( this.handler != null )
			{
				return this.handler.canAccept( fluidStack );
			}
		}

		return false;
	}

	/**
	 * Attempts to extract the gas from the sub-grid.
	 */
	@Override
	public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable mode, final BaseActionSource source )
	{
		// Is the fluid an essentia gas?
		if( this.isFluidEssentiaGas( request ) )
		{
			if( this.handler != null )
			{
				// Halt the change event
				this.haltChanges = true;

				// Extract the gas
				IAEFluidStack extractedGas = this.handler.extractItems( request, mode, source );

				// Resume changes
				this.haltChanges = false;

				return extractedGas;
			}
		}

		return null;
	}

	/**
	 * Gets the gases from the sub-grid.
	 */
	@Override
	public IItemList<IAEFluidStack> getAvailableItems( final IItemList<IAEFluidStack> out )
	{
		if( this.handler != null )
		{
			// Get the subgrids fluids
			IItemList<IAEFluidStack> subGridFluids = this.handler.getAvailableItems( AEApi.instance().storage().createFluidList() );

			for( IAEFluidStack fluid : subGridFluids )
			{
				// Is the fluid as essentia gas?
				if( this.isFluidEssentiaGas( fluid ) )
				{
					// Add to the output list
					out.add( fluid );
				}
			}

		}

		return out;
	}

	/**
	 * Attempts to inject the gas into the sub-network.
	 */
	@Override
	public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable mode, final BaseActionSource source )
	{
		// Is the fluid an essentia gas?
		if( this.isFluidEssentiaGas( input ) )
		{
			if( this.handler != null )
			{
				// Halt the change event
				this.haltChanges = true;

				// Inject the gas
				IAEFluidStack remainingGas = this.handler.injectItems( input, mode, source );

				// Resume changes
				this.haltChanges = false;

				return remainingGas;
			}
		}
		return input;
	}

	/**
	 * Is the handler still valid for receiving changes?
	 */
	@Override
	public boolean isValid( final Object verificationToken )
	{
		return this.handler == verificationToken;
	}

	/**
	 * Sub-grid list changed, we should update.
	 */
	@Override
	public void onListUpdate()
	{
		if( this.canPostUpdate )
		{
			this.canPostUpdate = false;
			try
			{
				this.partStorageBus.getGridBlock().getGrid().postEvent( new MENetworkCellArrayUpdate() );
			}
			catch( Exception e )
			{

			}
		}
	}

	@Override
	public boolean onNeighborChange()
	{
		// Get the tile we are facing
		TileEntity tileEntity = this.getFaceingTile();

		// Is it an interface?
		if( tileEntity instanceof TileInterface )
		{
			// Get the tile hashcode
			int newHandlerHash = Platform.generateTileHash( tileEntity );

			// Do the hashes match?
			if( ( this.handlerHash == newHandlerHash ) && ( this.handlerHash != 0 ) )
			{
				return false;
			}

			// Post cell update to cell network.
			try
			{
				this.partStorageBus.getGridBlock().getGrid().postEvent( new MENetworkCellArrayUpdate() );
			}
			catch( Exception e )
			{

			}

			// Set the tile hashcode
			this.handlerHash = newHandlerHash;

			// Set the interface
			this.MEInterface = (TileInterface)tileEntity;

			// Clear the old handler.
			this.handler = null;

			// Get the fluid inventory
			IMEInventory inv = this.MEInterface.getFluidInventory();

			// Ensure the fluid inventory was retrieved
			if( inv != null )
			{
				// Create the handler
				this.handler = new MEInventoryHandler<IAEFluidStack>( inv, StorageChannel.FLUIDS );

				// Set the handler properties
				this.handler.myAccess = this.getAccess();
				this.handler.myWhitelist = IncludeExclude.WHITELIST;
				this.handler.myPriority = this.getPriority();

				if( inv instanceof IMEMonitor )
				{
					( (IMEMonitor)inv ).addListener( this, this.handler );
				}
			}

			return true;
		}

		// Not facing interface
		this.handlerHash = 0;
		this.MEInterface = null;

		// Was the handler attached to an interface?
		if( this.handler != null )
		{
			this.handler = null;
			return true;
		}

		return false;
	}

	/**
	 * A change occurred in the sub-grid, inform the host grid.
	 */
	@Override
	public void postChange( final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> change, final BaseActionSource actionSource )
	{
		// Are changes halted?
		if( this.haltChanges )
		{
			return;
		}

		// Update the host grid
		this.postAlterationToHostGrid( change );
	}

	@Override
	public void tickingRequest( final IGridNode node, final int TicksSinceLastCall )
	{
		this.canPostUpdate = true;
	}

}
