package thaumicenergistics.registries;

import thaumicenergistics.features.AbstractBasicFeature;
import thaumicenergistics.features.CommonDependantItems;
import thaumicenergistics.features.FeatureACT;
import thaumicenergistics.features.FeatureAutocrafting;
import thaumicenergistics.features.FeatureCells;
import thaumicenergistics.features.FeatureConversionCores;
import thaumicenergistics.features.FeatureEssentiaIOBuses;
import thaumicenergistics.features.FeatureEssentiaMonitoring;
import thaumicenergistics.features.FeatureEssentiaProvider;
import thaumicenergistics.features.FeatureGearbox;
import thaumicenergistics.features.FeatureInfusionProvider;
import thaumicenergistics.features.FeatureQuartzDupe;
import thaumicenergistics.features.FeatureResearchSetup;
import thaumicenergistics.features.FeatureVisRelayInterface;
import thaumicenergistics.features.FeatureWrenchFocus;
import thaumicenergistics.features.ICraftingFeature;
import thaumicenergistics.features.IThaumcraftResearchFeature;

/**
 * Acts as a safe way to depend on features that can be disabled in both TC and
 * AE.
 * 
 * @author Nividica
 * 
 */
public class FeatureRegistry
{

	/**
	 * Singleton instance of the FR
	 */
	private static FeatureRegistry instanceFR;

	/**
	 * Commonly used dependency items.
	 */
	private CommonDependantItems commonItems;

	/**
	 * True if the dependencies have been built.
	 */
	private boolean hasBuiltDependencies = false;

	/**
	 * Sets up the TC research tab.
	 */
	public FeatureResearchSetup featureResearchSetup;

	/**
	 * Autocrafting.
	 */
	public FeatureAutocrafting featureAutoCrafting;

	/**
	 * Essentia storage cells.
	 */
	public FeatureCells featureCells;

	/**
	 * Arcane Crafting Terminal.
	 */
	public FeatureACT featureACT;

	/**
	 * Vis Relay Interface.
	 */
	public FeatureVisRelayInterface featureVRI;

	/**
	 * Essentia IO buses.
	 */
	public FeatureEssentiaIOBuses featureEssentiaIOBuses;

	/**
	 * Infusion provider.
	 */
	public FeatureInfusionProvider featureInfusionProvider;

	/**
	 * Essentia Provider.
	 */
	public FeatureEssentiaProvider featureEssentiaProvider;

	/**
	 * Terminal, wireless terminal, level emitter, and storage monitors.
	 */
	public FeatureEssentiaMonitoring featureEssentiaMonitoring;

	/**
	 * Conversion cores.
	 */
	public FeatureConversionCores featureConversionCores;

	/**
	 * Gearboxes.
	 */
	public FeatureGearbox featureGearbox;

	/**
	 * Wrench focus.
	 */
	public FeatureWrenchFocus featureWrenchFocus;

	/**
	 * Quartz duplication
	 */
	public FeatureQuartzDupe featureQuartzDupe;

	/**
	 * Private constructor
	 */
	private FeatureRegistry()
	{
	}

	/**
	 * Gets the instance of the FR
	 * 
	 * @return
	 */
	public static FeatureRegistry instance()
	{
		if( FeatureRegistry.instanceFR == null )
		{
			FeatureRegistry.instanceFR = new FeatureRegistry();
		}

		return FeatureRegistry.instanceFR;
	}

	/**
	 * Determines what features can or can not be enabled.
	 */
	private void buildDependencies()
	{
		// Build common items
		this.commonItems = new CommonDependantItems();

		// Build setup
		this.featureResearchSetup = new FeatureResearchSetup();

		// Build autocrafting
		this.featureAutoCrafting = new FeatureAutocrafting( this );

		// Build cells
		this.featureCells = new FeatureCells( this );

		// Build ACT
		this.featureACT = new FeatureACT( this );

		// Build VRI
		this.featureVRI = new FeatureVisRelayInterface( this );

		// Build IO buses
		this.featureEssentiaIOBuses = new FeatureEssentiaIOBuses( this );

		// Build infusion provider
		this.featureInfusionProvider = new FeatureInfusionProvider( this );

		// Build essentia provider
		this.featureEssentiaProvider = new FeatureEssentiaProvider( this );

		// Build monitoring
		this.featureEssentiaMonitoring = new FeatureEssentiaMonitoring( this );

		// Build conversion cores
		this.featureConversionCores = new FeatureConversionCores( this );

		// Build gearboxes
		this.featureGearbox = new FeatureGearbox();

		// Build wrench focus
		this.featureWrenchFocus = new FeatureWrenchFocus( this );

		// Build quartz dupe
		this.featureQuartzDupe = new FeatureQuartzDupe( this );

		this.hasBuiltDependencies = true;
	}

	/**
	 * Gets items that are depended on.
	 * 
	 * @return
	 */
	public CommonDependantItems getCommonItems()
	{
		return this.commonItems;
	}

	/**
	 * Registers all features of ThE with Minecraft.
	 */
	public void registerFeatures()
	{
		// Ensure the dependencies have been built
		if( !this.hasBuiltDependencies )
		{
			this.buildDependencies();
		}

		// Start with the setup
		this.featureResearchSetup.registerResearch();

		// List of features
		AbstractBasicFeature[] features = new AbstractBasicFeature[] { this.featureAutoCrafting, this.featureCells, this.featureACT, this.featureVRI,
						this.featureEssentiaIOBuses, this.featureInfusionProvider, this.featureEssentiaProvider, this.featureEssentiaMonitoring,
						this.featureConversionCores, this.featureGearbox, this.featureWrenchFocus, this.featureQuartzDupe };

		for( AbstractBasicFeature feature : features )
		{
			if( feature.isAvailable() )
			{
				// Crafting?
				if( feature instanceof ICraftingFeature )
				{
					( (ICraftingFeature)feature ).registerCrafting();
				}

				// Research?
				if( feature instanceof IThaumcraftResearchFeature )
				{
					( (IThaumcraftResearchFeature)feature ).registerResearch();
				}
			}
		}
	}

}
