package thaumicenergistics.api;

import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * Thaumic Energistics API
 */
public abstract class TEApi
{
	protected static TEApi instance;

	/**
	 * Thaumic Energistics API
	 */
	public static TEApi instance()
	{
		if( TEApi.instance == null )
		{
			TEApi.instance = new thaumicenergistics.implementaion.API();
		}
		return TEApi.instance;
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
	 * Transport permissions.
	 */
	public abstract ITransportPermissions transportPermissions();

}
