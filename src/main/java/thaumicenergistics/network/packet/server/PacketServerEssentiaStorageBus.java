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
	private static final byte MODE_REQUEST_SET_VOID = 1;

	private AEPartEssentiaStorageBus part;
	private boolean isVoidAllowed;

	public PacketServerEssentiaStorageBus createRequestFullUpdate( final EntityPlayer player, final AEPartEssentiaStorageBus part )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaStorageBus.MODE_REQUEST_FULL_UPDATE;

		// Set the part
		this.part = part;

		return this;
	}

	public PacketServerEssentiaStorageBus createRequestSetVoidAllowed( final EntityPlayer player, final AEPartEssentiaStorageBus part,
																		final boolean isVoidAllowed )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaStorageBus.MODE_REQUEST_SET_VOID;

		// Set the part
		this.part = part;

		// Set if void is allowed
		this.isVoidAllowed = isVoidAllowed;

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

		case PacketServerEssentiaStorageBus.MODE_REQUEST_SET_VOID:
			// Request set void
			this.part.onClientRequestSetVoidMode( this.player, this.isVoidAllowed );
			break;
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case PacketServerEssentiaStorageBus.MODE_REQUEST_FULL_UPDATE:
			// Read the part
			this.part = ( (AEPartEssentiaStorageBus)AbstractPacket.readPart( stream ) );
			break;

		case PacketServerEssentiaStorageBus.MODE_REQUEST_SET_VOID:
			// Read the part
			this.part = ( (AEPartEssentiaStorageBus)AbstractPacket.readPart( stream ) );
			// Read void
			this.isVoidAllowed = stream.readBoolean();
			break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case PacketServerEssentiaStorageBus.MODE_REQUEST_FULL_UPDATE:
			// Write the part
			AbstractPacket.writePart( this.part, stream );
			break;

		case PacketServerEssentiaStorageBus.MODE_REQUEST_SET_VOID:
			// Write the part
			AbstractPacket.writePart( this.part, stream );
			// Write void
			stream.writeBoolean( this.isVoidAllowed );
			break;
		}
	}

}
