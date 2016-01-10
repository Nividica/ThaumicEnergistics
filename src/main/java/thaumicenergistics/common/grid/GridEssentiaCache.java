package thaumicenergistics.common.grid;

import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.*;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.common.items.ItemCraftingAspect;
import thaumicenergistics.common.registries.ItemEnum;
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
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
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
	private class AspectCraftingWatcher
		implements IMEMonitorHandlerReceiver<IAEItemStack>
	{

		public AspectCraftingWatcher()
		{
		}

		@Override
		public boolean isValid( final Object verificationToken )
		{
			return GridEssentiaCache.this.internalGrid == verificationToken;
		}

		@Override
		public void onListUpdate()
		{
			// Ignored
		}

		@Override
		public void postChange( final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final BaseActionSource actionSource )
		{
			for( IAEItemStack stack : change )
			{
				// Is the stack craftable, has NBT tag, and is a crafting aspect?
				if( stack.isCraftable()
								&& stack.hasTagCompound()
								&& ( stack.getItem() instanceof ItemCraftingAspect ) )
				{
					GridEssentiaCache.this.markForUpdate();
					break;
				}
			}
		}

	}

	public class EssentiaWatcherManager
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
		public void postChange( final IMEEssentiaMonitor fromMonitor, final Iterable<IAspectStack> changes )
		{
			// Fast bail
			if( this.watchedAspects.isEmpty() )
			{
				return;
			}

			// Loop over all changes
			for( IAspectStack change : changes )
			{
				// Is the change being watched for?
				if( this.watchedAspects.containsKey( change.getAspect() ) )
				{
					// Get the set
					HashSet<IEssentiaWatcher> watcherSet = this.watchedAspects.get( change.getAspect() );

					// Get the full amount in the system
					long fullAmount = GridEssentiaCache.this.getEssentiaAmount( change.getAspect() );

					// Update each watcher
					for( IEssentiaWatcher watcher : watcherSet )
					{
						// Get the watchers host
						IEssentiaWatcherHost host = watcher.getHost();

						// Update the host
						if( host != null )
						{
							host.onEssentiaChange( change.getAspect(), fullAmount, change.getStackSize() );
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
	final IGrid internalGrid;

	/**
	 * Manages the watchers
	 */
	private final EssentiaWatcherManager essentiaWatcherManger;

	/**
	 * The 'result' of essentia crafting operations.
	 */
	private final ItemStack aspectItem;

	/**
	 * Watches the item network for essentia related events.
	 */
	private AspectCraftingWatcher craftingWatcher;

	public GridEssentiaCache( final IGrid grid )
	{
		// Set the grid
		this.internalGrid = grid;

		// Create the watcher manager
		this.essentiaWatcherManger = new EssentiaWatcherManager();

		// Set the aspect item
		this.aspectItem = ItemEnum.CRAFTING_ASPECT.getStack();

		// Create the crafting watcher
		this.craftingWatcher = new AspectCraftingWatcher();
	}

	/**
	 * Add in any craftable aspects
	 */
	@Override
	protected void updateCacheToMatchNetwork()
	{
		// Call super
		super.updateCacheToMatchNetwork();

		// Is the network powered?
		if( !this.energyGrid.isNetworkPowered() )
		{
			return;
		}

		// Get the item monitor
		IStorageGrid storage = (IStorageGrid)this.internalGrid.getCache( IStorageGrid.class );
		IMEMonitor<IAEItemStack> itemMonitor = storage.getItemInventory();

		// Get stored items
		IItemList<IAEItemStack> storedItems = itemMonitor.getStorageList();
		if( ( storedItems == null ) || ( storedItems.size() == 0 ) )
		{
			return;
		}

		// Create the aspect list
		HashSet<Aspect> craftableAspects = new HashSet<Aspect>();

		// Check each item for craftability and type
		for( IAEItemStack stack : storedItems )
		{
			if( stack == null )
			{
				continue;
			}

			// Is the stack craftable, has NBT tag, and is a crafting aspect?
			if( stack.isCraftable()
							&& stack.hasTagCompound()
							&& ( stack.getItem() instanceof ItemCraftingAspect ) )
			{
				// Get the aspect
				Aspect aspect = ItemCraftingAspect.getAspect( stack.getTagCompound().getNBTTagCompoundCopy() );
				if( aspect != null )
				{
					// Add the aspect
					craftableAspects.add( aspect );
				}

			}
		}

		// Anything added?
		if( craftableAspects.size() > 0 )
		{
			this.setCraftableAspects( craftableAspects );
		}

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
			EssentiaWatcher watcher = new EssentiaWatcher( this.essentiaWatcherManger, host );

			// Add to the watcher manager
			this.essentiaWatcherManger.addWatcher( gridNode, watcher );

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

		// Set the crafting watcher
		storage.getItemInventory().addListener( this.craftingWatcher, this.internalGrid );
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
			this.essentiaWatcherManger.removeWatcher( gridNode );
		}
	}

	/**
	 * Called by the crafting watcher when an update is needed.
	 */
	void markForUpdate()
	{
		this.cacheNeedsUpdate = true;
	}
}
