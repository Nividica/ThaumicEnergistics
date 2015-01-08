package thaumicenergistics.integration;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import thaumicenergistics.blocks.AbstractBlockProviderBase;
import thaumicenergistics.blocks.BlockArcaneAssembler;
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
	public static void callbackRegister( final IWailaRegistrar registrar )
	{
		// Register the providers
		registrar.registerBodyProvider( ModuleWaila.instance, AbstractBlockProviderBase.class );
		registrar.registerBodyProvider( ModuleWaila.instance, BlockArcaneAssembler.class );
	}

	/**
	 * Changes the body of the Waila message.
	 */
	@Override
	public List<String> getWailaBody( final ItemStack itemStack, final List<String> tooltip, final IWailaDataAccessor accessor,
										final IWailaConfigHandler config )
	{
		// Get the tile entity
		TileEntity tileEntity = accessor.getTileEntity();

		// Does the tile implement the tooltip method?
		if( tileEntity instanceof IWailaSource )
		{
			// Add the info
			( (IWailaSource)tileEntity ).addWailaInformation( tooltip );
		}

		return tooltip;
	}

	@Override
	public List<String> getWailaHead( final ItemStack itemStack, final List<String> currenttip, final IWailaDataAccessor accessor,
										final IWailaConfigHandler config )
	{
		// Ignored
		return currenttip;
	}

	@Override
	public ItemStack getWailaStack( final IWailaDataAccessor accessor, final IWailaConfigHandler config )
	{
		// Ignored
		return null;
	}

	@Override
	public List<String> getWailaTail( final ItemStack itemStack, final List<String> currenttip, final IWailaDataAccessor accessor,
										final IWailaConfigHandler config )
	{
		// Ignored
		return currenttip;
	}

}
