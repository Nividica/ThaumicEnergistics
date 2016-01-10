package thaumicenergistics.common.features;

import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;

public class FeatureThaumicEnergisticsResearch
	extends ThEThaumcraftResearchFeature
{

	/**
	 * Research background image
	 */
	private static final ResourceLocation RESEARCH_BACKGROUND = new ResourceLocation( ThaumicEnergistics.MOD_ID,
					"textures/research/Research.Background.png" );

	/**
	 * Research tab image
	 */
	private static final ResourceLocation TAB_ICON = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/research/tab.icon.png" );

	public FeatureThaumicEnergisticsResearch()
	{
		super( ResearchTypes.BASIC.getKey() );
	}

	@Override
	protected boolean checkConfigs( final IThEConfig theConfig )
	{
		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return null;
	}

	@Override
	protected ThEThaumcraftResearchFeature getParentFeature()
	{
		return null;
	}

	@Override
	protected void registerCrafting( final CommonDependantItems cdi )
	{
	}

	@Override
	protected void registerResearch()
	{

		// Create the research tab
		ResearchCategories.registerCategory(
			ResearchRegistry.TERESEARCH_TAB, FeatureThaumicEnergisticsResearch.TAB_ICON,
			FeatureThaumicEnergisticsResearch.RESEARCH_BACKGROUND );

		// Create the basic research item
		ResearchTypes.BASIC.researchItem = new ResearchItem( this.researchKey,
						ResearchRegistry.TERESEARCH_TAB, new AspectList(),
						0, 0, 0, FeatureThaumicEnergisticsResearch.TAB_ICON );
		ResearchTypes.BASIC.researchItem.setPages( new ResearchPage[] {
						new ResearchPage( ResearchTypes.BASIC.getPageName( 1 ) ) } );
		ResearchTypes.BASIC.researchItem.setRound().setAutoUnlock();
		ResearchTypes.BASIC.researchItem.registerResearchItem();
	}

	@Override
	public void registerPseudoParents()
	{
	}

}
