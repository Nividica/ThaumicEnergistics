package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import thaumicenergistics.items.ItemFocusAEWrench;
import thaumicenergistics.network.NetworkHandler;
import thaumicenergistics.network.packet.ThEServerPacket;
import appeng.util.Platform;

public class Packet_S_WrenchFocus
	extends ThEServerPacket
{
	// Seems redundant, but is used as a safegaurd
	private static final byte MODE_FOCUSWRENCH = 1;

	private float eyeHeight;
	private int x, y, z, side;

	public static void sendWrenchFocusRequest( final EntityPlayer player, final MovingObjectPosition position )
	{
		Packet_S_WrenchFocus packet = new Packet_S_WrenchFocus();

		// Set player
		packet.player = player;

		// Set mode
		packet.mode = Packet_S_WrenchFocus.MODE_FOCUSWRENCH;

		// Set eye height
		packet.eyeHeight = Platform.getEyeOffset( player );

		// Set position
		packet.x = position.blockX;
		packet.y = position.blockY;
		packet.z = position.blockZ;

		// Set side
		packet.side = position.sideHit;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
		if( this.mode == Packet_S_WrenchFocus.MODE_FOCUSWRENCH )
		{
			// Read the eye height
			this.eyeHeight = stream.readFloat();

			// Read position
			this.x = stream.readInt();
			this.y = stream.readInt();
			this.z = stream.readInt();

			// Read side
			this.side = stream.readInt();
		}
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		// Write the eye height
		stream.writeFloat( this.eyeHeight );

		// Write position
		stream.writeInt( this.x ).writeInt( this.y ).writeInt( this.z );

		// Write side
		stream.writeInt( this.side );
	}

	@Override
	public void execute()
	{
		if( this.mode == Packet_S_WrenchFocus.MODE_FOCUSWRENCH )
		{
			// Create the MOP
			MovingObjectPosition position = new MovingObjectPosition( this.x, this.y, this.z, this.side, Vec3.createVectorHelper( 0, 0, 0 ), true );

			// Call on the wrench focus to handle the dismantle
			ItemFocusAEWrench.performDismantleOnPartHost( this.player, this.eyeHeight, position );
		}
	}
}
