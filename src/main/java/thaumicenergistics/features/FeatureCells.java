package thaumicenergistics.features;

import java.util.ArrayList;
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
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.registries.FeatureRegistry;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.registries.ResearchRegistry.ResearchTypes;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cpw.mods.fml.common.registry.GameRegistry;

public class FeatureCells
	extends AbstractDependencyFeature
	implements IThaumcraftResearchFeature, ICraftingFeature
{

	public FeatureCells( final FeatureRegistry fr )
	{
		super( fr );
	}

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
	public void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My items
		ItemStack EssentiaStorageComponent_1k = ThEApi.instance().items().EssentiaStorageComponent_1k.getStack();
		ItemStack EssentiaStorageComponent_4k = ThEApi.instance().items().EssentiaStorageComponent_4k.getStack();
		ItemStack EssentiaStorageComponent_16k = ThEApi.instance().items().EssentiaStorageComponent_16k.getStack();
		ItemStack EssentiaStorageComponent_64k = ThEApi.instance().items().EssentiaStorageComponent_64k.getStack();
		ItemStack EssentiaCell_Casing = ThEApi.instance().items().EssentiaCell_Casing.getStack();
		ItemStack EssentiaCell_1k = ThEApi.instance().items().EssentiaCell_1k.getStack();
		ItemStack EssentiaCell_4k = ThEApi.instance().items().EssentiaCell_4k.getStack();
		ItemStack EssentiaCell_16k = ThEApi.instance().items().EssentiaCell_16k.getStack();
		ItemStack EssentiaCell_64k = ThEApi.instance().items().EssentiaCell_64k.getStack();
		ItemStack EssentiaCellWorkbench = ThEApi.instance().blocks().EssentiaCellWorkbench.getStack();

		// Item Groups
		ArrayList<ItemStack> GroupQuartz = new ArrayList<ItemStack>( 3 );
		GroupQuartz.add( cdi.CertusQuartz );
		GroupQuartz.add( cdi.ChargedCertusQuartz );
		GroupQuartz.add( cdi.PureCertusQuartz );

		// 1K Storage Component
		AspectList storage1kAspects = new AspectList();
		storage1kAspects.add( Aspect.FIRE, 3 );
		storage1kAspects.add( Aspect.ORDER, 1 );
		RecipeRegistry.STORAGE_COMPONENT_1K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaStorageComponent_1k,
			storage1kAspects, new Object[] { "EQ ", "QPQ", " QE", 'E', cdi.EtheralEssence, 'Q', cdi.CertusQuartz, 'P', cdi.LogicProcessor } );
		this.replaceRecipeIngredientWithGroup( (ShapedArcaneRecipe)RecipeRegistry.STORAGE_COMPONENT_1K, cdi.CertusQuartz, GroupQuartz );

		// 4K Storage Component
		AspectList storage4kAspects = new AspectList();
		storage4kAspects.add( Aspect.FIRE, 3 );
		storage4kAspects.add( Aspect.ORDER, 2 );
		RecipeRegistry.STORAGE_COMPONENT_4K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaStorageComponent_4k,
			storage4kAspects, new Object[] { "EPE", "1G1", "E1E", 'E', cdi.EtheralEssence, '1', EssentiaStorageComponent_1k, 'P',
							cdi.CalculationProcessor, 'G', cdi.QuartzGlass } );

		// 16K Storage Component
		AspectList storage16kAspects = new AspectList();
		storage16kAspects.add( Aspect.FIRE, 3 );
		storage16kAspects.add( Aspect.ORDER, 4 );
		RecipeRegistry.STORAGE_COMPONENT_16K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaStorageComponent_16k,
			storage16kAspects, new Object[] { "SPE", "4G4", "E4S", 'E', cdi.EtheralEssence, 'S', cdi.SalisMundus, '4', EssentiaStorageComponent_4k,
							'P', cdi.EngineeringProcessor, 'G', cdi.QuartzGlass } );

		// 16K Storage Component
		AspectList storage64kAspects = new AspectList();
		storage64kAspects.add( Aspect.FIRE, 3 );
		storage64kAspects.add( Aspect.ORDER, 8 );
		RecipeRegistry.STORAGE_COMPONENT_64K = ThaumcraftApi.addArcaneCraftingRecipe( ResearchTypes.STORAGE.getKey(), EssentiaStorageComponent_64k,
			storage64kAspects, new Object[] { "SPS", "6G6", "S6S", 'S', cdi.SalisMundus, '6', EssentiaStorageComponent_16k, 'P',
							cdi.EngineeringProcessor, 'G', cdi.QuartzGlass } );

		// Workbench
		RecipeRegistry.BLOCK_CELL_WORKBENCH = new ShapelessOreRecipe( EssentiaCellWorkbench, new Object[] { EssentiaStorageComponent_1k,
						cdi.MECellWorkbench } );
		GameRegistry.addRecipe( RecipeRegistry.BLOCK_CELL_WORKBENCH );

		// Storage Housing
		RecipeRegistry.STORAGE_HOUSING = new ShapedOreRecipe( EssentiaCell_Casing, false, new Object[] { "WRW", "R R", "TTT", 'W', cdi.WardedGlass,
						'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot } );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_HOUSING );

		// 1K Storage Cell
		RecipeRegistry.STORAGE_CELL_1K_SHAPED = new ShapedOreRecipe( EssentiaCell_1k, false, new Object[] { "WRW", "RCR", "TTT", 'W',
						cdi.WardedGlass, 'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot, 'C', EssentiaStorageComponent_1k } );
		RecipeRegistry.STORAGE_CELL_1K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_1k, EssentiaStorageComponent_1k, EssentiaCell_Casing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_1K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_1K_SHAPELESS );

		// 4K Storage Cell
		RecipeRegistry.STORAGE_CELL_4K_SHAPED = new ShapedOreRecipe( EssentiaCell_4k, false, new Object[] { "WRW", "RCR", "TTT", 'W',
						cdi.WardedGlass, 'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot, 'C', EssentiaStorageComponent_4k } );
		RecipeRegistry.STORAGE_CELL_4K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_4k, EssentiaStorageComponent_4k, EssentiaCell_Casing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_4K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_4K_SHAPELESS );

		// 16K Storage Cell
		RecipeRegistry.STORAGE_CELL_16K_SHAPED = new ShapedOreRecipe( EssentiaCell_16k, false, new Object[] { "WRW", "RCR", "TTT", 'W',
						cdi.WardedGlass, 'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot, 'C', EssentiaStorageComponent_16k } );
		RecipeRegistry.STORAGE_CELL_16K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_16k, EssentiaStorageComponent_16k, EssentiaCell_Casing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_16K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_16K_SHAPELESS );

		// 64K Storage Cell
		RecipeRegistry.STORAGE_CELL_64K_SHAPED = new ShapedOreRecipe( EssentiaCell_64k, false, new Object[] { "WRW", "RCR", "TTT", 'W',
						cdi.WardedGlass, 'R', cdi.RedstoneDust, 'T', cdi.ThaumiumIngot, 'C', EssentiaStorageComponent_64k } );
		RecipeRegistry.STORAGE_CELL_64K_SHAPELESS = new ShapelessOreRecipe( EssentiaCell_64k, EssentiaStorageComponent_64k, EssentiaCell_Casing );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_64K_SHAPED );
		GameRegistry.addRecipe( RecipeRegistry.STORAGE_CELL_64K_SHAPELESS );
	}

	@Override
	public void registerResearch()
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
						new ResearchPage( storageComponentRecipes ), new ResearchPage( RecipeRegistry.STORAGE_HOUSING ),
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

}
