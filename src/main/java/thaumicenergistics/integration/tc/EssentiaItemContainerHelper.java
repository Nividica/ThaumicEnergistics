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
import thaumcraft.common.items.ItemEssence;
import thaumcraft.common.items.ItemResource;
import thaumicenergistics.api.IThEEssentiaContainerPermission;
import thaumicenergistics.api.IThETransportPermissions;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.aspect.AspectStack;

/**
 * Helper class for working with Thaumcraft item essentia containers.
 * 
 * @author Nividica
 * 
 */
public final class EssentiaItemContainerHelper
{
	/**
	 * Singleton
	 */
	public static final EssentiaItemContainerHelper instance = new EssentiaItemContainerHelper();

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
	public ImmutablePair<Integer, ItemStack> extractFromContainer( final ItemStack container, final AspectStack aspectStack )
	{
		// Ensure we have a valid container
		if( container == null )
		{
			return null;
		}

		// Ensure it is a container
		if( !this.isContainer( container ) )
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
					resultStack = this.createFilledJar( aspectStack.aspect, 0, container.getItemDamage(), true );
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
	public ImmutablePair<Integer, ItemStack> extractFromContainer( final ItemStack container, final int drainAmount_EU )
	{
		// Ensure the container is valid
		if( container == null )
		{
			return null;
		}

		// Create an aspect stack based on whats in the container
		AspectStack stack = new AspectStack( this.getAspectInContainer( container ), drainAmount_EU );

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
		if( !this.isLabel( label ) )
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
	 * Returns the aspect of whatever essentia is in the container.
	 * This method supports jar labels.
	 * 
	 * @param container
	 * @return Aspect if container has one, null otherwise.
	 */
	public Aspect getAspectInContainer( final ItemStack container )
	{
		// Is it a label?
		if( this.isLabel( container ) )
		{
			return this.getAspectFromLabel( container );
		}
		// Is the itemstack valid?
		else if( container != null )
		{
			// Is it a container?
			if( container.getItem() instanceof IEssentiaContainerItem )
			{
				// Is the container whitelisted?
				if( this.isContainerWhitelisted( container ) )
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
	public AspectStack getAspectStackFromContainer( final ItemStack container )
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
	public ImmutablePair<Integer, ItemStack> injectIntoContainer( final ItemStack container, final AspectStack aspectStack )
	{
		// Is there an item?
		if( container == null )
		{
			return null;
		}

		// Is the item an essentia container?
		if( !this.isContainer( container ) )
		{
			return null;
		}

		// Is the container a labeled jar?
		if( this.doesJarHaveLabel( container ) )
		{
			// Does the label match the aspect we are going to fill
			// with?
			if( aspectStack.aspect != this.getJarLabelAspect( container ) )
			{
				// Aspect does not match the jar's label
				return null;
			}
		}

		// Get the container item
		Item containerItem = container.getItem();

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
		int amountToFill = (int)aspectStack.amount;

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
			resultStack = this.createFilledPhial( aspectStack.aspect );
		}
		// Is it an empty jar?
		else if( containerItem instanceof BlockJarItem )
		{
			// Create a fillable jar
			resultStack = this.createFilledJar( aspectStack.aspect, amountToFill, container.getItemDamage(), false );
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
	public boolean isContainer( final ItemStack container )
	{
		// Is the container not null?
		if( container != null )
		{
			// Return if the container whitelisted or not.
			return this.isContainerWhitelisted( container );
		}

		return false;
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
	 * True if the item is a container or label
	 * 
	 * @param stack
	 * @return
	 */
	public boolean isContainerOrLabel( final ItemStack stack )
	{
		return this.isContainer( stack ) || this.isLabel( stack );
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
	 * True if the itemstack is a jar label.
	 * 
	 * @param stack
	 * @return
	 */
	public boolean isLabel( final ItemStack stack )
	{
		// Ensure the stack is not null
		if( stack != null )
		{
			// True if resource and meta = 13
			return( ( stack.getItem() instanceof ItemResource ) && ( stack.getItemDamage() == 13 ) );
		}

		return false;
	}

	/**
	 * Setup the standard white list
	 */
	public void registerThaumcraftContainers()
	{
		// Phials
		this.perms.addEssentiaContainerItemToTransportPermissions( ItemEssence.class, PHIAL_CAPACITY, 0, false );
		this.perms.addEssentiaContainerItemToTransportPermissions( ItemEssence.class, PHIAL_CAPACITY, 1, false );

		// Filled jar
		this.perms.addEssentiaContainerItemToTransportPermissions( ItemJarFilled.class, JAR_CAPACITY, 0, true );

		// Void jar
		this.perms.addEssentiaContainerItemToTransportPermissions( ItemJarFilled.class, JAR_CAPACITY, 3, true );

		// Label
		//this.emptyJarLabel = new ItemStack( thaumcraft.common.config.ConfigItems.itemResource, 1, 13 );
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
				labelAspect = this.getAspectInContainer( jar );
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
		if( !this.isLabel( label ) )
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
