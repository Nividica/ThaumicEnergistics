package thaumicenergistics.registries;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.Config;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.IConfig;
import thaumicenergistics.api.TEApi;
import thaumicenergistics.integration.tc.PseudoResearchItem;
import thaumicenergistics.items.ItemMaterial;
import appeng.api.AEApi;

public class ResearchRegistry
{
	// Fake research, serves as placeholder for existing research
	private static enum PseudoResearchTypes
	{
			DISTILESSENTIA ("DISTILESSENTIA", "ALCHEMY", -2, 0),
			TUBEFILTER ("TUBEFILTER", "ALCHEMY", -3, 0),
			MIRROR ("MIRROR", "ARTIFICE", -4, 0),
			JAR ("JARLABEL", "ALCHEMY", -4, 0),
			INFUSION ("INFUSION", "ARTIFICE", -6, 0),
			VISPOWER ("VISPOWER", "THAUMATURGY", 2, -1),
			COREUSE ("COREUSE", "GOLEMANCY", 4, 3),
			DUPE ("ALCHEMICALDUPLICATION", "ALCHEMY", -6, -7);

		private String realResearchKey;
		private String realResearchCategory;
		private int column;
		private int row;
		private PseudoResearchItem researchItem;

		private PseudoResearchTypes( final String key, final String cat, final int column, final int row )
		{
			this.realResearchCategory = cat;
			this.realResearchKey = key;
			this.column = column;
			this.row = row;
		}

		public String getKey()
		{
			return ThaumicEnergistics.MOD_ID + ".Pseudo." + this.realResearchKey;
		}

		public void registerPsudeoResearch()
		{
			this.researchItem = PseudoResearchItem.newPseudo( this.getKey(), TERESEARCH_TAB, this.realResearchKey, this.realResearchCategory,
				this.column, this.row );
			this.researchItem.registerResearchItem();
		}
	}

	// Research types
	public static enum ResearchTypes
	{
			BASIC ("RESEARCH", 0, 0),
			CORES ("CORES", -1, -2),
			STORAGE ("STORAGE", -1, 2),
			IO ("IO", -2, -2),
			ARCANETERMINAL ("ARCANETERM", 0, -4),
			ESSENTIATERMINAL ("ESSTERM", -1, -4),
			ESSENTIAPROVIDER ("ESSPROV", -2, -4),
			INFUSIONPROVIDER ("INFPROV", -5, -2),
			VISINTERFACE ("VISINT", 2, 0),
			IRONGEARBOX ("IRONGEARBOX", 3, 2),
			THAUMIUMGEARBOX ("THAUMGBOX", 3, 3),
			CERTUSDUPE ("CERTUSDUPE", -6, -6);

		private String internalName;

		private int column;

		private int row;

		ResearchItem researchItem;

		private ResearchTypes( final String internalName, final int column, final int row )
		{
			this.internalName = "TE" + internalName;
			this.row = row;
			this.column = column;
		}

		public String getKey()
		{
			return ThaumicEnergistics.MOD_ID + "." + this.internalName;
		}

		public ResearchItem getResearchItem()
		{
			return this.researchItem;
		}

		/**
		 * Convenience function to aid in research item creation.
		 * 
		 * @param aspectList
		 * @param column
		 * @param row
		 * @param complexity
		 * @param icon
		 * @param pages
		 */
		void createResearchItem( final AspectList aspectList, final int complexity, final ItemStack icon, final ResearchPage[] pages )
		{
			this.researchItem = new ResearchItem( this.getKey(), TERESEARCH_TAB, aspectList, this.column, this.row, complexity, icon );
			this.researchItem.setPages( pages );
		}

		String getPageName( final int index )
		{
			return ThaumicEnergistics.MOD_ID + ".research_page." + this.internalName + "." + index;
		}
	}

	// Complexity of research
	private static final int COMPLEXITY_SMALL = 1;
	private static final int COMPLEXITY_MEDIUM = 2;
	private static final int COMPLEXITY_LARGE = 3;

	// Tab name and icon
	private static final String TERESEARCH_TAB = ThaumicEnergistics.MOD_ID;

