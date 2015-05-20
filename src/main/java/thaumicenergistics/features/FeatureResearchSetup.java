package thaumicenergistics.features;

import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.Config;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.IConfig;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.registries.ResearchRegistry.ResearchTypes;

public class FeatureResearchSetup
	extends AbstractBasicFeature
	implements IThaumcraftResearchFeature
{

	public static final ResourceLocation RESEARCH_BACKGROUND = new ResourceLocation( ThaumicEnergistics.MOD_ID,
					"textures/research/Research.Background.png" );

	public static final ResourceLocation TAB_ICON = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/research/tab.icon.png" );

	public FeatureResearchSetup()
	{
		super( true );
	}

	private void addPseudoParents()
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

				case WARDED:
					if( !Config.wardedStone )
					{
						// Skip this, warded research is disabled
						continue;
					}

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

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf )
		{
			return ResearchTypes.BASIC.getKey();
		}

		return "";
	}

	@Override
	public void registerResearch()
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

		// Add the psudeo parents
		this.addPseudoParents();
	}

}
