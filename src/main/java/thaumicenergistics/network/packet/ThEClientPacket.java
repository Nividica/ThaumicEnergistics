package thaumicenergistics.network.packet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Packet to be sent to the client.
 * 
 * @author Nividica
 * 
 */
public abstract class ThEClientPacket
	extends ThEBasePacket
{
	@SideOnly(Side.CLIENT)
	protected abstract void wrappedExecute();

	@Override
	public final void execute()
	{
		// Ensure we have a player
		if( this.player == null )
		{
			return;
		}

		// Ensure this is client side
		if( FMLCommonHandler.instance().getEffectiveSide().isClient() )
		{
			this.wrappedExecute();
		}
	}
}
