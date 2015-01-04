package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.container.ContainerKnowledgeInscriber;
import thaumicenergistics.network.packet.AbstractServerPacket;

public class PacketServerKnowledgeInscriber
	extends AbstractServerPacket
{
	private static final byte MODE_FULL_UPDATE = 0, MODE_SAVEDELETE = 1;

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

	public PacketServerKnowledgeInscriber createRequestFullUpdate( final EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerKnowledgeInscriber.MODE_FULL_UPDATE;

		return this;
	}

	public PacketServerKnowledgeInscriber createRequestSaveDelete( final EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerKnowledgeInscriber.MODE_SAVEDELETE;

		return this;
	}

	@Override
	public void execute()
	{
		// If the player is not null, and they have the KI container open
		if( ( this.player != null ) && ( this.player.openContainer instanceof ContainerKnowledgeInscriber ) )
		{
			switch ( this.mode )
			{
				case PacketServerKnowledgeInscriber.MODE_FULL_UPDATE:
					// Request full update
					( (ContainerKnowledgeInscriber)this.player.openContainer ).onClientRequestFullUpdate( this.player );
					break;

				case PacketServerKnowledgeInscriber.MODE_SAVEDELETE:
					// Request save/delete
					( (ContainerKnowledgeInscriber)this.player.openContainer ).onClientRequestSaveOrDelete( this.player );
					break;
			}
		}
	}
}
