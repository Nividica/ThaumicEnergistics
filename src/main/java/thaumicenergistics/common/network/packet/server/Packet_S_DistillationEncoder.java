package thaumicenergistics.common.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.common.container.ContainerDistillationPatternEncoder;
import thaumicenergistics.common.network.NetworkHandler;

public class Packet_S_DistillationEncoder
	extends ThEServerPacket
{
	private static final byte MODE_ENCODE = 1;

	public static void sendEncodePattern( final EntityPlayer player )
	{
		// Create a new packet
		Packet_S_DistillationEncoder packet = new Packet_S_DistillationEncoder();

		// Set the player
		packet.player = player;

		// Set the mode
		packet.mode = MODE_ENCODE;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
	}

	@Override
	public void execute()
	{
		// Sanity check
		if( this.mode != MODE_ENCODE )
		{
			return;
		}

		// Get the players open container
		if( this.player.openContainer instanceof ContainerDistillationPatternEncoder )
		{
			// Send the encode
			( (ContainerDistillationPatternEncoder)this.player.openContainer ).onEncodePattern();
		}
	}

}
