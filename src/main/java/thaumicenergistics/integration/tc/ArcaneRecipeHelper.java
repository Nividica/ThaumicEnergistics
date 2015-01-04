package thaumicenergistics.integration.tc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.tiles.TileMagicWorkbench;

public class ArcaneRecipeHelper
{
	/**
	 * Singleton.
	 */
	public static final ArcaneRecipeHelper instance = new ArcaneRecipeHelper();

	/**
	 * Private constructor.
	 */
	private ArcaneRecipeHelper()
	{
		// Intentionally Empty
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
	public IArcaneRecipe findMatchingArcaneResult( final IInventory sourceInventory, final int firstSlotIndex, final int gridSize,
													final EntityPlayer player )
	{
		// Create a new workbench tile
		TileMagicWorkbench workbenchTile = new TileMagicWorkbench();

		// Load the workbench inventory
		for( int slotIndex = 0; slotIndex < gridSize; slotIndex++ )
		{
			// Set the slot
			workbenchTile.setInventorySlotContentsSoftly( slotIndex, sourceInventory.getStackInSlot( slotIndex + firstSlotIndex ) );
		}

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

		// Invalidate the tile (not sure if this is needed, but seems a good idea)
		workbenchTile.invalidate();

		// Return the result
		return arcaneRecipe;
	}

}
