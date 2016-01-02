package thaumicenergistics.common.features;

import java.util.EnumSet;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import appeng.api.AEApi;

public class FeatureQuartzDupe
	extends AbstractDependencyFeature
{
	@Override
	protected boolean checkConfigs()
	{
		// Depends on ThE config
		if( !ThEApi.instance().config().allowedToDuplicateCertusQuartz() )
		{
			return false;
		}
		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return null;
	}

	@Override
	protected void registerCrafting()
	{
		// Certus
		String Certus1 = "crystalCertusQuartz";
		ItemStack Certus2 = OreDictionary.getOres( Certus1 ).get( 0 ).copy();
		Certus2.stackSize = 2;

		// Nether
		String NetherQuartz1 = "gemQuartz";
		ItemStack NetherQuartz2 = OreDictionary.getOres( NetherQuartz1 ).get( 0 ).copy();
		NetherQuartz2.stackSize = 2;

		// Certus Quartz
		if( FeatureRegistry.instance().getCommonItems().CertusQuartz != null )
		{
			AspectList certusAspects = new AspectList();
			certusAspects.add( Aspect.CRYSTAL, 4 );
			certusAspects.add( Aspect.WATER, 2 );
			RecipeRegistry.DUPE_CERTUS = ThaumcraftApi.addCrucibleRecipe( ResearchRegistry.ResearchTypes.CERTUSDUPE.getKey(), Certus2, Certus1,
				certusAspects );
		}

		// Nether Quartz
		AspectList nQAspects = new AspectList();
		nQAspects.add( Aspect.CRYSTAL, 4 );
		nQAspects.add( Aspect.WATER, 2 );
		nQAspects.add( Aspect.ENERGY, 2 );
		RecipeRegistry.DUPE_NETHER_QUARTZ = ThaumcraftApi.addCrucibleRecipe( ResearchRegistry.ResearchTypes.CERTUSDUPE.getKey(), NetherQuartz2,
			NetherQuartz1, nQAspects );

	}

	@Override
	protected void registerResearch()
	{
		// Set aspects
		AspectList certusDupeAspects = new AspectList();
		certusDupeAspects.add( Aspect.CRYSTAL, 5 );

		// Get icon
		ItemStack certusDupeIcon = AEApi.instance().definitions().materials().certusQuartzCrystal().maybeStack( 1 ).get();

		// Set pages
		ResearchPage[] certusDupePages = new ResearchPage[] { new ResearchPage( ResearchTypes.CERTUSDUPE.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.DUPE_CERTUS ), new ResearchPage( RecipeRegistry.DUPE_NETHER_QUARTZ ) };
		// Create the item
		ResearchTypes.CERTUSDUPE.createResearchItem( certusDupeAspects, ResearchRegistry.COMPLEXITY_SMALL, certusDupeIcon, certusDupePages );

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

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.CERTUSDUPE.getKey();
		}

		// No parent
		return "";
	}

	@Override
	public EnumSet<PseudoResearchTypes> getPseudoParentTypes()
	{
		return EnumSet.of( PseudoResearchTypes.DUPE );
	}
}
