package thaumicenergistics.common.registries;

import thaumicenergistics.common.features.*;

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
	private final CommonDependantItems commonItems;

	/**
	 * All features in a nice compact array.
	 */
	private final ThEFeatureBase[] featuresList;

	/**
	 * Set to true when registerFeatures() is called.
	 */
	private boolean hasRegistered = false;

	/**
	 * Sets up the TC research tab.
	 */
	public final FeatureResearchSetup featureResearchSetup;

	/**
	 * Autocrafting.
	 */
	public final FeatureAutocrafting_Arcane featureAutoCrafting_Arcane;

	/**
	 * Essentia storage cells.
	 */
	public final FeatureCells featureCells;

	/**
	 * Arcane Crafting Terminal.
	 */
	public final FeatureACT featureACT;

	/**
	 * Vis Relay Interface.
	 */
	public final FeatureVisRelayInterface featureVRI;

	/**
	 * Essentia IO buses.
	 */
	public final FeatureEssentiaIOBuses featureEssentiaIOBuses;

	/**
	 * Infusion provider.
	 */
	public final FeatureInfusionProvider featureInfusionProvider;

	/**
	 * Essentia Provider.
	 */
	public final FeatureEssentiaProvider featureEssentiaProvider;

	/**
	 * Terminal, wireless terminal, level emitter, and storage monitors.
	 */
	public final FeatureEssentiaMonitoring featureEssentiaMonitoring;

	/**
	 * Conversion cores.
	 */
	public final FeatureConversionCores featureConversionCores;

	/**
	 * Gearboxes.
	 */
	public final FeatureGearbox featureGearbox;

	/**
	 * Wrench focus.
	 */
	public final FeatureWrenchFocus featureWrenchFocus;

	/**
	 * Quartz duplication.
	 */
	public final FeatureQuartzDupe featureQuartzDupe;

	/**
	 * Essentia Vibration Chamber.
	 */
	public final FeatureEssentiaVibrationChamber featureEssentiaVibrationChamber;

	/**
	 * Distillation Pattern Encoder.
	 */
	public final FeatureAutocrafting_Essentia featureAutocrafting_Essentia;

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
		this.featureAutoCrafting_Arcane = new FeatureAutocrafting_Arcane();

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

		// Build distillation pattern encoder
		this.featureAutocrafting_Essentia = new FeatureAutocrafting_Essentia();

		// Build array of features
		this.featuresList = new ThEFeatureBase[] { this.featureAutoCrafting_Arcane, this.featureCells, this.featureACT, this.featureVRI,
						this.featureEssentiaIOBuses, this.featureInfusionProvider, this.featureEssentiaProvider, this.featureEssentiaMonitoring,
						this.featureConversionCores, this.featureGearbox, this.featureWrenchFocus, this.featureQuartzDupe,
						this.featureEssentiaVibrationChamber, this.featureAutocrafting_Essentia };
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
		for( ThEFeatureBase feature : this.featuresList )
		{
			// Is the feature a dependency feature?
			if( feature instanceof ThEDependencyFeatureBase )
			{
				// Evaluate the dependencies
				( (ThEDependencyFeatureBase)feature ).evaluateDependencies( this );
			}
		}

		// Start with the setup
		this.featureResearchSetup.registerFeature();

		// Register each feature
		for( ThEFeatureBase feature : this.featuresList )
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
