package thaumicenergistics.util;

import org.apache.logging.log4j.Level;
import thaumicenergistics.ThaumicEnergistics;
import com.google.common.collect.ObjectArrays;
import cpw.mods.fml.common.FMLLog;

public class ThELog
{
	/**
	 * Mod ID as title case.
	 */
	private static final String NICE_MOD_ID = "ThaumicEnergistics";

	/**
	 * Appends this mod's ID to the object array.
	 * 
	 * @param data
	 * @return
	 */
	private static Object[] appendModID( final Object[] data )
	{
		if( data != null )
		{
			return ObjectArrays.concat( NICE_MOD_ID, data );
		}

		return new Object[] { NICE_MOD_ID };
	}

	/**
	 * Displays the section header.
	 */
	public static long beginSection( final String section )
	{
		FMLLog.log( Level.INFO, "[%s] Starting (%s)", ThaumicEnergistics.MOD_ID, section );
		return System.currentTimeMillis();
	}

	/**
	 * Displays the section footer.
	 */
	public static void endSection( final String section, final long sectionStartTime )
	{
		FMLLog.log( Level.INFO, "[%s] Finished (%s in %dms)", ThaumicEnergistics.MOD_ID, section, ( System.currentTimeMillis() - sectionStartTime ) );
	}

	/**
	 * Logs basic info.
	 * 
	 * @param format
	 * @param data
	 */
	public static void info( final String format, final Object ... data )
	{
		FMLLog.log( Level.INFO, "[%s] " + format, appendModID( data ) );
	}

	/**
	 * Logs an error.
	 * 
	 * @param format
	 * @param data
	 */
	public static void severe( final String format, final Object ... data )
	{
		FMLLog.log( Level.ERROR, "[%s] " + format, appendModID( data ) );
	}

	/**
	 * Logs a warning.
	 * 
	 * @param format
	 * @param data
	 */
	public static void warning( final String format, final Object ... data )
	{
		FMLLog.log( Level.WARN, "[%s] " + format, appendModID( data ) );
	}

}
