package thaumicenergistics.api.registry;

import java.util.HashMap;
import net.minecraft.item.Item;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.blocks.BlockJarItem;
import thaumcraft.common.blocks.ItemJarFilled;
import cpw.mods.fml.common.FMLCommonHandler;

public class TEPermissionsItem
{

	/**
	 * Collection of ContainerInfo indexed by metadata.
	 * 
	 * @author Nividica
	 * 
	 */
	private class ContainerItemInfoCollection
	{
		/**
		 * Metadata -> Info map
		 */
		private HashMap<Integer, ContainerInfo> containers = new HashMap<Integer, TEPermissionsItem.ContainerInfo>();

		/**
		 * Constructor adding the specified info the the collection
		 * 
		 * @param capacity
		 * @param canHoldPartialAmount
		 * @param metadata
		 */
		public ContainerItemInfoCollection( final int capacity, final boolean canHoldPartialAmount, final int metadata )
		{
			this.addContainerInfo( capacity, canHoldPartialAmount, metadata );
		}

		/**
		 * Adds the specified info to the collection
		 * 
		 * @param capacity
		 * @param canHoldPartialAmount
		 * @param metadata
		 * @return
		 */
		public ContainerItemInfoCollection addContainerInfo( final int capacity, final boolean canHoldPartialAmount, final int metadata )
		{
			// Create the container info
			ContainerInfo info = new ContainerInfo( capacity, canHoldPartialAmount );

			// Add to the map
			this.containers.put( metadata, info );

			return this;
		}

		/**
		 * Gets the info about the item using the specified metadata.
		 * 
		 * @param metadata
		 * @return {@link ContainerInfo} if a match is found, null otherwise.
		 */
		public ContainerInfo getInfo( final int metadata )
		{
			return this.containers.get( metadata );
		}
	}

	/**
	 * Basic information about a thaumcraft container.
	 * 
	 * @author Nividica
	 * 
	 */
	public class ContainerInfo
	{
		/**
		 * The maximum amount this container can hold
		 */
		public int capacity;

		/**
		 * Can the container be partialy filled?
		 */
		public boolean canHoldPartialAmount;

		public ContainerInfo( final int capacity, final boolean canHoldPartialAmount )
		{
			this.capacity = capacity;
			this.canHoldPartialAmount = canHoldPartialAmount;
		}
	}

	/**
	 * Holds a list of items we are allowed to interact with, and their
	 * capacity.
	 */
	private final HashMap<Class<? extends IEssentiaContainerItem>, ContainerItemInfoCollection> itemWhitelist = new HashMap<Class<? extends IEssentiaContainerItem>, ContainerItemInfoCollection>();

	/**
	 * Adds an item to the whitelist that must match the specified metadata
	 * value.
	 * 
	 * @param itemClass
	 * @param capacity
	 * @param metadata
	 * @param canHoldPartialAmount
	 */
	public void addItemToWhitelist( final Class<? extends IEssentiaContainerItem> itemClass, final int capacity, final int metadata,
									final boolean canHoldPartialAmount )
	{
		// Do we have an item?
		if( itemClass != null )
		{
			// Is it already registered?
			if( this.itemWhitelist.containsKey( itemClass ) )
			{
				// Add to the existing registry
				this.itemWhitelist.get( itemClass ).addContainerInfo( capacity, canHoldPartialAmount, metadata );
			}
			else
			{
				// Create a new registry
				this.itemWhitelist.put( itemClass, new ContainerItemInfoCollection( capacity, canHoldPartialAmount, metadata ) );
			}

			// Log the addition
			FMLCommonHandler.instance().getFMLLogger().info( "Adding " + itemClass.toString() + "[" + metadata + "] to item whitelist." );
		}
	}

	/**
	 * Gets the information about the container as it was registered to the
	 * whitelist.
	 * 
	 * @param container
	 * @param metadata
	 * @return Info if was registered, null otherwise.
	 */
	public ContainerInfo getContainerInfo( final Class<? extends Item> container, final int metadata )
	{
		// Is the item registered?
		if( this.itemWhitelist.containsKey( container ) )
		{
			// Return the info
			return this.itemWhitelist.get( container ).getInfo( metadata );
		}

		// Special check for empty jars
		if( container == BlockJarItem.class )
		{
			// Return the info for filled jars
			return this.itemWhitelist.get( ItemJarFilled.class ).getInfo( metadata );
		}

		// Not registered return null
		return null;
	}
}
