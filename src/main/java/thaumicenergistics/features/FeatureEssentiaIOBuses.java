package thaumicenergistics.features;

import java.util.ArrayList;
import java.util.EnumSet;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.registries.FeatureRegistry;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.registries.ResearchRegistry.ResearchTypes;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

public class FeatureEssentiaIOBuses
	extends AbstractDependencyFeature
{

	private boolean isImportExportEnabled = false;

	public FeatureEssentiaIOBuses( final FeatureRegistry fr )
	{
		super( fr );
	}

	@Override
	protected boolean checkConfigs()
	{
		this.isImportExportEnabled = ( AEConfig.instance.isFeatureEnabled( AEFeature.ImportBus ) || AEConfig.instance
						.isFeatureEnabled( AEFeature.ExportBus ) );

		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return null;
	}

	@Override
	protected void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// My items
		ItemStack DiffusionCore = ThEApi.instance().items().DiffusionCore.getStack();
		ItemStack CoalescenceCore = ThEApi.instance().items().CoalescenceCore.getStack();
		ItemStack EssentiaStorageBus = ThEApi.instance().parts().Essentia_StorageBus.getStack();

		// Set Storage Bus aspects
		AspectList storageAspectList = new AspectList();
		storageAspectList.add( Aspect.FIRE, 3 );
		storageAspectList.add( Aspect.EARTH, 3 );
		storageAspectList.add( Aspect.WATER, 1 );

		// Storage Bus recipe
		Object[] recipeStorageBus = new Object[] { true, "DFC", "IWI", 'D', DiffusionCore, 'C', CoalescenceCore, 'I', cdi.IronIngot, 'F',
						cdi.FilterTube, 'W', cdi.WardedGlass };

		// Register the storage bus
		RecipeRegistry.PART_STORAGE_BUS = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IO.getKey(), EssentiaStorageBus,
			storageAspectList, recipeStorageBus );

		// Is import and export enabled?
		if( this.isImportExportEnabled )
		{
			// My items
			ItemStack EssentiaImportBus = ThEApi.instance().parts().Essentia_ImportBus.getStack();
			ItemStack EssentiaExportBus = ThEApi.instance().parts().Essentia_ExportBus.getStack();

			// Set IO Bus aspects
			AspectList ioAspectList = new AspectList();
			ioAspectList.add( Aspect.FIRE, 2 );
			ioAspectList.add( Aspect.EARTH, 2 );
			ioAspectList.add( Aspect.WATER, 1 );

			// Import Bus recipe
			Object[] recipeImportBus = new Object[] { "JDJ", "IFI", 'J', cdi.WardedJar, 'D', DiffusionCore, 'I', cdi.IronIngot, 'F', cdi.FilterTube };

			// Export Bus recipe
			Object[] recipeExportBus = new Object[] { "JCJ", "IFI", 'J', cdi.WardedJar, 'C', CoalescenceCore, 'I', cdi.IronIngot, 'F', cdi.FilterTube };

			// Register Import Bus
			RecipeRegistry.PART_IMPORT_BUS = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IO.getKey(), EssentiaImportBus,
				ioAspectList, recipeImportBus );

			// Register Export Bus
			RecipeRegistry.PART_EXPORT_BUS = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IO.getKey(), EssentiaExportBus,
				ioAspectList, recipeExportBus );
		}
	}

	@Override
	protected void registerResearch()
	{
		// Set the research aspects
		AspectList ioAspectList = new AspectList();
		ioAspectList.add( Aspect.MECHANISM, 5 );
		ioAspectList.add( Aspect.METAL, 3 );
		ioAspectList.add( Aspect.CRYSTAL, 3 );
		ioAspectList.add( Aspect.AIR, 3 );

		// Set the icon
		ItemStack ioIcon = AEPartsEnum.EssentiaExportBus.getStack();

		ArrayList<ResearchPage> pageList = new ArrayList<ResearchPage>();

		// Info pages
		if( this.isImportExportEnabled )
		{
			pageList.add( new ResearchPage( ResearchTypes.IO.getPageName( 1 ) ) );
		}
		pageList.add( new ResearchPage( ResearchTypes.IO.getPageName( 2 ) ) );

		// Recipe pages
		if( this.isImportExportEnabled )
		{
			pageList.add( new ResearchPage( RecipeRegistry.PART_IMPORT_BUS ) );
			pageList.add( new ResearchPage( RecipeRegistry.PART_EXPORT_BUS ) );
		}

		pageList.add( new ResearchPage( RecipeRegistry.PART_STORAGE_BUS ) );

		// Set the pages
		ResearchPage[] ioPages = pageList.toArray( new ResearchPage[pageList.size()] );

		// Create the IO research
		ResearchTypes.IO.createResearchItem( ioAspectList, ResearchRegistry.COMPLEXITY_MEDIUM, ioIcon, ioPages );
		ResearchTypes.IO.researchItem.setParents( this.getFirstValidParentKey( false ), PseudoResearchTypes.TUBEFILTER.getKey() );
		ResearchTypes.IO.researchItem.setParentsHidden( "TUBEFILTER" );
		ResearchTypes.IO.researchItem.setConcealed();
		ResearchTypes.IO.researchItem.registerResearchItem();
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.IO.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureConversionCores.getFirstValidParentKey( true );
	}

	@Override
	public EnumSet<PseudoResearchTypes> getPseudoParentTypes()
	{
		return EnumSet.of( PseudoResearchTypes.TUBEFILTER );
	}
}
