package thaumicenergistics.common.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides logging functions for ThE.
 * 
 * @author Nividica
 * 
 */
public class ThELog
{
	public static final Logger log = LogManager.getLogger( "Thaumic Energistics" );

	/**
	 * Displays the section header.
	 */
	public static long beginSection( final String section )
	{
		log.info( String.format( "Starting (%s)", section ) );
		return System.currentTimeMillis();
	}

	/**
	 * Logs a debug statement.
	 * 
	 * @param format
	 * @param data
	 */
	public static void debug( final String format, final Object ... data )
	{
		log.debug( String.format( format, data ) );
	}

	/**
	 * Displays the section footer.
	 */
	public static void endSection( final String section, final long sectionStartTime )
	{
		log.info( String.format( "Finished (%s in %dms)", section, ( System.currentTimeMillis() - sectionStartTime ) ) );
	}

	/**
	 * Logs an exception.
	 * 
	 * @param e
	 * @param format
	 * @param data
	 */
	public static void error( final Throwable e, final String format, final Object ... data )
	{
		log.error( String.format( format, data ), e );
	}

	/**
	 * Logs basic info.
	 * 
	 * @param format
	 * @param data
	 */
	public static void info( final String format, final Object ... data )
	{
		log.info( String.format( format, data ) );
	}

	/**
	 * Logs an error.<br>
	 * If there is an exception available, use {@code error}.
	 * 
	 * @param format
	 * @param data
	 */
	public static void severe( final String format, final Object ... data )
	{
		log.error( String.format( format, data ) );
	}

	/**
	 * Logs a warning.
	 * 
	 * @param format
	 * @param data
	 */
	public static void warning( final String format, final Object ... data )
	{
		log.warn( String.format( format, data ) );
	}

}
