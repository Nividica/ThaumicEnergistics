package thaumicenergistics.proxy;

import net.minecraftforge.oredict.OreDictionary;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.items.ItemMaterial.MaterialTypes;
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

		// Add iron gear to the oredic
		OreDictionary.registerOre( "gearIron", MaterialTypes.IRON_GEAR.getItemStack() );
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
	@SuppressWarnings("unchecked")
	public void registerSpatialIOMovables()
	{
		IMovableRegistry movableRegistry = AEApi.instance().registries().movable();
		for( TileEnum tile : TileEnum.values() )
		{
			movableRegistry.whiteListTileEntity( tile.getTileClass() );
		}
	}

	/**
	 * Registers tile entities with the game.
	 */
	@SuppressWarnings("unchecked")
	public void registerTileEntities()
	{
		for( TileEnum tile : TileEnum.values() )
		{
			GameRegistry.registerTileEntity( tile.getTileClass(), tile.getTileID() );
		}
	}

}
