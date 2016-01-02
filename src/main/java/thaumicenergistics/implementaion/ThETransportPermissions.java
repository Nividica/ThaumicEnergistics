package thaumicenergistics.implementaion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.blocks.BlockJarItem;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumicenergistics.api.IThEEssentiaContainerPermission;
import thaumicenergistics.api.IThETransportPermissions;
import thaumicenergistics.common.utils.ThELog;

class ThETransportPermissions
	implements IThETransportPermissions
{
	/**
	 * Collection of ContainerInfo indexed by damage value.
	 * 
	 * @author Nividica
	 * 
	 */
	private class ContainerCollection
	{
		/**
		 * DamageValue -> Info map
		 */
		private HashMap<Integer, ContainerInfo> containers = new HashMap<Integer, ContainerInfo>();

		/**
		 * Constructor adding the specified info the the collection
		 * 
		 * @param capacity
		 * @param canHoldPartialAmount
		 * @param damageValue
		 */
		public ContainerCollection( final int capacity, final boolean canHoldPartialAmount, final int damageValue )
		{
			this.addContainer( capacity, canHoldPartialAmount, damageValue );
		}

		/**
		 * Adds the specified info to the collection
		 * 
		 * @param capacity
		 * @param canHoldPartialAmount
		 * @param damageValue
		 * @return
		 */
		public ContainerCollection addContainer( final int capacity, final boolean canHoldPartialAmount, final int damageValue )
		{
			// Create the container info
			ContainerInfo info = new ContainerInfo( capacity, canHoldPartialAmount );

			// Add to the map
			this.containers.put( damageValue, info );

			return this;
		}

		/**
		 * Gets the info about the item using the specified metadata.
		 * 
		 * @param damageValue
		 * @return {@link ContainerInfo} if a match is found, null otherwise.
		 */
		public ContainerInfo getInfo( final int damageValue )
		{
			return this.containers.get( damageValue );
		}
	}

	/**
	 * Basic information about a thaumcraft container.
	 * 
	 * @author Nividica
	 * 
	 */
	private class ContainerInfo
		implements IThEEssentiaContainerPermission
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
	private final Set<Class<? extends IAspectContainer>> tileExtractWhiteList = new HashSet<Class<? extends IAspectContainer>>();

	/**
	 * Holds a list of tiles that we are allowed to inject into.
	 */
	private final Set<Class<? extends IAspectContainer>> tileInjectWhiteList = new HashSet<Class<? extends IAspectContainer>>();

	/**
	 * Holds the capacities for each registered tile.
	 */
	private final HashMap<Class<? extends IAspectContainer>, Integer> tileCapacities = new HashMap<Class<? extends IAspectContainer>, Integer>();

	@Override
	public <T extends TileEntity & IAspectContainer> boolean addAspectContainerTileToBothPermissions( final Class<T> tileClass, final int capacity )
	{
		// Add to both
		return( this.addAspectContainerTileToInjectPermissions( tileClass, capacity ) | this.addAspectContainerTileToExtractPermissions( tileClass,
			capacity ) );
	}

	@Override
	public <T extends TileEntity & IAspectContainer> boolean addAspectContainerTileToExtractPermissions( final Class<T> tileClass, final int capacity )
	{
		// Ensure we have a tile
		if( tileClass != null )
		{
			// Set capacity
			this.tileCapacities.put( tileClass, capacity );

			// Is it not already registered?
			if( this.tileExtractWhiteList.add( tileClass ) )
			{
				// Log the addition
				ThELog.info( "Added %s with capacity %d to extraction whitelist.", tileClass.toString(), capacity );
			}

			return true;
		}

		return false;
	}

	@Override
	public <T extends TileEntity & IAspectContainer> boolean addAspectContainerTileToInjectPermissions( final Class<T> tileClass, final int capacity )
	{
		// Ensure we have a tile
		if( tileClass != null )
		{
			// Set capacity
			this.tileCapacities.put( tileClass, capacity );

			// Is it not already registered?
			if( this.tileInjectWhiteList.add( tileClass ) )
			{
				// Log the addition
				ThELog.info( "Added %s with capacity %d to injection whitelist.", tileClass.toString(), capacity );
			}

			return true;
		}

		return false;
	}

	@Override
	public void addEssentiaContainerItemToTransportPermissions( final Class<? extends IEssentiaContainerItem> itemClass, final int capacity,
																final int damageValue, final boolean canHoldPartialAmount )
	{
		// Do we have an item?
		if( itemClass != null )
		{
			// Is it already registered?
			if( this.itemWhitelist.containsKey( itemClass ) )
			{
				// Add to the existing registry
				this.itemWhitelist.get( itemClass ).addContainer( capacity, canHoldPartialAmount, damageValue );
			}
			else
			{
				// Create a new registry
				this.itemWhitelist.put( itemClass, new ContainerCollection( capacity, canHoldPartialAmount, damageValue ) );
			}

			// Log the addition
			ThELog.info( "Adding %s[%d] to item whitelist.", itemClass.toString(), damageValue );
		}
	}

	@Override
	public void addEssentiaContainerItemToTransportPermissions( final ItemStack containerItem, final int capacity, final boolean canHoldPartialAmount )
	{
		// Ensure the item is a valid container
		if( ( containerItem == null ) || !( containerItem.getItem() instanceof IEssentiaContainerItem ) )
		{
			return;
		}

		try
		{
			this.addEssentiaContainerItemToTransportPermissions( (Class<? extends IEssentiaContainerItem>)containerItem.getItem().getClass(),
				capacity, containerItem.getItemDamage(), canHoldPartialAmount );
		}
		catch( Throwable t )
		{
		}

	}

	@Override
	public boolean canExtractFromAspectContainerTile( final IAspectContainer container )
	{
		// Return if it is whitelisted or not
		return( this.tileExtractWhiteList.contains( container.getClass() ) );
	}

	@Override
	public boolean canInjectToAspectContainerTile( final IAspectContainer container )
	{
		// Return if it is whitelisted or not
		return( this.tileInjectWhiteList.contains( container.getClass() ) );
	}

	@Override
	public int getAspectContainerTileCapacity( final IAspectContainer container )
	{
		// Is the capacity registered?
		if( this.tileCapacities.containsKey( container.getClass() ) )
		{
			return this.tileCapacities.get( container.getClass() );
		}

		return -1;
	}

	@Override
	public IThEEssentiaContainerPermission getEssentiaContainerInfo( final Class<? extends Item> itemClass, final int damageValue )
	{
		// Is the item registered?
		if( this.itemWhitelist.containsKey( itemClass ) )
		{
			// Return the info
			return this.itemWhitelist.get( itemClass ).getInfo( damageValue );
		}

		// Special check for empty jars
		if( itemClass == BlockJarItem.class )
		{
			// Return the info for filled jars
			return this.itemWhitelist.get( ItemJarFilled.class ).getInfo( damageValue );
		}

		// Not registered return null
		return null;
	}

}
