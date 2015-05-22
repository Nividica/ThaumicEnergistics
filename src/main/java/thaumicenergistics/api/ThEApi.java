package thaumicenergistics.api;

import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * Thaumic Energistics API
 */
public abstract class ThEApi
{
	protected static ThEApi api;

	/**
	 * Thaumic Energistics API
	 */
	public static ThEApi instance()
	{
		// Have we already retrieved the api?
		if( ThEApi.api == null )
		{
			try
			{
				// Attempt to locate the API implementation
				Class clazz = Class.forName( "thaumicenergistics.implementaion.API" );

				// Attempt to get the API instance
				ThEApi.api = (ThEApi)clazz.getField( "instance" ).get( clazz );
			}
			catch( Throwable e )
			{
				// Unable to locate the API, return null
				return null;
			}
		}

		return ThEApi.api;
	}

	/**
	 * Blocks
	 */
	public abstract IThEBlocks blocks();

	/**
	 * Configuration
	 */
	public abstract IThEConfig config();

	/**
	 * Essentia Gasses
	 */
	public abstract ImmutableList<List<IThEEssentiaGas>> essentiaGases();

	/**
	 * Gets the ThE interaction manager
	 * 
	 * @return
	 */
	public abstract IThEInteractionHelper interact();

	/**
	 * Items
	 */
	public abstract IThEItems items();

	/**
	 * Cable Parts
	 */
	public abstract IThEParts parts();

	/**
	 * Transport Permissions.
	 */
	public abstract IThETransportPermissions transportPermissions();

}
