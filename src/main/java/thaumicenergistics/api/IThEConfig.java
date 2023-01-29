package thaumicenergistics.api;

/**
 * Thaumic Energistics configuration options.
 *
 * @author Nividica
 *
 */
public interface IThEConfig {

    /**
     * If true essentia gas will be blacklisted from ExtraCells.
     *
     * @return
     */
    boolean blacklistEssentiaFluidInExtraCells();

    /**
     * Controls the conversion ratio of essentia/fluid. <BR>
     * 1 essentia unit is converted to this many mb's of fluid.
     */
    int conversionMultiplier();

    /**
     * Can the Arcane Assembler be crafted.
     *
     * @return
     */
    boolean craftArcaneAssembler();

    /**
     * Can the ACT be crafted.
     *
     * @return
     */
    boolean craftArcaneCraftingTerminal();

    /**
     * Can the DPE be crafted.
     *
     * @return
     */
    boolean craftDistillationPatternEncoder();

    /**
     * Can Essentia cells be crafted.
     *
     * @return
     */
    boolean craftEssentiaCells();

    /**
     * Can the Essentia Provider be crafted.
     */
    boolean craftEssentiaProvider();

    /**
     * Can the EVC be crafted.
     *
     * @return
     */
    boolean craftEssentiaVibrationChamber();

    /**
     * Can the golem backpack be crafted.
     *
     * @return
     */
    boolean craftGolemWifiBackpack();

    /**
     * Can the Infusion Provider be crafted.
     */
    boolean craftInfusionProvider();

    /**
     * Controls if the import/export/storage can be crafted.
     *
     * @return
     */
    boolean craftIOBuses();

    /**
     * Can the VRI be crafted.
     *
     * @return
     */
    boolean craftVisRelayInterface();

    /**
     * Can the WET be crafted.
     *
     * @return
     */
    boolean craftWirelessEssentiaTerminal();

    /**
     * If true the iron and thaumium gearbox's will be rendered as a standard block.
     */
    boolean disableGearboxModel();

    /**
     * Controls if Certus Quartz can be duped in the crucible.
     */
    boolean enableCertusQuartzDupe();

    /**
     * Is the wrench focus enabled.
     *
     * @return
     */
    boolean enableWrenchFocus();

    /**
     * When enabled, overwrites the AE2 facade setting for certain Thaumcraft blocks, allowing them to become facades.
     *
     * @return
     */
    boolean forceTCFacades();
}
