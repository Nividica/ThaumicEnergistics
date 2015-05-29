package thaumicenergistics.integration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.IMEEssentiaMonitor;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

/**
 * Computer Craft integration
 * 
 * @author Nividica
 * 
 */
public class ModuleCC
{
	/**
	 * Treats an Essentia Provider as a ComputerCraft peripheral.
	 * 
	 * @author Nividica
	 * 
	 */
	public static class EssentiaProviderPeripheral
		implements IPeripheral
	{
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

		public EssentiaProviderPeripheral( final World world, final int x, final int y, final int z )
		{
			// Mark the world
			this.epWorld = new WeakReference<World>( world );

			// Mark the position
			this.epX = x;
			this.epY = y;
			this.epZ = z;
		}

		/**
		 * Returns how much of an aspect is in the system from its tag.
		 * 
		 * @param args
		 * @return
		 */
		private Object[] ccGetAmount( final Object[] args ) throws LuaException, InterruptedException
		{
			// Ensure there are arguments
			if( ( args == null ) || ( args.length == 0 ) )
			{
				throw new LuaException( "Expected argument: <string>AspectName" );
			}

			// Get the provider
			TileEssentiaProvider provider = this.getProvider();

			// Is the provider active?
			if( !provider.isActive() )
			{
				// Inactive
				return null;
			}

			List<Object> results = new ArrayList<Object>();

			// For each argument, attempt to get the aspect
			for( Object arg : args )
			{
				Long amount = null;

				// Ensure the argument is a string
				if( arg instanceof String )
				{
					// The aspect to look up
					Aspect aspect = null;

					// Get the name, in lowercase
					String aspectName = ( (String)arg ).toLowerCase();

					// Does the map contain this entry?
					if( EssentiaProviderPeripheral.aspectNameReverseMap.contains( aspectName ) )
					{
						// Get the aspect
						aspect = EssentiaProviderPeripheral.aspectNameReverseMap.get( aspectName );
					}
					else
					{
						// Search all aspects
						for( Aspect searchAspect : Aspect.aspects.values() )
						{
							// Do the names match?
							if( searchAspect.getName().equalsIgnoreCase( aspectName ) )
							{
								// Found it
								aspect = searchAspect;

								// Update the map
								EssentiaProviderPeripheral.aspectNameReverseMap.put( aspectName, searchAspect );

								// Break the search
								break;
							}
						}
					}

					// Was an aspect found?
					if( aspect != null )
					{
						// Get the amount
						amount = provider.getAspectAmountInNetwork( aspect );
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

				// Add the amount to the results
				results.add( amount );
			}

			return results.toArray();
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
			// Get the provider
			TileEssentiaProvider provider = this.getProvider();

			// Return the providers active state
			return new Object[] { provider.isActive() };
		}

		/**
		 * Attempts to get the Essentia Provider
		 * 
		 * @return
		 */
		private TileEssentiaProvider getProvider() throws LuaException, InterruptedException
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
				// Return the provider
				return (TileEssentiaProvider)te;
			}

			throw new LuaException( "EssentiaProvider not found" );

		}

		private IMEEssentiaMonitor getProviderMonitor() throws LuaException, InterruptedException
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
				return provider.getActionableNode().getGrid().getCache( IMEEssentiaMonitor.class );
			}
			catch( NullPointerException npe )
			{
				// Could not get the monitor
				return null;
			}
		}

		@Override
		public void attach( final IComputerAccess computer )
		{
			// Ignored
		}

		@Override
		public Object[] callMethod( final IComputerAccess computer, final ILuaContext context, final int method, final Object[] arguments )
						throws LuaException, InterruptedException
		{
			Object[] results = null;

			// Determine which method was called.
			switch ( CCMethods.VALUES[method] )
			{
				case getOnline:
					results = this.ccGetOnline();
					break;
				case getAspects:
					results = this.ccGetAspects();
					break;
				case getAmount:
					results = this.ccGetAmount( arguments );
					break;
			}

			return results;
		}

		@Override
		public void detach( final IComputerAccess computer )
		{
			// Ignored
		}

		@Override
		public boolean equals( final IPeripheral other )
		{
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
			return "EssentiaProvider";
		}

	}

	public static class ThEPeripherals
		implements IPeripheralProvider
	{

		@Override
		public IPeripheral getPeripheral( final World world, final int x, final int y, final int z, final int side )
		{
			try
			{
				// Get the tile entity at that position
				TileEntity te = world.getTileEntity( x, y, z );

				// Is the entity an Essentia Provider?
				if( te instanceof TileEssentiaProvider )
				{
					// Create the peripheral
					return new EssentiaProviderPeripheral( world, x, y, z );
				}
			}
			catch( Exception e )
			{
				// Silently ignore
			}

			return null;
		}

	}

	enum CCMethods
	{
			/**
			 * Is the peripheral active and online?
			 */
			getOnline,

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
			 * TODO: Register for network changes
			 * Args: aspect names, can be null for all aspects
			 */
			register,

			/**
			 * TODO: Unregister for network changes
			 * Args: aspect names, can be null for all aspects
			 */
			unregister,

			/**
			 * TODO: Provide a help object.
			 */
			help;

		/**
		 * List of methods.
		 */
		public final static CCMethods[] VALUES;

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

	/**
	 * Integrates with CC
	 */
	public ModuleCC()
	{
		// Create and register the handler
		ComputerCraftAPI.registerPeripheralProvider( new ThEPeripherals() );
	}
}
