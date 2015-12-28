package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.container.ContainerKnowledgeInscriber;
import thaumicenergistics.network.NetworkHandler;
import thaumicenergistics.network.packet.ThEServerPacket;

public class Packet_S_KnowledgeInscriber
	extends ThEServerPacket
{
	private static final byte MODE_FULL_UPDATE = 0, MODE_SAVEDELETE = 1;

	public static void sendFullUpdateRequest( final EntityPlayer player )
	{
		Packet_S_KnowledgeInscriber packet = new Packet_S_KnowledgeInscriber();

		// Set the player
		packet.player = player;

		// Set the mode
		packet.mode = Packet_S_KnowledgeInscriber.MODE_FULL_UPDATE;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendSaveDelete( final EntityPlayer player )
	{
		Packet_S_KnowledgeInscriber packet = new Packet_S_KnowledgeInscriber();

		// Set the player
		packet.player = player;

		// Set the mode
		packet.mode = Packet_S_KnowledgeInscriber.MODE_SAVEDELETE;

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
			case Packet_S_KnowledgeInscriber.MODE_FULL_UPDATE:
				// Request full update
				( (ContainerKnowledgeInscriber)this.player.openContainer ).onClientRequestFullUpdate( this.player );
				break;

			case Packet_S_KnowledgeInscriber.MODE_SAVEDELETE:
				// Request save/delete
				( (ContainerKnowledgeInscriber)this.player.openContainer ).onClientRequestSaveOrDelete( this.player );
				break;
			}
		}
	}
}
