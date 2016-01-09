package thaumicenergistics.common.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.PartEssentiaExportBus;
import thaumicenergistics.common.parts.ThEPartEssentiaIOBus_Base;

public class Packet_S_EssentiaIOBus
	extends ThEServerPacket
{
	private static final byte MODE_REQUEST_FULL_UPDATE = 0, MODE_REQUEST_CHANGE_REDSTONE_MODE = 1, MODE_REQUEST_CHANGE_VOID_MODE = 2;

	private ThEPartEssentiaIOBus_Base part;

	/**
	 * Creates the packet
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_S_EssentiaIOBus newPacket( final EntityPlayer player, final byte mode, final ThEPartEssentiaIOBus_Base part )
	{
		// Create the packet
		Packet_S_EssentiaIOBus packet = new Packet_S_EssentiaIOBus();

		// Set the player & mode & part
		packet.player = player;
		packet.mode = mode;
		packet.part = part;

		return packet;
	}

	/**
	 * Sends a request to the server for a full update of the buses state.
	 * 
	 * @param player
	 * @param part
	 * @return
	 */
	public static void sendRequestFilterList( final EntityPlayer player, final ThEPartEssentiaIOBus_Base part )
	{
		Packet_S_EssentiaIOBus packet = newPacket( player, MODE_REQUEST_FULL_UPDATE, part );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Sends a request to the server to change the redstone mode.
	 * 
	 * @param player
	 * @param part
	 * @return
	 */
	public static void sendRedstoneModeChange( final EntityPlayer player, final ThEPartEssentiaIOBus_Base part )
	{
		Packet_S_EssentiaIOBus packet = newPacket( player, MODE_REQUEST_CHANGE_REDSTONE_MODE, part );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Sends a request to the server to update the void mode.
	 * 
	 * @param player
	 * @param part
	 * @return
	 */
	public static void sendVoidModeChange( final EntityPlayer player, final PartEssentiaExportBus part )
	{
		Packet_S_EssentiaIOBus packet = newPacket( player, MODE_REQUEST_CHANGE_VOID_MODE, part );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
		case Packet_S_EssentiaIOBus.MODE_REQUEST_FULL_UPDATE:
			// Request a full update
			this.part.onClientRequestFilterList( this.player );
			break;

		case Packet_S_EssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE:
			// Request a redstone mode change
			this.part.onClientRequestChangeRedstoneMode( this.player );
			break;

		case Packet_S_EssentiaIOBus.MODE_REQUEST_CHANGE_VOID_MODE:
			// Request a void mode change
			if( this.part instanceof PartEssentiaExportBus )
			{
				( (PartEssentiaExportBus)this.part ).toggleVoidMode( this.player );
			}
			break;
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case Packet_S_EssentiaIOBus.MODE_REQUEST_FULL_UPDATE:
		case Packet_S_EssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE:
		case Packet_S_EssentiaIOBus.MODE_REQUEST_CHANGE_VOID_MODE:
			// Read the part
			this.part = ( (ThEPartEssentiaIOBus_Base)ThEBasePacket.readPart( stream ) );
			break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case Packet_S_EssentiaIOBus.MODE_REQUEST_FULL_UPDATE:
		case Packet_S_EssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE:
		case Packet_S_EssentiaIOBus.MODE_REQUEST_CHANGE_VOID_MODE:
			// Write the part
			ThEBasePacket.writePart( this.part, stream );
			break;
		}
	}

}
