package thaumicenergistics.common.features;

import java.util.ArrayList;
import java.util.EnumSet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.Config;
import thaumicenergistics.api.IThEItems;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cpw.mods.fml.common.registry.GameRegistry;

public class FeatureCells
	extends AbstractDependencyFeature
{
	/**
	 * Helper function to replace a single item with a group of items in a
	 * recipe.
	 * 
	 * @param recipe
	 * @param ingredient
	 * @param group
	 */
	private void replaceRecipeIngredientWithGroup( final ShapedArcaneRecipe recipe, final ItemStack ingredient, final ArrayList<ItemStack> group )
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

	@Override
	protected boolean checkConfigs()
	{
		// Depends on cells
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.StorageCells ) )
		{
			return false;
		}

		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.ChargedCertusQuartz, cdi.CertusQuartz, cdi.PureCertusQuartz, cdi.LogicProcessor, cdi.EngineeringProcessor,
						cdi.CalculationProcessor, cdi.QuartzGlass, cdi.MECellWorkbench };
	}

	@Override
	protected void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My items
		IThEItems theItems = ThEApi.instance().items();
		ItemStack EssentiaStorageComponent_1k = theItems.EssentiaStorageComponent_1k.getStack();
		ItemStack EssentiaStorageComponent_4k = theItems.EssentiaStorageComponent_4k.getStack();
		ItemStack EssentiaStorageComponent_16k = theItems.EssentiaStorageComponent_16k.getStack();
		ItemStack EssentiaStorageComponent_64k = theItems.EssentiaStorageComponent_64k.getStack();
		ItemStack EssentiaCell_Housing = theItems.EssentiaCell_Casing.getStack();
		ItemStack EssentiaCell_1k = theItems.EssentiaCell_1k.getStack();
		ItemStack EssentiaCell_4k = theItems.EssentiaCell_4k.getStack();
		ItemStack EssentiaCell_16k = theItems.EssentiaCell_16k.getStack();
		ItemStack EssentiaCell_64k = theItems.EssentiaCell_64k.getStack();
		ItemStack EssentiaCellWorkbench = ThEApi.instance().blocks().EssentiaCellWorkbench.getStack();

		// Item Groups
		ArrayList<ItemStack> GroupQuartz = new ArrayList<ItemStack>( 3 );
		GroupQuartz.add( cdi.CertusQuartz );
		GroupQuartz.add( cdi.ChargedCertusQuartz );
		GroupQuartz.add( cdi.PureCertusQuartz );

		// Housing ===============================================

		// Housing recipe
		Object[] recipeHousing = new Object[] { "WRW", "R R", "TTT", 'W', cdi.WardedGlass, 'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot };

		// Register Housing
		GameRegistry.addRecipe( RecipeRegistry.ITEM_STORAGE_HOUSING = new ShapedOreRecipe( EssentiaCell_Housing, false, recipeHousing ) );

		// 1K ====================================================

		// 1K Storage aspects
		AspectList aspects1KStorage = new AspectList();
		aspects1KStorage.add( Aspect.FIRE, 3 );
		aspects1KStorage.add( Aspect.ORDER, 1 );

		// 1K Storage recipe
		Object[] recipe1KStorage = new Object[] { "EQ ", "QPQ", " QE", 'E', cdi.EtheralEssence, 'Q', cdi.CertusQuartz, 'P', cdi.LogicProcessor };

		// 1K Cell recipe
		Object[] recipe1KCell = new Object[] { "WRW", "RCR", "TTT", 'W', cdi.WardedGlass, 'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot, 'C',
						EssentiaStorageComponent_1k };

		// Register 1K storage
		RecipeRegistry.ITEM_STORAGE_COMPONENT_1K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(),
			EssentiaStorageComponent_1k,
			aspects1KStorage, recipe1KStorage );

		// Register 1K cell
		GameRegistry.addRecipe( RecipeRegistry.ITEM_STORAGE_CELL_1K_SHAPED = new ShapedOreRecipe( EssentiaCell_1k, false, recipe1KCell ) );
		GameRegistry.addRecipe( RecipeRegistry.ITEM_STORAGE_CELL_1K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_1k, EssentiaStorageComponent_1k,
						EssentiaCell_Housing ) );

		// Replace regular certus quartz with any of the certus quartz variants in the 1K storage component
		this.replaceRecipeIngredientWithGroup( (ShapedArcaneRecipe)RecipeRegistry.ITEM_STORAGE_COMPONENT_1K, cdi.CertusQuartz, GroupQuartz );

		// 4K ===================================================

		// 4K Storage aspects
		AspectList aspects4KStorage = new AspectList();
		aspects4KStorage.add( Aspect.FIRE, 3 );
		aspects4KStorage.add( Aspect.ORDER, 2 );

		// 4K Storage recipe
		Object[] recipe4KStorage = new Object[] { "EPE", "1G1", "E1E", 'E', cdi.EtheralEssence, '1', EssentiaStorageComponent_1k, 'P',
						cdi.CalculationProcessor, 'G', cdi.QuartzGlass };

		// 4K Cell recipe
		Object[] recipe4KCell = new Object[] { "WRW", "RCR", "TTT", 'W', cdi.WardedGlass, 'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot, 'C',
						EssentiaStorageComponent_4k };

		// Register 4K storage
		RecipeRegistry.ITEM_STORAGE_COMPONENT_4K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(),
			EssentiaStorageComponent_4k,
			aspects4KStorage, recipe4KStorage );

		// Register 4K cell
		GameRegistry.addRecipe( RecipeRegistry.ITEM_STORAGE_CELL_4K_SHAPED = new ShapedOreRecipe( EssentiaCell_4k, false, recipe4KCell ) );
		GameRegistry.addRecipe( RecipeRegistry.ITEM_STORAGE_CELL_4K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_4k, EssentiaStorageComponent_4k,
						EssentiaCell_Housing ) );

		// 16K ===================================================

		// 16K Storage aspects
		AspectList aspects16KStorage = new AspectList();
		aspects16KStorage.add( Aspect.FIRE, 3 );
		aspects16KStorage.add( Aspect.ORDER, 4 );

		// 16K Storage recipe
		Object[] recipe16KStorage = new Object[] { "SPE", "4G4", "E4S", 'E', cdi.EtheralEssence, 'S', cdi.SalisMundus, '4',
						EssentiaStorageComponent_4k, 'P', cdi.EngineeringProcessor, 'G', cdi.QuartzGlass };

		// 16K Cell recipe
		Object[] recipe16KCell = new Object[] { "WRW", "RCR", "TTT", 'W', cdi.WardedGlass, 'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot, 'C',
						EssentiaStorageComponent_16k };

		// Register 16K storage
		RecipeRegistry.ITEM_STORAGE_COMPONENT_16K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(),
			EssentiaStorageComponent_16k,
			aspects16KStorage, recipe16KStorage );

		// Register 16K cell
		GameRegistry.addRecipe( RecipeRegistry.ITEM_STORAGE_CELL_16K_SHAPED = new ShapedOreRecipe( EssentiaCell_16k, false, recipe16KCell ) );
		GameRegistry.addRecipe( RecipeRegistry.ITEM_STORAGE_CELL_16K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_16k,
						EssentiaStorageComponent_16k,
						EssentiaCell_Housing ) );

		// 64K ===================================================

		// 64K Storage aspects
		AspectList aspects64KStorage = new AspectList();
		aspects64KStorage.add( Aspect.FIRE, 3 );
		aspects64KStorage.add( Aspect.ORDER, 8 );

		// 64K Storage recipe
		Object[] recipe64KStorage = new Object[] { "SPS", "6G6", "S6S", 'S', cdi.SalisMundus, '6', EssentiaStorageComponent_16k, 'P',
						cdi.EngineeringProcessor, 'G', cdi.QuartzGlass };

		// 64K Cell recipe
		Object[] recipe64KCell = new Object[] { "WRW", "RCR", "TTT", 'W', cdi.WardedGlass, 'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot, 'C',
						EssentiaStorageComponent_64k };

		// Register 64K storage
		RecipeRegistry.ITEM_STORAGE_COMPONENT_64K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(),
			EssentiaStorageComponent_64k,
			aspects64KStorage, recipe64KStorage );

		// 64K Storage Cell
		GameRegistry.addRecipe( RecipeRegistry.ITEM_STORAGE_CELL_64K_SHAPED = new ShapedOreRecipe( EssentiaCell_64k, false, recipe64KCell ) );
		GameRegistry.addRecipe( RecipeRegistry.ITEM_STORAGE_CELL_64K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_64k,
						EssentiaStorageComponent_64k,
						EssentiaCell_Housing ) );

		// Workbench
		GameRegistry.addRecipe( RecipeRegistry.BLOCK_CELL_WORKBENCH = new ShapelessOreRecipe( EssentiaCellWorkbench, EssentiaStorageComponent_1k,
						cdi.MECellWorkbench ) );
	}

	@Override
	protected void registerResearch()
	{
		// Set the research aspects
		AspectList storageAspectList = new AspectList();
		storageAspectList.add( Aspect.VOID, 5 );
		storageAspectList.add( Aspect.ENERGY, 5 );
		storageAspectList.add( Aspect.CRYSTAL, 3 );
		storageAspectList.add( Aspect.METAL, 3 );

		// Set the icon
		ItemStack storageIcon = ThEApi.instance().items().EssentiaCell_64k.getStack();

		// Get the component recipes
		IArcaneRecipe[] storageComponentRecipes = new IArcaneRecipe[] { RecipeRegistry.ITEM_STORAGE_COMPONENT_4K,
						RecipeRegistry.ITEM_STORAGE_COMPONENT_16K,
						RecipeRegistry.ITEM_STORAGE_COMPONENT_64K };

		// Get the cell shaped recipes
		IRecipe[] storageCellsShaped = new IRecipe[] { RecipeRegistry.ITEM_STORAGE_CELL_1K_SHAPED, RecipeRegistry.ITEM_STORAGE_CELL_4K_SHAPED,
						RecipeRegistry.ITEM_STORAGE_CELL_16K_SHAPED, RecipeRegistry.ITEM_STORAGE_CELL_64K_SHAPED };

		// Get the cell shapeless recipes
		IRecipe[] storageCellsShapeless = new IRecipe[] { RecipeRegistry.ITEM_STORAGE_CELL_1K_SHAPELESS,
						RecipeRegistry.ITEM_STORAGE_CELL_4K_SHAPELESS,
						RecipeRegistry.ITEM_STORAGE_CELL_16K_SHAPELESS, RecipeRegistry.ITEM_STORAGE_CELL_64K_SHAPELESS };

		// Set the pages
		ResearchPage[] storagePages = new ResearchPage[] { new ResearchPage( ResearchTypes.STORAGE.getPageName( 1 ) ),
						new ResearchPage( ResearchTypes.STORAGE.getPageName( 2 ) ), new ResearchPage( RecipeRegistry.ITEM_STORAGE_COMPONENT_1K ),
						new ResearchPage( storageComponentRecipes ), new ResearchPage( RecipeRegistry.ITEM_STORAGE_HOUSING ),
						new ResearchPage( storageCellsShaped ), new ResearchPage( storageCellsShapeless ),
						new ResearchPage( RecipeRegistry.BLOCK_CELL_WORKBENCH ) };

		String[] storageParents;

		// Is the warded stone research enabled?
		if( Config.wardedStone )
		{
			storageParents = new String[3];
			storageParents[2] = PseudoResearchTypes.WARDED.getKey();
		}
		else
		{
			storageParents = new String[2];
		}
		storageParents[0] = this.getFirstValidParentKey( false );
		storageParents[1] = PseudoResearchTypes.DISTILESSENTIA.getKey();

		// Create the storage research
		ResearchTypes.STORAGE.createResearchItem( storageAspectList, ResearchRegistry.COMPLEXITY_MEDIUM, storageIcon, storagePages );
		ResearchTypes.STORAGE.researchItem.setParents( storageParents );
		ResearchTypes.STORAGE.researchItem.setParentsHidden( "DISTILESSENTIA" );
		ResearchTypes.STORAGE.researchItem.registerResearchItem();
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.STORAGE.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureResearchSetup.getFirstValidParentKey( true );
	}

	@Override
	public EnumSet<PseudoResearchTypes> getPseudoParentTypes()
	{
		if( Config.wardedStone )
		{
			return EnumSet.of( PseudoResearchTypes.WARDED, PseudoResearchTypes.DISTILESSENTIA );
		}

		return EnumSet.of( PseudoResearchTypes.DISTILESSENTIA );
	}

}
