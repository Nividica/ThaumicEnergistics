package thaumicenergistics.common.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.AEPartEssentiaStorageBus;

public class Packet_S_EssentiaStorageBus
	extends ThEServerPacket
{
	private static final byte MODE_REQUEST_FULL_UPDATE = 0;
	private static final byte MODE_REQUEST_SET_VOID = 1;

	private AEPartEssentiaStorageBus part;
	private boolean isVoidAllowed;

	/**
	 * Creates the packet
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_S_EssentiaStorageBus newPacket( final EntityPlayer player, final byte mode, final AEPartEssentiaStorageBus part )
	{
		// Create the packet
		Packet_S_EssentiaStorageBus packet = new Packet_S_EssentiaStorageBus();

		// Set the player & mode & part
		packet.player = player;
		packet.mode = mode;
		packet.part = part;

		return packet;
	}

	public static void sendFullUpdateRequest( final EntityPlayer player, final AEPartEssentiaStorageBus part )
	{
		Packet_S_EssentiaStorageBus packet = newPacket( player, MODE_REQUEST_FULL_UPDATE, part );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void setVoidAllowed( final EntityPlayer player, final AEPartEssentiaStorageBus part,
										final boolean isVoidAllowed )
	{
		Packet_S_EssentiaStorageBus packet = newPacket( player, MODE_REQUEST_SET_VOID, part );

		// Set if void is allowed
		packet.isVoidAllowed = isVoidAllowed;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
		case Packet_S_EssentiaStorageBus.MODE_REQUEST_FULL_UPDATE:
			// Request a full update
			this.part.onClientRequestFullUpdate( this.player );
			break;

		case Packet_S_EssentiaStorageBus.MODE_REQUEST_SET_VOID:
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
		case Packet_S_EssentiaStorageBus.MODE_REQUEST_FULL_UPDATE:
			// Read the part
			this.part = ( (AEPartEssentiaStorageBus)ThEBasePacket.readPart( stream ) );
			break;

		case Packet_S_EssentiaStorageBus.MODE_REQUEST_SET_VOID:
			// Read the part
			this.part = ( (AEPartEssentiaStorageBus)ThEBasePacket.readPart( stream ) );
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
		case Packet_S_EssentiaStorageBus.MODE_REQUEST_FULL_UPDATE:
			// Write the part
			ThEBasePacket.writePart( this.part, stream );
			break;

		case Packet_S_EssentiaStorageBus.MODE_REQUEST_SET_VOID:
			// Write the part
			ThEBasePacket.writePart( this.part, stream );
			// Write void
			stream.writeBoolean( this.isVoidAllowed );
			break;
		}
	}

}
