package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.blocks.BlockEnum;
import thaumicenergistics.common.registries.*;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import thaumicenergistics.common.tiles.TileEssentiaProvider;

/**
 * {@link TileEssentiaProvider} feature.
 * 
 * @author Nividica
 * 
 */
public class FeatureEssentiaProvider
	extends ThEThaumcraftResearchFeature
{

	public FeatureEssentiaProvider()
	{
		super( ResearchTypes.ESSENTIA_PROVIDER.getKey() );
	}

	@Override
	protected boolean checkConfigs( final IThEConfig theConfig )
	{
		// Depends on ThE config
		return theConfig.craftEssentiaProvider();
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.MEInterface };
	}

	@Override
	protected ThEThaumcraftResearchFeature getParentFeature()
	{
		return FeatureRegistry.instance().featureEssentiaIOBuses;
	}

	@Override
	protected void registerCrafting( final CommonDependantItems cdi )
	{
		// My items
		ItemStack DiffusionCore = ThEApi.instance().items().DiffusionCore.getStack();
		ItemStack CoalescenceCore = ThEApi.instance().items().CoalescenceCore.getStack();
		ItemStack EssentiaProvider = ThEApi.instance().blocks().EssentiaProvider.getStack();

		// Set Essentia Provider aspects
		AspectList essentiaProviderList = new AspectList();
		essentiaProviderList.add( Aspect.MECHANISM, 64 );
		essentiaProviderList.add( Aspect.MAGIC, 32 );
		essentiaProviderList.add( Aspect.ORDER, 32 );
		essentiaProviderList.add( Aspect.EXCHANGE, 16 );

		// Essentia Provider recipe
		ItemStack[] recipeEssentiaProvider = { cdi.FilterTube, cdi.SalisMundus, CoalescenceCore, cdi.WaterShard, cdi.FilterTube, cdi.SalisMundus,
						DiffusionCore, cdi.WaterShard };

		// Register Essentia Provider
		RecipeRegistry.BLOCK_ESSENTIA_PROVIDER = ThaumcraftApi.addInfusionCraftingRecipe( this.researchKey,
			EssentiaProvider, 3, essentiaProviderList, cdi.MEInterface, recipeEssentiaProvider );
	}

	@Override
	protected void registerPseudoParents()
	{
	}

	@Override
	protected void registerResearch()
	{
		// Set Essentia Provider research aspects
		AspectList essentiaProviderList = new AspectList();
		essentiaProviderList.add( Aspect.MECHANISM, 3 );
		essentiaProviderList.add( Aspect.MAGIC, 5 );
		essentiaProviderList.add( Aspect.ORDER, 3 );
		essentiaProviderList.add( Aspect.SENSES, 7 );

		// Set research icon
		ItemStack essentiaProviderIcon = new ItemStack( BlockEnum.ESSENTIA_PROVIDER.getBlock(), 1 );

		// Set research pages
		ResearchPage[] essentiaProviderPages = new ResearchPage[] { new ResearchPage( ResearchTypes.ESSENTIA_PROVIDER.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_ESSENTIA_PROVIDER ) };

		// Create the research
		ResearchTypes.ESSENTIA_PROVIDER.createResearchItem( essentiaProviderList, ResearchRegistry.COMPLEXITY_LARGE, essentiaProviderIcon,
			essentiaProviderPages );
		ResearchTypes.ESSENTIA_PROVIDER.researchItem.setParents( this.getFirstValidParentKey( false ) );
		ResearchTypes.ESSENTIA_PROVIDER.researchItem.setParentsHidden( "INFUSION", "TUBEFILTER" );
		ResearchTypes.ESSENTIA_PROVIDER.researchItem.setConcealed();
		ResearchTypes.ESSENTIA_PROVIDER.researchItem.registerResearchItem();
	}

}
