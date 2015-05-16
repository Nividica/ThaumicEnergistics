package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import thaumicenergistics.parts.AEPartEssentiaExportBus;
import thaumicenergistics.parts.AbstractAEPartEssentiaIOBus;

public class PacketServerEssentiaIOBus
	extends AbstractServerPacket
{
	private static final byte MODE_REQUEST_FULL_UPDATE = 0, MODE_REQUEST_CHANGE_REDSTONE_MODE = 1, MODE_REQUEST_CHANGE_VOID_MODE = 2;

	private AbstractAEPartEssentiaIOBus part;

	/**
	 * Sends a request to the server to change the redstone mode.
	 * 
	 * @param player
	 * @param part
	 * @return
	 */
	public PacketServerEssentiaIOBus createRequestChangeRedstoneMode( final EntityPlayer player, final AbstractAEPartEssentiaIOBus part )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE;

		// Set the part
		this.part = part;

		return this;
	}

	/**
	 * Sends a request to the server to update the void mode.
	 * 
	 * @param player
	 * @param part
	 * @return
	 */
	public PacketServerEssentiaIOBus createRequestChangeVoidMode( final EntityPlayer player, final AEPartEssentiaExportBus part )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_VOID_MODE;

		// Set the part
		this.part = part;

		return this;
	}

	/**
	 * Sends a request to the server for a full update of the buses state.
	 * 
	 * @param player
	 * @param part
	 * @return
	 */
	public PacketServerEssentiaIOBus createRequestFullUpdate( final EntityPlayer player, final AbstractAEPartEssentiaIOBus part )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaIOBus.MODE_REQUEST_FULL_UPDATE;

		// Set the part
		this.part = part;

		return this;
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaIOBus.MODE_REQUEST_FULL_UPDATE:
				// Request a full update
				this.part.onClientRequestFullUpdate( this.player );
				break;

			case PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE:
				// Request a redstone mode change
				this.part.onClientRequestChangeRedstoneMode( this.player );
				break;

			case PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_VOID_MODE:
				// Request a void mode change
				if( this.part instanceof AEPartEssentiaExportBus )
				{
					( (AEPartEssentiaExportBus)this.part ).onClientRequestChangeVoidMode( this.player );
				}
				break;
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaIOBus.MODE_REQUEST_FULL_UPDATE:
			case PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE:
			case PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_VOID_MODE:
				// Read the part
				this.part = ( (AbstractAEPartEssentiaIOBus)AbstractPacket.readPart( stream ) );
				break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaIOBus.MODE_REQUEST_FULL_UPDATE:
			case PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_REDSTONE_MODE:
			case PacketServerEssentiaIOBus.MODE_REQUEST_CHANGE_VOID_MODE:
				// Write the part
				AbstractPacket.writePart( this.part, stream );
				break;
		}
	}

}
