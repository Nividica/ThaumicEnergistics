package thaumicenergistics.api;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
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
	 * Opens the wireless gui for the specified player.
	 * The item the player is holding is used for the settings and power.
	 * 
	 * @param player
	 */
	public abstract void openWirelessTerminalGui( final EntityPlayer player, final IWirelessEssentiaTerminal terminalInterface );

	/**
	 * Cable Parts
	 */
	public abstract Parts parts();

	/**
	 * Transport Permissions.
	 */
	public abstract ITransportPermissions transportPermissions();

}
