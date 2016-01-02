package thaumicenergistics.integration.tc;

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
import thaumcraft.common.items.ItemCrystalEssence;
import thaumcraft.common.items.ItemEssence;
import thaumcraft.common.items.ItemResource;
import thaumcraft.common.items.ItemWispEssence;
import thaumicenergistics.api.IThEEssentiaContainerPermission;
import thaumicenergistics.api.IThETransportPermissions;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.items.IRestrictedEssentiaContainerItem;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.common.items.ItemBlockEssentiaVibrationChamber;
import thaumicenergistics.common.storage.AspectStack;
import thaumicenergistics.common.tiles.abstraction.TileEVCBase;

/**
 * Helper class for working with Thaumcraft item essentia containers.
 * 
 * @author Nividica
 * 
 */
public final class EssentiaItemContainerHelper
{
	public enum AspectItemType
	{
			/**
			 * Unknown item, or non-whitelisted container
			 */
			Invalid,

			/**
			 * Whitelisted container.
			 */
			EssentiaContainer,

			/**
			 * Warded jar label.
			 */
			JarLabel,

			/**
			 * Crystallized essentia.
			 */
			CrystallizedEssentia,

			/**
			 * Wispy Essence.
			 */
			WispEssence;
	}

	/**
	 * Singleton
	 */
	public static final EssentiaItemContainerHelper INSTANCE = new EssentiaItemContainerHelper();

	/**
	 * NBT key used to get the aspect of a jar's label.
	 */
	private static final String JAR_LABEL_NBT_KEY = "AspectFilter";

	/**
	 * Standard Thaumcraft jar capacity.
	 */
	private static final int JAR_CAPACITY = 64;

	/**
	 * Standard Thaumcraft phial capacity.
	 */
	private static final int PHIAL_CAPACITY = 8;

	/**
	 * Cache of the item permissions.
	 */
	private IThETransportPermissions perms;

	/**
	 * Private constructor
	 */
	private EssentiaItemContainerHelper()
	{
		this.perms = ThEApi.instance().transportPermissions();
	}

	/**
	 * Gets the first aspect contained in the IEssentiaContainerItem. Ignores the whitelist.
	 * 
	 * @param itemStack
	 * @return
	 */
	private Aspect getAspectFromAnyContainerItem( final ItemStack itemStack )
	{
		// Is it a container?
		if( itemStack.getItem() instanceof IEssentiaContainerItem )
		{
			// Get the list of aspects from the container
			AspectList aspectList = ( (IEssentiaContainerItem)itemStack.getItem() ).getAspects( itemStack );

			// Is there are list?
			if( aspectList != null )
			{
				// Return the aspect contained
				return aspectList.getAspects()[0];
			}
		}

		return null;
	}

	/**
	 * Returns an itemstack with a single empty jar
	 * 
	 * @param metadata
	 * @return
	 */
	public ItemStack createEmptyJar( final int metadata )
	{
		// Create and return the jar
		return new ItemStack( ConfigBlocks.blockJar, 1, metadata );
	}

