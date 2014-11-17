package thaumicenergistics.api;

import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * Thaumic Energistics API
 */
public abstract class TEApi
{
	protected static TEApi api;

	/**
	 * Thaumic Energistics API
	 */
	public static TEApi instance()
	{
		// Have we already retrieved the api?
		if( TEApi.api == null )
		{
			try
			{
				// Attempt to locate the API implementation
				Class clazz = Class.forName( "thaumicenergistics.implementaion.API" );

				// Attempt to get the API instance
				TEApi.api = (TEApi)clazz.getField( "instance" ).get( clazz );
			}
			catch( Throwable e )
			{
				// Unable to locate the API, return null
				return null;
			}
		}

		return TEApi.api;
	}

	/**
	 * Blocks
	 */
	public abstract Blocks blocks();

	/**
	 * Configuration
	 */
	public abstract IConfig config();

	/**
	 * Converts an amount of milibuckets to an amount of Essentia.
	 * 
	 * @return
	 */
	public abstract long convertEssentiaAmountToFluidAmount( long essentiaAmount );

	/**
	 * Converts an amount of Essentia to an amount of milibuckets.
	 * 
	 * @return
	 */
	public abstract long convertFluidAmountToEssentiaAmount( long milibuckets );

	/**
	 * Essentia Gasses
	 */
	public abstract ImmutableList<List<IEssentiaGas>> essentiaGases();

	/**
	 * Items
	 */
	public abstract Items items();

	/**
	 * Cable Parts
	 */
	public abstract Parts parts();

	/**
	 * Transport Permissions.
	 */
	public abstract ITransportPermissions transportPermissions();

}
