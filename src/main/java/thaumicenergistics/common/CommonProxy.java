package thaumicenergistics.common;

import net.minecraftforge.oredict.OreDictionary;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.items.ItemMaterial;
import thaumicenergistics.common.registries.BlockEnum;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.TileEnum;
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
			GameRegistry.registerBlock( block.getBlock(), block.itemBlockClass, block.getUnlocalizedName() );
		}
	}

	/**
	 * Registers all ThE features
	 */
	public void registerFeatures()
	{
		FeatureRegistry.instance().registerFeatures();
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
		OreDictionary.registerOre( "gearIron", ItemMaterial.MaterialTypes.IRON_GEAR.getStack() );
	}

	/**
	 * Used by client proxy
	 */
	public void registerRenderers()
	{
		// Ignored server side.
	}

	/**
	 * Adds tile entities to the AppEng2 SpatialIO whitelist
	 */
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
	public void registerTileEntities()
	{
		for( TileEnum tile : TileEnum.values() )
		{
			GameRegistry.registerTileEntity( tile.getTileClass(), tile.getTileID() );
		}
	}

}
