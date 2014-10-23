package thaumicenergistics.integration;

import net.minecraft.nbt.NBTTagCompound;
import thaumicenergistics.ThaumicEnergistics;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;

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
	 * Integrates with the specified mod if it exists
	 * 
	 * @param modID
	 */
	private static void integrateWithMod( final String modID )
	{
		// Is the mod loaded?
		if( !Loader.isModLoaded( modID ) )
		{
			// Log skipping
			FMLLog.info( "%s: Mod %s not loaded. Skipping integration.", ThaumicEnergistics.MOD_ID, modID );
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

	/**
	 * Integrates with NEI
	 */
	private static void integrateWithNEI()
	{
		try
		{
			// Attempt to get the NEI module
			Class<?> module = Class.forName( IntegrationCore.CLASS_PATH + "NEI" );

			// Instantiate it
			module.newInstance();

			// All done
			FMLLog.info( "%s: Successfully integrated with NEI", ThaumicEnergistics.MOD_ID );
		}
		catch( Exception e )
		{
			// Log that we are not integrating with NEI
			FMLLog.info( "%s: Skipping integration with NEI", ThaumicEnergistics.MOD_ID );
		}
	}

	/**
	 * Integrates with version checker
	 */
	private static void integrateWithVersionChecker()
	{
		// Create the tag
		NBTTagCompound tag = new NBTTagCompound();

		// Set the project name
		tag.setString( "curseProjectName", "223666-thaumic-energistics" );

		// Set the file name
		tag.setString( "curseFilenameParser", "thaumicenergistics-[].jar" );

		// Set the mod name
		tag.setString( "modDisplayName", "Thaumic Energistics" );

		// Send to version checker
		FMLInterModComms.sendRuntimeMessage( ThaumicEnergistics.MOD_ID, "VersionChecker", "addCurseCheck", tag );

	}

	/**
	 * Integrate with all modules
	 */
	public static void init()
	{
		// Integrate with version checker
		IntegrationCore.integrateWithVersionChecker();

		// Integrate with NEI
		IntegrationCore.integrateWithNEI();

		// Integrate with Waila
		IntegrationCore.integrateWithMod( IntegrationCore.MODID_WAILA );
	}
}
