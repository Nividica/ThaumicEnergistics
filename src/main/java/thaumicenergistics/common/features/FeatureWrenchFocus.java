package thaumicenergistics.common.features;

import java.util.EnumSet;
import net.minecraft.item.ItemStack;
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
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

public class FeatureWrenchFocus
	extends AbstractDependencyFeature
{
	@Override
	protected boolean checkConfigs()
	{
		// Depends on quartz tools
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.CertusQuartzTools ) )
		{
			return false;
		}

		// Depends on quartz wrench
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.QuartzWrench ) )
		{
			return false;
		}

		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.CertusWrench };
	}

	@Override
	protected void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My items
		ItemStack WrenchFocus = ThEApi.instance().items().WandFocusAEWrench.getStack();

		// Wrench focus
		AspectList wrenchAspects = new AspectList();
		wrenchAspects.add( Aspect.AIR, 10 );
		wrenchAspects.add( Aspect.FIRE, 10 );
		RecipeRegistry.ITEM_WRENCH_FOCUS = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.FOCUSWRENCH.getKey(), WrenchFocus,
			wrenchAspects, new Object[] { "ANF", "NWN", "FNA", 'A', cdi.AirShard, 'F', cdi.FireShard, 'N', cdi.NetherQuartz, 'W', cdi.CertusWrench } );
	}

	@Override
	protected void registerResearch()
	{
		// Set the research aspects
		AspectList focusAspects = new AspectList();
		focusAspects.add( Aspect.MECHANISM, 6 );
		focusAspects.add( Aspect.TOOL, 5 );
		focusAspects.add( Aspect.MAGIC, 3 );

		// Set the icon
		ItemStack focusIcon = ThEApi.instance().items().WandFocusAEWrench.getStack();

		// Set the pages
		ResearchPage[] focusPages = new ResearchPage[] { new ResearchPage( ResearchTypes.FOCUSWRENCH.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.ITEM_WRENCH_FOCUS ) };

		// Create the research
		ResearchTypes.FOCUSWRENCH.createResearchItem( focusAspects, ResearchRegistry.COMPLEXITY_SMALL, focusIcon, focusPages );
		ResearchTypes.FOCUSWRENCH.researchItem.setParents( PseudoResearchTypes.FOCUSFIRE.getKey() ).setSecondary();
		ResearchTypes.FOCUSWRENCH.researchItem.registerResearchItem();
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.FOCUSWRENCH.getKey();
		}

		// No parent
		return "";
	}

	@Override
	public EnumSet<PseudoResearchTypes> getPseudoParentTypes()
	{
		return EnumSet.of( PseudoResearchTypes.FOCUSFIRE );
	}

}
