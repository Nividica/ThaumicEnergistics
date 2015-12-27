package thaumicenergistics.integration.tc;

import java.util.ArrayList;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

public class ArcaneCraftingPattern
	implements ICraftingPatternDetails
{
	private static final String NBTKEY_INGREDIENT_NUM = "input#", NBTKEY_RESULT = "output", NBTKEY_INGREDIENT_TYPE = "ThE_crafting_type",
					NBTKEY_ARRAY_SIZE = "size";
	private static final int NBTKEY_ARRAYTYPE = 2, NBTKEY_ITEMTYPE = 1;

	private static final int GRID_SIZE = 9;

	/**
	 * Aspects required.
	 */
	public AspectList aspects;

	/**
	 * Ingredients.
	 */
	public Object[] ingredients = new Object[ArcaneCraftingPattern.GRID_SIZE];

	/**
	 * Crafting result.
	 */
	public IAEItemStack result;

	/**
	 * The knowledge core this pattern belongs to.
	 */
	public ItemStack knowledgeCoreHost;

	/**
	 * Cached array of required aspects
	 */
	private Aspect[] cachedAspects;

	/**
	 * Ingredients as AE itemstacks.
	 */
	private IAEItemStack[] ingredientsAE;

	/**
	 * 
	 * @param knowledgeCore
	 * @param aspects
	 * @param craftingResult
	 * @param craftingIngredients
	 */
	public ArcaneCraftingPattern( final ItemStack knowledgeCore, final AspectList aspects, final ItemStack craftingResult,
									final Object[] craftingIngredients )
	{
		// Set the core
		this.knowledgeCoreHost = knowledgeCore;

		// Set the aspects
		this.aspects = aspects.copy();

		// Set the result
		this.result = AEApi.instance().storage().createItemStack( craftingResult );

		// Set the ingredients
		for( int index = 0; ( ( index < this.ingredients.length ) && ( index < craftingIngredients.length ) ); ++index )
		{
			// Get the next ingredient
			Object nextIng = craftingIngredients[index];

			// Add to the ingredients if not null
			if( nextIng != null )
			{
				this.ingredients[index] = nextIng;
			}
		}
	}

	/**
	 * 
	 * @param knowledgeCore
	 * @param data
	 */
	public ArcaneCraftingPattern( final ItemStack knowledgeCore, final NBTTagCompound data )
	{
		// Set the host
		this.knowledgeCoreHost = knowledgeCore;

		// Create the aspect list
		this.aspects = new AspectList();

		// Read the data
		this.readFromNBT( data );
	}

	/**
	 * Checks if one item can be substituted for another.
	 * 
	 * @param target
	 * @param input
	 * @return
	 */
	private boolean canSubstitueItem( final ItemStack target, final ItemStack input )
	{
		// Do the itemstacks directly match?
		if( ItemStack.areItemStacksEqual( target, input ) )
		{
			return true;
		}

		// Do the items match?
		if( target.getItem() != input.getItem() )
		{
			// Mismatched items
			return false;
		}

		// NBT data present?
		if( target.hasTagCompound() )
		{
			// Do the tags match?
			if( !ThaumcraftApiHelper.areItemStackTagsEqualForCrafting( input, target ) )
			{
				// Tags do not match
				return false;
			}
		}

		// Finally check item damage (32767 is some magic number from thaumcraft)
		return( ( target.getItemDamage() == 32767 ) || ( target.getItemDamage() == input.getItemDamage() ) );
	}

	/**
	 * Converts the itemlist into it's AE counterpart
	 */
	private void setupAEIngredientList()
	{
		this.ingredientsAE = new AEItemStack[ArcaneCraftingPattern.GRID_SIZE];

		for( int index = 0; index < ArcaneCraftingPattern.GRID_SIZE; ++index )
		{
			// Get the ingredient
			Object ing = this.ingredients[index];

			// Check for null
			if( ing == null )
			{
				this.ingredientsAE[index] = null;
				continue;
			}

			// Itemstack?
			if( ing instanceof ItemStack )
			{
				this.ingredientsAE[index] = AEApi.instance().storage().createItemStack( (ItemStack)ing );
			}
			// Array
			else if( ing instanceof ArrayList )
			{
				// Use the first ingredient
				ItemStack firstIng = ( (ArrayList<ItemStack>)ing ).get( 0 );

				this.ingredientsAE[index] = AEApi.instance().storage().createItemStack( firstIng );
			}
		}
	}

	@Override
	public boolean canSubstitute()
	{
		//return false;
		return true;
	}

	/**
	 * Returns a cached array of the required aspects for this pattern.
	 * 
	 * @return
	 */
	public Aspect[] getCachedAspects()
	{
		if( this.cachedAspects == null )
		{
			this.cachedAspects = this.aspects.getAspects();
		}

		return this.cachedAspects;
	}

	@Override
	public IAEItemStack[] getCondensedInputs()
	{
		// Create a temp array
		ArrayList<IAEItemStack> cond = new ArrayList<IAEItemStack>();

		if( this.ingredientsAE == null )
		{
			this.setupAEIngredientList();
		}

		// Add non-null ingredients
		for( int index = 0; index < this.ingredientsAE.length; index++ )
		{
			if( this.ingredientsAE[index] != null )
			{
				cond.add( this.ingredientsAE[index] );
			}
		}

		// Return the ingredients
		return cond.toArray( new IAEItemStack[cond.size()] );
	}

	@Override
	public IAEItemStack[] getCondensedOutputs()
	{
		// Is the result null?
		if( this.result == null )
		{
			// Return empty array
			return new IAEItemStack[0];
		}

		// Return the result
		return new IAEItemStack[] { this.result };
	}

	@Override
	public IAEItemStack[] getInputs()
	{
		if( this.ingredientsAE == null )
		{
			this.setupAEIngredientList();
		}

		return this.ingredientsAE;
	}

	@Override
	public ItemStack getOutput( final InventoryCrafting craftingInv, final World world )
	{
		return this.result.getItemStack();
	}

	@Override
	public IAEItemStack[] getOutputs()
	{
		// Return the result
		return new IAEItemStack[] { this.result };
	}

	@Override
	public ItemStack getPattern()
	{
		return this.knowledgeCoreHost;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public boolean isCraftable()
	{
		return( this.result != null );
	}

	@Override
	public boolean isValidItemForSlot( final int slotIndex, final ItemStack input, final World world )
	{
		// Get the ingredient
		Object ing = this.ingredients[slotIndex];

		// Ensure nothing is null
		if( ( ing == null ) || ( input == null ) || ( input.getItem() == null ) )
		{
			// Null detected
			return false;
		}

		// What type is the ingredient?
		if( ing instanceof ItemStack )
		{
			// Cast to itemstack
			ItemStack target = ( (ItemStack)ing );
			return this.canSubstitueItem( target, input );
		}
		else if( ing instanceof ArrayList )
		{
			// Cast to list
			ArrayList<ItemStack> items = (ArrayList<ItemStack>)ing;

			// Check each item
			for( ItemStack target : items )
			{
				if( this.canSubstitueItem( target, input ) )
				{
					return true;
				}
			}

		}

		return false;

	}

	public void readFromNBT( final NBTTagCompound data )
	{
		// Read the aspects
		this.aspects.readFromNBT( data );

		// Read ingredients
		for( int index = 0; index < ArcaneCraftingPattern.GRID_SIZE; index++ )
		{
			if( data.hasKey( ArcaneCraftingPattern.NBTKEY_INGREDIENT_NUM + index ) )
			{
				// Get the tag
				NBTTagCompound ingData = data.getCompoundTag( ArcaneCraftingPattern.NBTKEY_INGREDIENT_NUM + index );

				// What type is the tag?
				if( ingData.hasKey( ArcaneCraftingPattern.NBTKEY_INGREDIENT_TYPE ) )
				{
					int type = ingData.getInteger( ArcaneCraftingPattern.NBTKEY_INGREDIENT_TYPE );

					// Itemstack
					if( type == ArcaneCraftingPattern.NBTKEY_ITEMTYPE )
					{
						this.ingredients[index] = ItemStack.loadItemStackFromNBT( ingData );
					}
					// Array
					else if( type == ArcaneCraftingPattern.NBTKEY_ARRAYTYPE )
					{
						// Create the list
						ArrayList<ItemStack> list = new ArrayList<ItemStack>();

						// Get the count
						int count = ingData.getInteger( ArcaneCraftingPattern.NBTKEY_ARRAY_SIZE );

						for( int i = 0; i < count; ++i )
						{
							// Get the subtag
							NBTTagCompound subTag = ingData.getCompoundTag( ArcaneCraftingPattern.NBTKEY_INGREDIENT_NUM + i );

							// Get the stack
							try
							{
								ItemStack stack = ItemStack.loadItemStackFromNBT( subTag );
								if( stack != null )
								{
									list.add( stack );
								}
							}
							catch( Exception e )
							{
								// Silently ignore invalid items
							}
						}

						// Set the ingredient to the list
						this.ingredients[index] = list;
					}
				}
				else
				{
					// Old AE tag
					IAEItemStack ing = AEItemStack.loadItemStackFromNBT( ingData );
					this.ingredients[index] = ing.getItemStack();
				}
			}
			else
			{
				this.ingredients[index] = null;
			}
		}

		// Read the result
		if( data.hasKey( ArcaneCraftingPattern.NBTKEY_RESULT ) )
		{
			NBTTagCompound outData = data.getCompoundTag( ArcaneCraftingPattern.NBTKEY_RESULT );
			this.result = AEItemStack.loadItemStackFromNBT( outData );
		}
	}

	@Override
	public void setPriority( final int priority )
	{
		// Ignored.
	}

	/**
	 * Write's the pattern to the NBT tag.
	 * 
	 * @param data
	 * @return
	 */
	public NBTTagCompound writeToNBT( final NBTTagCompound data )
	{
		// Write the aspects
		this.aspects.writeToNBT( data );

		// Write the ingredients
		for( int index = 0; index < ArcaneCraftingPattern.GRID_SIZE; index++ )
		{
			// Ensure it is not null
			if( this.ingredients[index] != null )
			{
				// Get the ingredient
				Object ing = this.ingredients[index];

				// Create the tag
				NBTTagCompound ingData = new NBTTagCompound();

				// What type is the ingredient?
				if( ing instanceof IAEItemStack )
				{
					// Write the ingredient data
					( (IAEItemStack)ing ).getItemStack().writeToNBT( ingData );
					ingData.setInteger( ArcaneCraftingPattern.NBTKEY_INGREDIENT_TYPE, ArcaneCraftingPattern.NBTKEY_ITEMTYPE );
				}
				else if( ing instanceof ItemStack )
				{
					( (ItemStack)ing ).writeToNBT( ingData );
					ingData.setInteger( ArcaneCraftingPattern.NBTKEY_INGREDIENT_TYPE, ArcaneCraftingPattern.NBTKEY_ITEMTYPE );
				}
				else if( ing instanceof ArrayList )
				{
					// Cast to array list
					ArrayList<ItemStack> ingList = (ArrayList<ItemStack>)ing;

					// Set type and count
					ingData.setInteger( ArcaneCraftingPattern.NBTKEY_INGREDIENT_TYPE, ArcaneCraftingPattern.NBTKEY_ARRAYTYPE );
					ingData.setInteger( ArcaneCraftingPattern.NBTKEY_ARRAY_SIZE, ingList.size() );

					// Add each item
					for( int i = 0; i < ingList.size(); ++i )
					{
						// Create a new tag
						NBTTagCompound subTag = new NBTTagCompound();

						// Write the item
						ingList.get( i ).writeToNBT( subTag );

						// Add the sub tag
						ingData.setTag( ArcaneCraftingPattern.NBTKEY_INGREDIENT_NUM + i, subTag );
					}

				}

				// Write into the main data tag
				data.setTag( ArcaneCraftingPattern.NBTKEY_INGREDIENT_NUM + index, ingData );
			}
		}

		// Write the result
		NBTTagCompound outData = new NBTTagCompound();
		this.result.writeToNBT( outData );
		data.setTag( ArcaneCraftingPattern.NBTKEY_RESULT, outData );

		return data;
	}

}
