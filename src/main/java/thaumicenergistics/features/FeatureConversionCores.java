package thaumicenergistics.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.items.ItemMaterial;
import thaumicenergistics.registries.FeatureRegistry;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.registries.ResearchRegistry.ResearchTypes;

public class FeatureConversionCores
	extends AbstractDependencyFeature
	implements IThaumcraftResearchFeature, ICraftingFeature
{

	public FeatureConversionCores( final FeatureRegistry fr )
	{
		super( fr );
	}

	@Override
	protected boolean checkConfigs()
	{
		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.FormationCore, cdi.AnnihilationCore };
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.CORES.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureResearchSetup.getFirstValidParentKey( true );
	}

	@Override
	public void registerCrafting()
	{

		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My items
		ItemStack DiffusionCore = ThEApi.instance().items().DiffusionCore.getStack();
		ItemStack CoalescenceCore = ThEApi.instance().items().CoalescenceCore.getStack();

		// Coalescence Core
		AspectList coalescenceAspects = new AspectList();
		coalescenceAspects.add( Aspect.WATER, 2 );
		coalescenceAspects.add( Aspect.ORDER, 2 );
		RecipeRegistry.MATERIAL_COALESCENCE_CORE = ThaumcraftApi.addShapelessArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.CORES.getKey(),
			CoalescenceCore, coalescenceAspects, cdi.QuickSilverDrop, cdi.QuickSilverDrop, cdi.QuickSilverDrop, cdi.OrderShard, cdi.FormationCore );

		// Diffusion Core
		AspectList diffusionAspects = new AspectList();
		diffusionAspects.add( Aspect.WATER, 2 );
		diffusionAspects.add( Aspect.ENTROPY, 2 );
		RecipeRegistry.MATERIAL_DIFFUSION_CORE = ThaumcraftApi.addShapelessArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.CORES.getKey(),
			DiffusionCore, diffusionAspects, cdi.QuickSilverDrop, cdi.QuickSilverDrop, cdi.QuickSilverDrop, cdi.EntropyShard, cdi.AnnihilationCore );
	}

	@Override
	public void registerResearch()
	{
		// Set the research aspects
		AspectList coreAspectList = new AspectList();
		coreAspectList.add( Aspect.SLIME, 3 );
		coreAspectList.add( Aspect.MAGIC, 5 );
		coreAspectList.add( Aspect.MECHANISM, 5 );
		coreAspectList.add( Aspect.EXCHANGE, 5 );

		// Set the icon
		ItemStack coreIcon = ItemMaterial.MaterialTypes.COALESCENCE_CORE.getItemStack();

		// Set the pages
		ResearchPage[] corePages = new ResearchPage[] { new ResearchPage( ResearchTypes.CORES.getPageName( 1 ) ),
						new ResearchPage( ResearchTypes.CORES.getPageName( 2 ) ), new ResearchPage( RecipeRegistry.MATERIAL_COALESCENCE_CORE ),
						new ResearchPage( RecipeRegistry.MATERIAL_DIFFUSION_CORE ) };

		// Create the core research
		ResearchTypes.CORES.createResearchItem( coreAspectList, ResearchRegistry.COMPLEXITY_MEDIUM, coreIcon, corePages );
		ResearchTypes.CORES.researchItem.setParents( this.getFirstValidParentKey( false ), PseudoResearchTypes.DISTILESSENTIA.getKey() );
		ResearchTypes.CORES.researchItem.setParentsHidden( "DISTILESSENTIA" );
		ResearchTypes.CORES.researchItem.registerResearchItem();
	}

}
