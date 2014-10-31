package thaumicenergistics;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import thaumicenergistics.api.TEApi;
import thaumicenergistics.api.registry.TEConfig;

class ConfigHelper
{
	/**
	 * Name of the crafting category
	 */
	private static final String CATEGORY_CRAFTING = "crafting";

	/**
	 * Mod configuration
	 */
	private static Configuration CONFIGURATION_SETTINGS;

	/**
	 * Synchronizes the config file and the settings.
	 */
	private static void synchronizeConfigFile()
	{
		// Get the config
		TEConfig config = TEApi.instance.config;

		// Sync the essentia fluid conversion ratio
		config.CONVERSION_MULTIPLIER = ConfigHelper.CONFIGURATION_SETTINGS
						.getInt(
							"Essentia Fluid Ratio",
							Configuration.CATEGORY_GENERAL,
							config.CONVERSION_MULTIPLIER,
							1,
							10000,
							"Controls the conversion ratio of essentia/fluid. 1 essentia is converted to this many milibuckets of fluid. "
											+ "Please be aware that this value effects how much fluid is transferred through the AE system, which also effects transfer speed and power consumption. "
											+ "Very high values may make it impossible to use fluid transfer devices such as the ME IO Port, or anything from EC2." );

		// Sync essentia provider
		config.ALLOW_CRAFTING_ESSENTIA_PROVIDER = ConfigHelper.CONFIGURATION_SETTINGS.getBoolean( "Allow Crafting Essentia Provider",
			ConfigHelper.CATEGORY_CRAFTING, config.ALLOW_CRAFTING_ESSENTIA_PROVIDER, "Controls if the Essentia Provider is allowed to be crafted." );

		// Sync essentia provider
		config.ALLOW_CRAFTING_INFUSION_PROVIDER = ConfigHelper.CONFIGURATION_SETTINGS.getBoolean( "Allow Crafting Infusion Provider",
			ConfigHelper.CATEGORY_CRAFTING, config.ALLOW_CRAFTING_INFUSION_PROVIDER, "Controls if the Infusion Provider is allowed to be crafted." );

		// Has the config file changed?
		if( ConfigHelper.CONFIGURATION_SETTINGS.hasChanged() )
		{
			// Save it
			ConfigHelper.CONFIGURATION_SETTINGS.save();
		}
	}

	/**
	 * Loads and synchronizes the config file.
	 * 
	 * @param configFile
	 */
	public static void loadAndSyncConfigFile( final File configFile )
	{
		ConfigHelper.CONFIGURATION_SETTINGS = new Configuration( configFile );
		ConfigHelper.synchronizeConfigFile();
	}

}
