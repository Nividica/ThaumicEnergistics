package thaumicenergistics.integration;

import java.lang.ref.WeakReference;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
		 * What world is the provider in?
		 */
		private final WeakReference<World> epWorld;

		/**
		 * Where in the world is the provider?
		 */
		private final int epX, epY, epZ;

		/**
		 * Reference to the actual provider.
		 */
		private WeakReference<TileEssentiaProvider> epReference;

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
		 * Attempts to get the Essentia Provider
		 * 
		 * @return
		 */
		private TileEssentiaProvider getProvider()
		{
			TileEssentiaProvider provider = null;

			// Is the providers reference still valid?
			if( ( this.epReference != null ) && ( provider = this.epReference.get() ) != null )
			{
				return provider;
			}

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
				// Set the return value
				provider = (TileEssentiaProvider)te;

				// Create a reference
				this.epReference = new WeakReference<TileEssentiaProvider>( provider );
			}

			return provider;
		}

		/**
		 * Gets the aspects stored in the network the provider is attached to.
		 * 
		 * @return
		 */
		private Object[] lmGetAspects()
		{
			// Attempt to get the provider
			TileEssentiaProvider provider;
			if( ( provider = this.getProvider() ) == null )
			{
				// Could not get the provider
				return null;
			}

			// Return the providers active state
			return provider.getOrderedAspectAmounts();
		}

		/**
		 * Returns if the provider is online, or null if the provider is
		 * invalid.
		 * 
		 * @return
		 */
		private Object[] lmGetOnline()
		{
			// Attempt to get the provider
			TileEssentiaProvider provider;
			if( ( provider = this.getProvider() ) == null )
			{
				// Could not get the provider
				return new Object[] { false };
			}

			// Return the providers active state
			return new Object[] { provider.isActive() };
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

			try
			{
				// Determine which method was called.
				switch ( CCMethods.VALUES[method] )
				{
					case getOnline:
						results = this.lmGetOnline();
						break;
					case getAspects:
						results = this.lmGetAspects();
						break;
				}
			}
			catch( Exception e )
			{
				// Silently ignore
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
			return "ThE-EssentiaProvider";
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
			 */
			getAspects;

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
