package thaumicenergistics.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.MutablePair;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.blocks.BlockJarItem;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemEssence;
import thaumicenergistics.aspect.AspectStack;

public class EssentiaItemContainerHelper
{

	private static final int PHIAL_SIZE = 8;
	private static final int JAR_SIZE = 64;
	private static final String JAR_LABEL_NBT_KEY = "AspectFilter";

	public static ItemStack createEmptyJar()
	{
		// Create and return the jar
		return new ItemStack( ConfigBlocks.blockJar, 1 );
	}

	public static ItemStack createEmptyPhial()
	{
		// Create and return the phial
		return new ItemStack( ConfigItems.itemEssence, 1, 0 );
	}

	public static ItemStack createFilledJar( Aspect aspect, int amount, boolean withLabel )
	{

		ItemStack jar;

		// If there is no aspect, or it would be empty with no label
		if ( ( aspect == null ) || ( ( amount <= 0 ) && !withLabel ) )
		{
			// Create an empty jar
			jar = EssentiaItemContainerHelper.createEmptyJar();
		}
		else
		{
			// Create an empty fillable jar
			jar = new ItemStack( ConfigItems.itemJarFilled, 1 );

			// Are we putting any essentia in the jar?
			if ( amount > 0 )
			{
				// Is there too much to fit in a jar?
				if ( amount > EssentiaItemContainerHelper.JAR_SIZE )
				{
					// Reduce to the max a jar can hold
					amount = EssentiaItemContainerHelper.JAR_SIZE;
				}

				// Set the aspect and amount
				( (ItemJarFilled) jar.getItem() ).setAspects( jar, new AspectList().add( aspect, amount ) );
			}

			// Are we putting a label on it?
			if ( withLabel )
			{
				EssentiaItemContainerHelper.setJarLabel( jar, aspect );
			}
		}

		// Return the jar
		return jar;
	}

	public static ItemStack createFilledPhial( Aspect aspect )
	{
		if ( aspect == null )
		{
			return EssentiaItemContainerHelper.createEmptyPhial();
		}

		// Create the phial
		ItemStack phial = new ItemStack( ConfigItems.itemEssence, 1, 1 );

		// Set it's aspect
		( (ItemEssence) phial.getItem() ).setAspects( phial, new AspectList().add( aspect, EssentiaItemContainerHelper.PHIAL_SIZE ) );

		return phial;
	}

	public static boolean doesJarHaveLabel( ItemStack jar )
	{
		// If the jar's label aspect is not null, there is a label
		return ( EssentiaItemContainerHelper.getJarLabelAspect( jar ) != null );
	}

	public static MutablePair<Integer, ItemStack> drainContainer( ItemStack container, AspectStack aspectStack )
	{
		if ( container == null )
		{
			return null;
		}

		Item containerItem = container.getItem();

		if ( EssentiaItemContainerHelper.isContainer( container ) )
		{
			AspectList aspectList = ( (IEssentiaContainerItem) containerItem ).getAspects( container );

			if ( aspectStack.aspect == aspectList.getAspects()[0] )
			{
				int containerAmountStored = aspectList.getAmount( aspectStack.aspect );

				if ( containerAmountStored > 0 )
				{

					int amountToDrain = (int) aspectStack.amount;

					if ( amountToDrain > containerAmountStored )
					{
						amountToDrain = containerAmountStored;
					}

					// Is this a phial?
					if ( containerItem instanceof ItemEssence )
					{
						// Is the drain amount correct?
						if ( amountToDrain < EssentiaItemContainerHelper.PHIAL_SIZE )
						{
							// Can not partially drain phials
							return null;
						}

						// Drain is to much, reduce
						amountToDrain = EssentiaItemContainerHelper.PHIAL_SIZE;
					}

					ItemStack resultStack = null;

					// Is this a phial?
					if ( containerItem instanceof ItemEssence )
					{
						// Create an empty phial for the output
						resultStack = EssentiaItemContainerHelper.createEmptyPhial();
					}
					else if ( containerItem instanceof ItemJarFilled )
					{
						// Calculate how much is left
						int remainaingAmount = containerAmountStored - amountToDrain;

						// Did we drain it all?
						if ( remainaingAmount <= 0 )
						{
							// Was the jar labeled?
							if ( EssentiaItemContainerHelper.doesJarHaveLabel( container ) )
							{
								// Create an empty labeled jar
								resultStack = EssentiaItemContainerHelper.createFilledJar( aspectStack.aspect, 0, true );
							}
							else
							{
								// Create an empty jar for the output
								resultStack = EssentiaItemContainerHelper.createEmptyJar();
							}
						}
						else
						{
							// Create a fillable jar filled with the remaining
							// amount
							resultStack = EssentiaItemContainerHelper.createFilledJar( aspectStack.aspect, remainaingAmount,
								EssentiaItemContainerHelper.doesJarHaveLabel( container ) );
						}

					}

					return new MutablePair<Integer, ItemStack>( amountToDrain, resultStack );
				}
			}

		}

		return null;

	}

