package thaumicenergistics.common.features;

import java.util.EnumSet;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.Config;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.*;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;

public class FeatureInfusionProvider
	extends ThEDependencyFeatureBase
{
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
	protected void registerCrafting( final CommonDependantItems cdi )
	{
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
		RecipeRegistry.BLOCK_INFUSION_PROVIDER = ThaumcraftApi.addInfusionCraftingRecipe( ResearchRegistry.ResearchTypes.INFUSION_PROVIDER.getKey(),
			InfusionProvider, 4, infusionProviderList, cdi.MEInterface, infusionProviderRecipeItems );
	}

	@Override
	protected void registerResearch()
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
		ResearchPage[] infusionProviderPages = new ResearchPage[] { new ResearchPage( ResearchTypes.INFUSION_PROVIDER.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_INFUSION_PROVIDER ) };

		// Are mirrors allowed?
		String researchKeyMirrorOrJar = ( Config.allowMirrors ? "MIRROR" : "JARLABEL" );
		String pseudoKeyMirrorOrJar = ( Config.allowMirrors ? PseudoResearchTypes.MIRROR.getKey() : PseudoResearchTypes.JAR.getKey() );

		// Create the infusion provider research
		ResearchTypes.INFUSION_PROVIDER.createResearchItem( infusionProviderList, ResearchRegistry.COMPLEXITY_LARGE, infusionProviderIcon,
			infusionProviderPages );
		ResearchTypes.INFUSION_PROVIDER.researchItem.setParents( this.getFirstValidParentKey( false ), pseudoKeyMirrorOrJar,
			PseudoResearchTypes.INFUSION.getKey() );
		ResearchTypes.INFUSION_PROVIDER.researchItem.setParentsHidden( researchKeyMirrorOrJar, "INFUSION" );
		ResearchTypes.INFUSION_PROVIDER.researchItem.setConcealed().setSpecial();
		ResearchTypes.INFUSION_PROVIDER.researchItem.registerResearchItem();
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.INFUSION_PROVIDER.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureEssentiaIOBuses.getFirstValidParentKey( true );
	}

	@Override
	public EnumSet<PseudoResearchTypes> getPseudoParentTypes()
	{
		return EnumSet.of( PseudoResearchTypes.INFUSION, ( Config.allowMirrors ? PseudoResearchTypes.MIRROR : PseudoResearchTypes.JAR ) );
	}

}
