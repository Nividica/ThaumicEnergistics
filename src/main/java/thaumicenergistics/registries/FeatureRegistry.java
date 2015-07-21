package thaumicenergistics.registries;

import thaumicenergistics.features.AbstractBasicFeature;
import thaumicenergistics.features.AbstractDependencyFeature;
import thaumicenergistics.features.CommonDependantItems;
import thaumicenergistics.features.FeatureACT;
import thaumicenergistics.features.FeatureAutocrafting;
import thaumicenergistics.features.FeatureCells;
import thaumicenergistics.features.FeatureConversionCores;
import thaumicenergistics.features.FeatureEssentiaIOBuses;
import thaumicenergistics.features.FeatureEssentiaMonitoring;
import thaumicenergistics.features.FeatureEssentiaProvider;
import thaumicenergistics.features.FeatureEssentiaVibrationChamber;
import thaumicenergistics.features.FeatureGearbox;
import thaumicenergistics.features.FeatureInfusionProvider;
import thaumicenergistics.features.FeatureQuartzDupe;
import thaumicenergistics.features.FeatureResearchSetup;
import thaumicenergistics.features.FeatureVisRelayInterface;
import thaumicenergistics.features.FeatureWrenchFocus;

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
	 * Set to true when registerFeatures() is called.
	 */
	private boolean hasRegistered = false;

	/**
	 * All features in a nice compact array.
	 */
	private final AbstractBasicFeature[] featuresList;

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
	 * Essentia Vibration Chamber
	 */
	public FeatureEssentiaVibrationChamber featureEssentiaVibrationChamber;

	/**
	 * Private constructor
	 */
	private FeatureRegistry()
	{
		// Build common items
		this.commonItems = new CommonDependantItems();

		// Build setup
		this.featureResearchSetup = new FeatureResearchSetup();

		// Build autocrafting
		this.featureAutoCrafting = new FeatureAutocrafting();

		// Build cells
		this.featureCells = new FeatureCells();

		// Build ACT
		this.featureACT = new FeatureACT();

		// Build VRI
		this.featureVRI = new FeatureVisRelayInterface();

		// Build IO buses
		this.featureEssentiaIOBuses = new FeatureEssentiaIOBuses();

		// Build infusion provider
		this.featureInfusionProvider = new FeatureInfusionProvider();

		// Build essentia provider
		this.featureEssentiaProvider = new FeatureEssentiaProvider();

		// Build monitoring
		this.featureEssentiaMonitoring = new FeatureEssentiaMonitoring();

		// Build conversion cores
		this.featureConversionCores = new FeatureConversionCores();

		// Build gearboxes
		this.featureGearbox = new FeatureGearbox();

		// Build wrench focus
		this.featureWrenchFocus = new FeatureWrenchFocus();

		// Build quartz dupe
		this.featureQuartzDupe = new FeatureQuartzDupe();

		// Build essentia vibration chamber
		this.featureEssentiaVibrationChamber = new FeatureEssentiaVibrationChamber();

		// Build array of features
		this.featuresList = new AbstractBasicFeature[] { this.featureAutoCrafting, this.featureCells, this.featureACT, this.featureVRI,
						this.featureEssentiaIOBuses, this.featureInfusionProvider, this.featureEssentiaProvider, this.featureEssentiaMonitoring,
						this.featureConversionCores, this.featureGearbox, this.featureWrenchFocus, this.featureQuartzDupe,
						this.featureEssentiaVibrationChamber };
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
		// Has registration already occurred?
		if( this.hasRegistered )
		{
			// Bail
			return;
		}

		// Build common items
		this.commonItems.buildCommon();

		// Build dependencies
		for( AbstractBasicFeature feature : this.featuresList )
		{
			// Is the feature a dependency feature?
			if( feature instanceof AbstractDependencyFeature )
			{
				// Evaluate the dependencies
				( (AbstractDependencyFeature)feature ).evaluateDependencies( this );
			}
		}

		// Start with the setup
		this.featureResearchSetup.registerFeature();

		// Register each feature
		for( AbstractBasicFeature feature : this.featuresList )
		{
			// Attempt to register the feature
			feature.registerFeature();
		}

		// Finish the registration
		this.featureResearchSetup.finalizeRegistration( this.featuresList );

		// Mark that registration has occurred.
		this.hasRegistered = true;
	}

}
