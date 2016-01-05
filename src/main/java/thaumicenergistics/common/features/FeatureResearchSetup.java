package thaumicenergistics.common.features;

import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;

public class FeatureResearchSetup
	extends ThEFeatureBase
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

	/**
	 * Set to true when the research has been finalized.
	 */
	private boolean hasFinalizedResearch = false;

	public FeatureResearchSetup()
	{
		// Inform the super we are enabled by default.
		super( true );
	}

	@Override
	protected void registerResearch()
	{

		// Create the research tab
		ResearchCategories
						.registerCategory( ResearchRegistry.TERESEARCH_TAB, FeatureResearchSetup.TAB_ICON, FeatureResearchSetup.RESEARCH_BACKGROUND );

		// Create the basic research item
		ResearchTypes.BASIC.researchItem = new ResearchItem( ResearchTypes.BASIC.getKey(), ResearchRegistry.TERESEARCH_TAB, new AspectList(), 0, 0,
						0, FeatureResearchSetup.TAB_ICON );
		ResearchTypes.BASIC.researchItem.setPages( new ResearchPage[] { new ResearchPage( ResearchTypes.BASIC.getPageName( 1 ) ) } );
		ResearchTypes.BASIC.researchItem.setStub().setRound().setAutoUnlock();
		ResearchTypes.BASIC.researchItem.registerResearchItem();
	}

	/**
	 * Finishes the Thaumcraft registration by inserting the pseudo-parents for
	 * any enabled research features.
	 * 
	 * @param featureList
	 */
	public void finalizeRegistration( final ThEFeatureBase[] featureList )
	{
		if( this.hasFinalizedResearch )
		{
			return;
		}

		// Loop over all features
		for( ThEFeatureBase feature : featureList )
		{
			// Is the feature available?
			if( feature.isAvailable() )
			{
				// Get parent(s)
				for( PseudoResearchTypes type : feature.getPseudoParentTypes() )
				{
					// Register parent
					type.registerPsudeoResearch();
				}
			}
		}

		// Mark that finalization has occurred.
		this.hasFinalizedResearch = true;
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf )
		{
			return ResearchTypes.BASIC.getKey();
		}

		return "";
	}

}
