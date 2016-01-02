package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.AEPartsEnum;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

public class FeatureACT
	extends AbstractDependencyFeature
{

	@Override
	protected boolean checkConfigs()
	{
		// Depends on crafting terminal
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.CraftingTerminal ) )
		{
			return false;
		}

		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.CalculationProcessor, cdi.METerminal };
	}

	@Override
	protected void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My items
		ItemStack ArcaneCraftingTerminal = ThEApi.instance().parts().ArcaneCrafting_Terminal.getStack();

		// Arcane Crafting Terminal
		AspectList actAspectList = new AspectList();

		// Set aspects
		actAspectList.add( Aspect.AIR, 10 );
		actAspectList.add( Aspect.EARTH, 10 );
		actAspectList.add( Aspect.ENTROPY, 10 );
		actAspectList.add( Aspect.FIRE, 10 );
		actAspectList.add( Aspect.ORDER, 10 );
		actAspectList.add( Aspect.WATER, 10 );

		// Set recipe
		Object[] actRecipe = new Object[] { cdi.METerminal, cdi.ArcaneWorkTable, cdi.CalculationProcessor };

		// Register
		RecipeRegistry.PART_ARCANE_TERMINAL = ThaumcraftApi.addShapelessArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.ARCANETERMINAL.getKey(),
			ArcaneCraftingTerminal, actAspectList, actRecipe );
	}

	@Override
	protected void registerResearch()
	{
		// Set the research aspects
		AspectList actAspectList = new AspectList();
		actAspectList.add( Aspect.CRAFT, 5 );
		actAspectList.add( Aspect.ENERGY, 3 );
		actAspectList.add( Aspect.MECHANISM, 3 );
		actAspectList.add( Aspect.VOID, 3 );

		// Set the icon
		ItemStack actIcon = AEPartsEnum.ArcaneCraftingTerminal.getStack();

		// Set the pages
		ResearchPage[] actPages = new ResearchPage[] { new ResearchPage( ResearchTypes.ARCANETERMINAL.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.PART_ARCANE_TERMINAL ) };

		// Create the IO research
		ResearchTypes.ARCANETERMINAL.createResearchItem( actAspectList, ResearchRegistry.COMPLEXITY_SMALL, actIcon, actPages );
		ResearchTypes.ARCANETERMINAL.researchItem.setParents( this.getFirstValidParentKey( false ) );
		ResearchTypes.ARCANETERMINAL.researchItem.registerResearchItem();
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.ARCANETERMINAL.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureResearchSetup.getFirstValidParentKey( true );
	}
}
