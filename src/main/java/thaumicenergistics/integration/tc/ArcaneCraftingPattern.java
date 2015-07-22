package thaumicenergistics.integration.tc;

import java.util.ArrayList;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

public class ArcaneCraftingPattern
	implements ICraftingPatternDetails
{
	private static final String NBTKEY_INGREDIENT = "input#", NBTKEY_RESULT = "output";

	private static final int GRID_SIZE = 9;

	/**
	 * Aspects required.
	 */
	public AspectList aspects;

	/**
	 * Ingredients.
	 */
	public IAEItemStack[] ingredients = new IAEItemStack[ArcaneCraftingPattern.GRID_SIZE];

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
	 * 
	 * @param knowledgeCore
	 * @param aspects
	 * @param craftingResult
	 * @param craftingIngredients
	 */
	public ArcaneCraftingPattern( final ItemStack knowledgeCore, final AspectList aspects, final ItemStack craftingResult,
									final ItemStack[] craftingIngredients )
	{
		// Set the core
		this.knowledgeCoreHost = knowledgeCore;

		// Set the aspects
		this.aspects = aspects.copy();

		// Set the result
		this.result = AEApi.instance().storage().createItemStack( craftingResult );

		// Set the ingredients
		for( int index = 0; ( ( index < this.ingredients.length ) && ( index < craftingIngredients.length ) ); index++ )
		{
			// Get the next ingredient
			ItemStack nextIng = craftingIngredients[index];

			// Add to the ingredients if not null
			if( nextIng != null )
			{
				this.ingredients[index] = AEApi.instance().storage().createItemStack( nextIng );
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

	@Override
	public boolean canSubstitute()
	{
		// This needs to be precise Issue #216
		return false;
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

		// Add non-null ingredients
		for( int index = 0; index < this.ingredients.length; index++ )
		{
			if( this.ingredients[index] != null )
			{
				cond.add( this.ingredients[index] );
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
		return this.ingredients;
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
	public boolean isValidItemForSlot( final int slotIndex, final ItemStack repStack, final World world )
	{
		// Get the item currently in the slot
		IAEItemStack ingStack = this.ingredients[slotIndex];

		// Ensure nothing is null
		if( ( ingStack == null ) || ( ingStack.getItem() == null ) || ( repStack == null ) || ( repStack.getItem() == null ) )
		{
			// Null detected
			return false;
		}

		// Does the item directly match?
		if( ItemStack.areItemStacksEqual( ingStack.getItemStack(), repStack ) )
		{
			return true;
		}

		// Does the item via ore dictionary fast match?
		if( OreDictionary.itemMatches( ingStack.getItemStack(), repStack, false ) )
		{
			return true;
		}

		// Crazy stuff with ore dictionary IDs
		int[] sourceIDs = OreDictionary.getOreIDs( ingStack.getItemStack() );
		int[] repIDs = OreDictionary.getOreIDs( repStack );
		for( int i = 0; i < sourceIDs.length; i++ )
		{
			for( int j = 0; j < repIDs.length; j++ )
			{
				if( sourceIDs[i] == repIDs[j] )
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
			if( data.hasKey( ArcaneCraftingPattern.NBTKEY_INGREDIENT + index ) )
			{
				NBTTagCompound ingData = data.getCompoundTag( ArcaneCraftingPattern.NBTKEY_INGREDIENT + index );
				this.ingredients[index] = AEItemStack.loadItemStackFromNBT( ingData );
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
				// Create the tag
				NBTTagCompound ingData = new NBTTagCompound();

				// Write the ingredient data
				this.ingredients[index].writeToNBT( ingData );

				// Write into the main data tag
				data.setTag( ArcaneCraftingPattern.NBTKEY_INGREDIENT + index, ingData );
			}
		}

		// Write the result
		NBTTagCompound outData = new NBTTagCompound();
		this.result.writeToNBT( outData );
		data.setTag( ArcaneCraftingPattern.NBTKEY_RESULT, outData );

		return data;
	}

}