	private static final ResourceLocation TAB_ICON = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/research/tab.icon.png" );
	private static final ResourceLocation RESEARCH_BACKGROUND = new ResourceLocation( ThaumicEnergistics.MOD_ID,
					"textures/research/Research.Background.png" );

	private static void addPseudoParents()
	{
		// Cache the configs
		IConfig teConfig = ThaumicEnergistics.config;

		// Get all pseudo types
		PseudoResearchTypes[] pseudoTypes = PseudoResearchTypes.values();
		for( PseudoResearchTypes type : pseudoTypes )
		{
			switch ( type )
			{
				case DUPE:
					if( !teConfig.allowedToDuplicateCertusQuartz() )
					{
						// Skip this, dupes not allowed.
						continue;
					}
					break;

				case JAR:
					if( Config.allowMirrors || !teConfig.allowedToCraftInfusionProvider() )
					{
						// Skip this, mirrors are allowed.
						continue;
					}
					break;

				case MIRROR:
					if( !Config.allowMirrors || !teConfig.allowedToCraftInfusionProvider() )
					{
						// Skip this, mirrors are not allowed.
						continue;
					}
					break;

				case INFUSION:
					if( !teConfig.allowedToCraftInfusionProvider() )
					{
						// Skip this, infusion provider not allowed
						continue;
					}
					break;

				default:
					break;

			}

			// Check for the jar/mirror setting
			if( ( type == PseudoResearchTypes.JAR ) && Config.allowMirrors )
			{
				continue;
			}
			else if( ( type == PseudoResearchTypes.MIRROR ) && !Config.allowMirrors )
			{
				continue;
			}

			type.registerPsudeoResearch();
		}

	}

	private static void registerACT()
	{
		// Set the research aspects
		AspectList actAspectList = new AspectList();
		actAspectList.add( Aspect.CRAFT, 5 );
		actAspectList.add( Aspect.ENERGY, 3 );
		actAspectList.add( Aspect.MECHANISM, 3 );
		actAspectList.add( Aspect.VOID, 3 );

		// Set the icon
		ItemStack actIcon = AEPartsEnum.ArcaneCraftingTerminal.getStack();

		// Set the pages
		ResearchPage[] actPages = new ResearchPage[] { new ResearchPage( ResearchTypes.ARCANETERMINAL.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.PART_ARCANE_TERMINAL ) };

		// Create the IO research
		ResearchTypes.ARCANETERMINAL.createResearchItem( actAspectList, COMPLEXITY_SMALL, actIcon, actPages );
		ResearchTypes.ARCANETERMINAL.researchItem.setParents( ResearchTypes.BASIC.getKey() );
		ResearchTypes.ARCANETERMINAL.researchItem.registerResearchItem();

	}

	private static void registerBasic()
	{
		// Create the basic research item
		ResearchTypes.BASIC.researchItem = new ResearchItem( ResearchTypes.BASIC.getKey(), TERESEARCH_TAB, new AspectList(), 0, 0, 0, TAB_ICON );
		ResearchTypes.BASIC.researchItem.setPages( new ResearchPage[] { new ResearchPage( ResearchTypes.BASIC.getPageName( 1 ) ) } );
		ResearchTypes.BASIC.researchItem.setStub().setRound().setAutoUnlock();
		ResearchTypes.BASIC.researchItem.registerResearchItem();

	}

	private static void registerCertusDupe()
	{
		// Set aspects
		AspectList certusDupeAspects = new AspectList();
		certusDupeAspects.add( Aspect.CRYSTAL, 5 );

		// Get icon
		ItemStack certusDupeIcon = AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 );

		// Set pages
		ResearchPage[] certusDupePages = new ResearchPage[] { new ResearchPage( ResearchTypes.CERTUSDUPE.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.DUPE_CERTUS ) };
		// Create the item
		ResearchTypes.CERTUSDUPE.createResearchItem( certusDupeAspects, COMPLEXITY_SMALL, certusDupeIcon, certusDupePages );

