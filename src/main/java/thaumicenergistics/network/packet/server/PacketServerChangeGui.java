package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import thaumicenergistics.gui.TEGuiHandler;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import thaumicenergistics.parts.AbstractAEPartBase;

public class PacketServerChangeGui
	extends AbstractServerPacket
{
	private static final byte MODE_REGULAR = 0;
	private static final byte MODE_PART = 1;

	private int guiID;
	private AbstractAEPartBase part;
	private World world;
	private int x;
	private int y;
	private int z;

	public PacketServerChangeGui createChangeGuiRequest( final AbstractAEPartBase part, final EntityPlayer player, final World world, final int x,
															final int y, final int z )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerChangeGui.MODE_PART;

		// Set the part
		this.part = part;

		// Set the world
		this.world = world;

		// Set the coords
		this.x = x;
		this.y = y;
		this.z = z;

		return this;
	}

	public PacketServerChangeGui createChangeGuiRequest( final int guiID, final EntityPlayer player, final World world, final int x, final int y,
															final int z )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerChangeGui.MODE_REGULAR;

		// Set the guiID
		this.guiID = guiID;

		// Set the world
		this.world = world;

		// Set the coords
		this.x = x;
		this.y = y;
		this.z = z;

		return this;
	}

	@Override
	public void execute()
	{
		if( this.mode == PacketServerChangeGui.MODE_REGULAR )
		{
			// Launch regular
			TEGuiHandler.launchGui( this.guiID, this.player, this.world, this.x, this.y, this.z );
		}
		else if( this.mode == PacketServerChangeGui.MODE_PART )
		{
			// Launch part
			TEGuiHandler.launchGui( this.part, this.player, this.world, this.x, this.y, this.z );
		}

	}

	@Override
	public void readData( final ByteBuf stream )
	{
		if( this.mode == PacketServerChangeGui.MODE_REGULAR )
		{
			// Read the ID
			this.guiID = stream.readInt();
		}
		else if( this.mode == PacketServerChangeGui.MODE_PART )
		{
			// Read the part
			this.part = AbstractPacket.readPart( stream );
		}

		// Read the world
		this.world = AbstractPacket.readWorld( stream );

		// Read the coords
		this.x = stream.readInt();
		this.y = stream.readInt();
		this.z = stream.readInt();
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		if( this.mode == PacketServerChangeGui.MODE_REGULAR )
		{
			// Write the ID
			stream.writeInt( this.guiID );
		}
		else if( this.mode == PacketServerChangeGui.MODE_PART )
		{
			// Write the part
			AbstractPacket.writePart( this.part, stream );
		}

		// Write the world
		AbstractPacket.writeWorld( this.world, stream );

		// Write the coords
		stream.writeInt( this.x );
		stream.writeInt( this.y );
		stream.writeInt( this.z );
	}

}
