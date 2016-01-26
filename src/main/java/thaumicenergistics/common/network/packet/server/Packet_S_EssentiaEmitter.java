package thaumicenergistics.common.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.PartEssentiaLevelEmitter;

/**
 * {@link PartEssentiaLevelEmitter} server-bound packet.
 * 
 * @author Nividica
 * 
 */
public class Packet_S_EssentiaEmitter
	extends ThEServerPacket
{
	private static final byte MODE_SEND_WANTED = 1,
					MODE_ADJUST_WANTED = 2,
					MODE_TOGGLE_REDSTONE = 3;

	private PartEssentiaLevelEmitter part;
	private long wantedAmount;
	private int adjustmentAmount;

	/**
	 * Creates the packet
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_S_EssentiaEmitter newPacket( final EntityPlayer player, final byte mode )
	{
		// Create the packet
		Packet_S_EssentiaEmitter packet = new Packet_S_EssentiaEmitter();

		// Set the player & mode
		packet.player = player;
		packet.mode = mode;

		return packet;
	}

	public static void sendRedstoneModeToggle( final PartEssentiaLevelEmitter part, final EntityPlayer player )
	{
		Packet_S_EssentiaEmitter packet = newPacket( player, MODE_TOGGLE_REDSTONE );

		// Set the part
		packet.part = part;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Creates a packet to update the wanted amount on the server
	 * 
	 * @param wantedAmount
	 * @param part
	 * @param player
	 * @return
	 */
	public static void sendWantedAmount( final long wantedAmount, final PartEssentiaLevelEmitter part,
											final EntityPlayer player )
	{
		Packet_S_EssentiaEmitter packet = newPacket( player, MODE_SEND_WANTED );

		// Set the part
		packet.part = part;

		// Set the wanted amount
		packet.wantedAmount = wantedAmount;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Creates a packet to adjust the wanted amount on the server
	 * 
	 * @param adjustmentAmount
	 * @param part
	 * @param player
	 * @return
	 */
	public static void sendWantedAmountDelta( final int adjustmentAmount, final PartEssentiaLevelEmitter part,
												final EntityPlayer player )
	{
		Packet_S_EssentiaEmitter packet = newPacket( player, MODE_ADJUST_WANTED );

		// Set the part
		packet.part = part;

		// Set the adjustment
		packet.adjustmentAmount = adjustmentAmount;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	public void execute()
	{
		// Null check
		if( this.part == null )
		{
			return;
		}

		switch ( this.mode )
		{
		case MODE_SEND_WANTED:
			// Set the wanted amount
			this.part.onSetThresholdLevel( this.wantedAmount, this.player );
			break;

		case MODE_ADJUST_WANTED:
			// Set the adjustment amount
			this.part.onAdjustThresholdLevel( this.adjustmentAmount, this.player );
			break;

		case MODE_TOGGLE_REDSTONE:
			// Toggle the redstone mode
			this.part.onClientToggleRedstoneMode( this.player );
			break;
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case MODE_TOGGLE_REDSTONE:
			// Read the part
			this.part = (PartEssentiaLevelEmitter)ThEBasePacket.readPart( stream );
			break;

		case MODE_SEND_WANTED:
			// Read the part
			this.part = (PartEssentiaLevelEmitter)ThEBasePacket.readPart( stream );

			// Read the wanted amount
			this.wantedAmount = stream.readLong();
			break;

		case MODE_ADJUST_WANTED:
			// Read the part
			this.part = (PartEssentiaLevelEmitter)ThEBasePacket.readPart( stream );

			// Read the adjustment amount
			this.adjustmentAmount = stream.readInt();
			break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case MODE_TOGGLE_REDSTONE:
			// Write the part
			ThEBasePacket.writePart( this.part, stream );
			break;

		case MODE_SEND_WANTED:
			// Write the part
			ThEBasePacket.writePart( this.part, stream );

			// Write wanted amount
			stream.writeLong( this.wantedAmount );
			break;

		case MODE_ADJUST_WANTED:
			// Write the part
			ThEBasePacket.writePart( this.part, stream );

			// Write the adjustment amount
			stream.writeInt( this.adjustmentAmount );
			break;
		}
	}

}