	/**
	 * Returns an itemstack with a single empty phial
	 * 
	 * @return
	 */
	public ItemStack createEmptyPhial()
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
	public ItemStack createFilledJar( final Aspect aspect, int amount, final int metadata, final boolean withLabel )
	{

		ItemStack jar;

		// If there is no aspect, or it would be empty with no label
		if( ( aspect == null ) || ( ( amount <= 0 ) && !withLabel ) )
		{
			// Create an empty jar
			jar = this.createEmptyJar( metadata );
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
				this.setJarLabel( jar, aspect );
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
	public ItemStack createFilledPhial( final Aspect aspect )
	{
		if( aspect == null )
		{
			return this.createEmptyPhial();
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
	public boolean doesJarHaveLabel( final ItemStack jar )
	{
		// If the jar's label aspect is not null, there is a label
		return( this.getJarLabelAspect( jar ) != null );
	}

	/**
	 * Extracts essentia from the specified container using the
	 * aspect and amount specified by the AspectStack.
	 * 
	 * @param container
	 * @param aspectStack
	 * @return
	 */
	public ImmutablePair<Integer, ItemStack> extractFromContainer( final ItemStack container, final IAspectStack aspectStack )
	{
		// Ensure it is a container
		if( this.getItemType( container ) != AspectItemType.EssentiaContainer )
		{
			return null;
		}

		// Get the item in the stack
		Item containerItem = container.getItem();

		// Get the info about the container
		IThEEssentiaContainerPermission info = this.getContainerInfo( containerItem, container.getItemDamage() );

		// Ensure we got info
		if( info == null )
		{
			return null;
		}

		// Get the aspect in the container
		AspectList aspectList = ( (IEssentiaContainerItem)containerItem ).getAspects( container );

		// Does the container have the aspect we are wanting to extract?
		if( aspectStack.getAspect() != aspectList.getAspects()[0] )
		{
			return null;
		}

		// Get how much is stored in the container
		int containerAmountStored = aspectList.getAmount( aspectStack.getAspect() );

		// Is there any stored in the container?
		if( containerAmountStored <= 0 )
		{
			return null;
		}

		// Copy the amount we want to drain
		int amountToDrain = (int)aspectStack.getStackSize();

		// Can this container do partial fills?
		if( !info.canHoldPartialAmount() )
		{
			// Ensure the request is for the containers capacity
			if( amountToDrain < info.maximumCapacity() )
			{
				// Can not partially drain this container
				return null;
			}
			else if( amountToDrain > info.maximumCapacity() )
			{
				// Drain is to much, reduce to preset capacity
				amountToDrain = info.maximumCapacity();
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
		if( ( amountToDrain == containerAmountStored ) || ( amountToDrain == info.maximumCapacity() ) )
		{
			// Is this a phial?
			if( containerItem instanceof ItemEssence )
			{
				// Create an empty phial for the output
				resultStack = this.createEmptyPhial();
			}
			else if( containerItem instanceof ItemJarFilled )
			{
				// Was the jar labeled?
				if( this.doesJarHaveLabel( container ) )
				{
					// Create an empty labeled jar
					resultStack = this.createFilledJar( aspectStack.getAspect(), 0, container.getItemDamage(), true );
				}
				else
				{
					// Create an empty jar for the output
					resultStack = this.createEmptyJar( container.getItemDamage() );
				}
			}

		}

		// Have we already set the result stack?
		if( resultStack == null )
		{
			// Make a copy of the container
			resultStack = container.copy();

			// Reduce the list amount
			aspectList.remove( aspectStack.getAspect(), amountToDrain );

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
	public ImmutablePair<Integer, ItemStack> extractFromContainer( final ItemStack container, final int drainAmount_EU )
	{
		// Ensure the container is valid
		if( container == null )
		{
			return null;
		}

		// Create an aspect stack based on whats in the container
		IAspectStack stack = new AspectStack( this.getAspectInContainer( container ), drainAmount_EU );

		// Extract
		return this.extractFromContainer( container, stack );
	}

	/**
	 * Gets the aspect on the label.
	 * Can return null.
	 * 
	 * @param label
	 * @return
	 */
	public Aspect getAspectFromLabel( final ItemStack label )
	{
		// Ensure the item is a label
		if( this.getItemType( label ) != AspectItemType.JarLabel )
		{
			return null;
		}

		// Get the item
		ItemResource rLabel = (ItemResource)label.getItem();

		// Get the aspects
		AspectList aspects = rLabel.getAspects( label );

		// Ensure there is an aspect to get
		if( ( aspects == null ) || ( aspects.size() == 0 ) )
		{
			return null;
		}

		// Return the aspect
		return aspects.getAspects()[0];
	}

	/**
	 * Returns the aspect in the container, or null if empty or invalid container.
	 * 
	 * @param itemStack
	 * @return
	 */
	public Aspect getAspectInContainer( final ItemStack itemStack )
	{
		// Ensure the item is a valid container
		if( this.getItemType( itemStack ) == AspectItemType.EssentiaContainer )
		{
			return this.getAspectFromAnyContainerItem( itemStack );
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
	public IAspectStack getAspectStackFromContainer( final ItemStack container )
	{
		// Get the aspect of the essentia in the container
		Aspect aspect = this.getAspectInContainer( container );

		// Did we get an aspect?
		if( aspect == null )
		{
			return null;
		}

		// get the amount stored in the container
		int stored = this.getContainerStoredAmount( container );

		return new AspectStack( aspect, stored );
	}

	/**
	 * Gets the maximum amount the specified container can hold
	 * 
	 * @param container
	 * @return
	 */
	public int getContainerCapacity( final ItemStack container )
	{
		// Is the container not null?
		if( container != null )
		{
			// Get the item from the stack
			Item containerItem = container.getItem();

			// Get the info about the container
			IThEEssentiaContainerPermission info = this.getContainerInfo( containerItem, container.getItemDamage() );

			// Did we get any info?
			if( info != null )
			{
				return info.maximumCapacity();
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
	public IThEEssentiaContainerPermission getContainerInfo( final Item item, final int metadata )
	{
		// Is the item not null?
		if( item != null )
		{
			return this.perms.getEssentiaContainerInfo( item.getClass(), metadata );
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
	public IThEEssentiaContainerPermission getContainerInfo( final ItemStack itemStack )
	{
		// Is the itemstack not null?
		if( itemStack != null )
		{
			return this.perms.getEssentiaContainerInfo( itemStack.getItem().getClass(), itemStack.getItemDamage() );
		}

		return null;
	}

	/**
	 * Gets the amount stored in the container.
	 * 
	 * @param container
	 * @return
	 */
	public int getContainerStoredAmount( final ItemStack container )
	{
		// Is the container valid?
		if( ( container != null ) && ( container.getItem() instanceof IEssentiaContainerItem ) )
		{
			// Is the container whitelisted?
			if( this.isContainerWhitelisted( container ) )
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
	 * Returns the aspect of of the specified item. This method supports jars, jar labels and crystallized essentia.
	 * This should not be used to validate a container.
	 * 
	 * @param itemStack
	 * @return Aspect if container has one, null otherwise.
	 */
	public Aspect getFilterAspectFromItem( final ItemStack itemStack )
	{
		// Get the type
		AspectItemType type = this.getItemType( itemStack );

		switch ( type )
		{
		case JarLabel:
			return this.getAspectFromLabel( itemStack );

		case CrystallizedEssentia:
		case EssentiaContainer:
		case WispEssence:
			return this.getAspectFromAnyContainerItem( itemStack );

		case Invalid:
			break;

		}

		return null;
	}

	/**
	 * Gets the aspect type of the item.
	 * 
	 * @param itemStack
	 * @return
	 */
	public AspectItemType getItemType( final ItemStack itemStack )
	{
		// Ensure the stack is not null
		if( itemStack != null )
		{
			// Get the item
			Item item = itemStack.getItem();

			// Ensure the item is not null
			if( item != null )
			{

				// Valid container?
				if( this.isContainerWhitelisted( itemStack ) )
				{
					return AspectItemType.EssentiaContainer;
				}
				// IEssentiaContainerItem?
				else if( item instanceof IEssentiaContainerItem )
				{

					// Crystallized Essentia?
					if( item instanceof ItemCrystalEssence )
					{
						return AspectItemType.CrystallizedEssentia;
					}

					// Wisp essence?
					if( item instanceof ItemWispEssence )
					{
						return AspectItemType.WispEssence;
					}
				}

				// Label?
				if( ( item instanceof ItemResource ) && ( itemStack.getItemDamage() == 13 ) )
				{
					return AspectItemType.JarLabel;
				}

			}

		}

		return AspectItemType.Invalid;
	}

	/**
	 * Gets the aspect represented by a jar's label.
	 * 
	 * @param jar
	 * @return
	 */
	public Aspect getJarLabelAspect( final ItemStack jar )
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
	 * Creates a copy of the container filled with the additional gas.
	 * 
	 * @param container
	 * @param aspectStack
	 * @return The amount that was inject, and the new container.
	 */
	public ImmutablePair<Integer, ItemStack> injectIntoContainer( final ItemStack container, final IAspectStack aspectStack )
	{
		// Is the item an essentia container?
		if( this.getItemType( container ) != AspectItemType.EssentiaContainer )
		{
			return null;
		}

		// Get the container item
		Item containerItem = container.getItem();
		if( containerItem == null )
		{
			// Invalid item
			return null;
		}

		// Is the container a labeled jar?
		if( containerItem instanceof ItemJarFilled )
		{
			if( this.doesJarHaveLabel( container ) )
			{
				// Does the label match the aspect we are going to fill
				// with?
				if( aspectStack.getAspect() != this.getJarLabelAspect( container ) )
				{
					// Aspect does not match the jar's label
					return null;
				}
			}
		}
		// Is the container restricted?
		else if( containerItem instanceof IRestrictedEssentiaContainerItem )
		{
			// Ensure the container accepts this essentia
			if( !( ( (IRestrictedEssentiaContainerItem)containerItem ).acceptsAspect( aspectStack.getAspect() ) ) )
			{
				// Not acceptable
				return null;
			}
		}

		// Get the info about the container
		IThEEssentiaContainerPermission info = this.getContainerInfo( containerItem, container.getItemDamage() );

		// Ensure we got the info
		if( info == null )
		{
			return null;
		}

		// Get how much essentia is in the container
		int containerAmountStored = this.getContainerStoredAmount( container );

		// Calculate how much remaining storage is in the container
		int remainaingStorage = info.maximumCapacity() - containerAmountStored;

		// Is there any room left in the container?
		if( remainaingStorage <= 0 )
		{
			return null;
		}

		// Set the amount to fill
		int amountToFill = (int)aspectStack.getStackSize();

		// Can this container do partial fills?
		if( !info.canHoldPartialAmount() )
		{
			// Ensure the request is for the containers capacity
			if( amountToFill < info.maximumCapacity() )
			{
				// Can not partially fill this container
				return null;
			}
			else if( amountToFill > info.maximumCapacity() )
			{
				// Drain is to much, reduce to preset capacity
				amountToFill = info.maximumCapacity();
			}
		}
		else
		{
			// Would the resulting amount be more than the container can hold?
			if( amountToFill > ( containerAmountStored + info.maximumCapacity() ) )
			{
				// Adjust the amount
				amountToFill = info.maximumCapacity() - containerAmountStored;
			}
		}

		ItemStack resultStack = null;

		// Is this a phial?
		if( containerItem instanceof ItemEssence )
		{
			// Create a new phial
			resultStack = this.createFilledPhial( aspectStack.getAspect() );
		}
		// Is it an empty jar?
		else if( containerItem instanceof BlockJarItem )
		{
			// Create a fillable jar
			resultStack = this.createFilledJar( aspectStack.getAspect(), amountToFill, container.getItemDamage(), false );
		}

		// Have we already set the result?
		if( resultStack == null )
		{
			// Get the currently stored list.
			AspectList aspectList = ( (IEssentiaContainerItem)containerItem ).getAspects( container );

			// Is the containers list null?
			if( aspectList == null )
			{
				// Create a new aspect list
				aspectList = new AspectList();
			}
			// Does the list have anything in it?
			else if( aspectList.size() > 0 )
			{
				// Does the stored aspect match the aspect to inject?
				if( aspectStack.getAspect() != aspectList.getAspects()[0] )
				{
					// Don't mix aspects
					return null;
				}
			}

			// Increase the list amount
			aspectList.add( aspectStack.getAspect(), amountToFill );

			// Make a copy of the request
			resultStack = container.copy();

			// Set the stored amount
			( (IEssentiaContainerItem)resultStack.getItem() ).setAspects( resultStack, aspectList );
		}

		return new ImmutablePair<Integer, ItemStack>( amountToFill, resultStack );
	}

	/**
	 * Checks if the specified itemstack represents an empty container.
	 * 
	 * @param container
	 * @return
	 */
	public boolean isContainerEmpty( final ItemStack container )
	{
		return( this.getContainerStoredAmount( container ) == 0 );
	}

	/**
	 * Checks if the specified itemstack represents a non-empty container.
	 * 
	 * @param container
	 * @return
	 */
	public boolean isContainerFilled( final ItemStack container )
	{

		return( this.getContainerStoredAmount( container ) > 0 );
	}

	/**
	 * Quick check to see if the item is whitelisted.
	 * 
	 * @param item
	 * @param metadata
	 * @return
	 */
	public boolean isContainerWhitelisted( final Item container, final int metadata )
	{
		return this.getContainerInfo( container, metadata ) != null;
	}

	/**
	 * Quick check to see if the itemstack is whitelisted.
	 * 
	 * @param item
	 * @return
	 */
	public boolean isContainerWhitelisted( final ItemStack container )
	{
		return this.getContainerInfo( container ) != null;
	}

	/**
	 * Setup the standard white list
	 */
	public void registerDefaultContainers()
	{
		// Phials
		this.perms.addEssentiaContainerItemToTransportPermissions( ItemEssence.class, PHIAL_CAPACITY, 0, false );
		this.perms.addEssentiaContainerItemToTransportPermissions( ItemEssence.class, PHIAL_CAPACITY, 1, false );

		// Filled jar
		this.perms.addEssentiaContainerItemToTransportPermissions( ItemJarFilled.class, JAR_CAPACITY, 0, true );

		// Void jar
		this.perms.addEssentiaContainerItemToTransportPermissions( ItemJarFilled.class, JAR_CAPACITY, 3, true );

		// Vibration chamber
		this.perms.addEssentiaContainerItemToTransportPermissions( ItemBlockEssentiaVibrationChamber.class, TileEVCBase.MAX_ESSENTIA_STORED, 0, true );
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
	public ItemStack setJarLabel( final ItemStack jar, final Aspect OverrideAspect )
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
				labelAspect = this.getFilterAspectFromItem( jar );
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

	/**
	 * Sets a labels aspect
	 * 
	 * @param label
	 * @param aspect
	 */
	public void setLabelAspect( final ItemStack label, final Aspect aspect )
	{
		// Ensure the item is a label
		if( this.getItemType( label ) != AspectItemType.JarLabel )
		{
			return;
		}

		// Get the item
		ItemResource rLabel = (ItemResource)label.getItem();

		// Create the aspects
		AspectList aspects = new AspectList();
		aspects.add( aspect, 1 );

		// Set the aspect
		rLabel.setAspects( label, aspects );

	}
}