	public static MutablePair<Integer, ItemStack> drainContainer( ItemStack container, int drainAmount_EU )
	{
		if ( container == null )
		{
			return null;
		}

		AspectStack stack = new AspectStack( EssentiaItemContainerHelper.getAspectInContainer( container ), drainAmount_EU );

		return EssentiaItemContainerHelper.drainContainer( container, stack );
	}

	public static MutablePair<Integer, ItemStack> fillContainer( ItemStack container, AspectStack aspectStack )
	{
		// Is there an item?
		if ( container == null )
		{
			return null;
		}

		// Is the item an essentia container?
		if ( EssentiaItemContainerHelper.isContainer( container ) )
		{
			// Get how much essentia is in the container
			int containerAmountStored = EssentiaItemContainerHelper.getContainerStoredAmount( container );

			// Calculate how much remaining storage is in the container
			int remainaingStorage = EssentiaItemContainerHelper.getContainerCapacity( container ) - containerAmountStored;

			if ( remainaingStorage > 0 )
			{
				// Is the container a labeled jar?
				if ( EssentiaItemContainerHelper.doesJarHaveLabel( container ) )
				{
					// Does the label match the aspect we are going to fill
					// with?
					if ( aspectStack.aspect != EssentiaItemContainerHelper.getJarLabelAspect( container ) )
					{
						// Aspect does not match the jar's label
						return null;
					}
				}

				// Get the container item
				Item containerItem = container.getItem();

				// Set the amount to fill
				int amountToFill = (int) aspectStack.amount;

				// Is the amount to fill more than the remaining storage?
				if ( amountToFill > remainaingStorage )
				{
					// Adjust the amount to fill to the remaining storage in the
					// container.
					amountToFill = remainaingStorage;
				}

				// Is this a phial?
				if ( containerItem instanceof ItemEssence )
				{
					// Is the fill amount correct?
					if ( amountToFill < EssentiaItemContainerHelper.PHIAL_SIZE )
					{
						// Can not partially fill phials
						return null;
					}

					// Fill is to much, reduce
					amountToFill = EssentiaItemContainerHelper.PHIAL_SIZE;
				}

				ItemStack resultStack = null;

				// Is this a phial?
				if ( containerItem instanceof ItemEssence )
				{
					// Create a new phial
					resultStack = EssentiaItemContainerHelper.createFilledPhial( aspectStack.aspect );
				}
				// Is it an empty jar?
				else if ( containerItem instanceof BlockJarItem )
				{
					// Create a fillable jar
					resultStack = EssentiaItemContainerHelper.createFilledJar( aspectStack.aspect, amountToFill, false );
				}
				// Is it a partially filled or labeled jar?
				else if ( containerItem instanceof ItemJarFilled )
				{
					// Create a fillable jar
					resultStack = EssentiaItemContainerHelper.createFilledJar( aspectStack.aspect, amountToFill + containerAmountStored,
						EssentiaItemContainerHelper.doesJarHaveLabel( container ) );

				}

				return new MutablePair<Integer, ItemStack>( amountToFill, resultStack );

			}

		}

		return null;
	}

