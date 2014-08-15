package thaumicenergistics.registries;

import java.util.ArrayList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumicenergistics.items.ItemMaterial;
import thaumicenergistics.items.ItemStorageBase;
import thaumicenergistics.registries.ResearchRegistry.ResearchTypes;
import appeng.api.AEApi;
import appeng.api.definitions.Blocks;
import appeng.api.definitions.Materials;
import appeng.api.definitions.Parts;
import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeRegistry
{
	public static IArcaneRecipe STORAGE_COMPONENT_1K;
	public static IArcaneRecipe STORAGE_COMPONENT_4K;
	public static IArcaneRecipe STORAGE_COMPONENT_16K;
	public static IArcaneRecipe STORAGE_COMPONENT_64K;
	public static IRecipe STORAGE_CASING;
	public static IRecipe STORAGE_CELL_1K_SHAPED;
	public static IRecipe STORAGE_CELL_4K_SHAPED;
	public static IRecipe STORAGE_CELL_16K_SHAPED;
	public static IRecipe STORAGE_CELL_64K_SHAPED;
	public static IRecipe STORAGE_CELL_1K_SHAPELESS;
	public static IRecipe STORAGE_CELL_4K_SHAPELESS;
	public static IRecipe STORAGE_CELL_16K_SHAPELESS;
	public static IRecipe STORAGE_CELL_64K_SHAPELESS;
	public static IArcaneRecipe MATERIAL_DIFFUSION_CORE;
	public static IArcaneRecipe MATERIAL_COALESCENCE_CORE;
	public static IArcaneRecipe PART_IMPORT_BUS;
	public static IArcaneRecipe PART_EXPORT_BUS;
	public static IArcaneRecipe PART_STORAGE_BUS;
	public static IArcaneRecipe PART_ESSENTIA_TERMINAL;
	public static IArcaneRecipe PART_ARCANE_TERMINAL;
	public static IArcaneRecipe PART_ESSENTIA_LEVEL_EMITTER;
	public static InfusionRecipe INFUSION_PROVIDER;
	public static InfusionRecipe ESSENTIA_PROVIDER;

	/**
	 * Register my recipes
	 */
	public static void registerRecipies()
	{

		// Cache the AE item collections
		Materials aeMaterials = AEApi.instance().materials();
		Blocks aeBlocks = AEApi.instance().blocks();
		Parts aeParts = AEApi.instance().parts();

		// Register materials
		RecipeRegistry.registerMaterials( aeMaterials );

		// Register ME cell components
		RecipeRegistry.registerComponents( aeMaterials, aeBlocks );

		// Register ME cells
		RecipeRegistry.registerMECells();

		// Register AE parts
		RecipeRegistry.registerParts( aeParts, aeMaterials );

		// Register the providers
		RecipeRegistry.registerProviders( aeBlocks );
	}

	private static void registerComponents( Materials aeMaterials, Blocks aeBlocks )
	{
		// Thaumcraft items
		ItemStack EtheralEssence = new ItemStack( ConfigItems.itemWispEssence );

		ItemStack SalisMundus = new ItemStack( ConfigItems.itemResource, 1, 14 );

		// AppEng items
		ItemStack LogicProcessor = aeMaterials.materialLogicProcessor.stack( 1 );

		ItemStack CalculationProcessor = aeMaterials.materialCalcProcessor.stack( 1 );

		ItemStack EngineeringProcessor = aeMaterials.materialEngProcessor.stack( 1 );

		ItemStack CertusQuartz = aeMaterials.materialCertusQuartzCrystal.stack( 1 );

		ItemStack ChargedCertusQuartz = aeMaterials.materialCertusQuartzCrystalCharged.stack( 1 );

		ItemStack PureCertusQuartz = aeMaterials.materialPureifiedCertusQuartzCrystal.stack( 1 );

		ItemStack QuartzGlass = aeBlocks.blockQuartzGlass.stack( 1 );

		// My items
		ItemStack EssentiaStorageComponent_1K = ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_1K );

		ItemStack EssentiaStorageComponent_4K = ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_4K );

		ItemStack EssentiaStorageComponent_16K = ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_16K );

		ItemStack EssentiaStorageComponent_64K = ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_64K );

		// Item Groups		
		ArrayList<ItemStack> GroupQuartz = new ArrayList<ItemStack>( 3 );
		GroupQuartz.add( CertusQuartz );
		GroupQuartz.add( ChargedCertusQuartz );
		GroupQuartz.add( PureCertusQuartz );

		// 1K Storage Component
		AspectList storage1kAspects = new AspectList();
		storage1kAspects.add( Aspect.FIRE, 3 );
		storage1kAspects.add( Aspect.ORDER, 1 );
		RecipeRegistry.STORAGE_COMPONENT_1K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaStorageComponent_1K,
			storage1kAspects, new Object[] { "EQE", "QPQ", "EQE", 'E', EtheralEssence, 'Q', CertusQuartz, 'P', LogicProcessor } );
		RecipeRegistry.replaceRecipeIngredientWithGroup( (ShapedArcaneRecipe)RecipeRegistry.STORAGE_COMPONENT_1K, CertusQuartz, GroupQuartz );

		// 4K Storage Component
		AspectList storage4kAspects = new AspectList();
		storage4kAspects.add( Aspect.FIRE, 3 );
		storage4kAspects.add( Aspect.ORDER, 2 );
		RecipeRegistry.STORAGE_COMPONENT_4K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaStorageComponent_4K,
			storage4kAspects, new Object[] { "EPE", "1G1", "E1E", 'E', EtheralEssence, '1', EssentiaStorageComponent_1K, 'P', CalculationProcessor,
							'G', QuartzGlass } );

		// 16K Storage Component
		AspectList storage16kAspects = new AspectList();
		storage16kAspects.add( Aspect.FIRE, 3 );
		storage16kAspects.add( Aspect.ORDER, 4 );
		RecipeRegistry.STORAGE_COMPONENT_16K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaStorageComponent_16K,
			storage16kAspects, new Object[] { "SPE", "4G4", "E4S", 'E', EtheralEssence, 'S', SalisMundus, '4', EssentiaStorageComponent_4K, 'P',
							EngineeringProcessor, 'G', QuartzGlass } );

		// 16K Storage Component
		AspectList storage64kAspects = new AspectList();
		storage64kAspects.add( Aspect.FIRE, 3 );
		storage64kAspects.add( Aspect.ORDER, 8 );
		RecipeRegistry.STORAGE_COMPONENT_64K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaStorageComponent_64K,
			storage64kAspects, new Object[] { "SPS", "6G6", "S6S", 'S', SalisMundus, '6', EssentiaStorageComponent_16K, 'P', EngineeringProcessor,
							'G', QuartzGlass } );
	}

	private static void registerMECells()
	{
		// Minecraft items
		ItemStack RedstoneDust = new ItemStack( (Item)Item.itemRegistry.getObject( "redstone" ) );

		// Thaumcraft items
		ItemStack WardedGlass = new ItemStack( ConfigBlocks.blockCosmeticOpaque, 1, 2 );

		ItemStack ThaumiumIngot = new ItemStack( ConfigItems.itemResource, 1, 2 );

		// My items
		ItemStack EssentiaStorageCasing = ItemEnum.STORAGE_CASING.getItemStackWithSize( 1 );

		ItemStack EssentiaStorageCell_1K = ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_1K );

		ItemStack EssentiaStorageCell_4K = ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_4K );

		ItemStack EssentiaStorageCell_16K = ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_16K );

		ItemStack EssentiaStorageCell_64K = ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_64K );

		ItemStack EssentiaStorageComponent_1K = RecipeRegistry.STORAGE_COMPONENT_1K.getRecipeOutput();

		ItemStack EssentiaStorageComponent_4K = RecipeRegistry.STORAGE_COMPONENT_4K.getRecipeOutput();

		ItemStack EssentiaStorageComponent_16K = RecipeRegistry.STORAGE_COMPONENT_16K.getRecipeOutput();

		ItemStack EssentiaStorageComponent_64K = RecipeRegistry.STORAGE_COMPONENT_64K.getRecipeOutput();

		// Storage Casing
		RecipeRegistry.STORAGE_CASING = new ShapedOreRecipe( EssentiaStorageCasing, false, new Object[] { "WRW", "R R", "TTT", 'W', WardedGlass, 'R',
						RedstoneDust, 'T', ThaumiumIngot } );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CASING );

		// 1K Storage Cell
		RecipeRegistry.STORAGE_CELL_1K_SHAPED = new ShapedOreRecipe( EssentiaStorageCell_1K, false, new Object[] { "WRW", "RCR", "TTT", 'W',
						WardedGlass, 'R', RedstoneDust, 'T', ThaumiumIngot, 'C', EssentiaStorageComponent_1K } );
		RecipeRegistry.STORAGE_CELL_1K_SHAPELESS = new ShapelessOreRecipe( EssentiaStorageCell_1K, EssentiaStorageComponent_1K, EssentiaStorageCasing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_1K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_1K_SHAPELESS );

		// 4K Storage Cell
		RecipeRegistry.STORAGE_CELL_4K_SHAPED = new ShapedOreRecipe( EssentiaStorageCell_4K, false, new Object[] { "WRW", "RCR", "TTT", 'W',
						WardedGlass, 'R', RedstoneDust, 'T', ThaumiumIngot, 'C', EssentiaStorageComponent_4K } );
		RecipeRegistry.STORAGE_CELL_4K_SHAPELESS = new ShapelessOreRecipe( EssentiaStorageCell_4K, EssentiaStorageComponent_4K, EssentiaStorageCasing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_4K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_4K_SHAPELESS );

		// 16K Storage Cell
		RecipeRegistry.STORAGE_CELL_16K_SHAPED = new ShapedOreRecipe( EssentiaStorageCell_16K, false, new Object[] { "WRW", "RCR", "TTT", 'W',
						WardedGlass, 'R', RedstoneDust, 'T', ThaumiumIngot, 'C', EssentiaStorageComponent_16K } );
		RecipeRegistry.STORAGE_CELL_16K_SHAPELESS = new ShapelessOreRecipe( EssentiaStorageCell_16K, EssentiaStorageComponent_16K,
						EssentiaStorageCasing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_16K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_16K_SHAPELESS );

		// 64K Storage Cell
		RecipeRegistry.STORAGE_CELL_64K_SHAPED = new ShapedOreRecipe( EssentiaStorageCell_64K, false, new Object[] { "WRW", "RCR", "TTT", 'W',
						WardedGlass, 'R', RedstoneDust, 'T', ThaumiumIngot, 'C', EssentiaStorageComponent_64K } );
		RecipeRegistry.STORAGE_CELL_64K_SHAPELESS = new ShapelessOreRecipe( EssentiaStorageCell_64K, EssentiaStorageComponent_64K,
						EssentiaStorageCasing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_64K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_64K_SHAPELESS );
	}

	private static void registerMaterials( Materials aeMaterials )
	{
		// Thaumcraft items
		ItemStack EntropyShard = new ItemStack( ConfigItems.itemShard, 1, 5 );

		ItemStack OrderShard = new ItemStack( ConfigItems.itemShard, 1, 4 );

		ItemStack QuickSilver = new ItemStack( ConfigItems.itemResource, 1, 3 );

		// AppEng items
		ItemStack FormationCore = aeMaterials.materialFormationCore.stack( 1 );

		ItemStack AnnihilationCore = aeMaterials.materialAnnihilationCore.stack( 1 );

		// My items
		ItemStack DiffusionCore = ItemMaterial.MaterialTypes.DIFFUSION_CORE.getItemStack();

		ItemStack CoalescenceCore = ItemMaterial.MaterialTypes.COALESCENCE_CORE.getItemStack();

		// Coalescence Core
		AspectList coalescenceAspects = new AspectList();
		coalescenceAspects.add( Aspect.WATER, 2 );
		coalescenceAspects.add( Aspect.ORDER, 2 );
		RecipeRegistry.MATERIAL_COALESCENCE_CORE = ThaumcraftApi.addShapelessArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.CORES.getKey(),
			CoalescenceCore, coalescenceAspects, QuickSilver, OrderShard, FormationCore );

		// Diffusion Core
		AspectList diffusionAspects = new AspectList();
		diffusionAspects.add( Aspect.WATER, 2 );
		diffusionAspects.add( Aspect.ENTROPY, 2 );
		RecipeRegistry.MATERIAL_DIFFUSION_CORE = ThaumcraftApi.addShapelessArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.CORES.getKey(),
			DiffusionCore, diffusionAspects, QuickSilver, EntropyShard, AnnihilationCore );
	}

	private static void registerParts( Parts aeParts, Materials aeMaterials )
	{
		// Minecraft items
		String IronIngot = "ingotIron";
		
		ItemStack RedstoneTorch = new ItemStack( net.minecraft.init.Blocks.redstone_torch );

		// AppEng items
		ItemStack DarkIlluminatedPanel = aeParts.partDarkMonitor.stack( 1 );

		ItemStack IlluminatedPanel = aeParts.partSemiDarkMonitor.stack( 1 );

		ItemStack BrightIlluminatedPanel = aeParts.partMonitor.stack( 1 );

		ItemStack LogicProcessor = aeMaterials.materialLogicProcessor.stack( 1 );

		ItemStack CalculationProcessor = aeMaterials.materialCalcProcessor.stack( 1 );

		ItemStack METerminal = aeParts.partTerminal.stack( 1 );

		// Thaumcraft items
		ItemStack FilterTube = new ItemStack( ConfigBlocks.blockTube, 1, 3 );

		ItemStack WardedJar = new ItemStack( ConfigBlocks.blockJar, 1, 0 );

		ItemStack WardedGlass = new ItemStack( ConfigBlocks.blockCosmeticOpaque, 1, 2 );

		ItemStack ArcaneWorkTable = new ItemStack( ConfigBlocks.blockTable, 1, 15 );

		ItemStack AspectFilter = new ItemStack( ConfigItems.itemResource, 1, 8 );
		
		ItemStack SalisMundus = new ItemStack( ConfigItems.itemResource, 1, 14 );

		// My items
		ItemStack DiffusionCore = RecipeRegistry.MATERIAL_DIFFUSION_CORE.getRecipeOutput();

		ItemStack CoalescencenCore = RecipeRegistry.MATERIAL_COALESCENCE_CORE.getRecipeOutput();

		ItemStack EssentiaImportBus = AEPartsEnum.EssentiaImportBus.getStack();

		ItemStack EssentiaExportBus = AEPartsEnum.EssentiaExportBus.getStack();

		ItemStack EssentiaStorageBus = AEPartsEnum.EssentiaStorageBus.getStack();

		ItemStack EssentiaTerminal = AEPartsEnum.EssentiaTerminal.getStack();

		ItemStack ArcaneCraftingTerminal = AEPartsEnum.ArcaneCraftingTerminal.getStack();
		
		ItemStack EssentiaLevelEmitter = AEPartsEnum.EssentiaLevelEmitter.getStack();

		// Item Groups		
		ArrayList<ItemStack> GroupPanel = new ArrayList<ItemStack>( 3 );
		GroupPanel.add( DarkIlluminatedPanel );
		GroupPanel.add( IlluminatedPanel );
		GroupPanel.add( BrightIlluminatedPanel );

		// Import Bus
		AspectList ioAspectList = new AspectList();
		ioAspectList.add( Aspect.FIRE, 2 );
		ioAspectList.add( Aspect.EARTH, 2 );
		ioAspectList.add( Aspect.WATER, 1 );
		RecipeRegistry.PART_IMPORT_BUS = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IO.getKey(), EssentiaImportBus,
			ioAspectList, new Object[] { "JDJ", "IFI", 'J', WardedJar, 'D', DiffusionCore, 'I', IronIngot, 'F', FilterTube } );

		// Export Bus
		RecipeRegistry.PART_EXPORT_BUS = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IO.getKey(), EssentiaExportBus,
			ioAspectList, new Object[] { "JCJ", "IFI", 'J', WardedJar, 'C', CoalescencenCore, 'I', IronIngot, 'F', FilterTube } );

		// Storage Bus
		AspectList storageAspectList = new AspectList();
		storageAspectList.add( Aspect.FIRE, 3 );
		storageAspectList.add( Aspect.EARTH, 3 );
		storageAspectList.add( Aspect.WATER, 1 );
		RecipeRegistry.PART_STORAGE_BUS = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IO.getKey(), EssentiaStorageBus,
			storageAspectList, new Object[] { true, "DFC", "IWI", 'D', DiffusionCore, 'C', CoalescencenCore, 'I', IronIngot, 'F', FilterTube, 'W',
							WardedGlass } );

		// Essentia Terminal
		AspectList etAspectList = new AspectList();
		etAspectList.add( Aspect.WATER, 5 );
		etAspectList.add( Aspect.ORDER, 2 );
		etAspectList.add( Aspect.FIRE, 1 );
		RecipeRegistry.PART_ESSENTIA_TERMINAL = ThaumcraftApi.addShapelessArcaneCraftingRecipe(
			ResearchRegistry.ResearchTypes.ESSENTIATERMINAL.getKey(), EssentiaTerminal, etAspectList, IlluminatedPanel, DiffusionCore,
			CoalescencenCore, LogicProcessor, AspectFilter );
		RecipeRegistry.replaceRecipeIngredientWithGroup( (ShapelessArcaneRecipe)RecipeRegistry.PART_ESSENTIA_TERMINAL, IlluminatedPanel, GroupPanel );

		// Arcane Crafting Terminal
		AspectList actAspectList = new AspectList();
		actAspectList.add( Aspect.AIR, 10 );
		actAspectList.add( Aspect.EARTH, 10 );
		actAspectList.add( Aspect.ENTROPY, 10 );
		actAspectList.add( Aspect.FIRE, 10 );
		actAspectList.add( Aspect.ORDER, 10 );
		actAspectList.add( Aspect.WATER, 10 );
		RecipeRegistry.PART_ARCANE_TERMINAL = ThaumcraftApi.addShapelessArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.ARCANETERMINAL.getKey(),
			ArcaneCraftingTerminal, actAspectList, METerminal, ArcaneWorkTable, CalculationProcessor );
		
		// Essentia level emitter
		AspectList emitterAspectList = new AspectList();
		emitterAspectList.add( Aspect.FIRE, 4 );
		EssentiaLevelEmitter.stackSize = 4;
		RecipeRegistry.PART_ESSENTIA_LEVEL_EMITTER = ThaumcraftApi.addShapelessArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.ESSENTIATERMINAL.getKey(),
			EssentiaLevelEmitter, emitterAspectList, CalculationProcessor, RedstoneTorch, SalisMundus );
	}

	private static void registerProviders( Blocks aeBlocks )
	{
		// Thaumcraft items
		ItemStack FilteredPipe = new ItemStack( ConfigBlocks.blockTube, 1, 3 );

		ItemStack EssentiaMirrorOrJar = ( Config.allowMirrors ? new ItemStack( ConfigBlocks.blockMirror, 1, 6 ) : new ItemStack( ConfigBlocks.blockJar, 1, 0 ) );

		ItemStack SalisMundus = new ItemStack( ConfigItems.itemResource, 1, 14 );

		ItemStack WaterShard = new ItemStack( ConfigItems.itemShard, 1, 2 );

		ItemStack AerShard = new ItemStack( ConfigItems.itemShard, 1, 0 );

		// AE Items
		ItemStack MEInterface = aeBlocks.blockInterface.stack( 1 );

		// My Items
		ItemStack CoalescenceCore = ItemMaterial.MaterialTypes.COALESCENCE_CORE.getItemStack();

		ItemStack InfusionProvider = new ItemStack( BlockEnum.INFUSION_PROVIDER.getBlock(), 1 );

		ItemStack EssentiaProvider = new ItemStack( BlockEnum.ESSENTIA_PROVIDER.getBlock(), 1 );

		// Set required aspects for infusion
		AspectList infusionProviderList = new AspectList();
		infusionProviderList.add( Aspect.MECHANISM, 64 );
		infusionProviderList.add( Aspect.MAGIC, 32 );
		infusionProviderList.add( Aspect.ORDER, 32 );
		infusionProviderList.add( Aspect.EXCHANGE, 16 );

		// Infusion provider recipe items
		ItemStack[] infusionProviderRecipeItems = { EssentiaMirrorOrJar, SalisMundus, CoalescenceCore, AerShard, EssentiaMirrorOrJar, SalisMundus,
						CoalescenceCore, AerShard };

		// Create the infusion provider recipe
		RecipeRegistry.INFUSION_PROVIDER = ThaumcraftApi.addInfusionCraftingRecipe( ResearchRegistry.ResearchTypes.INFUSIONPROVIDER.getKey(),
			InfusionProvider, 4, infusionProviderList, MEInterface, infusionProviderRecipeItems );

		// Set required aspects for infusion
		AspectList essentiaProviderList = new AspectList();
		essentiaProviderList.add( Aspect.MECHANISM, 64 );
		essentiaProviderList.add( Aspect.MAGIC, 32 );
		essentiaProviderList.add( Aspect.ORDER, 32 );
		essentiaProviderList.add( Aspect.EXCHANGE, 16 );

		// Essentia provider recipe items
		ItemStack[] essentiaProviderRecipeItems = { FilteredPipe, SalisMundus, CoalescenceCore, WaterShard, FilteredPipe, SalisMundus, CoalescenceCore,
						WaterShard };

		// Create the essentia provider recipe
		RecipeRegistry.ESSENTIA_PROVIDER = ThaumcraftApi.addInfusionCraftingRecipe( ResearchRegistry.ResearchTypes.ESSENTIAPROVIDER.getKey(),
			EssentiaProvider, 3, essentiaProviderList, MEInterface, essentiaProviderRecipeItems );

	}

	private static void replaceRecipeIngredientWithGroup( ShapedArcaneRecipe recipe, ItemStack ingredient, ArrayList<ItemStack> group )
	{
		// Get the input
		Object[] input = recipe.getInput();

		// For every listed slot change the input to the group
		for( int index = 0; index < input.length; index++ )
		{
			Object slot = input[index];

			// Is this slot an itemstack?
			if( slot instanceof ItemStack )
			{
				// Does it match the stack to replace?
				if( ingredient.isItemEqual( (ItemStack)slot ) )
				{
					// Replace it
					input[index] = group;
				}
			}
		}
	}

	private static void replaceRecipeIngredientWithGroup( ShapelessArcaneRecipe recipe, ItemStack ingredient, ArrayList<ItemStack> group )
	{
		// Get the input
		ArrayList<Object> input = recipe.getInput();

		// For every listed slot change the input to the group
		for( int index = 0; index < input.size(); index++ )
		{
			Object slot = input.get( index );

			// Is this slot an itemstack?
			if( slot instanceof ItemStack )
			{
				// Does it match the stack to replace?
				if( ingredient.isItemEqual( (ItemStack)slot ) )
				{
					// Replace it
					input.set( index, group );
				}
			}
		}
	}

}
