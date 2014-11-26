package thaumicenergistics.registries;

import java.util.ArrayList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.Items;
import thaumicenergistics.api.TEApi;
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
	public static IArcaneRecipe MATERIAL_IRON_GEAR;
	public static IArcaneRecipe PART_IMPORT_BUS;
	public static IArcaneRecipe PART_EXPORT_BUS;
	public static IArcaneRecipe PART_STORAGE_BUS;
	public static IArcaneRecipe PART_ESSENTIA_TERMINAL;
	public static IArcaneRecipe PART_ARCANE_TERMINAL;
	public static IArcaneRecipe PART_ESSENTIA_LEVEL_EMITTER;
	public static IArcaneRecipe PART_VIS_INTERFACE;
	public static InfusionRecipe BLOCK_INFUSION_PROVIDER;
	public static InfusionRecipe BLOCK_ESSENTIA_PROVIDER;
	public static IArcaneRecipe BLOCK_IRONGEARBOX;
	public static CrucibleRecipe BLOCK_THAUMIUMGEARBOX;
	public static CrucibleRecipe DUPE_CERTUS;
	public static CrucibleRecipe DUPE_NETHER_QUARTZ;
	public static IRecipe BLOCK_CELL_WORKBENCH;

	private static void registerComponents( final Materials aeMaterials, final Blocks aeBlocks, final Items teItems )
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

		ItemStack CellWorkbench = aeBlocks.blockCellWorkbench.stack( 1 );

		// My items
		ItemStack EssentiaComp_1k = teItems.EssentiaStorageComponent_1k.getStack();

		ItemStack EssentiaComp_4k = teItems.EssentiaStorageComponent_4k.getStack();

		ItemStack EssentiaComp_16k = teItems.EssentiaStorageComponent_16k.getStack();

		ItemStack EssentiaComp_64k = teItems.EssentiaStorageComponent_64k.getStack();

		ItemStack EssentiaCellWorkbench = TEApi.instance().blocks().EssentiaCellWorkbench.getStack();

		// Item Groups		
		ArrayList<ItemStack> GroupQuartz = new ArrayList<ItemStack>( 3 );
		GroupQuartz.add( CertusQuartz );
		GroupQuartz.add( ChargedCertusQuartz );
		GroupQuartz.add( PureCertusQuartz );

		// 1K Storage Component
		AspectList storage1kAspects = new AspectList();
		storage1kAspects.add( Aspect.FIRE, 3 );
		storage1kAspects.add( Aspect.ORDER, 1 );
		RecipeRegistry.STORAGE_COMPONENT_1K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaComp_1k,
			storage1kAspects, new Object[] { "EQE", "QPQ", "EQE", 'E', EtheralEssence, 'Q', CertusQuartz, 'P', LogicProcessor } );
		RecipeRegistry.replaceRecipeIngredientWithGroup( (ShapedArcaneRecipe)RecipeRegistry.STORAGE_COMPONENT_1K, CertusQuartz, GroupQuartz );

		// 4K Storage Component
		AspectList storage4kAspects = new AspectList();
		storage4kAspects.add( Aspect.FIRE, 3 );
		storage4kAspects.add( Aspect.ORDER, 2 );
		RecipeRegistry.STORAGE_COMPONENT_4K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaComp_4k,
			storage4kAspects, new Object[] { "EPE", "1G1", "E1E", 'E', EtheralEssence, '1', EssentiaComp_1k, 'P', CalculationProcessor, 'G',
							QuartzGlass } );

		// 16K Storage Component
		AspectList storage16kAspects = new AspectList();
		storage16kAspects.add( Aspect.FIRE, 3 );
		storage16kAspects.add( Aspect.ORDER, 4 );
		RecipeRegistry.STORAGE_COMPONENT_16K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaComp_16k,
			storage16kAspects, new Object[] { "SPE", "4G4", "E4S", 'E', EtheralEssence, 'S', SalisMundus, '4', EssentiaComp_4k, 'P',
							EngineeringProcessor, 'G', QuartzGlass } );

		// 16K Storage Component
		AspectList storage64kAspects = new AspectList();
		storage64kAspects.add( Aspect.FIRE, 3 );
		storage64kAspects.add( Aspect.ORDER, 8 );
		RecipeRegistry.STORAGE_COMPONENT_64K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaComp_64k,
			storage64kAspects, new Object[] { "SPS", "6G6", "S6S", 'S', SalisMundus, '6', EssentiaComp_16k, 'P', EngineeringProcessor, 'G',
							QuartzGlass } );

		// Workbench
		RecipeRegistry.BLOCK_CELL_WORKBENCH = new ShapelessOreRecipe( EssentiaCellWorkbench, new Object[] { EssentiaComp_1k, CellWorkbench } );
		GameRegistry.addRecipe( RecipeRegistry.BLOCK_CELL_WORKBENCH );
	}

	private static void registerGearbox( final Blocks aeBlocks, final Items teItems )
	{
		// Minecraft items
		String Cobblestone = "cobblestone";

		// My items
		String IronGear = "gearIron";
		ItemStack IronGearBox = TEApi.instance().blocks().IronGearBox.getStack();
		ItemStack ThaumiumGearBox = TEApi.instance().blocks().ThaumiumGearBox.getStack();

		// Iron Gear Box
		AspectList igbAspects = new AspectList();
		igbAspects.add( Aspect.AIR, 2 );
		igbAspects.add( Aspect.ORDER, 2 );
		RecipeRegistry.BLOCK_IRONGEARBOX = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IRONGEARBOX.getKey(), IronGearBox,
			igbAspects, new Object[] { "SGS", "GGG", "SGS", 'S', Cobblestone, 'G', IronGear } );

		// Thaumium Gear Box
		AspectList tgbAspects = new AspectList();
		tgbAspects.add( Aspect.METAL, 16 );
		tgbAspects.add( Aspect.MAGIC, 16 );
		RecipeRegistry.BLOCK_THAUMIUMGEARBOX = ThaumcraftApi.addCrucibleRecipe( ResearchRegistry.ResearchTypes.THAUMIUMGEARBOX.getKey(),
			ThaumiumGearBox, IronGearBox, tgbAspects );

	}

	private static void registerMaterials( final Materials aeMaterials, final Items teItems )
	{
		// Minecraft items
		String IronIngot = "ingotIron";

		String NetherQuartz1 = "gemQuartz";
		ItemStack NetherQuartz2 = OreDictionary.getOres( NetherQuartz1 ).get( 0 ).copy();
		NetherQuartz2.stackSize = 2;

		// Thaumcraft items
		ItemStack EntropyShard = new ItemStack( ConfigItems.itemShard, 1, 5 );

		ItemStack OrderShard = new ItemStack( ConfigItems.itemShard, 1, 4 );

		ItemStack QuickSilver = new ItemStack( ConfigItems.itemResource, 1, 3 );

		// AppEng items
		ItemStack FormationCore = aeMaterials.materialFormationCore.stack( 1 );

		ItemStack AnnihilationCore = aeMaterials.materialAnnihilationCore.stack( 1 );

		String WoodGear = "gearWood";

		String Certus1 = "crystalCertusQuartz";
		ItemStack Certus2 = OreDictionary.getOres( Certus1 ).get( 0 ).copy();
		Certus2.stackSize = 2;

		// My items
		ItemStack DiffusionCore = teItems.DiffusionCore.getStack();

		ItemStack CoalescenceCore = teItems.CoalescenceCore.getStack();

		ItemStack IronGear = teItems.IronGear.getStack();

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

		// Iron Gear
		AspectList ironGearAspects = new AspectList();
		ironGearAspects.add( Aspect.EARTH, 1 );
		ironGearAspects.add( Aspect.FIRE, 1 );
		RecipeRegistry.MATERIAL_IRON_GEAR = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IRONGEARBOX.getKey(), IronGear,
			ironGearAspects, new Object[] { " I ", " W ", "I I", 'I', IronIngot, 'W', WoodGear } );

		// Quartz duplication
		if( ThaumicEnergistics.config.allowedToDuplicateCertusQuartz() )
		{
			// Certus Quartz
			AspectList certusAspects = new AspectList();
			certusAspects.add( Aspect.CRYSTAL, 4 );
			certusAspects.add( Aspect.WATER, 2 );
			RecipeRegistry.DUPE_CERTUS = ThaumcraftApi.addCrucibleRecipe( ResearchRegistry.ResearchTypes.CERTUSDUPE.getKey(), Certus2, Certus1,
				certusAspects );

			// Nether Quartz
			AspectList nQAspects = new AspectList();
			nQAspects.add( Aspect.CRYSTAL, 4 );
			nQAspects.add( Aspect.WATER, 2 );
			nQAspects.add( Aspect.ENERGY, 2 );
			RecipeRegistry.DUPE_NETHER_QUARTZ = ThaumcraftApi.addCrucibleRecipe( ResearchRegistry.ResearchTypes.CERTUSDUPE.getKey(), NetherQuartz2,
				NetherQuartz1, nQAspects );
		}
	}

	private static void registerMECells( final Items teItems )
	{
		// Minecraft items
		ItemStack RedstoneDust = new ItemStack( (Item)Item.itemRegistry.getObject( "redstone" ) );

		// Thaumcraft items
		Object WardedGlass;
		if( !Config.wardedStone )
		{
			WardedGlass = new ItemStack( ConfigBlocks.blockCosmeticOpaque, 1, 2 );
		}
		else
		{
			WardedGlass = "blockGlass";
		}

		ItemStack ThaumiumIngot = new ItemStack( ConfigItems.itemResource, 1, 2 );

		// My items
		ItemStack EssentiaCell_Casing = teItems.EssentiaCell_Casing.getStack();

		ItemStack EssentiaCell_1k = teItems.EssentiaCell_1k.getStack();

		ItemStack EssentiaCell_4k = teItems.EssentiaCell_4k.getStack();

		ItemStack EssentiaCell_16k = teItems.EssentiaCell_16k.getStack();

		ItemStack EssentiaCell_64k = teItems.EssentiaCell_64k.getStack();

		ItemStack EssentiaStorageComponent_1k = teItems.EssentiaStorageComponent_1k.getStack();

		ItemStack EssentiaStorageComponent_4k = teItems.EssentiaStorageComponent_4k.getStack();

		ItemStack EssentiaStorageComponent_16k = teItems.EssentiaStorageComponent_16k.getStack();

		ItemStack EssentiaStorageComponent_64k = teItems.EssentiaStorageComponent_64k.getStack();

		// Storage Casing
		RecipeRegistry.STORAGE_CASING = new ShapedOreRecipe( EssentiaCell_Casing, false, new Object[] { "WRW", "R R", "TTT", 'W', WardedGlass, 'R',
						RedstoneDust, 'T', ThaumiumIngot } );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CASING );

		// 1K Storage Cell
		RecipeRegistry.STORAGE_CELL_1K_SHAPED = new ShapedOreRecipe( EssentiaCell_1k, false, new Object[] { "WRW", "RCR", "TTT", 'W', WardedGlass,
						'R', RedstoneDust, 'T', ThaumiumIngot, 'C', EssentiaStorageComponent_1k } );
		RecipeRegistry.STORAGE_CELL_1K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_1k, EssentiaStorageComponent_1k, EssentiaCell_Casing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_1K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_1K_SHAPELESS );

		// 4K Storage Cell
		RecipeRegistry.STORAGE_CELL_4K_SHAPED = new ShapedOreRecipe( EssentiaCell_4k, false, new Object[] { "WRW", "RCR", "TTT", 'W', WardedGlass,
						'R', RedstoneDust, 'T', ThaumiumIngot, 'C', EssentiaStorageComponent_4k } );
		RecipeRegistry.STORAGE_CELL_4K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_4k, EssentiaStorageComponent_4k, EssentiaCell_Casing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_4K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_4K_SHAPELESS );

		// 16K Storage Cell
		RecipeRegistry.STORAGE_CELL_16K_SHAPED = new ShapedOreRecipe( EssentiaCell_16k, false, new Object[] { "WRW", "RCR", "TTT", 'W', WardedGlass,
						'R', RedstoneDust, 'T', ThaumiumIngot, 'C', EssentiaStorageComponent_16k } );
		RecipeRegistry.STORAGE_CELL_16K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_16k, EssentiaStorageComponent_16k, EssentiaCell_Casing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_16K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_16K_SHAPELESS );

		// 64K Storage Cell
		RecipeRegistry.STORAGE_CELL_64K_SHAPED = new ShapedOreRecipe( EssentiaCell_64k, false, new Object[] { "WRW", "RCR", "TTT", 'W', WardedGlass,
						'R', RedstoneDust, 'T', ThaumiumIngot, 'C', EssentiaStorageComponent_64k } );
		RecipeRegistry.STORAGE_CELL_64K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_64k, EssentiaStorageComponent_64k, EssentiaCell_Casing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_64K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_64K_SHAPELESS );
	}

	private static void registerParts( final Parts aeParts, final Materials aeMaterials, final Items teItems )
	{
		// Minecraft items
		String IronIngot = "ingotIron";

		ItemStack RedstoneTorch = new ItemStack( net.minecraft.init.Blocks.redstone_torch );

		// AppEng items
		String IlluminatedPanel = "itemIlluminatedPanel";

		ItemStack LogicProcessor = aeMaterials.materialLogicProcessor.stack( 1 );

		ItemStack CalculationProcessor = aeMaterials.materialCalcProcessor.stack( 1 );

		ItemStack METerminal = aeParts.partTerminal.stack( 1 );

		ItemStack MEP2P = aeParts.partP2PTunnelME.stack( 1 );

		// Thaumcraft items
		ItemStack FilterTube = new ItemStack( ConfigBlocks.blockTube, 1, 3 );

		ItemStack WardedJar = new ItemStack( ConfigBlocks.blockJar, 1, 0 );

		ItemStack WardedGlass = new ItemStack( ConfigBlocks.blockCosmeticOpaque, 1, 2 );

		ItemStack ArcaneWorkTable = new ItemStack( ConfigBlocks.blockTable, 1, 15 );

		ItemStack AspectFilter = new ItemStack( ConfigItems.itemResource, 1, 8 );

		ItemStack SalisMundus = new ItemStack( ConfigItems.itemResource, 1, 14 );

		ItemStack BallanceShard = new ItemStack( ConfigItems.itemShard, 1, 6 );

		// My items
		thaumicenergistics.api.Parts teParts = TEApi.instance().parts();

		ItemStack DiffusionCore = teItems.DiffusionCore.getStack();

		ItemStack CoalescencenCore = teItems.CoalescenceCore.getStack();

		ItemStack EssentiaImportBus = teParts.Essentia_ImportBus.getStack();

		ItemStack EssentiaExportBus = teParts.Essentia_ExportBus.getStack();

		ItemStack EssentiaStorageBus = teParts.Essentia_StorageBus.getStack();

		ItemStack EssentiaTerminal = teParts.Essentia_Terminal.getStack();

		ItemStack ArcaneCraftingTerminal = teParts.ArcaneCrafting_Terminal.getStack();

		ItemStack EssentiaLevelEmitter = teParts.Essentia_LevelEmitter.getStack();

		ItemStack VisInterface = teParts.VisRelay_Interface.getStack();

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
		RecipeRegistry.PART_ESSENTIA_LEVEL_EMITTER = ThaumcraftApi.addShapelessArcaneCraftingRecipe(
			ResearchRegistry.ResearchTypes.ESSENTIATERMINAL.getKey(), EssentiaLevelEmitter, emitterAspectList, CalculationProcessor, RedstoneTorch,
			SalisMundus );

		// Vis interface
		AspectList visInterfaceAspectList = new AspectList();
		visInterfaceAspectList.add( Aspect.AIR, 2 );
		visInterfaceAspectList.add( Aspect.EARTH, 2 );
		visInterfaceAspectList.add( Aspect.ENTROPY, 2 );
		visInterfaceAspectList.add( Aspect.FIRE, 2 );
		visInterfaceAspectList.add( Aspect.ORDER, 2 );
		visInterfaceAspectList.add( Aspect.WATER, 2 );
		RecipeRegistry.PART_VIS_INTERFACE = ThaumcraftApi.addShapelessArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.VISINTERFACE.getKey(),
			VisInterface, visInterfaceAspectList, BallanceShard, MEP2P );

	}

	private static void registerProviders( final Blocks aeBlocks, final Items teItems )
	{
		// Thaumcraft items
		ItemStack FilteredPipe = new ItemStack( ConfigBlocks.blockTube, 1, 3 );

		ItemStack EssentiaMirrorOrJar = ( Config.allowMirrors ? new ItemStack( ConfigBlocks.blockMirror, 1, 6 ) : new ItemStack(
						ConfigBlocks.blockJar, 1, 0 ) );

		ItemStack SalisMundus = new ItemStack( ConfigItems.itemResource, 1, 14 );

		ItemStack WaterShard = new ItemStack( ConfigItems.itemShard, 1, 2 );

		ItemStack AerShard = new ItemStack( ConfigItems.itemShard, 1, 0 );

		// AE Items
		ItemStack MEInterface = aeBlocks.blockInterface.stack( 1 );

		// My Items
		ItemStack CoalescenceCore = teItems.CoalescenceCore.getStack();

		ItemStack InfusionProvider = TEApi.instance().blocks().InfusionProvider.getStack();

		ItemStack EssentiaProvider = TEApi.instance().blocks().EssentiaProvider.getStack();

		if( ThaumicEnergistics.config.allowedToCraftInfusionProvider() )
		{
			// Set required aspects for infusion provider
			AspectList infusionProviderList = new AspectList();
			infusionProviderList.add( Aspect.MECHANISM, 64 );
			infusionProviderList.add( Aspect.MAGIC, 32 );
			infusionProviderList.add( Aspect.ORDER, 32 );
			infusionProviderList.add( Aspect.EXCHANGE, 16 );

			// Infusion provider recipe items
			ItemStack[] infusionProviderRecipeItems = { EssentiaMirrorOrJar, SalisMundus, CoalescenceCore, AerShard, EssentiaMirrorOrJar,
							SalisMundus, CoalescenceCore, AerShard };

			// Create the infusion provider recipe
			RecipeRegistry.BLOCK_INFUSION_PROVIDER = ThaumcraftApi.addInfusionCraftingRecipe(
				ResearchRegistry.ResearchTypes.INFUSIONPROVIDER.getKey(), InfusionProvider, 4, infusionProviderList, MEInterface,
				infusionProviderRecipeItems );
		}

		if( ThaumicEnergistics.config.allowedToCraftEssentiaProvider() )
		{
			// Set required aspects for essentia provider
			AspectList essentiaProviderList = new AspectList();
			essentiaProviderList.add( Aspect.MECHANISM, 64 );
			essentiaProviderList.add( Aspect.MAGIC, 32 );
			essentiaProviderList.add( Aspect.ORDER, 32 );
			essentiaProviderList.add( Aspect.EXCHANGE, 16 );

			// Essentia provider recipe items
			ItemStack[] essentiaProviderRecipeItems = { FilteredPipe, SalisMundus, CoalescenceCore, WaterShard, FilteredPipe, SalisMundus,
							CoalescenceCore, WaterShard };

			// Create the essentia provider recipe
			RecipeRegistry.BLOCK_ESSENTIA_PROVIDER = ThaumcraftApi.addInfusionCraftingRecipe(
				ResearchRegistry.ResearchTypes.ESSENTIAPROVIDER.getKey(), EssentiaProvider, 3, essentiaProviderList, MEInterface,
				essentiaProviderRecipeItems );
		}

	}

	private static void replaceRecipeIngredientWithGroup( final ShapedArcaneRecipe recipe, final ItemStack ingredient,
															final ArrayList<ItemStack> group )
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

	/**
	 * Register my recipes
	 */
	public static void registerRecipies()
	{

		// Cache the AE item collections
		Materials aeMaterials = AEApi.instance().materials();
		Blocks aeBlocks = AEApi.instance().blocks();
		Parts aeParts = AEApi.instance().parts();

		// Cache my API
		Items teItems = TEApi.instance().items();

		// Register materials
		RecipeRegistry.registerMaterials( aeMaterials, teItems );

		// Register ME cell components
		RecipeRegistry.registerComponents( aeMaterials, aeBlocks, teItems );

		// Register ME cells
		RecipeRegistry.registerMECells( teItems );

		// Register AE parts
		RecipeRegistry.registerParts( aeParts, aeMaterials, teItems );

		// Register the providers
		RecipeRegistry.registerProviders( aeBlocks, teItems );

		// Register the gearboxes
		RecipeRegistry.registerGearbox( aeBlocks, teItems );
	}

}
