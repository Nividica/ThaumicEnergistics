package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

public class FeatureEssentiaVibrationChamber
	extends AbstractDependencyFeature
{
	@Override
	protected boolean checkConfigs()
	{
		// Depends on power generation
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.PowerGen ) )
		{
			return false;
		}

		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		// Depends on warded jars, annihilation core, and vibration chamber
		return new Object[] { cdi.WardedJar, cdi.VibrationChamber, cdi.AnnihilationCore };
	}

	@Override
	protected void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My Items
		ItemStack EssVibrationChamber = ThEApi.instance().blocks().EssentiaVibrationChamber.getStack();
		ItemStack DiffusionCore = ThEApi.instance().items().DiffusionCore.getStack();

		// Chamber aspects
		AspectList chamberAspects = new AspectList();
		chamberAspects.add( Aspect.WATER, 2 );
		chamberAspects.add( Aspect.ENTROPY, 4 );
		chamberAspects.add( Aspect.FIRE, 7 );
		chamberAspects.add( Aspect.ORDER, 4 );

		// Register Chamber
		RecipeRegistry.BLOCK_ESSENTIA_VIBRATION_CHAMBER = ThaumcraftApi.addShapelessArcaneCraftingRecipe(
			ResearchTypes.ESSENTIAVIBRATIONCHAMBER.getKey(), EssVibrationChamber, chamberAspects, cdi.VibrationChamber, DiffusionCore, cdi.WardedJar );
	}

	@Override
	protected void registerResearch()
	{
		// Set the research aspects
		AspectList chamberAspects = new AspectList();
		chamberAspects.add( Aspect.MECHANISM, 5 ); // Its a machine that
		chamberAspects.add( Aspect.MAGIC, 3 ); //     Takes essentia, specifically
		chamberAspects.add( Aspect.WATER, 1 ); //     Takes liquid essentia then
		chamberAspects.add( Aspect.MOTION, 3 ); //    Vibrates to
		chamberAspects.add( Aspect.ENERGY, 5 ); //    Produces power

		// Get the icon
		ItemStack chamberIcon = ThEApi.instance().blocks().EssentiaVibrationChamber.getStack();

		// Create the pages
		ResearchPage[] storagePages = new ResearchPage[] { new ResearchPage( ResearchTypes.ESSENTIAVIBRATIONCHAMBER.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_ESSENTIA_VIBRATION_CHAMBER ),
						new ResearchPage( ResearchTypes.ESSENTIAVIBRATIONCHAMBER.getPageName( 2 ) ),
						new ResearchPage( ResearchTypes.ESSENTIAVIBRATIONCHAMBER.getPageName( 3 ) ) };

		// Create the research
		ResearchTypes.ESSENTIAVIBRATIONCHAMBER.createResearchItem( chamberAspects, ResearchRegistry.COMPLEXITY_MEDIUM, chamberIcon, storagePages );

		// Set the parent to the cores
		ResearchTypes.ESSENTIAVIBRATIONCHAMBER.researchItem.setParents( this.getFirstValidParentKey( false ) );

		// Hide until the parent has been researched
		ResearchTypes.ESSENTIAVIBRATIONCHAMBER.researchItem.setConcealed();

		// Register
		ResearchTypes.ESSENTIAVIBRATIONCHAMBER.researchItem.registerResearchItem();

	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.ESSENTIAVIBRATIONCHAMBER.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureConversionCores.getFirstValidParentKey( true );
	}

}
