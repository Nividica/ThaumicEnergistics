package thaumicenergistics.grid;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.networking.IMEEssentiaMonitor;
import thaumicenergistics.api.networking.IMEEssentiaMonitorReceiver;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.api.storage.IEssentiaRepo;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import com.google.common.collect.ImmutableList;

/**
 * Wraps a fluid & power grid for easy essentia conversion and usage.
 * 
 * @author Nividica
 * 
 */
public class EssentiaMonitor
	implements IMEEssentiaMonitor, IMEMonitorHandlerReceiver<IAEFluidStack>
{
	/**
	 * The amount of power required to transfer 1 essentia.
	 */
	public static final double AE_PER_ESSENTIA = 0.3;

	/**
	 * Objects who wish to be notified of any changes.
	 */
	protected final HashMap<IMEEssentiaMonitorReceiver, Object> listeners;

	/**
	 * The grids fluid monitor.
	 */
	protected IMEMonitor<IAEFluidStack> fluidMonitor;

	/**
	 * The grids energy manager.
	 */
	protected IEnergyGrid energyGrid;

	/**
	 * Collection backed by the cache that shows the essentia amounts in the network. Is created the first time it is needed.<br>
	 * Any changes to the cache are reflected in the view.
	 */
	private Collection<IAspectStack> cacheView;

	/**
	 * Used to validate the state of the fluid listener, can not be null
	 */
	private WeakReference<Object> token;

	/**
	 * Cache of essentia.
	 */
	private final IEssentiaRepo cache;

	/**
	 * When true the full storage list needs to be pulled to update the cache.
	 */
	protected boolean cacheNeedsUpdate = false;

	public EssentiaMonitor()
	{
		// Create the cache
		this.cache = new EssentiaRepo();

		// Create the listeners table
		this.listeners = new HashMap<IMEEssentiaMonitorReceiver, Object>();
	}

	/**
	 * Wraps the specified fluid monitor and energy grid.
	 * 
	 * @param fluidMonitor
	 * Fluid monitor to listen to
	 * @param energyGrid
	 * Energy grid to extract power from
	 * @param validationToken
	 * Used to validate the state of the fluid listener, can not be null
	 */
	public EssentiaMonitor( final IMEMonitor<IAEFluidStack> fluidMonitor, final IEnergyGrid energyGrid, final Object validationToken )
	{
		// Call default constructor
		this();

		// Call wrap
		this.wrap( fluidMonitor, energyGrid, validationToken );
	}

	/**
	 * Mirror method of the injectEssentia. Used to defer power calculations, because the simulation is not always accurate, and Essentia gas should
	 * not be stored in partial amounts.
	 * 
	 * @param aspect
	 * @param amount
	 * @param mode
	 * @param source
	 * @return Amount that was <strong>not</strong> injected
	 */
	private long injectEssentiaSafely( final Aspect aspect, final long amount, final Actionable mode, final BaseActionSource source,
										final GaseousEssentia essentiaGas )
	{
		// Create the fluid request
		IAEFluidStack fluidRequest = EssentiaConversionHelper.INSTANCE.createAEFluidStackInEssentiaUnits( essentiaGas, amount );

		// Inject fluid
		IAEFluidStack fluidRejected = this.fluidMonitor.injectItems( fluidRequest, mode, source );

		// Was any rejected?
		if( ( fluidRejected != null ) && ( fluidRejected.getStackSize() > 0 ) )
		{
			if( fluidRejected.getStackSize() == fluidRequest.getStackSize() )
			{
				// All essentia was rejected
				return amount;
			}

			// Calculate the adjusted amount, essentia gas can not be stored in partial units
			long rejectedAdjustedAmount = EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( fluidRejected.getStackSize() );
			return rejectedAdjustedAmount;
		}

		// All essentia was accepted.
		return 0;
	}

	/**
	 * Notifies all listeners of the specified changes.
	 * 
	 * @param changes
	 */
	private void notifyListeners( final List<IAspectStack> changes )
	{
		// Get an immutable copy
		ImmutableList<IAspectStack> changeList = ImmutableList.copyOf( changes );

		// Get the listener iterator
		Iterator<Entry<IMEEssentiaMonitorReceiver, Object>> entryIterator = this.listeners.entrySet().iterator();

		// Inform all listeners of the changes
		while( entryIterator.hasNext() )
		{
			// Get the listener
			Entry<IMEEssentiaMonitorReceiver, Object> entry = entryIterator.next();

			// Validate the token
			if( entry.getKey().isValid( entry.getValue() ) )
			{
				// Valid token
				entry.getKey().postChange( this, changeList );
			}
			else
			{
				// Invalid token, remove from listener list
				entryIterator.remove();
			}
		}
	}

	private boolean setAspectCraftability( final Aspect aspect, final boolean isCraftable, final boolean doNotify )
	{
		// Get the aspect stack
		IAspectStack stack = this.cache.getOrDefault( aspect, null );

		boolean changesMade = false;

		// Not stored?
		if( stack == null )
		{
			// If the cache doesn't have this aspect, and its not craftable then there is nothing to do
			if( isCraftable )
			{
				// Put it in the cache
				this.cache.postChange( aspect, 0, true );

				// Set changes made
				changesMade = true;
			}
		}
		else
		{
			// Is stored, but is stack size 0 and setting to not craftable?
			if( stack.isEmpty() && ( !isCraftable ) )
			{
				// Remove the stack
				this.cache.remove( aspect );

				// Set changes made
				changesMade = true;
			}
			// Is it changing?
			else if( stack.getCraftable() != isCraftable )
			{
				// Set craftability
				stack.setCraftable( isCraftable );

				// Set changes made
				changesMade = true;
			}
		}

		// Do notify?
		if( doNotify && changesMade && ( this.listeners.size() > 0 ) )
		{
			// Create the change
			ArrayList<IAspectStack> changes = new ArrayList<IAspectStack>();

			// Add the aspect
			changes.add( new AspectStack( aspect, 0, isCraftable ) );

			// Notify
			this.notifyListeners( changes );
		}

		return changesMade;
	}

	/**
	 * Sets if the aspect is craftable.
	 * 
	 * @param aspect
	 * @param isCraftable
	 */
	protected void setAspectCraftability( final Aspect aspect, final boolean isCraftable )
	{
		this.setAspectCraftability( aspect, isCraftable, true );
	}

	/**
	 * Sets each aspect as craftable.
	 * 
	 * @param aspects
	 */
	protected void setCraftableAspects( final HashSet<Aspect> aspects )
	{
		// Create the changes
		ArrayList<IAspectStack> changes = new ArrayList<IAspectStack>();

		// Set each aspect
		for( Aspect aspect : aspects )
		{
			// Was the crafting status changed?
			if( this.setAspectCraftability( aspect, true, false ) )
			{
				// Add the aspect
				changes.add( new AspectStack( aspect, 0, true ) );
			}
		}

		// Notify listeners?
		if( ( changes.size() > 0 ) && ( this.listeners.size() > 0 ) )
		{
			// Notify
			this.notifyListeners( changes );
		}
	}

	/**
	 * Updates the cache to match the contents of the network and updates any
	 * listeners of the changes.
	 */
	@SuppressWarnings("null")
	protected void updateCacheToMatchNetwork()
	{
		// Get the list of fluids in the network
		IItemList<IAEFluidStack> fluidStackList;

		// Validate the list
		if( ( fluidStackList = this.fluidMonitor.getStorageList() ) == null )
		{
			// Invalid list
			return;
		}

		// Changes made to the cache
		List<IAspectStack> aspectChanges = null;

		// The currently stored aspects
		Set<Aspect> previousAspects = null;

		// Are there any listeners?
		boolean hasListeners = ( this.listeners.size() > 0 );
		if( hasListeners )
		{
			// Create the change trackers
			aspectChanges = new ArrayList<IAspectStack>();
			previousAspects = new HashSet<Aspect>();
			previousAspects.addAll( this.cache.aspectSet() );
		}
		else
		{
			// Can safely clear the cache
			this.cache.clear();
		}

		// Loop over all fluids
		for( IAEFluidStack fluidStack : fluidStackList )
		{
			// Ensure the fluid is an essentia gas
			if( !( fluidStack.getFluid() instanceof GaseousEssentia ) )
			{
				// Not an essentia gas.
				continue;
			}

			// Get the gas aspect
			Aspect aspect = ( (GaseousEssentia)fluidStack.getFluid() ).getAspect();

			// Calculate the new amount
			Long newAmount = EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() );

			// Update the cache
			IAspectStack prevStack = this.cache.setAspect( aspect, newAmount, false );

			// Are there any listeners?
			if( hasListeners )
			{
				// Remove from the previous mapping
				previousAspects.remove( aspect );

				// Calculate the difference
				long diff = ( newAmount - ( prevStack != null ? prevStack.getStackSize() : 0 ) );

				if( diff != 0 )
				{
					// Add to the changes
					aspectChanges.add( new AspectStack( aspect, diff ) );
				}
			}
		}

		// Are there any listeners?
		if( hasListeners )
		{
			// Anything left in the previous mapping is no longer present in the network
			for( Aspect aspect : previousAspects )
			{
				aspectChanges.add( new AspectStack( aspect, -this.cache.remove( aspect ).getStackSize() ) );
			}

			// Notify listeners
			this.notifyListeners( aspectChanges );
		}

		// Mark the cache as valid
		this.cacheNeedsUpdate = false;

	}

	@Override
	public void addListener( final IMEEssentiaMonitorReceiver listener, final Object verificationToken )
	{
		// If this is the first listener, and the cache is out of sync, it needs to be updated first
		if( ( this.listeners.size() == 0 ) && ( this.cacheNeedsUpdate ) )
		{
			this.updateCacheToMatchNetwork();
		}

		this.listeners.put( listener, verificationToken );
	}

	/**
	 * Detaches from the fluid monitor.
	 * Note that the monitor is no longer valid from this point forward.
	 */
	public void detach()
	{
		if( this.fluidMonitor != null )
		{
			this.fluidMonitor.removeListener( this );
			this.token = null;
		}
	}

	@Override
	public long extractEssentia( final Aspect aspect, final long amount, final Actionable mode, final BaseActionSource source, final boolean powered )
	{
		// Ensure the aspect is not null, and the amount is > 0 
		if( ( aspect == null ) || ( amount <= 0 ) )
		{
			// Invalid arguments
			return 0;
		}

		// Get the gas form of the essentia
		GaseousEssentia essentiaGas;

		// Ensure there is a gas form of the aspect.
		if( ( essentiaGas = GaseousEssentia.getGasFromAspect( aspect ) ) == null )
		{
			// Unregistered aspect :(
			return 0;
		}

		if( powered )
		{
			// Simulate power extraction
			double powerRequest = EssentiaMonitor.AE_PER_ESSENTIA * amount;
			double powerReceived = this.energyGrid.extractAEPower( powerRequest, Actionable.SIMULATE, PowerMultiplier.CONFIG );

			// Was enough power extracted?
			if( powerReceived < powerRequest )
			{
				// Not enough power
				return 0;
			}
		}

		// Create the fluid request
		IAEFluidStack fluidRequest = EssentiaConversionHelper.INSTANCE.createAEFluidStackInEssentiaUnits( essentiaGas, amount );

		// Attempt the extraction
		IAEFluidStack fluidReceived = this.fluidMonitor.extractItems( fluidRequest, mode, source );

		// Was any fluid received?
		if( ( fluidReceived == null ) || ( fluidReceived.getStackSize() <= 0 ) )
		{
			// Fluid not found.
			return 0;
		}

		// Convert the received fluid into an aspect stack
		long extractedAmount = EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( fluidReceived.getStackSize() );

		// Extract power if modulating
		if( ( powered ) && ( mode == Actionable.MODULATE ) )
		{
			this.energyGrid.extractAEPower( EssentiaMonitor.AE_PER_ESSENTIA * extractedAmount, Actionable.MODULATE, PowerMultiplier.CONFIG );
		}

		return extractedAmount;

	}

	@Override
	public long getEssentiaAmount( final Aspect aspect )
	{
		if( !this.energyGrid.isNetworkPowered() )
		{
			return 0;
		}

		// Does the cache need to be updated?
		if( this.cacheNeedsUpdate )
		{
			// Update the cache
			this.updateCacheToMatchNetwork();
		}

		// Does the cache have this aspect?
		if( this.cache.containsAspect( aspect ) )
		{
			// Return the amount
			return this.cache.get( aspect ).getStackSize();
		}

		// Aspect not stored
		return 0;
	}

	@Override
	public Collection<IAspectStack> getEssentiaList()
	{
		if( !this.energyGrid.isNetworkPowered() )
		{
			return new ArrayList<IAspectStack>();
		}

		// Does the cache need to be updated?
		if( this.cacheNeedsUpdate )
		{
			this.updateCacheToMatchNetwork();
		}

		// Does the view need to be created?
		if( this.cacheView == null )
		{
			this.cacheView = Collections.unmodifiableCollection( this.cache.getAll() );
		}

		return this.cacheView;
	}

	@Override
	public long injectEssentia( final Aspect aspect, final long amount, final Actionable mode, final BaseActionSource source, final boolean powered )
	{
		// Ensure the aspect is not null, and the amount is > 0 
		if( ( aspect == null ) || ( amount <= 0 ) )
		{
			// Invalid arguments
			return amount;
		}

		// Get the gas form of the essentia
		GaseousEssentia essentiaGas;

		// Ensure there is a gas form of the aspect.
		if( ( essentiaGas = GaseousEssentia.getGasFromAspect( aspect ) ) == null )
		{
			// Unregistered aspect :(
			return amount;
		}

		// Simulate the injection
		long rejectedAmount = this.injectEssentiaSafely( aspect, amount, Actionable.SIMULATE, source, essentiaGas );
		long adjustedAmount = amount - rejectedAmount;

		// Is this a powered injection?
		if( powered )
		{
			// Simulate power extraction
			double powerRequest = EssentiaMonitor.AE_PER_ESSENTIA * adjustedAmount;
			double powerReceived = this.energyGrid.extractAEPower( powerRequest, Actionable.SIMULATE, PowerMultiplier.CONFIG );

			// Was enough power extracted?
			if( powerReceived < powerRequest )
			{
				// Not enough power
				return amount;
			}
		}

		// Modulating?
		if( mode == Actionable.MODULATE )
		{
			// Inject
			rejectedAmount = this.injectEssentiaSafely( aspect, adjustedAmount, Actionable.MODULATE, source, essentiaGas );
			adjustedAmount -= rejectedAmount;

			// Adjust and extract power
			if( powered )
			{
				double powerRequest = EssentiaMonitor.AE_PER_ESSENTIA * adjustedAmount;
				this.energyGrid.extractAEPower( powerRequest, Actionable.MODULATE, PowerMultiplier.CONFIG );
			}
		}

		return rejectedAmount;
	}

	@Override
	public boolean isValid( final Object verificationToken )
	{
		// Has a token been assigned?
		if( this.token != null )
		{
			// Does the token match?
			if( this.token.equals( verificationToken ) )
			{
				// Does the token still exist?
				Object vToken = this.token.get();
				if( vToken != null )
				{
					// Return true
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void onListUpdate()
	{
		// Mark that the cache needs to be updated
		this.cacheNeedsUpdate = true;
	}

	@Override
	public void postChange( final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> fluidChanges, final BaseActionSource actionSource )
	{
		// Ensure the cache is up to date
		if( this.cacheNeedsUpdate )
		{
			// No use updating an out of sync cache
			return;

			/*
			 * Note: this should never happen if there are listeners. As the cache will be updated when a list update occurs.
			 * If any changes occur between a call to onListUpdate() and onUpdateTick(), those changes will be ignored until the cache is updated. 
			 */
		}

		// Ensure there was a change
		if( fluidChanges == null )
		{
			return;
		}

		// True if there are any listeners
		boolean hasListeners = ( this.listeners.size() > 0 );

		// Changes made to the cache.
		List<IAspectStack> aspectChanges = null;

		// Search the changes for essentia gas
		for( IAEFluidStack change : fluidChanges )
		{
			// Is the change an essentia gas?
			if( ( change.getFluid() instanceof GaseousEssentia ) )
			{
				// Get the aspect
				Aspect aspect = ( (GaseousEssentia)change.getFluid() ).getAspect();

				// Calculate the difference
				long changeAmount = EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( change.getStackSize() );

				// Update the cache
				IAspectStack previous = this.cache.postChange( aspect, changeAmount, null );

				// Add to the changes
				if( hasListeners )
				{
					// Create the change list if needed
					if( aspectChanges == null )
					{
						aspectChanges = new ArrayList<IAspectStack>();
					}

					// Was there a previous stack?
					IAspectStack changeStack;
					if( previous != null )
					{
						// Re-use it, as it is no longer associated with anything
						// Plus, it carries the crafting info.
						changeStack = previous;
						previous.setStackSize( changeAmount );
					}
					else
					{
						// Create a new stack
						changeStack = new AspectStack( aspect, changeAmount );
					}

					// Add the change
					aspectChanges.add( changeStack );
				}
			}
		}

		// Notify any listeners
		if( ( aspectChanges != null ) && ( aspectChanges.size() > 0 ) )
		{
			this.notifyListeners( aspectChanges );
		}

	}

	@Override
	public void removeListener( final IMEEssentiaMonitorReceiver listener )
	{
		this.listeners.remove( listener );
	}

	/**
	 * Wraps the specified fluid monitor and energy grid.
	 * 
	 * @param fluidMonitor
	 * Fluid monitor to listen to
	 * @param energyGrid
	 * Energy grid to extract power from
	 * @param validationToken
	 * Used to validate the state of the fluid listener, can not be null
	 */
	public void wrap( final IMEMonitor<IAEFluidStack> fluidMonitor, final IEnergyGrid energyGrid, final Object validationToken )
	{
		// Ensure the token is not null
		if( validationToken != null )
		{
			// Set the token
			this.token = new WeakReference<Object>( validationToken );
		}
		else
		{
			// Throw exception
			throw new NullPointerException( "Validation Token Can Not Be Null" );
		}

		// Set the fluid monitor
		this.fluidMonitor = fluidMonitor;

		// Set the energy grid
		this.energyGrid = energyGrid;

		// Add listener
		this.fluidMonitor.addListener( this, this.token );

		// Mark that the cache needs to be updated
		this.cacheNeedsUpdate = true;
	}

}