	public static Aspect getAspectInContainer( ItemStack container )
	{
		if ( container != null )
		{
			if ( ( container.getItem() instanceof IEssentiaContainerItem ) )
			{
				AspectList itemList = ( (IEssentiaContainerItem) container.getItem() ).getAspects( container );

				if ( itemList != null )
				{
					return itemList.getAspects()[0];
				}
			}
		}

		return null;
	}

	public static AspectStack getAspectStackFromContainer( ItemStack container )
	{
		Aspect aspect = getAspectInContainer( container );

		if ( aspect == null )
		{
			return null;
		}

		int stored = getContainerStoredAmount( container );

		return new AspectStack( aspect, stored );
	}

	public static int getContainerCapacity( ItemStack container )
	{
		if ( container != null )
		{
			Item containerItem = container.getItem();

			if ( containerItem instanceof ItemEssence )
			{
				return EssentiaItemContainerHelper.PHIAL_SIZE;
			}
			else if ( ( containerItem instanceof ItemJarFilled ) || ( containerItem instanceof BlockJarItem ) )
			{
				return EssentiaItemContainerHelper.JAR_SIZE;
			}
		}

		return 0;
	}

	public static Aspect getJarLabelAspect( ItemStack jar )
	{
		Aspect labelAspect = null;

		// Ensure it is a jar
		if ( jar.getItem() instanceof ItemJarFilled )
		{
			// Does the jar have a label?
			if ( ( jar.hasTagCompound() ) && ( jar.stackTagCompound.hasKey( EssentiaItemContainerHelper.JAR_LABEL_NBT_KEY ) ) )
			{
				// Get the aspect tag from the NBT
				String tag = jar.stackTagCompound.getString( EssentiaItemContainerHelper.JAR_LABEL_NBT_KEY );

				// Set the label aspect
				labelAspect = Aspect.getAspect( tag );
			}
		}

		return labelAspect;

	}

	public static int getContainerStoredAmount( ItemStack container )
	{
		if ( container != null )
		{
			if ( ( container.getItem() instanceof IEssentiaContainerItem ) )
			{
				AspectList storedList = ( (IEssentiaContainerItem) container.getItem() ).getAspects( container );

				if ( storedList == null )
				{
					return 0;
				}

				return storedList.getAmount( storedList.getAspects()[0] );
			}
		}
		return 0;
	}

	public static boolean isContainer( ItemStack container )
	{
		if ( container != null )
		{
			return ( ( container.getItem() instanceof IEssentiaContainerItem ) || ( container.getItem() instanceof BlockJarItem ) );
		}

		return false;
	}

	public static boolean isContainerEmpty( ItemStack container )
	{
		return ( EssentiaItemContainerHelper.getContainerStoredAmount( container ) == 0 );
	}

	public static boolean isContainerFilled( ItemStack container )
	{

		return ( EssentiaItemContainerHelper.getContainerStoredAmount( container ) > 0 );
	}

	public static ItemStack setJarLabel( ItemStack jar, Aspect OverrideAspect )
	{
		// Ensure it is a jar
		if ( jar.getItem() instanceof ItemJarFilled )
		{
			Aspect labelAspect;

			// Are we overriding the aspect?
			if ( OverrideAspect != null )
			{
				labelAspect = OverrideAspect;
			}
			else
			{
				labelAspect = EssentiaItemContainerHelper.getAspectInContainer( jar );
			}

			// Ensure we have an aspect to set
			if ( labelAspect != null )
			{
				// Does the jar have a compound tag?
				if ( !jar.hasTagCompound() )
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
