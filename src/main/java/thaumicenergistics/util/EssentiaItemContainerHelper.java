package thaumicenergistics.util;

import java.util.HashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.blocks.BlockJarItem;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemEssence;
import thaumicenergistics.aspect.AspectStack;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Helper class for working with Thaumcraft item essentia containers.
 * 
 * @author Nividica
 * 
 */
public final class EssentiaItemContainerHelper
{
	/**
	 * Collection of ContainerInfo indexed by metadata.
	 * 
	 * @author Nividica
	 * 
	 */
	private static class ContainerItemInfoCollection
	{
		/**
		 * Metadata -> Info map
		 */
		private HashMap<Integer, ContainerInfo> containers = new HashMap<Integer, EssentiaItemContainerHelper.ContainerInfo>();

		/**
		 * Constructor adding the specified info the the collection
		 * 
		 * @param capacity
		 * @param canHoldPartialAmount
		 * @param metadata
		 */
		public ContainerItemInfoCollection( int capacity, boolean canHoldPartialAmount, int metadata )
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
		public ContainerItemInfoCollection addContainerInfo( int capacity, boolean canHoldPartialAmount, int metadata )
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
		public ContainerInfo getInfo( int metadata )
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
	public static class ContainerInfo
	{
		/**
		 * The maximum amount this container can hold
		 */
		public int capacity;

		/**
		 * Can the container be partialy filled?
		 */
		public boolean canHoldPartialAmount;

		public ContainerInfo( int capacity, boolean canHoldPartialAmount )
		{
			this.capacity = capacity;
			this.canHoldPartialAmount = canHoldPartialAmount;
		}
	}

	/**
	 * Standard Thaumcraft jar capacity
	 */
	private static final int JAR_CAPACITY = 64;

	/**
	 * Standard Thaumcraft phial capacity
	 */
	private static final int PHIAL_CAPACITY = 8;

	/**
	 * NBT key used to get the aspect of a jar's label
	 */
	private static final String JAR_LABEL_NBT_KEY = "AspectFilter";

	/**
	 * Holds a list of items we are allowed to interact with, and their
	 * capacity.
	 */
	private static final HashMap<Class<? extends IEssentiaContainerItem>, ContainerItemInfoCollection> itemWhitelist = new HashMap<Class<? extends IEssentiaContainerItem>, ContainerItemInfoCollection>();

