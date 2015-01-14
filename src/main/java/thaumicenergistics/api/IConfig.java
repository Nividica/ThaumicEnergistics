package thaumicenergistics.api;

public interface IConfig
{
	/**
	 * Controls if the Essentia Provider is allowed to be crafted.
	 */
	public boolean allowedToCraftEssentiaProvider();

	/**
	 * Controls if the Infusion Provider is allowed to be crafted.
	 */
	public boolean allowedToCraftInfusionProvider();

	/**
	 * Controls if Certus Quartz can be duped in the crucible.
	 */
	public boolean allowedToDuplicateCertusQuartz();

	/**
	 * Controls the conversion ratio of essentia/fluid. <BR>
	 * 1 essentia unit is converted to this many mb's of fluid.
	 */
	public int conversionMultiplier();

	/**
	 * If true the iron and thaumium gearbox's will be rendered as a standard
	 * block.
	 */
	public boolean gearboxModelDisabled();
}
