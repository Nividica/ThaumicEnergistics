package thaumicenergistics.registries;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class TEConfig
{
	/**
	 * Name of the crafting category
	 */
	private static final String CATEGORY_CRAFTING = "crafting";

	/**
	 * Mod configuration
	 */
	public static Configuration CONFIGURATION_SETTINGS;

	/**
	 * Controls the conversion ratio of essentia/fluid. <BR>
	 * 1 essentia unit is converted to this many mb's of fluid.
	 */
	public static int CONVERSION_MULTIPLIER = 250;

	/**
	 * Controls if the Essentia Provider is allowed to be crafted.
	 */
	public static boolean ALLOW_CRAFTING_ESSENTIA_PROVIDER = true;

	/**
	 * Controls if the Infusion Provider is allowed to be crafted.
	 */
	public static boolean ALLOW_CRAFTING_INFUSION_PROVIDER = true;

	/**
	 * Synchronizes the config file and the settings.
	 */
	private static void synchronizeConfigFile()
	{
		// Sync the essentia fluid conversion ratio
		TEConfig.CONVERSION_MULTIPLIER = TEConfig.CONFIGURATION_SETTINGS
						.getInt(
							"Essentia Fluid Ratio",
							Configuration.CATEGORY_GENERAL,
							TEConfig.CONVERSION_MULTIPLIER,
							1,
							10000,
							"Controls the conversion ratio of essentia/fluid. 1 essentia is converted to this many milibuckets of fluid. "
											+ "Please be aware that this value effects how much fluid is transferred through the AE system, which also effects transfer speed and power consumption. "
											+ "Very high values may make it impossible to use fluid transfer devices such as the ME IO Port, or anything from EC2." );

		// Sync essentia provider
		TEConfig.ALLOW_CRAFTING_ESSENTIA_PROVIDER = TEConfig.CONFIGURATION_SETTINGS.getBoolean( "Allow Crafting Essentia Provider",
			TEConfig.CATEGORY_CRAFTING, TEConfig.ALLOW_CRAFTING_ESSENTIA_PROVIDER, "Controls if the Essentia Provider is allowed to be crafted." );

		// Sync essentia provider
		TEConfig.ALLOW_CRAFTING_INFUSION_PROVIDER = TEConfig.CONFIGURATION_SETTINGS.getBoolean( "Allow Crafting Infusion Provider",
			TEConfig.CATEGORY_CRAFTING, TEConfig.ALLOW_CRAFTING_INFUSION_PROVIDER, "Controls if the Infusion Provider is allowed to be crafted." );

		// Has the config file changed?
		if( TEConfig.CONFIGURATION_SETTINGS.hasChanged() )
		{
			// Save it
			TEConfig.CONFIGURATION_SETTINGS.save();
		}
	}

	/**
	 * Loads and synchronizes the config file.
	 * 
	 * @param configFile
	 */
	public static void loadAndSyncConfigFile( final File configFile )
	{
		TEConfig.CONFIGURATION_SETTINGS = new Configuration( configFile );
		TEConfig.synchronizeConfigFile();
	}
}
