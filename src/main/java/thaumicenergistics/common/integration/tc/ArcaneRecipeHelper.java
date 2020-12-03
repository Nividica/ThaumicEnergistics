package thaumicenergistics.common.integration.tc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.tiles.TileMagicWorkbench;

public class ArcaneRecipeHelper
{
	/**
	 * Singleton.
	 */
	public static final ArcaneRecipeHelper INSTANCE = new ArcaneRecipeHelper();

	/**
	 * Private constructor.
	 */
	private ArcaneRecipeHelper()
	{
		// Intentionally Empty
	}

	/**
	 * Creates a workbench with its crafting grid set to match the source
	 * inventory.
	 *
	 * @param sourceInventory
	 * @param firstSlotIndex
	 * @param gridSize
	 * @return
	 */
	public TileMagicWorkbench createBridgeInventory( final IInventory sourceInventory, final int firstSlotIndex, final int gridSize )
	{
		// Create a new workbench tile
		TileMagicWorkbench workbenchTile = new TileMagicWorkbench();

		// Load the workbench inventory
		for( int slotIndex = 0; slotIndex < gridSize; slotIndex++ )
		{
			// Set the slot
			workbenchTile.setInventorySlotContentsSoftly( slotIndex, sourceInventory.getStackInSlot( slotIndex + firstSlotIndex ) );
		}

		return workbenchTile;
	}

	/**
	 * Searches for a matching arcane crafting recipe
	 *
	 * @param sourceInventory
	 * @param firstSlotIndex
	 * @param gridSize
	 * @param player
	 * @return
	 */
	public IArcaneRecipe findMatchingArcaneResult(	final IInventory sourceInventory, final int firstSlotIndex, final int gridSize,
													final EntityPlayer player )
	{
		// Create a the workbench
		TileMagicWorkbench workbenchTile = this.createBridgeInventory( sourceInventory, firstSlotIndex, gridSize );

		IArcaneRecipe arcaneRecipe = null;

		// Loop through all arcane crafting recipes
		for( Object currentRecipe : ThaumcraftApi.getCraftingRecipes() )
		{
			// Is the current recipe an arcane one?
			if( currentRecipe instanceof IArcaneRecipe )
			{
				// Does the recipe have a match?
				if( ( (IArcaneRecipe)currentRecipe ).matches( workbenchTile, player.worldObj, player ) )
				{
					// Found a match, stop searching
					arcaneRecipe = (IArcaneRecipe)currentRecipe;

					break;
				}
			}
		}

		// Return the result
		return arcaneRecipe;
	}

	/**
	 * Gets the base aspect cost of this recipe. Can return null.
	 *
	 * @param sourceInventory
	 * @param firstSlotIndex
	 * @param gridSize
	 * @param recipe
	 * @return
	 */
	public AspectList getRecipeAspectCost(	final IInventory sourceInventory, final int firstSlotIndex, final int gridSize,
											final IArcaneRecipe recipe )
	{
		// Ensure the recipe is valid
		if( recipe == null )
		{
			return null;
		}

		return recipe.getAspects( this.createBridgeInventory( sourceInventory, firstSlotIndex, gridSize ) );
	}

	/**
	 * Gets the item that results from this recipe. Can be null.
	 *
	 * @param sourceInventory
	 * @param firstSlotIndex
	 * @param gridSize
	 * @param recipe
	 * @return
	 */
	public ItemStack getRecipeOutput( final IInventory sourceInventory, final int firstSlotIndex, final int gridSize, final IArcaneRecipe recipe )
	{
		// Ensure the recipe is valid
		if( recipe == null )
		{
			return null;
		}

		return recipe.getCraftingResult( this.createBridgeInventory( sourceInventory, firstSlotIndex, gridSize ) );
	}

}
