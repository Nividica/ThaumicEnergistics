package thaumicenergistics.common.features;

import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.parts.AEPartsEnum;
import thaumicenergistics.common.parts.PartVisInterface;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;

/**
 * {@link PartVisInterface} feature.
 *
 * @author Nividica
 *
 */
public class FeatureVisRelayInterface
	extends ThEThaumcraftResearchFeature
{
	public FeatureVisRelayInterface()
	{
		super( ResearchTypes.VIS_RELAY_INTERFACE.getKey() );
	}

	@Override
	protected boolean checkConfigs( final IThEConfig theConfig )
	{
		// Depends on P2P
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.P2PTunnel ) )
		{
			return false;
		}

		return theConfig.craftVisRelayInterface();
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.MEP2P };
	}

	@Override
	protected ThEThaumcraftResearchFeature getParentFeature()
	{
		return FeatureRegistry.instance().featureACT;
	}

	@Override
	protected void registerCrafting( final CommonDependantItems cdi )
	{
		// My items
		ItemStack VisInterface = ThEApi.instance().parts().VisRelay_Interface.getStack();

		// Vis interface
		AspectList visInterfaceAspectList = new AspectList();
		visInterfaceAspectList.add( Aspect.AIR, 2 );
		visInterfaceAspectList.add( Aspect.EARTH, 2 );
		visInterfaceAspectList.add( Aspect.ENTROPY, 2 );
		visInterfaceAspectList.add( Aspect.FIRE, 2 );
		visInterfaceAspectList.add( Aspect.ORDER, 2 );
		visInterfaceAspectList.add( Aspect.WATER, 2 );
		RecipeRegistry.PART_VIS_INTERFACE = ThaumcraftApi.addShapelessArcaneCraftingRecipe( this.researchKey,
			VisInterface, visInterfaceAspectList, cdi.BallanceShard, cdi.MEP2P );
	}

	@Override
	protected void registerPseudoParents()
	{
		PseudoResearchTypes.VISPOWER.registerPsudeoResearch();
	}

	@Override
	protected void registerResearch()
	{
		// Set the research aspects
		AspectList vriAspects = new AspectList();
		vriAspects.add( Aspect.AURA, 5 );
		vriAspects.add( Aspect.ENERGY, 4 );
		vriAspects.add( Aspect.VOID, 3 );
		vriAspects.add( Aspect.MECHANISM, 2 );

		// Set the icon
		ItemStack vriIcon = AEPartsEnum.VisInterface.getStack();

		// Set the pages
		ResearchPage[] vriPages = new ResearchPage[] { new ResearchPage( ResearchTypes.VIS_RELAY_INTERFACE.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.PART_VIS_INTERFACE ),
						new ResearchPage( ResearchTypes.VIS_RELAY_INTERFACE.getPageName( 2 ) ),
						new ResearchPage( ResearchTypes.VIS_RELAY_INTERFACE.getPageName( 3 ) ) };

		// Create the vis relay interface research
		ResearchTypes.VIS_RELAY_INTERFACE.createResearchItem( vriAspects, ResearchRegistry.COMPLEXITY_MEDIUM, vriIcon, vriPages );
		ResearchTypes.VIS_RELAY_INTERFACE.researchItem.setParents( this.getFirstValidParentKey( false ), PseudoResearchTypes.VISPOWER.getKey() );
		ResearchTypes.VIS_RELAY_INTERFACE.researchItem.setParentsHidden( "VISPOWER" );
		ResearchTypes.VIS_RELAY_INTERFACE.researchItem.registerResearchItem();
	}

}
