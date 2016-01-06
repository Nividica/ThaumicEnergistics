package thaumicenergistics.common.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.ThEPartBase;

public class Packet_S_ChangeGui
	extends ThEServerPacket
{
	/**
	 * Packet modes
	 */
	private static final byte MODE_REGULAR = 0,
					MODE_PART = 1,
					MODE_WIRELESS = 2;

	private int guiID;
	private ThEPartBase part;
	private World world;
	private int x;
	private int y;
	private int z;

	/**
	 * Creates the packet
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_S_ChangeGui newPacket( final EntityPlayer player, final byte mode )
	{
		// Create the packet
		Packet_S_ChangeGui packet = new Packet_S_ChangeGui();

		// Set the player & mode
		packet.player = player;
		packet.mode = mode;

		return packet;
	}

	public static void sendGuiChange( final int guiID, final EntityPlayer player, final World world, final int x, final int y,
										final int z )
	{
		Packet_S_ChangeGui packet = newPacket( player, MODE_REGULAR );

		// Set the guiID
		packet.guiID = guiID;

		// Set the world
		packet.world = world;

		// Set the coords
		packet.x = x;
		packet.y = y;
		packet.z = z;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendGuiChangeToPart( final ThEPartBase part, final EntityPlayer player, final World world, final int x,
											final int y, final int z )
	{
		Packet_S_ChangeGui packet = newPacket( player, MODE_PART );

		// Set the part
		packet.part = part;

		// Set the world
		packet.world = world;

		// Set the coords
		packet.x = x;
		packet.y = y;
		packet.z = z;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendGuiChangeToWirelessTerminal( final EntityPlayer player )
	{
		Packet_S_ChangeGui packet = newPacket( player, MODE_WIRELESS );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
		case MODE_REGULAR:
			// Launch regular
			ThEGuiHandler.launchGui( this.guiID, this.player, this.world, this.x, this.y, this.z );
			break;

		case MODE_PART:
			// Launch part
			ThEGuiHandler.launchGui( this.part, this.player, this.world, this.x, this.y, this.z );
			break;

		case MODE_WIRELESS:
			// Launch wireless
			ThEApi.instance().interact().openWirelessTerminalGui( this.player );
			break;
		}

	}

	@Override
	public void readData( final ByteBuf stream )
	{
		if( this.mode != MODE_WIRELESS )
		{
			if( this.mode == MODE_REGULAR )
			{
				// Read the ID
				this.guiID = stream.readInt();
			}
			else if( this.mode == MODE_PART )
			{
				// Read the part
				this.part = ThEBasePacket.readPart( stream );
			}

			// Read the world
			this.world = ThEBasePacket.readWorld( stream );

			// Read the coords
			this.x = stream.readInt();
			this.y = stream.readInt();
			this.z = stream.readInt();
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		if( this.mode != MODE_WIRELESS )
		{
			if( this.mode == MODE_REGULAR )
			{
				// Write the ID
				stream.writeInt( this.guiID );
			}
			else if( this.mode == MODE_PART )
			{
				// Write the part
				ThEBasePacket.writePart( this.part, stream );
			}
			// Write the world
			ThEBasePacket.writeWorld( this.world, stream );

			// Write the coords
			stream.writeInt( this.x );
			stream.writeInt( this.y );
			stream.writeInt( this.z );
		}
	}

}
