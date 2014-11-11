package thaumicenergistics.proxy;

import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import thaumicenergistics.tileentities.TileGearBox;
import thaumicenergistics.tileentities.TileInfusionProvider;
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

		// Add essentia provider
		movableRegistry.whiteListTileEntity( TileEssentiaProvider.class );

		// Add infusion provider
		movableRegistry.whiteListTileEntity( TileInfusionProvider.class );

		// Added gearbox
		movableRegistry.whiteListTileEntity( TileGearBox.class );
	}

	/**
	 * Registers tile entities with the game.
	 */
	public void registerTileEntities()
	{
		GameRegistry.registerTileEntity( TileEssentiaProvider.class, TileEssentiaProvider.TILE_ID );
		GameRegistry.registerTileEntity( TileInfusionProvider.class, TileInfusionProvider.TILE_ID );
		GameRegistry.registerTileEntity( TileGearBox.class, TileGearBox.TILE_ID );
	}

}
