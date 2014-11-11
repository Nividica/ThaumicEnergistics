package thaumicenergistics.integration;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import thaumicenergistics.blocks.AbstractBlockProviderBase;
import thaumicenergistics.tileentities.TileProviderBase;
import appeng.core.localization.WailaText;
import cpw.mods.fml.common.event.FMLInterModComms;

public class ModuleWaila
	implements IWailaDataProvider
{

	/**
	 * Singleton
	 */
	public static ModuleWaila instance;

	/**
	 * Attempts to integrate with Waila
	 */
	public ModuleWaila()
	{
		// Set the singleton
		ModuleWaila.instance = this;

		// Register with Waila
		FMLInterModComms.sendMessage( "Waila", "register", ModuleWaila.class.getCanonicalName() + ".callbackRegister" );
	}

	/**
	 * Called by Waila to register our hooks.
	 * 
	 * @param registrar
	 */
	public static void callbackRegister( IWailaRegistrar registrar )
	{
		// Register the providers
		registrar.registerBodyProvider( ModuleWaila.instance, AbstractBlockProviderBase.class );
	}

	/**
	 * Changes the body of the Waila message.
	 */
	@Override
	public List<String> getWailaBody( ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		// Get the tile entity
		TileEntity tileProvider = accessor.getTileEntity();

		// Did we get a valid provider?
		if( tileProvider instanceof TileProviderBase )
		{
			// Is it active?
			if( ( (TileProviderBase)tileProvider ).isActive() )
			{
				// Get activity string from AppEng2
				currenttip.add( WailaText.DeviceOnline.getLocal() );
			}
			else
			{
				// Get activity string from AppEng2
				currenttip.add( WailaText.DeviceOffline.getLocal() );
			}

			// Add the color
			currenttip.add( "Color: " + ( (TileProviderBase)tileProvider ).getColor().toString() );
		}

		return currenttip;
	}

	@Override
	public List<String> getWailaHead( ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		// Ignored
		return currenttip;
	}

	@Override
	public ItemStack getWailaStack( IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		// Ignored
		return null;
	}

	@Override
	public List<String> getWailaTail( ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		// Ignored
		return currenttip;
	}

}
