package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.parts.AEPartsEnum;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;
import thaumicenergistics.common.registries.*;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

/**
 * {@link PartArcaneCraftingTerminal} feature.
 * 
 * @author Nividica
 * 
 */
public class FeatureACT
	extends ThEThaumcraftResearchFeature
{

	public FeatureACT()
	{
		super( ResearchTypes.ARCANE_TERMINAL.getKey() );
	}

	@Override
	protected boolean checkConfigs( final IThEConfig theConfig )
	{
		// Depends on crafting terminal
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.CraftingTerminal ) )
		{
			return false;
		}

		return theConfig.craftArcaneCraftingTerminal();
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.CalculationProcessor, cdi.METerminal };
	}

	@Override
	protected ThEThaumcraftResearchFeature getParentFeature()
	{
		return FeatureRegistry.instance().featureThaumicEnergistics;
	}

	@Override
	protected void registerCrafting( final CommonDependantItems cdi )
	{
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
		RecipeRegistry.PART_ARCANE_TERMINAL = ThaumcraftApi.addShapelessArcaneCraftingRecipe( this.researchKey,
			ArcaneCraftingTerminal, actAspectList, actRecipe );
	}

	@Override
	protected void registerPseudoParents()
	{
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
		ResearchPage[] actPages = new ResearchPage[] { new ResearchPage( ResearchTypes.ARCANE_TERMINAL.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.PART_ARCANE_TERMINAL ) };

		// Create the IO research
		ResearchTypes.ARCANE_TERMINAL.createResearchItem( actAspectList, ResearchRegistry.COMPLEXITY_SMALL, actIcon, actPages );
		ResearchTypes.ARCANE_TERMINAL.researchItem.setParents( this.getFirstValidParentKey( false ) );
		ResearchTypes.ARCANE_TERMINAL.researchItem.registerResearchItem();
	}
}
