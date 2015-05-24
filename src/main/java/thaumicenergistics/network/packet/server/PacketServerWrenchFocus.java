package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import thaumicenergistics.items.ItemFocusAEWrench;
import thaumicenergistics.network.packet.AbstractServerPacket;
import appeng.util.Platform;

public class PacketServerWrenchFocus
	extends AbstractServerPacket
{
	// Seems redundant, but is used as a safegaurd
	private static final byte MODE_FOCUSWRENCH = 1;

	private float eyeHeight;
	private int x, y, z, side;

	@Override
	protected void readData( final ByteBuf stream )
	{
		if( this.mode == PacketServerWrenchFocus.MODE_FOCUSWRENCH )
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

	public PacketServerWrenchFocus createWrenchFocusRequest( final EntityPlayer player, final MovingObjectPosition position )
	{
		// Set player
		this.player = player;

		// Set mode
		this.mode = PacketServerWrenchFocus.MODE_FOCUSWRENCH;

		// Set eye height
		this.eyeHeight = Platform.getEyeOffset( player );

		// Set position
		this.x = position.blockX;
		this.y = position.blockY;
		this.z = position.blockZ;

		// Set side
		this.side = position.sideHit;

		return this;
	}

	@Override
	public void execute()
	{
		if( this.mode == PacketServerWrenchFocus.MODE_FOCUSWRENCH )
		{
			// Create the MOP
			MovingObjectPosition position = new MovingObjectPosition( this.x, this.y, this.z, this.side, Vec3.createVectorHelper( 0, 0, 0 ), true );

			// Call on the wrench focus to handle the dismantle
			ItemFocusAEWrench.performDismantleOnPartHost( this.player, this.eyeHeight, position );
		}
	}
}