		// Set parents
		ResearchTypes.CERTUSDUPE.researchItem.setParents( PseudoResearchTypes.DUPE.getKey() );
		ResearchTypes.CERTUSDUPE.researchItem.setParentsHidden( "ALCHEMICALDUPLICATION" );

		// Set secondary and concealed
		ResearchTypes.CERTUSDUPE.researchItem.setSecondary().setConcealed();

		// Trigger when certus is scanned
		ResearchTypes.CERTUSDUPE.researchItem.setItemTriggers( certusDupeIcon );

		// Register
		ResearchTypes.CERTUSDUPE.researchItem.registerResearchItem();

	}

	private static void registerCores()
	{
		// Set the research aspects
		AspectList coreAspectList = new AspectList();
		coreAspectList.add( Aspect.SLIME, 3 );
		coreAspectList.add( Aspect.MAGIC, 5 );
		coreAspectList.add( Aspect.MECHANISM, 5 );
		coreAspectList.add( Aspect.EXCHANGE, 5 );

		// Set the icon
		ItemStack coreIcon = ItemMaterial.MaterialTypes.COALESCENCE_CORE.getItemStack();

		// Set the pages
		ResearchPage[] corePages = new ResearchPage[] { new ResearchPage( ResearchTypes.CORES.getPageName( 1 ) ),
						new ResearchPage( ResearchTypes.CORES.getPageName( 2 ) ), new ResearchPage( RecipeRegistry.MATERIAL_COALESCENCE_CORE ),
						new ResearchPage( RecipeRegistry.MATERIAL_DIFFUSION_CORE ) };

		// Create the core research
		ResearchTypes.CORES.createResearchItem( coreAspectList, COMPLEXITY_MEDIUM, coreIcon, corePages );
		ResearchTypes.CORES.researchItem.setParents( ResearchTypes.BASIC.getKey(), PseudoResearchTypes.DISTILESSENTIA.getKey() );
		ResearchTypes.CORES.researchItem.setParentsHidden( "DISTILESSENTIA" );
		ResearchTypes.CORES.researchItem.registerResearchItem();
	}

	private static void registerEssentiaProvider()
	{
		AspectList essentiaProviderList = new AspectList();
		essentiaProviderList.add( Aspect.MECHANISM, 3 );
		essentiaProviderList.add( Aspect.MAGIC, 5 );
		essentiaProviderList.add( Aspect.ORDER, 3 );
		essentiaProviderList.add( Aspect.SENSES, 7 );

		ItemStack essentiaProviderIcon = new ItemStack( BlockEnum.ESSENTIA_PROVIDER.getBlock(), 1 );

		ResearchPage[] essentiaProviderPages = new ResearchPage[] { new ResearchPage( ResearchTypes.ESSENTIAPROVIDER.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_ESSENTIA_PROVIDER ) };

		ResearchTypes.ESSENTIAPROVIDER.createResearchItem( essentiaProviderList, COMPLEXITY_LARGE, essentiaProviderIcon, essentiaProviderPages );
		ResearchTypes.ESSENTIAPROVIDER.researchItem.setParents( ResearchTypes.IO.getKey() );
		ResearchTypes.ESSENTIAPROVIDER.researchItem.setParentsHidden( "INFUSION", "TUBEFILTER" );
		ResearchTypes.ESSENTIAPROVIDER.researchItem.setConcealed();
		ResearchTypes.ESSENTIAPROVIDER.researchItem.registerResearchItem();
	}

	private static void registerEssentiaTerminal()
	{
		// Set the research aspects
		AspectList etAspectList = new AspectList();
		etAspectList.add( Aspect.EXCHANGE, 5 );
		etAspectList.add( Aspect.SENSES, 5 );
		etAspectList.add( Aspect.ENERGY, 3 );
		etAspectList.add( Aspect.WATER, 3 );

		// Set the icon
		ItemStack etIcon = AEPartsEnum.EssentiaTerminal.getStack();

		// Set the pages
		ResearchPage[] etPages = new ResearchPage[] { new ResearchPage( ResearchTypes.ESSENTIATERMINAL.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.PART_ESSENTIA_TERMINAL ),
						new ResearchPage( ResearchTypes.ESSENTIATERMINAL.getPageName( 2 ) ),
						new ResearchPage( RecipeRegistry.PART_ESSENTIA_LEVEL_EMITTER ) };

		// Create the IO research
		ResearchTypes.ESSENTIATERMINAL.createResearchItem( etAspectList, COMPLEXITY_SMALL, etIcon, etPages );
		ResearchTypes.ESSENTIATERMINAL.researchItem.setParents( ResearchTypes.CORES.getKey() );
		ResearchTypes.ESSENTIATERMINAL.researchItem.setConcealed();
		ResearchTypes.ESSENTIATERMINAL.researchItem.setSecondary();
		ResearchTypes.ESSENTIATERMINAL.researchItem.registerResearchItem();

	}

	private static void registerGearboxes()
	{
		// Set the research aspects for the Iron Gear Box
		AspectList igbAspects = new AspectList();
		igbAspects.add( Aspect.MECHANISM, 6 );
		igbAspects.add( Aspect.TREE, 4 );
		igbAspects.add( Aspect.METAL, 4 );

		// Set the icon for the Iron Gear Box
		ItemStack igbIcon = TEApi.instance().items().IronGear.getStack();

		// Set the pages for the Iron Gear Box
		ResearchPage[] igbPages = new ResearchPage[] { new ResearchPage( ResearchTypes.IRONGEARBOX.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.MATERIAL_IRON_GEAR ), new ResearchPage( RecipeRegistry.BLOCK_IRONGEARBOX ) };

		// Create the research for the Iron Gear Box
		ResearchTypes.IRONGEARBOX.createResearchItem( igbAspects, COMPLEXITY_SMALL, igbIcon, igbPages );

		// Set as secondary and register
		ResearchTypes.IRONGEARBOX.researchItem.setSecondary().registerResearchItem();

		// Set the research aspects for the Thaumium Gear Box
		AspectList tgbAspects = new AspectList();
		tgbAspects.add( Aspect.MECHANISM, 10 );
		tgbAspects.add( Aspect.MAGIC, 8 );
		tgbAspects.add( Aspect.METAL, 5 );

		// Set the icon for the Thaumium Gear Box
		ItemStack tgbIcon = TEApi.instance().blocks().ThaumiumGearBox.getStack();

		// Set the pages for the Thaumium Gear Box
		ResearchPage[] tgbPages = new ResearchPage[] { new ResearchPage( ResearchTypes.THAUMIUMGEARBOX.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_THAUMIUMGEARBOX ) };

		// Create the item for the Thaumium Gear Box
		ResearchTypes.THAUMIUMGEARBOX.createResearchItem( tgbAspects, COMPLEXITY_SMALL, tgbIcon, tgbPages );

		// Set parents for the Thaumium Gear Box
		ResearchTypes.THAUMIUMGEARBOX.researchItem.setParents( ResearchTypes.IRONGEARBOX.getKey(), PseudoResearchTypes.COREUSE.getKey() );
		ResearchTypes.THAUMIUMGEARBOX.researchItem.setParentsHidden( "COREUSE" );

		// Set as secondary and register
		ResearchTypes.THAUMIUMGEARBOX.researchItem.setSecondary().registerResearchItem();
	}

	private static void registerInfusionProvider()
	{
		// Set the research aspects
		AspectList infusionProviderList = new AspectList();
		infusionProviderList.add( Aspect.MECHANISM, 3 );
		infusionProviderList.add( Aspect.MAGIC, 3 );
		infusionProviderList.add( Aspect.EXCHANGE, 7 );
		infusionProviderList.add( Aspect.MOTION, 7 );
		infusionProviderList.add( Aspect.SENSES, 5 );

		// Set the icon
		ItemStack infusionProviderIcon = new ItemStack( BlockEnum.INFUSION_PROVIDER.getBlock(), 1 );

		// Set the pages
		ResearchPage[] infusionProviderPages = new ResearchPage[] { new ResearchPage( ResearchTypes.INFUSIONPROVIDER.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_INFUSION_PROVIDER ) };

		// Are mirrors allowed?
		String researchKeyMirrorOrJar = ( Config.allowMirrors ? "MIRROR" : "JARLABEL" );
		String pseudoKeyMirrorOrJar = ( Config.allowMirrors ? PseudoResearchTypes.MIRROR.getKey() : PseudoResearchTypes.JAR.getKey() );

		// Create the infusion provider research
		ResearchTypes.INFUSIONPROVIDER.createResearchItem( infusionProviderList, COMPLEXITY_LARGE, infusionProviderIcon, infusionProviderPages );
		ResearchTypes.INFUSIONPROVIDER.researchItem.setParents( ResearchTypes.IO.getKey(), pseudoKeyMirrorOrJar,
			PseudoResearchTypes.INFUSION.getKey() );
		ResearchTypes.INFUSIONPROVIDER.researchItem.setParentsHidden( researchKeyMirrorOrJar, "INFUSION" );
		ResearchTypes.INFUSIONPROVIDER.researchItem.setConcealed().setSpecial();
		ResearchTypes.INFUSIONPROVIDER.researchItem.registerResearchItem();
	}

	private static void registerIO()
	{
		// Set the research aspects
		AspectList ioAspectList = new AspectList();
		ioAspectList.add( Aspect.MECHANISM, 5 );
		ioAspectList.add( Aspect.METAL, 3 );
		ioAspectList.add( Aspect.CRYSTAL, 3 );
		ioAspectList.add( Aspect.AIR, 3 );

		// Set the icon
		ItemStack ioIcon = AEPartsEnum.EssentiaExportBus.getStack();

		// Set the pages
		ResearchPage[] ioPages = new ResearchPage[] { new ResearchPage( ResearchTypes.IO.getPageName( 1 ) ),
						new ResearchPage( ResearchTypes.IO.getPageName( 2 ) ), new ResearchPage( RecipeRegistry.PART_IMPORT_BUS ),
						new ResearchPage( RecipeRegistry.PART_EXPORT_BUS ), new ResearchPage( RecipeRegistry.PART_STORAGE_BUS ) };

		// Create the IO research
		ResearchTypes.IO.createResearchItem( ioAspectList, COMPLEXITY_MEDIUM, ioIcon, ioPages );
		ResearchTypes.IO.researchItem.setParents( ResearchTypes.CORES.getKey(), PseudoResearchTypes.TUBEFILTER.getKey() );
		ResearchTypes.IO.researchItem.setParentsHidden( "TUBEFILTER" );
		ResearchTypes.IO.researchItem.setConcealed();
		ResearchTypes.IO.researchItem.registerResearchItem();
	}

	private static void registerStorage()
	{
		// Set the research aspects
		AspectList storageAspectList = new AspectList();
		storageAspectList.add( Aspect.VOID, 5 );
		storageAspectList.add( Aspect.ENERGY, 5 );
		storageAspectList.add( Aspect.CRYSTAL, 3 );
		storageAspectList.add( Aspect.METAL, 3 );

		// Set the icon
		ItemStack storageIcon = TEApi.instance().items().EssentiaCell_64k.getStack();

		// Get the component recipes
		IArcaneRecipe[] storageComponentRecipes = new IArcaneRecipe[] { RecipeRegistry.STORAGE_COMPONENT_4K, RecipeRegistry.STORAGE_COMPONENT_16K,
						RecipeRegistry.STORAGE_COMPONENT_64K };

		// Get the cell shaped recipes
		IRecipe[] storageCellsShaped = new IRecipe[] { RecipeRegistry.STORAGE_CELL_1K_SHAPED, RecipeRegistry.STORAGE_CELL_4K_SHAPED,
						RecipeRegistry.STORAGE_CELL_16K_SHAPED, RecipeRegistry.STORAGE_CELL_64K_SHAPED };

		// Get the cell shapeless recipes
		IRecipe[] storageCellsShapeless = new IRecipe[] { RecipeRegistry.STORAGE_CELL_1K_SHAPELESS, RecipeRegistry.STORAGE_CELL_4K_SHAPELESS,
						RecipeRegistry.STORAGE_CELL_16K_SHAPELESS, RecipeRegistry.STORAGE_CELL_64K_SHAPELESS };

		// Set the pages
		ResearchPage[] storagePages = new ResearchPage[] { new ResearchPage( ResearchTypes.STORAGE.getPageName( 1 ) ),
						new ResearchPage( ResearchTypes.STORAGE.getPageName( 2 ) ), new ResearchPage( RecipeRegistry.STORAGE_COMPONENT_1K ),
						new ResearchPage( storageComponentRecipes ), new ResearchPage( RecipeRegistry.STORAGE_CASING ),
						new ResearchPage( storageCellsShaped ), new ResearchPage( storageCellsShapeless ) };

		// Create the storage research
		ResearchTypes.STORAGE.createResearchItem( storageAspectList, COMPLEXITY_MEDIUM, storageIcon, storagePages );
		ResearchTypes.STORAGE.researchItem.setParents( ResearchTypes.BASIC.getKey(), PseudoResearchTypes.DISTILESSENTIA.getKey() );
		ResearchTypes.STORAGE.researchItem.setParentsHidden( "DISTILESSENTIA" );
		ResearchTypes.STORAGE.researchItem.registerResearchItem();
	}

	private static void registerVisRelayInterface()
	{
		// Set the research aspects
		AspectList vriAspects = new AspectList();
		vriAspects.add( Aspect.AURA, 5 );
		vriAspects.add( Aspect.ENERGY, 4 );
		vriAspects.add( Aspect.VOID, 3 );
		vriAspects.add( Aspect.MECHANISM, 2 );

		// Set the icon
		ItemStack vriIcon = AEPartsEnum.VisInterface.getStack();

		// Set the pages
		ResearchPage[] vriPages = new ResearchPage[] { new ResearchPage( ResearchTypes.VISINTERFACE.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.PART_VIS_INTERFACE ), new ResearchPage( ResearchTypes.VISINTERFACE.getPageName( 2 ) ),
						new ResearchPage( ResearchTypes.VISINTERFACE.getPageName( 3 ) ) };

		// Create the vis relay interface research
		ResearchTypes.VISINTERFACE.createResearchItem( vriAspects, COMPLEXITY_MEDIUM, vriIcon, vriPages );
		ResearchTypes.VISINTERFACE.researchItem.setParents( ResearchTypes.BASIC.getKey(), PseudoResearchTypes.VISPOWER.getKey() );
		ResearchTypes.VISINTERFACE.researchItem.setParentsHidden( "VISPOWER" );
		ResearchTypes.VISINTERFACE.researchItem.registerResearchItem();
	}

	public static void registerResearch()
	{
		// Create our research tab
		ResearchCategories.registerCategory( TERESEARCH_TAB, TAB_ICON, ResearchRegistry.RESEARCH_BACKGROUND );

		// Central item
		ResearchRegistry.registerBasic();

		// Arcane Crafting Terminal
		ResearchRegistry.registerACT();

		// Transposition cores
		ResearchRegistry.registerCores();

		// ME Cells
		ResearchRegistry.registerStorage();

		// Buses
		ResearchRegistry.registerIO();

		// Essentia Terminal
		ResearchRegistry.registerEssentiaTerminal();

		// Gearboxes
		ResearchRegistry.registerGearboxes();

		// Infusion provider
		if( ThaumicEnergistics.config.allowedToCraftInfusionProvider() )
		{
			ResearchRegistry.registerInfusionProvider();
		}

		// Essentia provider
		if( ThaumicEnergistics.config.allowedToCraftEssentiaProvider() )
		{
			ResearchRegistry.registerEssentiaProvider();
		}

		// Vis Relay Interface
		ResearchRegistry.registerVisRelayInterface();

		// Certus Quartz Duplication
		if( ThaumicEnergistics.config.allowedToDuplicateCertusQuartz() )
		{
			ResearchRegistry.registerCertusDupe();
		}

		// Place parents
		ResearchRegistry.addPseudoParents();
	}
}
