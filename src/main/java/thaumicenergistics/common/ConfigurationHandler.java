package thaumicenergistics.common;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import thaumicenergistics.api.IThEConfig;

/**
 * Controls the ThE configuration.
 *
 * @author Nividica
 *
 */
class ConfigurationHandler implements IThEConfig {
    private static ConfigurationHandler INSTANCE;

    /**
     * Names of the categories.
     */
    private static final String CATEGORY_CRAFTING = "crafting",
            CATEGORY_CLIENT = "client",
            CATEGORY_INTEGRATION = "integration";

    /**
     * Default values.
     */
    private static final int DFT_CONVERSION_EXPONENT = 7;

    private static final boolean DFT_CRAFT_ARCANE_ASSEMBLER = true,
            DFT_CRAFT_ARCANE_CRAFTING_TERM = true,
            DFT_CRAFT_DISTILLATION_PATTERN_ENCODER = true,
            DFT_CRAFT_ESSENTIA_CELLS = true,
            DFT_CRAFT_ESSENTIA_PROVIDER = true,
            DFT_CRAFT_ESSENTIA_VIBRATION_CHAMBER = true,
            DFT_CRAFT_GOLEM_BACKPACK = true,
            DFT_CRAFT_INFUSION_PROVIDER = true,
            DFT_CRAFT_IO_BUSES = true,
            DFT_CRAFT_VIS_RELAY_INTERFACE = true,
            DFT_CRAFT_WIRELESS_ESSENTIA_TERM = true,
            DFT_DISABLE_GEARBOX = false,
            DFT_ENABLE_QUARTZ_DUPE = true,
            DFT_ENABLE_WRENCH_FOCUS = true,
            DFT_EXTRACELLS_BLIST = true,
            DFT_FORCE_TC_FACADES = true;

    /**
     * Mod configuration
     */
    private Configuration configSettings;

    /**
     * Controls the conversion ratio of essentia/fluid. <BR>
     * 1 essentia unit is converted to this many mb's of fluid.
     */
    private int conversionMultiplier = (int) Math.pow(2, DFT_CONVERSION_EXPONENT);

    private boolean craft_Arcane_Assembler = DFT_CRAFT_ARCANE_ASSEMBLER,
            craft_Arcane_Crafting_Term = DFT_CRAFT_ARCANE_CRAFTING_TERM,
            craft_Distillation_Pattern_Encoder = DFT_CRAFT_DISTILLATION_PATTERN_ENCODER,
            craft_Essentia_Cells = DFT_CRAFT_ESSENTIA_CELLS,
            craft_Essentia_Provider = DFT_CRAFT_ESSENTIA_PROVIDER,
            craft_Essentia_Vibration_Chamber = DFT_CRAFT_ESSENTIA_VIBRATION_CHAMBER,
            craft_GolemBackpack = DFT_CRAFT_GOLEM_BACKPACK,
            craft_Infusion_Provider = DFT_CRAFT_INFUSION_PROVIDER,
            craft_IO_Buses = DFT_CRAFT_IO_BUSES,
            craft_Vis_Relay_Interface = DFT_CRAFT_VIS_RELAY_INTERFACE,
            craft_Wireless_Essentia_Term = DFT_CRAFT_WIRELESS_ESSENTIA_TERM,
            disable_Gearbox = DFT_DISABLE_GEARBOX,
            enable_Quartz_Dupe = DFT_ENABLE_QUARTZ_DUPE,
            enable_Wrench_Focus = DFT_ENABLE_WRENCH_FOCUS,
            extracells_Blist = DFT_EXTRACELLS_BLIST,
            force_TC_Facades = DFT_FORCE_TC_FACADES;

    private ConfigurationHandler(final Configuration config) {
        this.configSettings = config;
        this.synchronizeConfigFile();
    }

    /**
     * Loads and synchronizes the config file.
     *
     * @param configFile
     */
    public static IThEConfig loadAndSyncConfigFile(final File configFile) {
        if (ConfigurationHandler.INSTANCE == null) {
            ConfigurationHandler.INSTANCE = new ConfigurationHandler(new Configuration(configFile));
        }
        return ConfigurationHandler.INSTANCE;
    }

