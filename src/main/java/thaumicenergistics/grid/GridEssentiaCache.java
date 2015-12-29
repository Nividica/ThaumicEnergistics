package thaumicenergistics.grid;

import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.items.ItemCraftingAspect;
import thaumicenergistics.registries.ItemEnum;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.google.common.collect.ImmutableSet;

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
	public class WatcherManager
		implements IMEEssentiaMonitorReceiver
	{
		/**
		 * Maps Node->Watcher
		 */
		private HashMap<IGridNode, IEssentiaWatcher> watchers = new HashMap<IGridNode, IEssentiaWatcher>();

		/**
		 * Maps Aspect -> Watchers
		 */
		private HashMap<Aspect, HashSet<IEssentiaWatcher>> watchedAspects = new HashMap<Aspect, HashSet<IEssentiaWatcher>>();

		/**
		 * True when the manager is listening for changes.
		 */
		private boolean isListeningForChanges = false;

		/**
		 * Adds a watcher.
		 * 
		 * @param node
		 * @param watcher
		 */
		public void addWatcher( final IGridNode node, final IEssentiaWatcher watcher )
		{
			// Add the watcher
			this.watchers.put( node, watcher );

			// Is the manager not listening for changes?
			if( !this.isListeningForChanges )
			{
				// Listen for changes
				GridEssentiaCache.this.addListener( this, null );
				this.isListeningForChanges = true;
			}
		}

		@Override
		public boolean isValid( final Object verificationToken )
		{
			return this.isListeningForChanges;
		}

		/**
		 * Called by watchers when a new aspect is to be tracked.
		 */
		public void onWatcherAddAspect( final IEssentiaWatcher watcher, final Aspect aspect )
		{
			HashSet<IEssentiaWatcher> aWatchers;

			// Does the set need to be created?
			if( !this.watchedAspects.containsKey( aspect ) )
			{
				// Create the set
				aWatchers = new HashSet<IEssentiaWatcher>();
				this.watchedAspects.put( aspect, aWatchers );
			}
			else
			{
				// Get the set
				aWatchers = this.watchedAspects.get( aspect );
			}

			// Add the watcher
			aWatchers.add( watcher );
		}

		/**
		 * Called by watchers just before they are cleared.
		 * 
		 * @param watcher
		 * @param previouslyTrackedAspects
		 */
		public void onWatcherCleared( final IEssentiaWatcher watcher, final HashSet<Aspect> previouslyTrackedAspects )
		{
			for( Aspect aspect : previouslyTrackedAspects )
			{
				this.onWatcherRemoveAspect( watcher, aspect );
			}
		}

		/**
		 * Called by watchers when an aspect is no longer to be tracked.
		 * 
		 * @param watcher
		 * @param aspect
		 */
		public void onWatcherRemoveAspect( final IEssentiaWatcher watcher, final Aspect aspect )
		{
			// Get the set
			HashSet<IEssentiaWatcher> aWatchers = this.watchedAspects.get( aspect );
			if( aWatchers != null )
			{
				// Remove the watcher
				aWatchers.remove( watcher );

				// Is the set empty?
				if( aWatchers.isEmpty() )
				{
					// Remove the mapping
					this.watchedAspects.remove( aspect );
				}
			}
		}

		@Override
		public void postChange( final IMEEssentiaMonitor fromMonitor, final Iterable<AspectStack> changes )
		{
			// Fast bail
			if( this.watchedAspects.isEmpty() )
			{
				return;
			}

			// Loop over all changes
			for( AspectStack change : changes )
			{
				// Is the change being watched for?
				if( this.watchedAspects.containsKey( change.aspect ) )
				{
					// Get the set
					HashSet<IEssentiaWatcher> watcherSet = this.watchedAspects.get( change.aspect );

					// Get the full amount in the system
					long fullAmount = GridEssentiaCache.this.getEssentiaAmount( change.aspect );

					// Update each watcher
					for( IEssentiaWatcher watcher : watcherSet )
					{
						// Get the watchers host
						IEssentiaWatcherHost host = watcher.getHost();

						// Update the host
						if( host != null )
						{
							host.onEssentiaChange( change.aspect, fullAmount, change.stackSize );
						}
					}
				}
			}
		}

		/**
		 * Removes a watcher.
		 * 
		 * @param node
		 */
		public void removeWatcher( final IGridNode node )
		{
			// Get the watcher
			IEssentiaWatcher watcher = this.watchers.get( node );
			if( watcher != null )
			{
				// Clear the watcher
				watcher.clear();

				// Remove the watcher
				this.watchers.remove( node );

				// Is the list empty?
				if( this.watchers.isEmpty() )
				{
					// Ensure the watched aspects is also empty
					this.watchedAspects.clear();

					// Stop listening
					GridEssentiaCache.this.removeListener( this );
					this.isListeningForChanges = false;
				}
			}
		}
	}

	/**
	 * Grid the cache is part of.
	 */
	private final IGrid internalGrid;

	/**
	 * Manages the watchers
	 */
	private final WatcherManager watcherManger;

	/**
	 * The 'result' of essentia crafting operations.
	 */
	private final ItemStack aspectItem;

	public GridEssentiaCache( final IGrid grid )
	{
		// Set the grid
		this.internalGrid = grid;

		// Create the watcher manager
		this.watcherManger = new WatcherManager();

		// Set the aspect item
		this.aspectItem = ItemEnum.CRAFTING_ASPECT.getStack();
	}

	@Override
	public void addNode( final IGridNode gridNode, final IGridHost machine )
	{
		// Does the node wish to watch for changes?
		if( machine instanceof IEssentiaWatcherHost )
		{
			// Cast
			IEssentiaWatcherHost host = (IEssentiaWatcherHost)machine;

			// Create the watcher
			EssentiaWatcher watcher = new EssentiaWatcher( this.watcherManger, host );

			// Add to the watcher manager
			this.watcherManger.addWatcher( gridNode, watcher );

			// Inform the host it has a watcher
			host.updateWatcher( watcher );
		}
	}

	@Override
	public long injectEssentia( final Aspect aspect, final long amount, final Actionable mode, final BaseActionSource source, final boolean powered )
	{
		// Call super
		long amountRejected = super.injectEssentia( aspect, amount, mode, source, powered );

		if( ( mode == Actionable.MODULATE ) && ( amountRejected < amount ) )
		{
			// Get the crafting grid
			ICraftingGrid craftingGrid = this.internalGrid.getCache( ICraftingGrid.class );

			// Get the CPU list
			ImmutableSet<ICraftingCPU> cpus = craftingGrid.getCpus();

			// Are there any crafting CPU's?
			if( cpus.size() > 0 )
			{
				// Set the aspect
				ItemCraftingAspect.setAspect( this.aspectItem, aspect );

				// Delay creating this as long as possible
				IAEItemStack aspectAEItem = null;

				// Set the amounts
				long amountToInject = amount - amountRejected;

				// Are any CPU's waiting for this?
				for( ICraftingCPU cpu : cpus )
				{
					// Is the cpu crafting, and is it a cluster?
					if( cpu.isBusy() && ( cpu instanceof CraftingCPUCluster ) )
					{
						if( aspectAEItem == null )
						{
							// Create AE version
							aspectAEItem = AEApi.instance().storage().createItemStack( this.aspectItem );
							aspectAEItem.setStackSize( 1 );
						}

						// Cast
						CraftingCPUCluster cluster = (CraftingCPUCluster)cpu;

						// Is the cluster waiting for an aspect item?
						if( cluster.isMaking( aspectAEItem ) )
						{
							// Not a fan of the loop, but inject items doesn't return anything useful
							for( ; ( amountToInject > 0 ) && ( cluster.isBusy() ); --amountToInject )
							{
								// Inject the aspect item
								cluster.injectItems( aspectAEItem, mode, source );
							}

							// Has everything been accounted for?
							if( amountToInject == 0 )
							{
								// All done
								break;
							}
						}
					}
				}
			}
		}

		return amountRejected;
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
		// Was the node an essentia watcher?
		if( machine instanceof IEssentiaWatcherHost )
		{
			this.watcherManger.removeWatcher( gridNode );
		}
	}

}
