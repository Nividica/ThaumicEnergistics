package thaumicenergistics.implementaion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.blocks.BlockJarItem;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumicenergistics.api.IEssentiaContainerPermission;
import thaumicenergistics.api.ITransportPermissions;
import thaumicenergistics.util.TELog;

class TETransportPermissions
	implements ITransportPermissions
{
	/**
	 * Collection of ContainerInfo indexed by metadata.
	 * 
	 * @author Nividica
	 * 
	 */
	private class ContainerCollection
	{
		/**
		 * Metadata -> Info map
		 */
		private HashMap<Integer, ContainerInfo> containers = new HashMap<Integer, ContainerInfo>();

		/**
		 * Constructor adding the specified info the the collection
		 * 
		 * @param capacity
		 * @param canHoldPartialAmount
		 * @param metadata
		 */
		public ContainerCollection( final int capacity, final boolean canHoldPartialAmount, final int metadata )
		{
			this.addContainer( capacity, canHoldPartialAmount, metadata );
		}

		/**
		 * Adds the specified info to the collection
		 * 
		 * @param capacity
		 * @param canHoldPartialAmount
		 * @param metadata
		 * @return
		 */
		public ContainerCollection addContainer( final int capacity, final boolean canHoldPartialAmount, final int metadata )
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
	private class ContainerInfo
		implements IEssentiaContainerPermission
	{
		private int capacity;
		private boolean canHoldPartialAmount;

		public ContainerInfo( final int capacity, final boolean canHoldPartialAmount )
		{
			this.capacity = capacity;
			this.canHoldPartialAmount = canHoldPartialAmount;
		}

		@Override
		public boolean canHoldPartialAmount()
		{
			return this.canHoldPartialAmount;
		}

		@Override
		public int maximumCapacity()
		{
			return this.capacity;
		}
	}

	/**
	 * List of items allowed to work with.
	 * Indexed by item class.
	 */
	private final HashMap<Class<? extends IEssentiaContainerItem>, ContainerCollection> itemWhitelist = new HashMap<Class<? extends IEssentiaContainerItem>, ContainerCollection>();

	/**
	 * Holds a list of tiles that we are allowed to extract from.
	 */
	private final List<Class<? extends TileEntity>> tileExtractWhiteList = new ArrayList<Class<? extends TileEntity>>();

	/**
	 * Holds a list of tiles that we are allowed to inject into.
	 */
	private final List<Class<? extends TileEntity>> tileInjectWhiteList = new ArrayList<Class<? extends TileEntity>>();

	@Override
	public boolean addAspectContainerTileToExtractPermissions( final Class<? extends TileEntity> tileClass )
	{
		// Ensure we have a tile
		if( tileClass != null )
		{
			// Ensure it is a container
			if( IAspectContainer.class.isAssignableFrom( tileClass ) )
			{
				// Is it already registered?
				if( !this.tileExtractWhiteList.contains( tileClass ) )
				{
					// Add to the list
					this.tileExtractWhiteList.add( tileClass );

					// Log the addition
					TELog.info( "Added %s to extraction whitelist.", tileClass.toString() );
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean addAspectContainerTileToInjectPermissions( final Class<? extends TileEntity> tileClass )
	{
		// Ensure we have a tile
		if( tileClass != null )
		{
			// Ensure it is a container
			if( IAspectContainer.class.isAssignableFrom( tileClass ) )
			{
				// Is it already registered?
				if( !this.tileInjectWhiteList.contains( tileClass ) )
				{
					// Add to the list
					this.tileInjectWhiteList.add( tileClass );

					// Log the addition
					TELog.info( "Added %s to injection whitelist.", tileClass.toString() );
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public void addEssentiaContainerItemToTransportPermissions( final Class<? extends IEssentiaContainerItem> itemClass, final int capacity,
																final int metadata, final boolean canHoldPartialAmount )
	{
		// Do we have an item?
		if( itemClass != null )
		{
			// Is it already registered?
			if( this.itemWhitelist.containsKey( itemClass ) )
			{
				// Add to the existing registry
				this.itemWhitelist.get( itemClass ).addContainer( capacity, canHoldPartialAmount, metadata );
			}
			else
			{
				// Create a new registry
				this.itemWhitelist.put( itemClass, new ContainerCollection( capacity, canHoldPartialAmount, metadata ) );
			}

			// Log the addition
			TELog.info( "Adding %s[%d] to item whitelist.", itemClass.toString(), metadata );
		}
	}

	@Override
	public boolean canExtractFromAspectContainerTile( final IAspectContainer container )
	{
		// Ensure the container is a tile.
		if( !( container instanceof TileEntity ) )
		{
			return false;
		}

		// Loop over blacklist
		for( Class<? extends TileEntity> whiteClass : this.tileExtractWhiteList )
		{
			// Is the container an instance of this whitelisted class?
			if( whiteClass.isInstance( container ) )
			{
				// Return that we can extract
				return true;
			}
		}

		// Return that we can not extract
		return false;
	}

	@Override
	public boolean canInjectToAspectContainerTile( final IAspectContainer container )
	{
		// Ensure the container is a tile.
		if( !( container instanceof TileEntity ) )
		{
			return false;
		}

		// Loop over blacklist
		for( Class<? extends TileEntity> whiteClass : this.tileInjectWhiteList )
		{
			// Is the container an instance of this whitelisted class?
			if( whiteClass.isInstance( container ) )
			{
				// Return that we can inject
				return true;
			}
		}

		// Return that we can not inject
		return false;
	}

	@Override
	public IEssentiaContainerPermission getEssentiaContainerInfo( final Class<? extends Item> itemClass, final int metadata )
	{
		// Is the item registered?
		if( this.itemWhitelist.containsKey( itemClass ) )
		{
			// Return the info
			return this.itemWhitelist.get( itemClass ).getInfo( metadata );
		}

		// Special check for empty jars
		if( itemClass == BlockJarItem.class )
		{
			// Return the info for filled jars
			return this.itemWhitelist.get( ItemJarFilled.class ).getInfo( metadata );
		}

		// Not registered return null
		return null;
	}

}
