package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import thaumicenergistics.parts.AEPartEssentiaIO;

public class PacketServerEssentiaIOBus
	extends AbstractServerPacket
{
	private static final byte MODE_REQUEST_FULL_UPDATE = 0;
	
	private static final byte MODE_REQUEST_CHANGE_REDSTONE_MODE = 1;
	
	private AEPartEssentiaIO part;

	public PacketServerEssentiaIOBus createRequestFullUpdate( EntityPlayer player, AEPartEssentiaIO part )
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketServerEssentiaIOBus.MODE_REQUEST_FULL_UPDATE;

		// Set the part
		this.part = part;
		
		return this;
	}

	public PacketServerEssentiaIOBus createRequestChangeRedstoneMode( EntityPlayer player, AEPartEssentiaIO part )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE;

		// Set the part
		this.part = part;
		
		return this;
	}

	@Override
	public void execute()
	{
		switch( this.mode )
		{
			case PacketServerEssentiaIOBus.MODE_REQUEST_FULL_UPDATE:
				// Request a full update
				this.part.onClientRequestFullUpdate( this.player );
				break;
				
			case PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE:
				// Request a redstone mode change
				this.part.onClientRequestChangeRedstoneMode( this.player );
				break;
		}
	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch( this.mode )
		{
			case PacketServerEssentiaIOBus.MODE_REQUEST_FULL_UPDATE:
			case PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE:
				// Read the part
				this.part = ( (AEPartEssentiaIO)AbstractPacket.readPart( stream ) );
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch( this.mode )
		{
			case PacketServerEssentiaIOBus.MODE_REQUEST_FULL_UPDATE:
			case PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE:
				// Write the part
				AbstractPacket.writePart( this.part, stream );
				break;
		}
	}

}
