package thaumicenergistics.integration.tc;

import java.util.ArrayList;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.aspects.AspectList;
import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;

public class ArcaneCraftingPattern
	implements ICraftingPatternDetails
{
	/**
	 * Aspects required.
	 */
	public AspectList aspects;

	/**
	 * Ingredients.
	 */
	public IAEItemStack[] ingredients = new IAEItemStack[9];

	/**
	 * Crafting result.
	 */
	public IAEItemStack result;

	/**
	 * The knowledge core this pattern belongs to.
	 */
	public ItemStack knowledgeCoreHost;

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

	@Override
	public boolean canSubstitute()
	{
		return true;
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
		// TODO Inspect the crafting inventory for vis reducing items.
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
		// TODO: Im not quite sure about this yet.

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

	@Override
	public void setPriority( final int priority )
	{
		// Ignored.
	}

}
