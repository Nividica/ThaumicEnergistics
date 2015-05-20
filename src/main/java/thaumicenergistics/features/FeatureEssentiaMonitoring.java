package thaumicenergistics.features;

import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.registries.FeatureRegistry;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.ResearchRegistry.ResearchTypes;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cpw.mods.fml.common.registry.GameRegistry;

public class FeatureEssentiaMonitoring
	extends AbstractDependencyFeature
	implements IThaumcraftResearchFeature, ICraftingFeature
{

	private boolean isWirelessEnabled = false, isConversionEnabled = false;;

	public FeatureEssentiaMonitoring( final FeatureRegistry fr )
	{
		super( fr );
	}

	@Override
	protected boolean checkConfigs()
	{
		this.isConversionEnabled = AEConfig.instance.isFeatureEnabled( AEFeature.PartConversionMonitor );

		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		this.isWirelessEnabled = AEConfig.instance.isFeatureEnabled( AEFeature.WirelessAccessTerminal ) && ( cdi.DenseCell != null ) &&
						( cdi.WirelessReceiver != null );

		return new Object[] { cdi.LogicProcessor, cdi.CalculationProcessor };
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.ESSENTIATERMINAL.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureConversionCores.getFirstValidParentKey( true );
	}

	@Override
	public void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My items
		ItemStack DiffusionCore = ThEApi.instance().items().DiffusionCore.getStack();
		ItemStack CoalescenceCore = ThEApi.instance().items().CoalescenceCore.getStack();
		ItemStack EssentiaTerminal = ThEApi.instance().parts().Essentia_Terminal.getStack();
		ItemStack EssentiaLevelEmitter = ThEApi.instance().parts().Essentia_LevelEmitter.getStack();
		ItemStack WirelessEssentiaTerminal = ThEApi.instance().items().WirelessEssentiaTerminal.getStack();
		ItemStack EssentiaStorageMonitor = ThEApi.instance().parts().Essentia_StorageMonitor.getStack();
		ItemStack EssentiaConversionMonitor = ThEApi.instance().parts().Essentia_ConversionMonitor.getStack();

		// Essentia Terminal
		AspectList etAspectList = new AspectList();
		etAspectList.add( Aspect.WATER, 5 );
		etAspectList.add( Aspect.ORDER, 2 );
		etAspectList.add( Aspect.FIRE, 1 );
		RecipeRegistry.PART_ESSENTIA_TERMINAL = ThaumcraftApi.addShapelessArcaneCraftingRecipe(
			ResearchRegistry.ResearchTypes.ESSENTIATERMINAL.getKey(), EssentiaTerminal, etAspectList, cdi.IlluminatedPanel, DiffusionCore,
			CoalescenceCore, cdi.LogicProcessor, cdi.VisFilter );

		// Wireless Essentia Terminal
		if( this.isWirelessEnabled )
		{
			RecipeRegistry.WIRELESS_ESSENTIA_TERMINAL = new ShapelessOreRecipe( WirelessEssentiaTerminal, cdi.WirelessReceiver, EssentiaTerminal,
							cdi.DenseCell );
			GameRegistry.addRecipe( RecipeRegistry.WIRELESS_ESSENTIA_TERMINAL );
		}

		// Essentia level emitter
		AspectList emitterAspectList = new AspectList();
		emitterAspectList.add( Aspect.FIRE, 4 );
		EssentiaLevelEmitter.stackSize = 4;
		RecipeRegistry.PART_ESSENTIA_LEVEL_EMITTER = ThaumcraftApi.addShapelessArcaneCraftingRecipe(
			ResearchRegistry.ResearchTypes.ESSENTIATERMINAL.getKey(), EssentiaLevelEmitter, emitterAspectList, cdi.CalculationProcessor,
			cdi.RedstoneTorch, cdi.SalisMundus );

		// Essentia storage monitor
		RecipeRegistry.PART_ESSENTIA_STORAGE_MONITOR = new ShapelessOreRecipe( EssentiaStorageMonitor, EssentiaLevelEmitter, cdi.IlluminatedPanel );
		GameRegistry.addRecipe( RecipeRegistry.PART_ESSENTIA_STORAGE_MONITOR );

		// Essentia conversion monitor
		if( this.isConversionEnabled )
		{
			RecipeRegistry.PART_ESSENTIA_CONVERSION_MONITOR = new ShapelessOreRecipe( EssentiaConversionMonitor, CoalescenceCore,
							EssentiaStorageMonitor, DiffusionCore );
			GameRegistry.addRecipe( RecipeRegistry.PART_ESSENTIA_CONVERSION_MONITOR );
		}
	}

	@Override
	public void registerResearch()
	{
		// Set the research aspects
		AspectList etAspectList = new AspectList();
		etAspectList.add( Aspect.EXCHANGE, 5 );
		etAspectList.add( Aspect.SENSES, 5 );
		etAspectList.add( Aspect.ENERGY, 3 );
		etAspectList.add( Aspect.WATER, 3 );

		// Set the icon
		ItemStack etIcon = AEPartsEnum.EssentiaTerminal.getStack();

		// Setup pages
		ArrayList<ResearchPage> pageList = new ArrayList<ResearchPage>();
		pageList.add( new ResearchPage( ResearchTypes.ESSENTIATERMINAL.getPageName( 1 ) ) );
		pageList.add( new ResearchPage( ResearchTypes.ESSENTIATERMINAL.getPageName( 2 ) ) );
		pageList.add( new ResearchPage( RecipeRegistry.PART_ESSENTIA_TERMINAL ) );
		if( this.isWirelessEnabled )
		{
			pageList.add( new ResearchPage( RecipeRegistry.WIRELESS_ESSENTIA_TERMINAL ) );
		}
		pageList.add( new ResearchPage( RecipeRegistry.PART_ESSENTIA_LEVEL_EMITTER ) );
		pageList.add( new ResearchPage( RecipeRegistry.PART_ESSENTIA_STORAGE_MONITOR ) );
		if( this.isConversionEnabled )
		{
			pageList.add( new ResearchPage( RecipeRegistry.PART_ESSENTIA_CONVERSION_MONITOR ) );
		}

		// Set the pages
		ResearchPage[] etPages = pageList.toArray( new ResearchPage[pageList.size()] );

		// Create the IO research
		ResearchTypes.ESSENTIATERMINAL.createResearchItem( etAspectList, ResearchRegistry.COMPLEXITY_SMALL, etIcon, etPages );
		ResearchTypes.ESSENTIATERMINAL.researchItem.setParents( this.getFirstValidParentKey( false ) );
		ResearchTypes.ESSENTIATERMINAL.researchItem.setConcealed();
		ResearchTypes.ESSENTIATERMINAL.researchItem.setSecondary();
		ResearchTypes.ESSENTIATERMINAL.researchItem.registerResearchItem();
	}
}
