package thaumicenergistics.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.Config;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.FeatureRegistry;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.registries.ResearchRegistry.ResearchTypes;

public class FeatureInfusionProvider
	extends AbstractDependencyFeature
	implements IThaumcraftResearchFeature, ICraftingFeature
{

	public FeatureInfusionProvider( final FeatureRegistry fr )
	{
		super( fr );
	}

	@Override
	protected boolean checkConfigs()
	{
		// Depends on ThEConfig
		if( !ThEApi.instance().config().allowedToCraftInfusionProvider() )
		{
			return false;
		}

		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.MEInterface };
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.INFUSIONPROVIDER.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureEssentiaIOBuses.getFirstValidParentKey( true );
	}

	@Override
	public void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My items
		ItemStack CoalescenceCore = ThEApi.instance().items().CoalescenceCore.getStack();
		ItemStack InfusionProvider = ThEApi.instance().blocks().InfusionProvider.getStack();

		// Set required aspects for infusion provider
		AspectList infusionProviderList = new AspectList();
		infusionProviderList.add( Aspect.MECHANISM, 64 );
		infusionProviderList.add( Aspect.MAGIC, 32 );
		infusionProviderList.add( Aspect.ORDER, 32 );
		infusionProviderList.add( Aspect.EXCHANGE, 16 );

		// Infusion provider recipe items
		ItemStack[] infusionProviderRecipeItems = { cdi.EssentiaMirror, cdi.SalisMundus, CoalescenceCore, cdi.AirShard, cdi.EssentiaMirror,
						cdi.SalisMundus, CoalescenceCore, cdi.AirShard };

		// Create the infusion provider recipe
		RecipeRegistry.BLOCK_INFUSION_PROVIDER = ThaumcraftApi.addInfusionCraftingRecipe( ResearchRegistry.ResearchTypes.INFUSIONPROVIDER.getKey(),
			InfusionProvider, 4, infusionProviderList, cdi.MEInterface, infusionProviderRecipeItems );
	}

	@Override
	public void registerResearch()
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
		ResearchTypes.INFUSIONPROVIDER.createResearchItem( infusionProviderList, ResearchRegistry.COMPLEXITY_LARGE, infusionProviderIcon,
			infusionProviderPages );
		ResearchTypes.INFUSIONPROVIDER.researchItem.setParents( this.getFirstValidParentKey( false ), pseudoKeyMirrorOrJar,
			PseudoResearchTypes.INFUSION.getKey() );
		ResearchTypes.INFUSIONPROVIDER.researchItem.setParentsHidden( researchKeyMirrorOrJar, "INFUSION" );
		ResearchTypes.INFUSIONPROVIDER.researchItem.setConcealed().setSpecial();
		ResearchTypes.INFUSIONPROVIDER.researchItem.registerResearchItem();
	}

}
