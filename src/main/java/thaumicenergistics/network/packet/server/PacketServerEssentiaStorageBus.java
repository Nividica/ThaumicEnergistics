package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;

public class PacketServerEssentiaStorageBus
	extends AbstractServerPacket
{
	private static final byte MODE_REQUEST_FULL_UPDATE = 0;

	AEPartEssentiaStorageBus part;

	public PacketServerEssentiaStorageBus createRequestFullUpdate( EntityPlayer player, AEPartEssentiaStorageBus part )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaStorageBus.MODE_REQUEST_FULL_UPDATE;

		// Set the part
		this.part = part;

		return this;
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaStorageBus.MODE_REQUEST_FULL_UPDATE:
				// Request a full update
				this.part.onClientRequestFullUpdate( this.player );
				break;
		}
	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaStorageBus.MODE_REQUEST_FULL_UPDATE:
				// Read the part
				this.part = ( (AEPartEssentiaStorageBus)AbstractPacket.readPart( stream ) );
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaStorageBus.MODE_REQUEST_FULL_UPDATE:
				// Write the part
				AbstractPacket.writePart( this.part, stream );
				break;
		}
	}

}
