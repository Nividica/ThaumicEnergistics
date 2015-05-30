package thaumicenergistics.integration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.grid.IMEEssentiaMonitor;
import thaumicenergistics.grid.IMEEssentiaMonitorReceiver;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * Treats an Essentia Provider as a ComputerCraft peripheral.
 * 
 * @author Nividica
 * 
 */
public class EssentiaProviderPeripheral
	implements IPeripheral, IEssentiaProviderWatcher
{
	private static enum CCEvents
	{
			/**
			 * A watched for aspect has changed amounts.
			 */
			AspectUpdate (1),

			/**
			 * The provider has powered on or off.
			 */
			PowerChange (2);

		/**
		 * ID of the event passed into CC
		 */
		public final int eventID;

		private CCEvents( final int ID )
		{
			this.eventID = ID;
		}
	}

	private static enum CCMethods
	{
			/**
			 * Is the provider active and online?
			 */
			isOnline,

			/**
			 * Gets the list of aspects stored in the network.
			 * Stored in pairs of (name,amount)
			 */
			getAspects,

			/**
			 * Gets how much of the specified essentias are in the network.
			 * Args: Param array of aspect names
			 */
			getAmount,

			/**
			 * Register for network changes
			 * Args: aspect names, can be null for all aspects
			 */
			registerAsWatcher,

			/**
			 * Unregister for network changes
			 * Args: aspect names, can be null for all aspects
			 */
			unregisterAsWatcher;

		/**
		 * List of methods.
		 */
		public final static EssentiaProviderPeripheral.CCMethods[] VALUES;

		/**
		 * List of LUA method names.
		 */
		public final static String[] NAMES;

		/**
		 * Static initializer block to ensure VALUES and NAMES maintains order
		 */
		static
		{
			// Cache the values
			VALUES = CCMethods.values();

			// Create the array
			NAMES = new String[VALUES.length];

			// Populate the array
			for( int i = 0; i < NAMES.length; ++i )
			{
				NAMES[i] = VALUES[i].name();
			}
		}
	}

	private class EssentiaWatcher
		implements IMEEssentiaMonitorReceiver
	{

		/**
		 * Computers that are listening for specific updates.
		 * The Boolean value specifies if the computer is watching for all changes(false) or specific aspects(true)
		 */
		private final HashMap<IComputerAccess, Boolean> computerWatchers;

		/**
		 * A mapping of Aspect->Computer(s).
		 */
		private final HashMap<Aspect, Set<IComputerAccess>> aspectWatchers;

		/**
		 * Computers watching for any change.
		 */
		private final HashSet<IComputerAccess> anyWatchers;

		/**
		 * Used to ensure concurrency.
		 */
		private final Object threadLock = new Object();

		/**
		 * True if this is attached to the network monitor.
		 */
		private boolean isReceving = false;

		public EssentiaWatcher()
		{
			// Setup the lists
			this.computerWatchers = new HashMap<IComputerAccess, Boolean>();
			this.aspectWatchers = new HashMap<Aspect, Set<IComputerAccess>>();
			this.anyWatchers = new HashSet<IComputerAccess>();
		}

		/**
		 * Adds a computer to the watch list, or updates an existing computer's aspects.
		 * 
		 * @param computer
		 * Computer to add.
		 * @param aspectsOfInterest
		 * Aspects to watch for. Can be null.
		 * @throws LuaException
		 * @throws InterruptedException
		 */
		public void addComputerWatch( final IComputerAccess computer, final Set<Aspect> aspectsOfInterest ) throws LuaException, InterruptedException
		{
			synchronized( this.threadLock )
			{
				// If the computer is already watching?
				if( this.computerWatchers.containsKey( computer ) )
				{
					// Was it watching for specific aspects?
					if( this.computerWatchers.get( computer ) )
					{
						// Remove the watches
						this.removeComputerWatcher( computer );
					}
				}

				// Is the computer watching specific aspects?
				boolean specificWatches = false;
				if( ( ( aspectsOfInterest != null ) && ( aspectsOfInterest.size() > 0 ) ) )
				{
					// Computer has specific watches
					specificWatches = true;

					for( Aspect watchAspect : aspectsOfInterest )
					{
						// Are there other watchers for this aspect?
						if( !this.aspectWatchers.containsKey( watchAspect ) )
						{
							// New aspect
							this.aspectWatchers.put( watchAspect, new HashSet<IComputerAccess>() );
						}

						// Add to the watch map
						this.aspectWatchers.get( watchAspect ).add( computer );
					}
				}
				else
				{
					// Computer is watching all aspects
					this.anyWatchers.add( computer );
				}

				// Add the computer
				this.computerWatchers.put( computer, specificWatches );

				// Attached to monitor?
				if( !this.isReceving )
				{
					// Get the monitor
					IMEEssentiaMonitor monitor = EssentiaProviderPeripheral.this.getProviderMonitor();
					if( monitor != null )
					{

						// Register
						monitor.addListener( this, null );

						// Mark as receiving
						this.isReceving = true;
					}
				}
			}
		}

		@Override
		public boolean isValid( final Object verificationToken )
		{
			synchronized( this.threadLock )
			{
				// Only valid if there are computer watchers
				this.isReceving = ( this.computerWatchers.size() > 0 );
			}
			return this.isReceving;
		}

		public void onProviderBroken()
		{
			synchronized( this.threadLock )
			{
				// Clear any watchers
				this.anyWatchers.clear();
				this.aspectWatchers.clear();
				this.computerWatchers.clear();
			}
		}

		@Override
		public void postChange( final IMEEssentiaMonitor fromMonitor, final Iterable<AspectStack> changes )
		{
			// Ensure the provider is there and active
			TileEssentiaProvider provider = EssentiaProviderPeripheral.this.getProvider();
			if( provider == null )
			{
				// The provider is no longer valid, clear everything
				this.onProviderBroken();
				return;
			}

			if( !provider.isActive() )
			{
				// Provider is inactive, can't pass events
				return;
			}

			synchronized( this.threadLock )
			{
				Set<IComputerAccess> specificWatchers;
				Set<IComputerAccess> invalidWatchers = null;
				Object[] ccChange = null;

				// For each change
				for( AspectStack change : changes )
				{
					// Are there computers watching for any change?
					if( this.anyWatchers.size() > 0 )
					{
						// Create the argument
						ccChange = new Object[] { change.aspect.getName(), change.stackSize };

						for( IComputerAccess computer : this.anyWatchers )
						{
							try
							{
								// Queue the event
								EssentiaProviderPeripheral.this.sendEvent( computer, CCEvents.AspectUpdate, ccChange );
							}
							catch( Exception e )
							{
								// The watcher is no longer valid
								if( invalidWatchers == null )
								{
									invalidWatchers = new HashSet<IComputerAccess>();
								}
								invalidWatchers.add( computer );
							}
						}
					}

					// Are there computers watching for this specific change?
					if( ( specificWatchers = this.aspectWatchers.get( change.aspect ) ) != null )
					{
						// Has the argument already been created?
						if( ccChange == null )
						{
							// Create the argument
							ccChange = new Object[] { change.aspect.getName(), change.stackSize };
						}

						// Update those watchers
						for( IComputerAccess computer : specificWatchers )
						{
							try
							{
								// Queue the event
								EssentiaProviderPeripheral.this.sendEvent( computer, CCEvents.AspectUpdate, ccChange );
							}
							catch( Exception e )
							{
								// The watcher is no longer valid
								if( invalidWatchers == null )
								{
									invalidWatchers = new HashSet<IComputerAccess>();
								}
								invalidWatchers.add( computer );
							}
						}
					}

				}

				// Remove any invalid watchers
				if( invalidWatchers != null )
				{
					for( IComputerAccess badComputer : invalidWatchers )
					{
						this.removeComputerWatcher( badComputer );
					}
				}
			}
		}

		/**
		 * Removes a computer from the watch list.
		 * 
		 * @param computer
		 * @throws LuaException
		 * @throws InterruptedException
		 */
		public void removeComputerWatcher( final IComputerAccess computer )
		{
			synchronized( this.threadLock )
			{
				if( this.computerWatchers.containsKey( computer ) )
				{
					// Was the computer watching for specific changes?
					if( this.computerWatchers.get( computer ) )
					{
						// Remove the mappings
						for( Set<IComputerAccess> watchSet : this.aspectWatchers.values() )
						{
							watchSet.remove( computer );
						}
					}
					else
					{
						// Remove the mapping
						this.anyWatchers.remove( computer );
					}

					// Remove the computer
					this.computerWatchers.remove( computer );
				}
			}
		}
	}

	/**
	 * Name of the peripheral.
	 */
	private static final String CC_PERIPHERAL_NAME = "EssentiaProvider";

	/**
	 * Name of the event to pass into CC.
	 */
	private static final String CC_EVENT_NAME = "essentia";

	/**
	 * Maps an aspects name to an Aspect.
	 */
	private final static Hashtable<String, Aspect> aspectNameReverseMap = new Hashtable<String, Aspect>();

	/**
	 * What world is the provider in?
	 */
	private final WeakReference<World> epWorld;

	/**
	 * Where in the world is the provider?
	 */
	private final int epX, epY, epZ;

	/**
	 * Watches the network for essentia changes.
	 */
	private final EssentiaWatcher watcher;

	/**
	 * Computers attached to this peripheral.
	 */
	private final HashSet<IComputerAccess> attachedComputers = new HashSet<IComputerAccess>();

	/**
	 * True if the peripheral is listening to the provider.
	 */
	private boolean isRegisteredAsBlockListener = false;

	public EssentiaProviderPeripheral( final World world, final int x, final int y, final int z )
	{
		// Mark the world
		this.epWorld = new WeakReference<World>( world );

		// Mark the position
		this.epX = x;
		this.epY = y;
		this.epZ = z;

		// Create the watcher
		this.watcher = new EssentiaWatcher();

		// Setup the block listener
		this.getProvider();
	}

	/**
	 * Returns how much of an aspect is in the system from its tag.
	 * 
	 * @param args
	 * @return
	 */
	@SuppressWarnings("null")
	private Object[] ccGetAmount( final Object[] args ) throws LuaException, InterruptedException
	{
		// Get the provider
		TileEssentiaProvider provider = this.getProvider();

		// Get the list of aspects
		List<Aspect> aspects = this.getAspectsFromArguments( args );

		// Create the results
		Object[] results = new Object[aspects.size()];

		// Is the provider active?
		boolean providerActive = ( provider != null ? provider.isActive() : false );

		// For aspect, get the amount
		for( int index = 0; index < results.length; ++index )
		{
			if( providerActive )
			{
				// Add the amount to the results
				results[index] = provider.getAspectAmountInNetwork( aspects.get( index ) );
			}
			else
			{
				// Provider is offline
				results[index] = 0;
			}
		}

		return results;
	}

	/**
	 * Gets the aspects stored in the network the provider is attached to.
	 * 
	 * @return
	 */
	private Object[] ccGetAspects() throws LuaException, InterruptedException
	{

		// Attempt to get the monitor
		IMEEssentiaMonitor monitor;
		if( ( monitor = this.getProviderMonitor() ) == null )
		{
			return null;
		}

		// Get the list of aspects in the network
		Collection<AspectStack> essentiaList = monitor.getEssentiaList();

		// Is there any essentia stored?
		if( essentiaList.size() == 0 )
		{
			// No essentia stored
			return null;
		}

		// Create the array
		Object[] ccList = new Object[essentiaList.size() * 2];

		int index = 0;
		for( AspectStack stack : essentiaList )
		{
			// Set the name
			ccList[index] = stack.getAspectName();

			// Set the amount
			ccList[index + 1] = stack.stackSize;

			// Inc the index
			index += 2;
		}

		return ccList;

	}

	/**
	 * Returns if the provider is online, or null if the provider is
	 * invalid.
	 * 
	 * @return
	 */
	private Object[] ccGetOnline() throws LuaException, InterruptedException
	{
		// Return the providers active state
		return new Object[] { this.isProviderAvailable() };
	}

	/**
	 * Registers a computer with the network watcher.
	 * 
	 * @param computer
	 * @param args
	 * @throws LuaException
	 * @throws InterruptedException
	 */
	private boolean ccRegisterAsWatcher( final IComputerAccess computer, final Object[] args ) throws LuaException, InterruptedException
	{
		// No provider
		if( !this.isProviderAvailable() )
		{
			return false;
		}

		// Is the computer watching for specific aspects?
		if( ( args != null ) && ( args.length > 0 ) )
		{
			// Create the aspect set
			HashSet<Aspect> aspects = new HashSet<Aspect>( this.getAspectsFromArguments( args ) );

			// Register for those aspects
			this.watcher.addComputerWatch( computer, aspects );
		}
		else
		{
			// Register for all
			this.watcher.addComputerWatch( computer, null );
		}

		return true;
	}

	/**
	 * Gets an aspect based on its friendly name.
	 * 
	 * @param name
	 * @return
	 */
	private Aspect getAspectFromName( final String name )
	{
		// Get the name, in lowercase
		String aspectName = name.toLowerCase();

		// Does the map contain this entry?
		if( EssentiaProviderPeripheral.aspectNameReverseMap.contains( aspectName ) )
		{
			// Get the aspect
			return EssentiaProviderPeripheral.aspectNameReverseMap.get( aspectName );
		}

		// Search all aspects
		for( Aspect searchAspect : Aspect.aspects.values() )
		{
			// Do the names match?
			if( searchAspect.getName().equalsIgnoreCase( aspectName ) )
			{
				// Update the map
				EssentiaProviderPeripheral.aspectNameReverseMap.put( aspectName, searchAspect );

				// Break the search
				return searchAspect;
			}
		}

		return null;
	}

	/**
	 * Gets a list of aspects based on the arguments passed in from the computer.
	 * 
	 * @param args
	 * @return
	 * @throws LuaException
	 * @throws InterruptedException
	 */
	private List<Aspect> getAspectsFromArguments( final Object[] args ) throws LuaException, InterruptedException
	{
		// Ensure there are arguments
		if( ( args == null ) || ( args.length == 0 ) )
		{
			throw new LuaException( "Expected argument: <string>AspectName" );
		}

		List<Aspect> results = new ArrayList<Aspect>();

		// For each argument, attempt to get the aspect				
		for( Object arg : args )
		{
			// Ensure the argument is a string
			if( arg instanceof String )
			{
				// The aspect to look up
				Aspect aspect = this.getAspectFromName( (String)arg );

				// Was an aspect found?
				if( aspect != null )
				{
					// Add to the results
					results.add( aspect );
				}
				else
				{
					throw new LuaException( String.format( "Invalid aspect name '%s'", arg ) );
				}
			}
			else
			{
				throw new LuaException( String.format( "Invalid argument '%s'", arg ) );
			}
		}

		return results;
	}

	/**
	 * Returns true if a provider is found, and it is powered up
	 * 
	 * @return
	 */
	private boolean isProviderAvailable()
	{
		TileEssentiaProvider provider = this.getProvider();
		return( provider != null ? provider.isActive() : false );
	}

	@Override
	public void attach( final IComputerAccess computer )
	{
		// Add to the attach list
		this.attachedComputers.add( computer );
	}

	@Override
	public Object[] callMethod( final IComputerAccess computer, final ILuaContext context, final int method, final Object[] arguments )
					throws LuaException, InterruptedException
	{
		Object[] results = null;

		// Determine which method was called.
		switch ( CCMethods.VALUES[method] )
		{
			case isOnline:
				results = this.ccGetOnline();
				break;

			case getAspects:
				results = this.ccGetAspects();
				break;

			case getAmount:
				results = this.ccGetAmount( arguments );
				break;

			case registerAsWatcher:
				results = new Object[] { this.ccRegisterAsWatcher( computer, arguments ) };
				break;

			case unregisterAsWatcher:
				this.watcher.removeComputerWatcher( computer );
				results = new Object[] { true };
				break;
		}

		return results;
	}

	@Override
	public void detach( final IComputerAccess computer )
	{
		// Remove from the list
		this.attachedComputers.remove( computer );

		// Remove the computer from the watcher
		this.watcher.removeComputerWatcher( computer );
	}

	@Override
	public boolean equals( final IPeripheral other )
	{
		// TODO: Make this more robust.
		return this.equals( other );
	}

	@Override
	public String[] getMethodNames()
	{
		return CCMethods.NAMES;
	}

	@Override
	public String getType()
	{
		return EssentiaProviderPeripheral.CC_PERIPHERAL_NAME;
	}

	@Override
	public void onProviderBroken()
	{
		// No longer registered
		this.isRegisteredAsBlockListener = false;

		// Inform any attached computers that the link is going down
		for( IComputerAccess computer : this.attachedComputers )
		{
			this.sendEvent( computer, CCEvents.PowerChange, false );
		}
		this.watcher.onProviderBroken();

	}

	@Override
	public void onProviderPowerChange( final boolean isOnline )
	{
		// Inform computers that the power status changed
		for( IComputerAccess computer : this.attachedComputers )
		{
			this.sendEvent( computer, CCEvents.PowerChange, isOnline );
		}
	}

	/**
	 * Attempts to get the Essentia Provider
	 * 
	 * @return
	 */
	TileEssentiaProvider getProvider()
	{
		// Attempt to get the world
		World world;
		if( ( this.epWorld == null ) || ( world = this.epWorld.get() ) == null )
		{
			return null;
		}

		// Get the tile entity at the recorded location
		TileEntity te = world.getTileEntity( this.epX, this.epY, this.epZ );

		// Is the entity a provider?
		if( te instanceof TileEssentiaProvider )
		{
			// Is the peripheral registered as a block watcher?
			if( !this.isRegisteredAsBlockListener )
			{
				// Register
				( (TileEssentiaProvider)te ).registerBlockWatcher( this );
				this.isRegisteredAsBlockListener = true;
			}

			// Return the provider
			return (TileEssentiaProvider)te;
		}

		return null;

	}

	/**
	 * Gets the essentia monitor from the provider.
	 * 
	 * @return
	 * @throws LuaException
	 * @throws InterruptedException
	 */
	IMEEssentiaMonitor getProviderMonitor() throws LuaException, InterruptedException
	{
		// Get the provider
		TileEssentiaProvider provider = this.getProvider();

		// Is the provider active?
		if( !provider.isActive() )
		{
			// Inactive
			return null;
		}

		// Attempt to get the essentia monitor
		try
		{
			return (IMEEssentiaMonitor)provider.getActionableNode().getGrid().getCache( IMEEssentiaMonitor.class );
		}
		catch( NullPointerException npe )
		{
			// Could not get the monitor
			return null;
		}
	}

	/**
	 * Sends an event to the specified computer
	 * 
	 * @param computer
	 * @param event
	 * @param additionalData
	 */
	void sendEvent( final IComputerAccess computer, final CCEvents event, final Object ... additionalData )
	{
		// Create the arguments
		Object[] args = new Object[( additionalData == null ? 1 : 1 + additionalData.length )];

		// Set the ID
		args[0] = event.eventID;

		// Copy the rest of the data
		if( additionalData != null )
		{
			System.arraycopy( additionalData, 0, args, 1, additionalData.length );
		}

		computer.queueEvent( EssentiaProviderPeripheral.CC_EVENT_NAME, args );
	}

}