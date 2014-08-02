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
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.items.ItemMaterial;
import thaumicenergistics.items.ItemStorageBase;

public class ResearchRegistry
{
	// Complexity of research
	private static final int COMPLEXITY_SMALL = 1;
	private static final int COMPLEXITY_MEDIUM = 2;
	private static final int COMPLEXITY_LARGE = 3;

	// Tab name and icon
	private static final String TERESEARCH_TAB = ThaumicEnergistics.MOD_ID;
	private static final ResourceLocation TAB_ICON = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/research/tab.icon.png" );

	// Research types
	public static enum ResearchTypes
	{
			BASIC ("RESEARCH"),
			CORES ("CORES"),
			STORAGE ("STORAGE"),
			IO ("IO"),
			ARCANETERMINAL ("ARCANETERM"),
			ESSENTIATERMINAL( "ESSTERM" );

		private String internalName;

		ResearchItem researchItem;

		private ResearchTypes( String internalName )
		{
			this.internalName = "TE" + internalName;
		}

		public String getKey()
		{
			return ThaumicEnergistics.MOD_ID + "." + this.internalName;
		}

		public ResearchItem getResearchItem()
		{
			return this.researchItem;
		}

		String getPageName( int index )
		{
			return ThaumicEnergistics.MOD_ID + ".research_page." + this.internalName + "." + index;
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
		void createResearchItem( AspectList aspectList, int column, int row, int complexity, ItemStack icon, ResearchPage[] pages )
		{
			this.researchItem = new ResearchItem( this.getKey(), TERESEARCH_TAB, aspectList, column, row, complexity, icon );
			this.researchItem.setPages( pages );
		}
	}

	public static void registerResearch()
	{
		// Create our research tab
		ResearchCategories.registerCategory( TERESEARCH_TAB, TAB_ICON, new ResourceLocation( "thaumcraft", "textures/gui/gui_researchback.png" ) );

		ResearchRegistry.registerBasic();

		ResearchRegistry.registerACT();

		ResearchRegistry.registerCores();

		ResearchRegistry.registerStorage();

		ResearchRegistry.registerIO();
		
		ResearchRegistry.registerEssentiaTerminal();
	}

	private static void registerBasic()
	{
		// Create the basic research item

		ResearchTypes.BASIC.researchItem = new ResearchItem( ResearchTypes.BASIC.getKey(), TERESEARCH_TAB, new AspectList(), 0, 0, 0, TAB_ICON );
		ResearchTypes.BASIC.researchItem.setPages( new ResearchPage[] { new ResearchPage( ResearchTypes.BASIC.getPageName( 1 ) ) } );
		ResearchTypes.BASIC.researchItem.setStub().setRound().setAutoUnlock();
		ResearchTypes.BASIC.researchItem.registerResearchItem();

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
		ResearchTypes.CORES.createResearchItem( coreAspectList, -2, -2, COMPLEXITY_MEDIUM, coreIcon, corePages );
		ResearchTypes.CORES.researchItem.setParents( ResearchTypes.BASIC.getKey() );
		ResearchTypes.CORES.researchItem.setParentsHidden( "DISTILESSENTIA" );
		ResearchTypes.CORES.researchItem.registerResearchItem();
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
		ItemStack storageIcon = ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_64K );

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
		ResearchTypes.STORAGE.createResearchItem( storageAspectList, 2, 2, COMPLEXITY_MEDIUM, storageIcon, storagePages );
		ResearchTypes.STORAGE.researchItem.setParents( ResearchTypes.BASIC.getKey() );
		ResearchTypes.STORAGE.researchItem.setParentsHidden( "DISTILESSENTIA" );
		ResearchTypes.STORAGE.researchItem.registerResearchItem();
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
		ResearchTypes.IO.createResearchItem( ioAspectList, -4, -2, COMPLEXITY_MEDIUM, ioIcon, ioPages );
		ResearchTypes.IO.researchItem.setParents( ResearchTypes.CORES.getKey() );
		ResearchTypes.IO.researchItem.setParentsHidden( "TUBEFILTER" );
		ResearchTypes.IO.researchItem.setConcealed();
		ResearchTypes.IO.researchItem.registerResearchItem();
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
		ResearchTypes.ARCANETERMINAL.createResearchItem( actAspectList, 1, -2, COMPLEXITY_SMALL, actIcon, actPages );
		ResearchTypes.ARCANETERMINAL.researchItem.registerResearchItem();

	}

	private static void registerEssentiaTerminal()
	{
		// Set the research aspects
		AspectList etAspectList = new AspectList();
		etAspectList.add( Aspect.EXCHANGE, 5 );
		etAspectList.add( Aspect.ENERGY, 3 );
		etAspectList.add( Aspect.WATER, 3 );

		// Set the icon
		ItemStack etIcon = AEPartsEnum.EssentiaTerminal.getStack();

		// Set the pages
		ResearchPage[] etPages = new ResearchPage[] { new ResearchPage( ResearchTypes.ESSENTIATERMINAL.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.PART_ESSENTIA_TERMINAL ) };

		// Create the IO research
		ResearchTypes.ESSENTIATERMINAL.createResearchItem( etAspectList, -2, -4, COMPLEXITY_SMALL, etIcon, etPages );
		ResearchTypes.ESSENTIATERMINAL.researchItem.setParents( ResearchTypes.CORES.getKey() );
		ResearchTypes.ESSENTIATERMINAL.researchItem.setConcealed();
		ResearchTypes.ESSENTIATERMINAL.researchItem.setSecondary();
		ResearchTypes.ESSENTIATERMINAL.researchItem.registerResearchItem();

	}
}
