package thaumicenergistics.network.packet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.network.ChannelHandler;

/**
 * Packet to be sent to the client.
 * @author Nividica
 *
 */
public abstract class AbstractClientPacket
	extends AbstractPacket
{
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
	
	@SideOnly(Side.CLIENT)
	protected abstract void wrappedExecute();

	/**
	 * Send this packet to all players.
	 */
	public void sendPacketToAllPlayers()
	{
		ChannelHandler.sendPacketToAllPlayers( this );
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
