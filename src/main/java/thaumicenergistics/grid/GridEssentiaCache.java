package thaumicenergistics.grid;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.networking.storage.IStorageGrid;

/**
 * Creates an essentia monitor for the attached grid.
 * 
 * @author Nividica
 * 
 */
public class GridEssentiaCache
	extends EssentiaMonitor
	implements IEssentiaGrid
{
	/**
	 * Grid the cache is part of.
	 */
	private final IGrid internalGrid;

	public GridEssentiaCache( final IGrid grid )
	{
		// Set the grid
		this.internalGrid = grid;
	}

	@Override
	public void addNode( final IGridNode gridNode, final IGridHost machine )
	{
		// Ignored
	}

	@MENetworkEventSubscribe
	public void onGridCacheReady( final MENetworkPostCacheConstruction event )
	{
		// Get the storage grid
		IStorageGrid storage = (IStorageGrid)this.internalGrid.getCache( IStorageGrid.class );

		// Wrap
		this.wrap( storage.getFluidInventory(), (IEnergyGrid)this.internalGrid.getCache( IEnergyGrid.class ), this.internalGrid );
	}

	@Override
	public void onJoin( final IGridStorage sourceStorage )
	{
		// Mark that the cache needs to be updated
		this.cacheNeedsUpdate = true;
	}

	@Override
	public void onSplit( final IGridStorage destinationStorage )
	{
	}

	@Override
	public void onUpdateTick()
	{
		try
		{
			/*
			 * If the cache is invalid and there are listeners this will update the cache to match the network.
			 * If there are no listeners the update is deferred until there are listeners, or the cache is accessed.
			 */
			if( this.cacheNeedsUpdate && ( this.listeners.size() > 0 ) )
			{
				// Update the cache
				this.updateCacheToMatchNetwork();
			}
		}
		catch( Exception e )
		{
			// Ignored
		}
	}

	@Override
	public void populateGridStorage( final IGridStorage destinationStorage )
	{
		// Ignored
	}

	@Override
	public void removeNode( final IGridNode gridNode, final IGridHost machine )
	{
		// Ignored
	}

}