	/**
	 * Adds an item to the whitelist.
	 * 
	 * @param item
	 * @param capacity
	 * @param canHoldPartialAmount
	 * @param metadata
	 */
	private static void addItemToWhitelist( Class<? extends IEssentiaContainerItem> item, int capacity, boolean canHoldPartialAmount, int metadata )
	{
		// Do we have an item?
		if( item != null )
		{
			// Is it already registered?
			if( EssentiaItemContainerHelper.itemWhitelist.containsKey( item ) )
			{
				// Add to the existing registry
				EssentiaItemContainerHelper.itemWhitelist.get( item ).addContainerInfo( capacity, canHoldPartialAmount, metadata );
			}
			else
			{
				// Create a new registry
				EssentiaItemContainerHelper.itemWhitelist.put( item, new ContainerItemInfoCollection( capacity, canHoldPartialAmount, metadata ) );
			}

			// Log the addition
			FMLCommonHandler.instance().getFMLLogger().info( "Adding " + item.toString() + "[" + metadata + "] to item whitelist." );
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
	private static ContainerInfo getContainerInfo( Class<? extends Item> container, int metadata )
	{
		// Is the item registered?
		if( EssentiaItemContainerHelper.itemWhitelist.containsKey( container ) )
		{
			// Return the info
			return EssentiaItemContainerHelper.itemWhitelist.get( container ).getInfo( metadata );
		}

		// Special check for empty jars
		if( container == BlockJarItem.class )
		{
			// Return the info for filled jars
			return EssentiaItemContainerHelper.itemWhitelist.get( ItemJarFilled.class ).getInfo( metadata );
		}

		// Not registered return null
		return null;
	}

	/**
	 * Adds an item to the whitelist that must match the specified metadata
	 * value.
	 * 
	 * @param item
	 * @param capacity
	 * @param metadata
	 * @param canHoldPartialAmount
	 */
	public static void addItemToWhitelist( Class<? extends IEssentiaContainerItem> item, int capacity, int metadata, boolean canHoldPartialAmount )
	{
		EssentiaItemContainerHelper.addItemToWhitelist( item, capacity, canHoldPartialAmount, metadata );
	}

	/**
	 * Returns an itemstack with a single empty jar
	 * 
	 * @param metadata
	 * @return
	 */
	public static ItemStack createEmptyJar( int metadata )
	{
		// Create and return the jar
		return new ItemStack( ConfigBlocks.blockJar, 1, metadata );
	}

	/**
	 * Returns an itemstack with a single empty phial
	 * 
	 * @return
	 */
	public static ItemStack createEmptyPhial()
	{
		// Create and return the phial
		return new ItemStack( ConfigItems.itemEssence, 1, 0 );
	}

	/**
	 * Returns an itemstack with a single filled jar.
	 * 
	 * @param aspect
	 * @param amount
	 * @param metadata
	 * @param withLabel
	 * @return
	 */
	public static ItemStack createFilledJar( Aspect aspect, int amount, int metadata, boolean withLabel )
	{

		ItemStack jar;

		// If there is no aspect, or it would be empty with no label
		if( ( aspect == null ) || ( ( amount <= 0 ) && !withLabel ) )
		{
			// Create an empty jar
			jar = EssentiaItemContainerHelper.createEmptyJar( metadata );
		}
		else
		{
			// Create an empty fillable jar
			jar = new ItemStack( ConfigItems.itemJarFilled, 1, metadata );

			// Are we putting any essentia in the jar?
			if( amount > 0 )
			{
				// Is there too much to fit in a jar?
				if( amount > EssentiaItemContainerHelper.JAR_CAPACITY )
				{
					// Reduce to the max a jar can hold
					amount = EssentiaItemContainerHelper.JAR_CAPACITY;
				}

				// Set the aspect and amount
				( (ItemJarFilled)jar.getItem() ).setAspects( jar, new AspectList().add( aspect, amount ) );
			}

			// Are we putting a label on it?
			if( withLabel )
			{
				EssentiaItemContainerHelper.setJarLabel( jar, aspect );
			}
		}

		// Return the jar
		return jar;
	}

	/**
	 * Returns an itemstack with a single filled phial.
	 * 
	 * @param aspect
	 * @return
	 */
	public static ItemStack createFilledPhial( Aspect aspect )
	{
		if( aspect == null )
		{
			return EssentiaItemContainerHelper.createEmptyPhial();
		}

		// Create the phial
		ItemStack phial = new ItemStack( ConfigItems.itemEssence, 1, 1 );

		// Set it's aspect
		( (ItemEssence)phial.getItem() ).setAspects( phial, new AspectList().add( aspect, EssentiaItemContainerHelper.PHIAL_CAPACITY ) );

		return phial;
	}

	/**
	 * Returns true if the jar has a label, false if it does not.
	 * 
	 * @param jar
	 * @return
	 */
	public static boolean doesJarHaveLabel( ItemStack jar )
	{
		// If the jar's label aspect is not null, there is a label
		return( EssentiaItemContainerHelper.getJarLabelAspect( jar ) != null );
	}

	/**
	 * Extracts essentia from the specified container using the
	 * aspect and amount specified by the AspectStack.
	 * 
	 * @param container
	 * @param aspectStack
	 * @return
	 */
	public static ImmutablePair<Integer, ItemStack> extractFromContainer( ItemStack container, AspectStack aspectStack )
	{
		// Ensure we have a valid container
		if( container == null )
		{
			return null;
		}

		// Ensure it is a container
		if( !EssentiaItemContainerHelper.isContainer( container ) )
		{
			return null;
		}

		// Get the item in the stack
		Item containerItem = container.getItem();

		// Get the info about the container
		ContainerInfo info = EssentiaItemContainerHelper.getContainerInfo( containerItem, container.getItemDamage() );

		// Ensure we got info
		if( info == null )
		{
			return null;
		}

		// Get the aspect in the container
		AspectList aspectList = ( (IEssentiaContainerItem)containerItem ).getAspects( container );

		// Does the container have the aspect we are wanting to extract?
		if( aspectStack.aspect != aspectList.getAspects()[0] )
		{
			return null;
		}

		// Get how much is stored in the container
		int containerAmountStored = aspectList.getAmount( aspectStack.aspect );

		// Is there any stored in the container?
		if( containerAmountStored <= 0 )
		{
			return null;
		}

		// Copy the amount we want to drain
		int amountToDrain = (int)aspectStack.amount;

		// Can this container do partial fills?
		if( !info.canHoldPartialAmount )
		{
			// Ensure the request is for the containers capacity
			if( amountToDrain < info.capacity )
			{
				// Can not partially drain this container
				return null;
			}
			else if( amountToDrain > info.capacity )
			{
				// Drain is to much, reduce to preset capacity
				amountToDrain = info.capacity;
			}
		}
		else
		{
			// Is the amount we want more than is in the container?
			if( amountToDrain > containerAmountStored )
			{
				// Adjust the amount to how much is in the container
				amountToDrain = containerAmountStored;
			}
		}

		ItemStack resultStack = null;

		// Did we completely drain the container?
		if( ( amountToDrain == containerAmountStored ) || ( amountToDrain == info.capacity ) )
		{
			// Is this a phial?
			if( containerItem instanceof ItemEssence )
			{
				// Create an empty phial for the output
				resultStack = EssentiaItemContainerHelper.createEmptyPhial();
			}
			else if( containerItem instanceof ItemJarFilled )
			{
				// Was the jar labeled?
				if( EssentiaItemContainerHelper.doesJarHaveLabel( container ) )
				{
					// Create an empty labeled jar
					resultStack = EssentiaItemContainerHelper.createFilledJar( aspectStack.aspect, 0, container.getItemDamage(), true );
				}
				else
				{
					// Create an empty jar for the output
					resultStack = EssentiaItemContainerHelper.createEmptyJar( container.getItemDamage() );
				}
			}

		}

		// Have we already set the result stack?
		if( resultStack == null )
		{
			// Make a copy of the container
			resultStack = container.copy();

			// Reduce the list amount
			aspectList.reduce( aspectStack.aspect, amountToDrain );

			// Set the stored amount
			( (IEssentiaContainerItem)resultStack.getItem() ).setAspects( resultStack, aspectList );
		}

		return new ImmutablePair<Integer, ItemStack>( amountToDrain, resultStack );

	}

	/**
	 * Extracts essentia from the specified container using the
	 * aspect in the container, and the amount(in essentia units).
	 * 
	 * @param container
	 * @param drainAmount_EU
	 * @return
	 */
	public static ImmutablePair<Integer, ItemStack> extractFromContainer( ItemStack container, int drainAmount_EU )
	{
		// Ensure the container is valid
		if( container == null )
		{
			return null;
		}

		// Create an aspect stack based on whats in the container
		AspectStack stack = new AspectStack( EssentiaItemContainerHelper.getAspectInContainer( container ), drainAmount_EU );

		// Extract
		return EssentiaItemContainerHelper.extractFromContainer( container, stack );
	}

	/**
	 * Returns the aspect of whatever essentia is in the container.
	 * 
	 * @param container
	 * @return Aspect if container has one, null otherwise.
	 */
	public static Aspect getAspectInContainer( ItemStack container )
	{
		// Is the container valid?
		if( ( container != null ) && ( container.getItem() instanceof IEssentiaContainerItem ) )
		{
			// Is the container whitelisted?
			if( EssentiaItemContainerHelper.isContainerWhitelisted( container ) )
			{
				// Get the list of aspects from the container
				AspectList aspectList = ( (IEssentiaContainerItem)container.getItem() ).getAspects( container );

				// Is there are list?
				if( aspectList != null )
				{
					// Return the aspect contained
					return aspectList.getAspects()[0];
				}
			}
		}

		return null;
	}

	/**
	 * Gets an {@link AspectStack} representing the essentia and
	 * amount in the container.
	 * 
	 * @param container
	 * @return AspectStack can read container, null otherwise.
	 */
	public static AspectStack getAspectStackFromContainer( ItemStack container )
	{
		// Get the aspect of the essentia in the container
		Aspect aspect = EssentiaItemContainerHelper.getAspectInContainer( container );

		// Did we get an aspect?
		if( aspect == null )
		{
			return null;
		}

		// get the amount stored in the container
		int stored = EssentiaItemContainerHelper.getContainerStoredAmount( container );

		return new AspectStack( aspect, stored );
	}

	/**
	 * Gets the maximum amount the specified container can hold
	 * 
	 * @param container
	 * @return
	 */
	public static int getContainerCapacity( ItemStack container )
	{
		// Is the container not null?
		if( container != null )
		{
			// Get the item from the stack
			Item containerItem = container.getItem();

			// Get the info about the container
			ContainerInfo info = EssentiaItemContainerHelper.getContainerInfo( containerItem, container.getItemDamage() );

			// Did we get any info?
			if( info != null )
			{
				return info.capacity;
			}
		}

		return 0;
	}

	/**
	 * Gets the information about the container as it was registered to the
	 * whitelist.
	 * 
	 * @param item
	 * @param metadata
	 * @return
	 */
	public static ContainerInfo getContainerInfo( Item item, int metadata )
	{
		// Is the item not null?
		if( item != null )
		{
			return EssentiaItemContainerHelper.getContainerInfo( item.getClass(), metadata );
		}

		return null;
	}

	/**
	 * Gets the information about the container as it was registered to the
	 * whitelist.
	 * 
	 * @param itemstack
	 * @return
	 */
	public static ContainerInfo getContainerInfo( ItemStack itemStack )
	{
		// Is the itemstack not null?
		if( itemStack != null )
		{
			return EssentiaItemContainerHelper.getContainerInfo( itemStack.getItem().getClass(), itemStack.getItemDamage() );
		}

		return null;
	}

	/**
	 * Gets the amount stored in the container.
	 * 
	 * @param container
	 * @return
	 */
	public static int getContainerStoredAmount( ItemStack container )
	{
		// Is the container valid?
		if( ( container != null ) && ( container.getItem() instanceof IEssentiaContainerItem ) )
		{
			// Is the container whitelisted?
			if( EssentiaItemContainerHelper.isContainerWhitelisted( container ) )
			{
				// Get the aspect list from the container
				AspectList storedList = ( (IEssentiaContainerItem)container.getItem() ).getAspects( container );

				// Is there a list?
				if( storedList == null )
				{
					return 0;
				}

				// Return the amount
				return storedList.getAmount( storedList.getAspects()[0] );
			}
		}
		return 0;
	}

	/**
	 * Gets the aspect represented by a jar's label.
	 * 
	 * @param jar
	 * @return
	 */
	public static Aspect getJarLabelAspect( ItemStack jar )
	{
		Aspect labelAspect = null;

		// Ensure it is a jar
		if( jar.getItem() instanceof ItemJarFilled )
		{
			// Does the jar have a label?
			if( ( jar.hasTagCompound() ) && ( jar.stackTagCompound.hasKey( EssentiaItemContainerHelper.JAR_LABEL_NBT_KEY ) ) )
			{
				// Get the aspect tag from the NBT
				String tag = jar.stackTagCompound.getString( EssentiaItemContainerHelper.JAR_LABEL_NBT_KEY );

				// Set the label aspect
				labelAspect = Aspect.getAspect( tag );
			}
		}

		return labelAspect;

	}

	/**
	 * Injects the aspect and amount as specified by the AspectStack
	 * into the container.
	 * 
	 * @param container
	 * @param aspectStack
	 * @return
	 */
	public static ImmutablePair<Integer, ItemStack> injectIntoContainer( ItemStack container, AspectStack aspectStack )
	{
		// Is there an item?
		if( container == null )
		{
			return null;
		}

		// Is the item an essentia container?
		if( !EssentiaItemContainerHelper.isContainer( container ) )
		{
			return null;
		}

		// Is the container a labeled jar?
		if( EssentiaItemContainerHelper.doesJarHaveLabel( container ) )
		{
			// Does the label match the aspect we are going to fill
			// with?
			if( aspectStack.aspect != EssentiaItemContainerHelper.getJarLabelAspect( container ) )
			{
				// Aspect does not match the jar's label
				return null;
			}
		}

		// Get the container item
		Item containerItem = container.getItem();

		// Get the info about the container
		ContainerInfo info = EssentiaItemContainerHelper.getContainerInfo( containerItem, container.getItemDamage() );

		// Ensure we got the info
		if( info == null )
		{
			return null;
		}
		// Get how much essentia is in the container
		int containerAmountStored = EssentiaItemContainerHelper.getContainerStoredAmount( container );

		// Calculate how much remaining storage is in the container
		int remainaingStorage = info.capacity - containerAmountStored;

		// Is there any room left in the container?
		if( remainaingStorage <= 0 )
		{
			return null;
		}

		// Set the amount to fill
		int amountToFill = (int)aspectStack.amount;

		// Can this container do partial fills?
		if( !info.canHoldPartialAmount )
		{
			// Ensure the request is for the containers capacity
			if( amountToFill < info.capacity )
			{
				// Can not partially fill this container
				return null;
			}
			else if( amountToFill > info.capacity )
			{
				// Drain is to much, reduce to preset capacity
				amountToFill = info.capacity;
			}
		}
		else
		{
			// Would the resulting amount be more than the container can hold?
			if( amountToFill > ( containerAmountStored + info.capacity ) )
			{
				// Adjust the amount
				amountToFill = info.capacity - containerAmountStored;
			}
		}

		ItemStack resultStack = null;

		// Is this a phial?
		if( containerItem instanceof ItemEssence )
		{
			// Create a new phial
			resultStack = EssentiaItemContainerHelper.createFilledPhial( aspectStack.aspect );
		}
		// Is it an empty jar?
		else if( containerItem instanceof BlockJarItem )
		{
			// Create a fillable jar
			resultStack = EssentiaItemContainerHelper.createFilledJar( aspectStack.aspect, amountToFill, container.getItemDamage(), false );
		}

		// Have we already set the result?
		if( resultStack == null )
		{
			// Make a copy of the request
			resultStack = container.copy();

			AspectList aspectList = ( (IEssentiaContainerItem)containerItem ).getAspects( container );

			// Did not we get a list?
			if( aspectList == null )
			{
				// Create a new aspect list
				aspectList = new AspectList();
			}

			// Increase the list amount
			aspectList.add( aspectStack.aspect, amountToFill );

			// Set the stored amount
			( (IEssentiaContainerItem)resultStack.getItem() ).setAspects( resultStack, aspectList );
		}

		return new ImmutablePair<Integer, ItemStack>( amountToFill, resultStack );
	}

	/**
	 * Checks if the specified itemstack represents a container we can use
	 * 
	 * @param container
	 * @return
	 */
	public static boolean isContainer( ItemStack container )
	{
		// Is the container not null?
		if( container != null )
		{
			// Return if the container whitelisted or not.
			return EssentiaItemContainerHelper.isContainerWhitelisted( container );
		}

		return false;
	}

	/**
	 * Checks if the specified itemstack represents an empty container.
	 * 
	 * @param container
	 * @return
	 */
	public static boolean isContainerEmpty( ItemStack container )
	{
		return( EssentiaItemContainerHelper.getContainerStoredAmount( container ) == 0 );
	}

	/**
	 * Checks if the specified itemstack represents a non-empty container.
	 * 
	 * @param container
	 * @return
	 */
	public static boolean isContainerFilled( ItemStack container )
	{

		return( EssentiaItemContainerHelper.getContainerStoredAmount( container ) > 0 );
	}

	/**
	 * Quick check to see if the item is whitelisted.
	 * 
	 * @param item
	 * @param metadata
	 * @return
	 */
	public static boolean isContainerWhitelisted( Item container, int metadata )
	{
		return EssentiaItemContainerHelper.getContainerInfo( container, metadata ) != null;
	}

	/**
	 * Quick check to see if the itemstack is whitelisted.
	 * 
	 * @param item
	 * @return
	 */
	public static boolean isContainerWhitelisted( ItemStack container )
	{
		return EssentiaItemContainerHelper.getContainerInfo( container ) != null;
	}

	/**
	 * Setup the standard white list
	 */
	public static void registerThaumcraftContainers()
	{
		// Phials
		EssentiaItemContainerHelper.addItemToWhitelist( ItemEssence.class, PHIAL_CAPACITY, 0, false );
		EssentiaItemContainerHelper.addItemToWhitelist( ItemEssence.class, PHIAL_CAPACITY, 1, false );

		// Filled jar
		EssentiaItemContainerHelper.addItemToWhitelist( ItemJarFilled.class, JAR_CAPACITY, 0, true );

		// Void jar
		EssentiaItemContainerHelper.addItemToWhitelist( ItemJarFilled.class, JAR_CAPACITY, 3, true );
	}

	/**
	 * Sets the specified jar's label.
	 * 
	 * @param jar
	 * @param OverrideAspect
	 * Override existing label and use this aspect. Can be null to use existing
	 * label.
	 * @return The specified itemstack.
	 */
	public static ItemStack setJarLabel( ItemStack jar, Aspect OverrideAspect )
	{
		// Ensure it is a jar
		if( jar.getItem() instanceof ItemJarFilled )
		{
			Aspect labelAspect;

			// Are we overriding the aspect?
			if( OverrideAspect != null )
			{
				labelAspect = OverrideAspect;
			}
			else
			{
				labelAspect = EssentiaItemContainerHelper.getAspectInContainer( jar );
			}

			// Ensure we have an aspect to set
			if( labelAspect != null )
			{
				// Does the jar have a compound tag?
				if( !jar.hasTagCompound() )
				{
					// Create a new compound tag
					jar.stackTagCompound = new NBTTagCompound();
				}

				// Set the label
				jar.stackTagCompound.setString( "AspectFilter", labelAspect.getTag() );
			}

		}

		return jar;
	}
}
