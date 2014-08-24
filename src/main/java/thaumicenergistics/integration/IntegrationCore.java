package thaumicenergistics.integration;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import thaumicenergistics.ThaumicEnergistics;

public final class IntegrationCore
{
	/**
	 * Class path to all integration modules
	 */
	private static final String CLASS_PATH = ThaumicEnergistics.MOD_ID + ".integration.Module";

	/**
	 * Mod ID for Waila
	 */
	private static final String MODID_WAILA = "Waila";

	/**
	 * Integrate with all modules
	 */
	public static void init()
	{
		// Integrate with Waila
		IntegrationCore.integrate( IntegrationCore.MODID_WAILA );
	}

	private static void integrate( String modID )
	{
		// Is the mod loaded?
		if( !Loader.isModLoaded( modID ) )
		{
			// Log skipping
			FMLLog.info( "%s: Mod %s not loaded. Skipping Integration.", ThaumicEnergistics.MOD_ID, modID );
		}

		try
		{
			// Attempt to get the module
			Class<?> module = Class.forName( IntegrationCore.CLASS_PATH + modID );
			
			// Instantiate it
			module.newInstance();

			// Log success
			FMLLog.info( "%s: Successfully integrated with %s", ThaumicEnergistics.MOD_ID, modID );
		}
		catch( Exception e )
		{

			// Log failure
			FMLLog.warning( "%s: Error encountered while integrating with %s", ThaumicEnergistics.MOD_ID, modID );
			e.printStackTrace( System.err );
		}
	}
}
