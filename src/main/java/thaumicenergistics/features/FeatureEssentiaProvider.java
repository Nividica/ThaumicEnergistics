package thaumicenergistics.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.FeatureRegistry;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.ResearchRegistry.ResearchTypes;

public class FeatureEssentiaProvider
	extends AbstractDependencyFeature
	implements IThaumcraftResearchFeature, ICraftingFeature
{

	public FeatureEssentiaProvider( final FeatureRegistry fr )
	{
		super( fr );
	}

	@Override
	protected boolean checkConfigs()
	{
		// Depends on ThE config
		if( !ThEApi.instance().config().allowedToCraftEssentiaProvider() )
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
			return ResearchTypes.ESSENTIAPROVIDER.getKey();
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
		ItemStack DiffusionCore = ThEApi.instance().items().DiffusionCore.getStack();
		ItemStack CoalescenceCore = ThEApi.instance().items().CoalescenceCore.getStack();
		ItemStack EssentiaProvider = ThEApi.instance().blocks().EssentiaProvider.getStack();

		// Set required aspects for essentia provider
		AspectList essentiaProviderList = new AspectList();
		essentiaProviderList.add( Aspect.MECHANISM, 64 );
		essentiaProviderList.add( Aspect.MAGIC, 32 );
		essentiaProviderList.add( Aspect.ORDER, 32 );
		essentiaProviderList.add( Aspect.EXCHANGE, 16 );

		// Essentia provider recipe items
		ItemStack[] essentiaProviderRecipeItems = { cdi.FilterTube, cdi.SalisMundus, CoalescenceCore, cdi.WaterShard, cdi.FilterTube,
						cdi.SalisMundus, DiffusionCore, cdi.WaterShard };

		// Create the essentia provider recipe
		RecipeRegistry.BLOCK_ESSENTIA_PROVIDER = ThaumcraftApi.addInfusionCraftingRecipe( ResearchRegistry.ResearchTypes.ESSENTIAPROVIDER.getKey(),
			EssentiaProvider, 3, essentiaProviderList, cdi.MEInterface, essentiaProviderRecipeItems );
	}

	@Override
	public void registerResearch()
	{
		AspectList essentiaProviderList = new AspectList();
		essentiaProviderList.add( Aspect.MECHANISM, 3 );
		essentiaProviderList.add( Aspect.MAGIC, 5 );
		essentiaProviderList.add( Aspect.ORDER, 3 );
		essentiaProviderList.add( Aspect.SENSES, 7 );

		ItemStack essentiaProviderIcon = new ItemStack( BlockEnum.ESSENTIA_PROVIDER.getBlock(), 1 );

		ResearchPage[] essentiaProviderPages = new ResearchPage[] { new ResearchPage( ResearchTypes.ESSENTIAPROVIDER.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_ESSENTIA_PROVIDER ) };

		ResearchTypes.ESSENTIAPROVIDER.createResearchItem( essentiaProviderList, ResearchRegistry.COMPLEXITY_LARGE, essentiaProviderIcon,
			essentiaProviderPages );
		ResearchTypes.ESSENTIAPROVIDER.researchItem.setParents( this.getFirstValidParentKey( false ) );
		ResearchTypes.ESSENTIAPROVIDER.researchItem.setParentsHidden( "INFUSION", "TUBEFILTER" );
		ResearchTypes.ESSENTIAPROVIDER.researchItem.setConcealed();
		ResearchTypes.ESSENTIAPROVIDER.researchItem.registerResearchItem();
	}

}
