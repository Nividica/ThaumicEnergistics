package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import thaumicenergistics.common.tiles.TileEssentiaVibrationChamber;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

/**
 * {@link TileEssentiaVibrationChamber} feature.
 * 
 * @author Nividica
 * 
 */
public class FeatureEssentiaVibrationChamber
	extends ThEThaumcraftResearchFeature
{
	public FeatureEssentiaVibrationChamber()
	{
		super( ResearchTypes.ESSENTIA_VIBRATION_CHAMBER.getKey() );
	}

	@Override
	protected boolean checkConfigs( final IThEConfig theConfig )
	{
		// Depends on power generation
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.PowerGen ) )
		{
			return false;
		}

		return theConfig.craftEssentiaVibrationChamber();
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		// Depends on warded jars, annihilation core, and vibration chamber
		return new Object[] { cdi.WardedJar, cdi.VibrationChamber, cdi.AnnihilationCore };
	}

	@Override
	protected ThEThaumcraftResearchFeature getParentFeature()
	{
		return FeatureRegistry.instance().featureConversionCores;
	}

	@Override
	protected void registerCrafting( final CommonDependantItems cdi )
	{
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
		RecipeRegistry.BLOCK_ESSENTIA_VIBRATION_CHAMBER = ThaumcraftApi.addShapelessArcaneCraftingRecipe( this.researchKey,
			EssVibrationChamber, chamberAspects, cdi.VibrationChamber, DiffusionCore,
			cdi.WardedJar );
	}

	@Override
	protected void registerPseudoParents()
	{
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
		ResearchPage[] storagePages = new ResearchPage[] { new ResearchPage( ResearchTypes.ESSENTIA_VIBRATION_CHAMBER.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_ESSENTIA_VIBRATION_CHAMBER ),
						new ResearchPage( ResearchTypes.ESSENTIA_VIBRATION_CHAMBER.getPageName( 2 ) ),
						new ResearchPage( ResearchTypes.ESSENTIA_VIBRATION_CHAMBER.getPageName( 3 ) ) };

		// Create the research
		ResearchTypes.ESSENTIA_VIBRATION_CHAMBER.createResearchItem( chamberAspects, ResearchRegistry.COMPLEXITY_MEDIUM, chamberIcon, storagePages );

		// Set the parent to the cores
		ResearchTypes.ESSENTIA_VIBRATION_CHAMBER.researchItem.setParents( this.getFirstValidParentKey( false ) );

		// Hide until the parent has been researched
		ResearchTypes.ESSENTIA_VIBRATION_CHAMBER.researchItem.setConcealed();

		// Register
		ResearchTypes.ESSENTIA_VIBRATION_CHAMBER.researchItem.registerResearchItem();

	}

}
