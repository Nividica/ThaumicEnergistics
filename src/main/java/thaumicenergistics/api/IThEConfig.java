package thaumicenergistics.api;

public interface IThEConfig
{
	/**
	 * If true essentia gas will be blacklisted from ExtraCells.
	 * 
	 * @return
	 */
	public boolean blacklistEssentiaFluidInExtraCells();

	/**
	 * Controls the conversion ratio of essentia/fluid. <BR>
	 * 1 essentia unit is converted to this many mb's of fluid.
	 */
	public int conversionMultiplier();

	/**
	 * Can the Arcane Assembler be crafted.
	 * 
	 * @return
	 */
	public boolean craftArcaneAssembler();

	/**
	 * Can the ACT be crafted.
	 * 
	 * @return
	 */
	public boolean craftArcaneCraftingTerminal();

	/**
	 * Can the DPE be crafted.
	 * 
	 * @return
	 */
	public boolean craftDistillationPatternEncoder();

	/**
	 * Can Essentia cells be crafted.
	 * 
	 * @return
	 */
	public boolean craftEssentiaCells();

	/**
	 * Can the Essentia Provider be crafted.
	 */
	public boolean craftEssentiaProvider();

	/**
	 * Can the EVC be crafted.
	 * 
	 * @return
	 */
	public boolean craftEssentiaVibrationChamber();

	/**
	 * Can the golem backpack be crafted.
	 * 
	 * @return
	 */
	public boolean craftGolemWifiBackpack();

	/**
	 * Can the Infusion Provider be crafted.
	 */
	public boolean craftInfusionProvider();

	/**
	 * Controls if the import/export/storage can be crafted.
	 * 
	 * @return
	 */
	public boolean craftIOBuses();

	/**
	 * Can the VRI be crafted.
	 * 
	 * @return
	 */
	public boolean craftVisRelayInterface();

	/**
	 * Can the WET be crafted.
	 * 
	 * @return
	 */
	public boolean craftWirelessEssentiaTerminal();

	/**
	 * If true the iron and thaumium gearbox's will be rendered as a standard
	 * block.
	 */
	public boolean disableGearboxModel();

	/**
	 * Controls if Certus Quartz can be duped in the crucible.
	 */
	public boolean enableCertusQuartzDupe();

	/**
	 * Is the wrench focus enabled.
	 * 
	 * @return
	 */
	public boolean enableWrenchFocus();

	/**
	 * When enabled, overwrites the AE2 facade setting for certain Thaumcraft blocks,
	 * allowing them to become facades.
	 * 
	 * @return
	 */
	public boolean forceTCFacades();
}
