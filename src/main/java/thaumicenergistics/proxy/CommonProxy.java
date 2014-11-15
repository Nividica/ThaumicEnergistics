package thaumicenergistics.proxy;

import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.TileEnum;
import appeng.api.AEApi;
import appeng.api.movable.IMovableRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy
{
	/**
	 * Registers blocks with the game.
	 */
	public void registerBlocks()
	{
		for( BlockEnum block : BlockEnum.VALUES )
		{
			GameRegistry.registerBlock( block.getBlock(), block.getUnlocalizedName() );
		}
	}

	/**
	 * Registers fluids with the game.
	 */
	public void registerFluids()
	{
		GaseousEssentia.registerGases();
	}

	/**
	 * Registers items with the game.
	 */
	public void registerItems()
	{
		for( ItemEnum item : ItemEnum.VALUES )
		{
			GameRegistry.registerItem( item.getItem(), item.getInternalName() );
		}
	}

	/**
	 * Registers recipes with the game.
	 */
	public void registerRecipes()
	{
		RecipeRegistry.registerRecipies();
	}

	/**
	 * Used by client proxy
	 */
	public void registerRenderers()
	{
		// Ignored server side.
	}

	/**
	 * Registers research with Thaumcraft.
	 */
	public void registerResearch()
	{
		ResearchRegistry.registerResearch();
	}

	/**
	 * Adds tile entities to the AppEng2 SpatialIO whitelist
	 */
	public void registerSpatialIOMovables()
	{
		IMovableRegistry movableRegistry = AEApi.instance().registries().moveable();
		for( TileEnum tile : TileEnum.values() )
		{
			movableRegistry.whiteListTileEntity( tile.getTileClass() );
		}
	}

	/**
	 * Registers tile entities with the game.
	 */
	@SuppressWarnings("deprecation")
	public void registerTileEntities()
	{
		for( TileEnum tile : TileEnum.values() )
		{
			// TODO: Drop legacy support at version 1.0
			GameRegistry.registerTileEntityWithAlternatives( tile.getTileClass(), tile.getTileID(), tile.getOldTileID() );
			//GameRegistry.registerTileEntity(
		}
	}

}
