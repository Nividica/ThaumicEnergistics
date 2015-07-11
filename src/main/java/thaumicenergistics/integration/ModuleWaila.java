package thaumicenergistics.integration;

import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumicenergistics.blocks.AbstractBlockProviderBase;
import thaumicenergistics.blocks.BlockArcaneAssembler;
import thaumicenergistics.tileentities.TileEssentiaVibrationChamber;
import cpw.mods.fml.common.event.FMLInterModComms;

public class ModuleWaila
	implements IWailaDataProvider
{

	/**
	 * Singleton
	 */
	public static ModuleWaila INSTANCE;

	/**
	 * Attempts to integrate with Waila
	 */
	public ModuleWaila()
	{
		// Set the singleton
		ModuleWaila.INSTANCE = this;

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
		registrar.registerBodyProvider( ModuleWaila.INSTANCE, AbstractBlockProviderBase.class );
		registrar.registerBodyProvider( ModuleWaila.INSTANCE, BlockArcaneAssembler.class );

		// Register the vibration chamber
		registrar.registerBodyProvider( ModuleWaila.INSTANCE, TileEssentiaVibrationChamber.class );
	}

	@Override
	public NBTTagCompound getNBTData( final EntityPlayerMP player, final TileEntity tileEntity, final NBTTagCompound data, final World world,
										final int x, final int y, final int z )
	{
		// Ignored
		return data;
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
