package thaumicenergistics.proxy;

import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.TileEntities;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import thaumicenergistics.tileentities.TileInfusionProvider;
import appeng.api.AEApi;
import appeng.api.movable.IMovableRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy
{
	/**
	 * Registers this mods blocks with the game.
	 */
	public void registerBlocks()
	{
		for( BlockEnum block : BlockEnum.values() )
		{
			GameRegistry.registerBlock( block.getBlock(), block.getUnlocalizedName() );
		}
	}

	/**
	 * Registers this mods fluids with the game.
	 */
	public void registerFluids()
	{
		GaseousEssentia.registerGases();
	}

	/**
	 * Registers this mods items with the game.
	 */
	public void registerItems()
	{

		for( ItemEnum item : ItemEnum.values() )
		{
			GameRegistry.registerItem( item.getItem(), item.getInternalName() );
		}

	}

	/**
	 * Registers this mods recipes with the game.
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
	}

	/**
	 * Registers this mods research with Thaumcraft.
	 */
	public void registerResearch()
	{
		ResearchRegistry.registerResearch();
	}

	/**
	 * Adds this mods tile entities to the AppEng2 SpatialIO whitelist
	 */
	public void registerSpatialIOMovables()
	{
		IMovableRegistry movableRegistry = AEApi.instance().registries().moveable();

		// Add essentia provider
		movableRegistry.whiteListTileEntity( TileEssentiaProvider.class );

		// Add infusion provider
		movableRegistry.whiteListTileEntity( TileInfusionProvider.class );
	}

	/**
	 * Registers this mods tile entities with the game.
	 */
	public void registerTileEntities()
	{
		TileEntities.registerTiles();
	}

}
