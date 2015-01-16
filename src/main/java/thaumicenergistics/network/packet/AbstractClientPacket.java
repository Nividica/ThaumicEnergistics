package thaumicenergistics.network.packet;

import thaumicenergistics.network.ChannelHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Packet to be sent to the client.
 * 
 * @author Nividica
 * 
 */
public abstract class AbstractClientPacket
	extends AbstractPacket
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

	/**
	 * Send this packet to the player.
	 * 
	 * @param player
	 */
	public void sendPacketToPlayer()
	{
		ChannelHandler.sendPacketToPlayer( this, this.player );
	}
}