    /**
     * Synchronizes the config file and the settings.
     */
    private void synchronizeConfigFile() {
        // General =========================================================
        int fluidPow = this.configSettings.getInt(
                "Essentia Fluid Ratio Exponent",
                Configuration.CATEGORY_GENERAL,
                ConfigurationHandler.DFT_CONVERSION_EXPONENT,
                1,
                11,
                "Controls the conversion ratio of essentia/fluid. 1 essentia is converted to (2^this) milibuckets of fluid. "
                        + "Please be aware that this value effects how much fluid is transferred through the AE system, which also effects transfer speed and power consumption. "
                        + "Values above 11 make it impossible to use fluid transfer devices such as the ME IO Port, or anything from EC2.");
        this.conversionMultiplier = (int) Math.pow(2, Math.min(fluidPow, 11));

        // Crafting ========================================================
        this.craft_Arcane_Assembler = this.configSettings.getBoolean(
                "Arcane Assembler",
                CATEGORY_CRAFTING,
                DFT_CRAFT_ARCANE_ASSEMBLER,
                "Controls if the Arcane Assembler can be crafted.");

        this.craft_Arcane_Crafting_Term = this.configSettings.getBoolean(
                "Arcane Crafting Terminal",
                CATEGORY_CRAFTING,
                DFT_CRAFT_ARCANE_CRAFTING_TERM,
                "Controls if the Arcane Crafting Terminal can be crafted.");

        this.craft_Distillation_Pattern_Encoder = this.configSettings.getBoolean(
                "Distillation Pattern Encoder.",
                CATEGORY_CRAFTING,
                DFT_CRAFT_DISTILLATION_PATTERN_ENCODER,
                "Controls if the Distillation Pattern Encoder can be crafted.");

        this.craft_Essentia_Cells = this.configSettings.getBoolean(
                "Essentia Cells",
                CATEGORY_CRAFTING,
                DFT_CRAFT_ESSENTIA_CELLS,
                "Controls if Essentia Cells and Components can be crafted.");

        this.craft_Essentia_Provider = this.configSettings.getBoolean(
                "Essentia Provider",
                CATEGORY_CRAFTING,
                DFT_CRAFT_ESSENTIA_PROVIDER,
                "Controls if the Essentia Provider can be crafted.");

        this.craft_Essentia_Vibration_Chamber = this.configSettings.getBoolean(
                "Essentia Vibration Chamber",
                CATEGORY_CRAFTING,
                DFT_CRAFT_ESSENTIA_VIBRATION_CHAMBER,
                "Controlls if the Essentia Vibration Chamber can be crafted.");

        this.craft_GolemBackpack = this.configSettings.getBoolean(
                "Golem Wifi Backpack",
                CATEGORY_CRAFTING,
                DFT_CRAFT_GOLEM_BACKPACK,
                "Controls if the Golem Wifi Backpack can be crafted.");

        this.craft_Infusion_Provider = this.configSettings.getBoolean(
                "Infusion Provider",
                CATEGORY_CRAFTING,
                DFT_CRAFT_INFUSION_PROVIDER,
                "Controls if the Infusion Provider can be crafted.");

        this.craft_IO_Buses = this.configSettings.getBoolean(
                "IO Buses",
                CATEGORY_CRAFTING,
                DFT_CRAFT_IO_BUSES,
                "Controls if the import and export buses can be crafted.");

        this.craft_Vis_Relay_Interface = this.configSettings.getBoolean(
                "Vis Relay Interface",
                CATEGORY_CRAFTING,
                DFT_CRAFT_VIS_RELAY_INTERFACE,
                "Controls if the Vis Relay Interface can be crafted.");

        this.craft_Wireless_Essentia_Term = this.configSettings.getBoolean(
                "Allow Crafting Wireless Essentia Terminal",
                CATEGORY_CRAFTING,
                DFT_CRAFT_WIRELESS_ESSENTIA_TERM,
                "Controls if the Wireless Essentia Terminal can be crafted.");

        this.enable_Quartz_Dupe = this.configSettings.getBoolean(
                "Certus Quartz Duplication",
                CATEGORY_CRAFTING,
                DFT_ENABLE_QUARTZ_DUPE,
                "Controls if Certus Quartz can be duplicated in the crucible.");

        this.enable_Wrench_Focus = this.configSettings.getBoolean(
                "Wrench Focus",
                CATEGORY_CRAFTING,
                DFT_ENABLE_WRENCH_FOCUS,
                "Controls if the Wrench Focus is enabled and craftable.");

        this.force_TC_Facades = this.configSettings.getBoolean(
                "Force TC Facades",
                CATEGORY_CRAFTING,
                DFT_FORCE_TC_FACADES,
                "When enabled, overwrites the AE2 facade settings for some Thaumcraft blocks, allowing their facades to be crafted.");

        // Client ==========================================================
        this.disable_Gearbox = this.configSettings.getBoolean(
                "Disable Gearbox Model",
                CATEGORY_CLIENT,
                DFT_DISABLE_GEARBOX,
                "The iron and thaumium gearboxes will be rendered as a standard block.");

        // Integration =====================================================
        this.extracells_Blist = this.configSettings.getBoolean(
                "ExtraCells Blacklist",
                CATEGORY_INTEGRATION,
                DFT_EXTRACELLS_BLIST,
                "Prevents extra cells from interacting with essentia gas");

        // Has the config file changed?
        if (this.configSettings.hasChanged()) {
            // Save it
            this.configSettings.save();
        }
    }

    @Override
    public boolean blacklistEssentiaFluidInExtraCells() {
        return this.extracells_Blist;
    }

    @Override
    public int conversionMultiplier() {
        return this.conversionMultiplier;
    }

    @Override
    public boolean craftArcaneAssembler() {
        return this.craft_Arcane_Assembler;
    }

    @Override
    public boolean craftArcaneCraftingTerminal() {
        return this.craft_Arcane_Crafting_Term;
    }

    @Override
    public boolean craftDistillationPatternEncoder() {
        return this.craft_Distillation_Pattern_Encoder;
    }

    @Override
    public boolean craftEssentiaCells() {
        return this.craft_Essentia_Cells;
    }

    @Override
    public boolean craftEssentiaProvider() {
        return this.craft_Essentia_Provider;
    }

    @Override
    public boolean craftEssentiaVibrationChamber() {
        return this.craft_Essentia_Vibration_Chamber;
    }

    @Override
    public boolean craftGolemWifiBackpack() {
        return this.craft_GolemBackpack;
    }

    @Override
    public boolean craftInfusionProvider() {
        return this.craft_Infusion_Provider;
    }

    @Override
    public boolean craftIOBuses() {
        return this.craft_IO_Buses;
    }

    @Override
    public boolean craftVisRelayInterface() {
        return this.craft_Vis_Relay_Interface;
    }

    @Override
    public boolean craftWirelessEssentiaTerminal() {
        return this.craft_Wireless_Essentia_Term;
    }

    @Override
    public boolean disableGearboxModel() {
        return this.disable_Gearbox;
    }

    @Override
    public boolean enableCertusQuartzDupe() {
        return this.enable_Quartz_Dupe;
    }

    @Override
    public boolean enableWrenchFocus() {
        return this.enable_Wrench_Focus;
    }

    @Override
    public boolean forceTCFacades() {
        return this.force_TC_Facades;
    }
}
