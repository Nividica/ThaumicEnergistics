package thaumicenergistics.common.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.PartEssentiaStorageBus;

/**
 * {@link PartEssentiaStorageBus} server-bound packet.
 * 
 * @author Nividica
 * 
 */
public class Packet_S_EssentiaStorageBus
	extends ThEServerPacket
{
	/**
	 * Packet modes
	 */
	private static final byte MODE_REQUEST_SET_VOID = 1;

	private PartEssentiaStorageBus part;
	private boolean isVoidAllowed;

	/**
	 * Creates the packet
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_S_EssentiaStorageBus newPacket( final EntityPlayer player, final byte mode, final PartEssentiaStorageBus part )
	{
		// Create the packet
		Packet_S_EssentiaStorageBus packet = new Packet_S_EssentiaStorageBus();

		// Set the player & mode & part
		packet.player = player;
		packet.mode = mode;
		packet.part = part;

		return packet;
	}

	public static void setVoidAllowed( final EntityPlayer player, final PartEssentiaStorageBus part,
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
		case Packet_S_EssentiaStorageBus.MODE_REQUEST_SET_VOID:
			// Read the part
			this.part = ( (PartEssentiaStorageBus)ThEBasePacket.readPart( stream ) );
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
		case Packet_S_EssentiaStorageBus.MODE_REQUEST_SET_VOID:
			// Write the part
			ThEBasePacket.writePart( this.part, stream );
			// Write void
			stream.writeBoolean( this.isVoidAllowed );
			break;
		}
	}

}
