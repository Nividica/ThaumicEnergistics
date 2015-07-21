package thaumicenergistics.features;

import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEItems;
import thaumicenergistics.api.IThEParts;
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
{

	private boolean isWirelessEnabled = false, isConversionEnabled = false;

	@Override
	protected boolean checkConfigs()
	{
		this.isConversionEnabled = AEConfig.instance.isFeatureEnabled( AEFeature.PartConversionMonitor );
		this.isWirelessEnabled = AEConfig.instance.isFeatureEnabled( AEFeature.WirelessAccessTerminal );
		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		this.isWirelessEnabled &= ( cdi.DenseCell != null ) && ( cdi.WirelessReceiver != null );

		return new Object[] { cdi.LogicProcessor, cdi.CalculationProcessor };
	}

	@Override
	protected void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My items
		IThEItems theItems = ThEApi.instance().items();
		IThEParts theParts = ThEApi.instance().parts();
		ItemStack DiffusionCore = theItems.DiffusionCore.getStack();
		ItemStack CoalescenceCore = theItems.CoalescenceCore.getStack();
		ItemStack EssentiaTerminal = theParts.Essentia_Terminal.getStack();
		ItemStack EssentiaLevelEmitter = theParts.Essentia_LevelEmitter.getStack();
		ItemStack EssentiaStorageMonitor = theParts.Essentia_StorageMonitor.getStack();

		// Set Essentia Terminal aspects
		AspectList etAspectList = new AspectList();
		etAspectList.add( Aspect.WATER, 5 );
		etAspectList.add( Aspect.ORDER, 2 );
		etAspectList.add( Aspect.FIRE, 1 );

		// Register Essentia Terminal
		RecipeRegistry.PART_ESSENTIA_TERMINAL = ThaumcraftApi.addShapelessArcaneCraftingRecipe(
			ResearchRegistry.ResearchTypes.ESSENTIATERMINAL.getKey(), EssentiaTerminal, etAspectList, cdi.IlluminatedPanel, DiffusionCore,
			CoalescenceCore, cdi.LogicProcessor, cdi.VisFilter );

		// Is wireless term enabled?
		if( this.isWirelessEnabled )
		{
			ItemStack WirelessEssentiaTerminal = theItems.WirelessEssentiaTerminal.getStack();

			// Register Wireless Essentia Terminal
			GameRegistry.addRecipe( RecipeRegistry.ITEM_WIRELESS_ESSENTIA_TERMINAL = new ShapelessOreRecipe( WirelessEssentiaTerminal,
							cdi.WirelessReceiver, EssentiaTerminal, cdi.DenseCell ) );
		}

		// Set Essentia Level Emitter aspects
		AspectList emitterAspectList = new AspectList();
		emitterAspectList.add( Aspect.FIRE, 4 );

		// Set Essentia Level Emitter crafting result count
		EssentiaLevelEmitter.stackSize = 4;

		// Register Essentia Level Emitter
		RecipeRegistry.PART_ESSENTIA_LEVEL_EMITTER = ThaumcraftApi.addShapelessArcaneCraftingRecipe(
			ResearchRegistry.ResearchTypes.ESSENTIATERMINAL.getKey(), EssentiaLevelEmitter, emitterAspectList, cdi.CalculationProcessor,
			cdi.RedstoneTorch, cdi.SalisMundus );

		// Register Essentia Storage Monitor
		GameRegistry.addRecipe( RecipeRegistry.PART_ESSENTIA_STORAGE_MONITOR = new ShapelessOreRecipe( EssentiaStorageMonitor, EssentiaLevelEmitter,
						cdi.IlluminatedPanel ) );

		// Is conversion monitor enabled?
		if( this.isConversionEnabled )
		{
			ItemStack EssentiaConversionMonitor = theParts.Essentia_ConversionMonitor.getStack();

			// Register Essentia Conversion Monitor
			GameRegistry.addRecipe( RecipeRegistry.PART_ESSENTIA_CONVERSION_MONITOR = new ShapelessOreRecipe( EssentiaConversionMonitor,
							CoalescenceCore, EssentiaStorageMonitor, DiffusionCore ) );
		}
	}

	@Override
	protected void registerResearch()
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
			pageList.add( new ResearchPage( RecipeRegistry.ITEM_WIRELESS_ESSENTIA_TERMINAL ) );
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
}
