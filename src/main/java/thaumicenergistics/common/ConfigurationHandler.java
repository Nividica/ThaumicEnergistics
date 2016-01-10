package thaumicenergistics.common;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import thaumicenergistics.api.IThEConfig;

class ConfigurationHandler
	implements IThEConfig
{
	private static ConfigurationHandler INSTANCE;

	/**
	 * Names of the categories.
	 */
	private static final String CATEGORY_CRAFTING = "crafting", CATEGORY_CLIENT = "client", CATEGORY_INTEGRATION = "integration";

	/**
	 * Default values.
	 */
	private static final int DEFAULT_CONVERSION_EXPONENT = 7;
	private static final boolean DEFAULT_ESSENTIAPROVIDER = true,
					DEFAULT_INFUSIONPROVIDER = true,
					DEFAULT_QUARTZ = true,
					DEFAULT_GEARBOX_DISABLED = false,
					DEFAULT_EXTRACELLS_BLIST = true,
					DEFAULT_FORCE_TC_FACADES = true;

	/**
	 * Mod configuration
	 */
	private Configuration configSettings;

	/**
	 * Controls the conversion ratio of essentia/fluid. <BR>
	 * 1 essentia unit is converted to this many mb's of fluid.
	 */
	private int conversionMultiplier = (int)Math.pow( 2, DEFAULT_CONVERSION_EXPONENT );

	/**
	 * Controls if the Essentia Provider is allowed to be crafted.
	 */
	private boolean allowEssentiaProvider = DEFAULT_ESSENTIAPROVIDER;

	/**
	 * Controls if the Infusion Provider is allowed to be crafted.
	 */
	private boolean allowInfusionProvider = DEFAULT_INFUSIONPROVIDER;

	/**
	 * Controls if Certus Quartz can be duped in the crucible.
	 */
	private boolean allowCertusDupe = DEFAULT_QUARTZ;

	/**
	 * Controls if the iron and thaumium gearbox's will be rendered as a
	 * standard block.
	 */
	private boolean gearboxModelDisabled = DEFAULT_GEARBOX_DISABLED;

	/**
	 * Controls if essentia gas is blacklisted
	 */
	private boolean extracellsBlacklist = DEFAULT_EXTRACELLS_BLIST;

	/**
	 * Allows specific blocks from Thaumcraft to be turned into facades.
	 */
	private boolean forceTCFacades = DEFAULT_FORCE_TC_FACADES;

	private ConfigurationHandler( final Configuration config )
	{
		this.configSettings = config;
		this.synchronizeConfigFile();
	}

	/**
	 * Loads and synchronizes the config file.
	 * 
	 * @param configFile
	 */
	public static IThEConfig loadAndSyncConfigFile( final File configFile )
	{
		if( ConfigurationHandler.INSTANCE == null )
		{
			ConfigurationHandler.INSTANCE = new ConfigurationHandler( new Configuration( configFile ) );
		}

		return ConfigurationHandler.INSTANCE;
	}

	/**
	 * Synchronizes the config file and the settings.
	 */
	private void synchronizeConfigFile()
	{
		// Sync the essentia fluid conversion ratio
		int fluidPow = this.configSettings
						.getInt(
							"Essentia Fluid Ratio Exponent",
							Configuration.CATEGORY_GENERAL,
							ConfigurationHandler.DEFAULT_CONVERSION_EXPONENT,
							1,
							11,
							"Controls the conversion ratio of essentia/fluid. 1 essentia is converted to (2^this) milibuckets of fluid. "
											+ "Please be aware that this value effects how much fluid is transferred through the AE system, which also effects transfer speed and power consumption. "
											+ "Values above 11 make it impossible to use fluid transfer devices such as the ME IO Port, or anything from EC2." );
		this.conversionMultiplier = (int)Math.pow( 2, Math.min( fluidPow, 11 ) );

		// Sync essentia provider
		this.allowEssentiaProvider = this.configSettings.getBoolean( "Allow Crafting Essentia Provider", CATEGORY_CRAFTING,
			DEFAULT_ESSENTIAPROVIDER, "Controls if the Essentia Provider is allowed to be crafted." );

		// Sync essentia provider
		this.allowInfusionProvider = this.configSettings.getBoolean( "Allow Crafting Infusion Provider", CATEGORY_CRAFTING,
			DEFAULT_INFUSIONPROVIDER, "Controls if the Infusion Provider is allowed to be crafted." );

		// Sync certus dupe
		this.allowCertusDupe = this.configSettings.getBoolean( "Certus Quartz Duplication", CATEGORY_CRAFTING,
			DEFAULT_QUARTZ, "Controls if Certus Quartz can be duplicated in the crucible." );

		// Gearbox model
		this.gearboxModelDisabled = this.configSettings.getBoolean( "Disable Gearbox Model", CATEGORY_CLIENT,
			DEFAULT_GEARBOX_DISABLED, "The iron and thaumium gearboxes will be rendered as a standard block." );

		// Extra cells blacklist
		this.extracellsBlacklist = this.configSettings.getBoolean( "ExtraCells Blacklist", CATEGORY_INTEGRATION,
			DEFAULT_EXTRACELLS_BLIST, "Prevents extra cells from interacting with essentia gas" );

		// Thaumcraft facades
		this.forceTCFacades = this.configSettings.getBoolean( "Force TC Facades", CATEGORY_CRAFTING, DEFAULT_FORCE_TC_FACADES,
			"When enabled, overwrites the AE2 facade setting for certain Thaumcraft blocks, allowing them to become facades." );

		// Has the config file changed?
		if( this.configSettings.hasChanged() )
		{
			// Save it
			this.configSettings.save();
		}
	}

	@Override
	public boolean allowedToCraftEssentiaProvider()
	{
		return this.allowEssentiaProvider;
	}

	@Override
	public boolean allowedToCraftInfusionProvider()
	{
		return this.allowInfusionProvider;
	}

	@Override
	public boolean allowedToDuplicateCertusQuartz()
	{
		return this.allowCertusDupe;
	}

	@Override
	public boolean blacklistEssentiaFluidInExtraCells()
	{
		return this.extracellsBlacklist;
	}

	@Override
	public int conversionMultiplier()
	{
		return this.conversionMultiplier;
	}

	@Override
	public boolean forceTCFacades()
	{
		return this.forceTCFacades;
	}

	@Override
	public boolean gearboxModelDisabled()
	{
		return this.gearboxModelDisabled;
	}

}
