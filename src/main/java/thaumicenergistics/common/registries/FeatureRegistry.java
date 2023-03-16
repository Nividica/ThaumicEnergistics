package thaumicenergistics.common.registries;

import java.util.ArrayList;

import thaumicenergistics.common.features.*;

/**
 * Acts as a safe way to depend on features that can be disabled in both TC and AE.
 *
 * @author Nividica
 *
 */
public class FeatureRegistry {

    /**
     * Singleton instance of the FR
     */
    private static FeatureRegistry INSTANCE;

    /**
     * All features.
     */
    private final ArrayList<ThEFeatureBase> featuresList = new ArrayList<ThEFeatureBase>(20);

    /**
     * Set to true when registerFeatures() is called.
     */
    private boolean hasRegistered = false;

    /**
     * Commonly used dependency items.
     */
    public final CommonDependantItems cdi;

    /**
     * Sets up the TC research tab.
     */
    public final FeatureThaumicEnergisticsResearch featureThaumicEnergistics;

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
     * Thaumcraft facades.
     */
    public final FeatureThaumcraftFacades featureTCFacades;

    /**
     * Golem wifi backpack.
     */
    public final FeatureGolemBackpack featureGolemWifiBackpack;

    /**
     * Cell microscope.
     */
    public final FeatureCellMicroscope featureCellMicroscope;

    /**
     * Private constructor
     */
    private FeatureRegistry() {
        // Build common items
        this.cdi = new CommonDependantItems();

        // Build ThE primary node
        this.featuresList.add(this.featureThaumicEnergistics = new FeatureThaumicEnergisticsResearch());

        // Build autocrafting
        this.featuresList.add(this.featureAutoCrafting_Arcane = new FeatureAutocrafting_Arcane());

        // Build cells
        this.featuresList.add(this.featureCells = new FeatureCells());

        // Build ACT
        this.featuresList.add(this.featureACT = new FeatureACT());

        // Build VRI
        this.featuresList.add(this.featureVRI = new FeatureVisRelayInterface());

        // Build IO buses
        this.featuresList.add(this.featureEssentiaIOBuses = new FeatureEssentiaIOBuses());

        // Build infusion provider
        this.featuresList.add(this.featureInfusionProvider = new FeatureInfusionProvider());

        // Build essentia provider
        this.featuresList.add(this.featureEssentiaProvider = new FeatureEssentiaProvider());

        // Build monitoring
        this.featuresList.add(this.featureEssentiaMonitoring = new FeatureEssentiaMonitoring());

        // Build conversion cores
        this.featuresList.add(this.featureConversionCores = new FeatureConversionCores());

        // Build gearboxes
        this.featuresList.add(this.featureGearbox = new FeatureGearbox());

        // Build wrench focus
        this.featuresList.add(this.featureWrenchFocus = new FeatureWrenchFocus());

        // Build quartz dupe
        this.featuresList.add(this.featureQuartzDupe = new FeatureQuartzDupe());

        // Build essentia vibration chamber
        this.featuresList.add(this.featureEssentiaVibrationChamber = new FeatureEssentiaVibrationChamber());

        // Build distillation pattern encoder
        this.featuresList.add(this.featureAutocrafting_Essentia = new FeatureAutocrafting_Essentia());

        // Build TC facades
        this.featuresList.add(this.featureTCFacades = new FeatureThaumcraftFacades());

        // Build golem wifi backpack
        this.featuresList.add(this.featureGolemWifiBackpack = new FeatureGolemBackpack());

        // Build cell microscope
        this.featuresList.add(this.featureCellMicroscope = new FeatureCellMicroscope());
    }

    /**
     * Gets the instance of the FR
     *
     * @return
     */
    public static FeatureRegistry instance() {
        if (FeatureRegistry.INSTANCE == null) {
            FeatureRegistry.INSTANCE = new FeatureRegistry();
        }

        return FeatureRegistry.INSTANCE;
    }

    /**
     * Registers all features of ThE with Minecraft.
     */
    public void registerFeatures() {
        // Has registration already occurred?
        if (this.hasRegistered) {
            // Bail
            return;
        }

        // Register each feature
        for (ThEFeatureBase feature : this.featuresList) {
            // Attempt to register the feature
            feature.registerFeature(this.cdi);
        }

        // Mark that registration has occurred.
        this.hasRegistered = true;
    }
}
