package thaumicenergistics.common.features;

import java.util.EnumSet;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.*;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

public class FeatureVisRelayInterface
	extends ThEDependencyFeatureBase
{
	@Override
	protected boolean checkConfigs()
	{
		// Depends on P2P
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.P2PTunnel ) )
		{
			return false;
		}

		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.MEP2P };
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
		RecipeRegistry.PART_VIS_INTERFACE = ThaumcraftApi.addShapelessArcaneCraftingRecipe(
			ResearchRegistry.ResearchTypes.VIS_RELAY_INTERFACE.getKey(),
			VisInterface, visInterfaceAspectList, cdi.BallanceShard, cdi.MEP2P );
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

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.VIS_RELAY_INTERFACE.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureACT.getFirstValidParentKey( true );
	}

	@Override
	public EnumSet<PseudoResearchTypes> getPseudoParentTypes()
	{
		return EnumSet.of( PseudoResearchTypes.VISPOWER );
	}

}
