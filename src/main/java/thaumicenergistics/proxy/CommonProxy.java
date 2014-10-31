package thaumicenergistics.proxy;

import net.minecraft.item.ItemStack;
import thaumicenergistics.api.TEApi;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.items.ItemStorageBase;
import thaumicenergistics.registries.AEPartsEnum;
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
		for( BlockEnum block : BlockEnum.VALUES )
		{
			GameRegistry.registerBlock( block.getBlock(), block.getUnlocalizedName() );
		}

		// Assign the blocks
		TEApi.instance.blocks.Essentia_Provider = new ItemStack( BlockEnum.ESSENTIA_PROVIDER.getBlock(), 1 );
		TEApi.instance.blocks.Infusion_Provider = new ItemStack( BlockEnum.INFUSION_PROVIDER.getBlock(), 1 );
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
		for( ItemEnum item : ItemEnum.VALUES )
		{
			GameRegistry.registerItem( item.getItem(), item.getInternalName() );
		}

		// Assign the items
		TEApi.instance.items.EssentiaCell_Casing = ItemEnum.STORAGE_CASING.getItemStackWithSize( 1 );
		TEApi.instance.items.EssentiaCell_1k = ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_1K );
		TEApi.instance.items.EssentiaCell_4k = ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_4K );
		TEApi.instance.items.EssentiaCell_16k = ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_16K );
		TEApi.instance.items.EssentiaCell_64k = ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_64K );
		TEApi.instance.items.EssentiaStorageComponent_1k = ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_1K );
		TEApi.instance.items.EssentiaStorageComponent_4k = ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_4K );
		TEApi.instance.items.EssentiaStorageComponent_16k = ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_16K );
		TEApi.instance.items.EssentiaStorageComponent_64k = ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_64K );
		TEApi.instance.items.DiffusionCore = ItemEnum.MATERIAL.getItemStackWithDamage( 0 );
		TEApi.instance.items.CoalescenceCore = ItemEnum.MATERIAL.getItemStackWithDamage( 1 );

		// Assign the parts
		TEApi.instance.parts.ArcaneCrafting_Terminal = AEPartsEnum.ArcaneCraftingTerminal.getStack();
		TEApi.instance.parts.Essentia_ExportBus = AEPartsEnum.EssentiaExportBus.getStack();
		TEApi.instance.parts.Essentia_ImportBus = AEPartsEnum.EssentiaImportBus.getStack();
		TEApi.instance.parts.Essentia_LevelEmitter = AEPartsEnum.EssentiaLevelEmitter.getStack();
		TEApi.instance.parts.Essentia_StorageBus = AEPartsEnum.EssentiaStorageBus.getStack();
		TEApi.instance.parts.Essentia_Terminal = AEPartsEnum.EssentiaTerminal.getStack();
		TEApi.instance.parts.VisRelay_Interface = AEPartsEnum.VisInterface.getStack();

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
		// Ignored server side.
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
