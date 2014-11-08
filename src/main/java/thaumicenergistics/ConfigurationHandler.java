package thaumicenergistics;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import thaumicenergistics.api.IConfig;

class ConfigurationHandler
	implements IConfig
{
	private static ConfigurationHandler instance;

	/**
	 * Name of the crafting category
	 */
	private static final String CATEGORY_CRAFTING = "crafting";

	/**
	 * Mod configuration
	 */
	private Configuration configSettings;

	/**
	 * Controls the conversion ratio of essentia/fluid. <BR>
	 * 1 essentia unit is converted to this many mb's of fluid.
	 */
	private int conversionMultiplier = 250;

	/**
	 * Controls if the Essentia Provider is allowed to be crafted.
	 */
	private boolean allowEssentiaProvider = true;

	/**
	 * Controls if the Infusion Provider is allowed to be crafted.
	 */
	private boolean allowInfusionProvider = true;

	/**
	 * Controls if Certus Quartz can be duped in the crucible.
	 */
	private boolean allowCertusDupe = true;

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
	public static IConfig loadAndSyncConfigFile( final File configFile )
	{
		if( ConfigurationHandler.instance == null )
		{
			ConfigurationHandler.instance = new ConfigurationHandler( new Configuration( configFile ) );
		}

		return ConfigurationHandler.instance;
	}

	/**
	 * Synchronizes the config file and the settings.
	 */
	private void synchronizeConfigFile()
	{
		// Sync the essentia fluid conversion ratio
		this.conversionMultiplier = this.configSettings
						.getInt(
							"Essentia Fluid Ratio",
							Configuration.CATEGORY_GENERAL,
							this.conversionMultiplier,
							1,
							10000,
							"Controls the conversion ratio of essentia/fluid. 1 essentia is converted to this many milibuckets of fluid. "
											+ "Please be aware that this value effects how much fluid is transferred through the AE system, which also effects transfer speed and power consumption. "
											+ "Very high values may make it impossible to use fluid transfer devices such as the ME IO Port, or anything from EC2." );

		// Sync essentia provider
		this.allowEssentiaProvider = this.configSettings.getBoolean( "Allow Crafting Essentia Provider", ConfigurationHandler.CATEGORY_CRAFTING,
			this.allowEssentiaProvider, "Controls if the Essentia Provider is allowed to be crafted." );

		// Sync essentia provider
		this.allowInfusionProvider = this.configSettings.getBoolean( "Allow Crafting Infusion Provider", ConfigurationHandler.CATEGORY_CRAFTING,
			this.allowInfusionProvider, "Controls if the Infusion Provider is allowed to be crafted." );

		// Sync certus dupe
		this.allowCertusDupe = this.configSettings.getBoolean( "Certus Quartz Duplication", ConfigurationHandler.CATEGORY_CRAFTING,
			this.allowCertusDupe, "Controls if Certus Quartz can be duplicated in the crucible." );

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
	public int conversionMultiplier()
	{
		return this.conversionMultiplier;
	}

}
