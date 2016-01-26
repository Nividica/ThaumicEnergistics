package thaumicenergistics.common.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.common.container.ContainerKnowledgeInscriber;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.tiles.TileKnowledgeInscriber;

/**
 * {@link TileKnowledgeInscriber} server-bound packet.
 * 
 * @author Nividica
 * 
 */
public class Packet_S_KnowledgeInscriber
	extends ThEServerPacket
{
	/**
	 * Packet modes
	 */
	private static final byte MODE_FULL_UPDATE = 0,
					MODE_SAVEDELETE = 1,
					MODE_CLEAR = 2;

	private static Packet_S_KnowledgeInscriber newPacket( final EntityPlayer player, final byte mode )
	{
		Packet_S_KnowledgeInscriber packet = new Packet_S_KnowledgeInscriber();

		// Set the player
		packet.player = player;

		// Set the mode
		packet.mode = mode;

		return packet;
	}

	public static void sendClearGrid( final EntityPlayer player )
	{
		// Create the packet
		Packet_S_KnowledgeInscriber packet = newPacket( player, MODE_CLEAR );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendFullUpdateRequest( final EntityPlayer player )
	{
		// Create the packet
		Packet_S_KnowledgeInscriber packet = newPacket( player, MODE_FULL_UPDATE );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendSaveDelete( final EntityPlayer player )
	{
		// Create the packet
		Packet_S_KnowledgeInscriber packet = newPacket( player, MODE_SAVEDELETE );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
		// Intentionally empty

	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		// Intentionally empty
	}

	@Override
	public void execute()
	{
		// If the player is not null, and they have the KI container open
		if( ( this.player != null ) && ( this.player.openContainer instanceof ContainerKnowledgeInscriber ) )
		{
			switch ( this.mode )
			{
			case MODE_FULL_UPDATE:
				// Request full update
				( (ContainerKnowledgeInscriber)this.player.openContainer ).onClientRequestSaveState();
				break;

			case MODE_SAVEDELETE:
				// Request save/delete
				( (ContainerKnowledgeInscriber)this.player.openContainer ).onClientRequestSaveOrDelete( this.player );
				break;

			case MODE_CLEAR:
				// Request clear
				( (ContainerKnowledgeInscriber)this.player.openContainer ).onClientRequestClearGrid();
				break;
			}
		}
	}
}
