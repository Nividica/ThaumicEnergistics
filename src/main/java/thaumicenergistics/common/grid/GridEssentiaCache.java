package thaumicenergistics.common.grid;

import java.util.HashSet;
import com.google.common.collect.ImmutableSet;
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
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.IEssentiaGrid;
import thaumicenergistics.api.grid.IEssentiaWatcherHost;
import thaumicenergistics.common.items.ItemCraftingAspect;
import thaumicenergistics.common.items.ItemEnum;

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
	final IGrid internalGrid;

	/**
	 * Manages the watchers
	 */
	private final EssentiaWatcherManager essentiaWatcherManger;

	/**
	 * The 'result' of essentia crafting operations.
	 */
	private final ItemStack craftingAspectItem;

	/**
	 * Watches the item network for essentia related item events.
	 */
	private final CraftingAspect_ItemWatcher craftingWatcher;

	public GridEssentiaCache( final IGrid grid )
	{
		// Set the grid
		this.internalGrid = grid;

		// Create the watcher manager
		this.essentiaWatcherManger = new EssentiaWatcherManager( this );

		// Set the aspect item
		this.craftingAspectItem = ItemEnum.CRAFTING_ASPECT.getStack();

		// Create the crafting watcher
		this.craftingWatcher = new CraftingAspect_ItemWatcher( this );
	}

	/**
	 * Called by the crafting watcher when an update is needed.
	 */
	void markForUpdate()
	{
		this.cacheNeedsUpdate = true;
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
			if( stack.isCraftable() && stack.hasTagCompound() && ( stack.getItem() instanceof ItemCraftingAspect ) )
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
				ItemCraftingAspect.setAspect( this.craftingAspectItem, aspect );

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
							aspectAEItem = AEApi.instance().storage().createItemStack( this.craftingAspectItem );
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
}
