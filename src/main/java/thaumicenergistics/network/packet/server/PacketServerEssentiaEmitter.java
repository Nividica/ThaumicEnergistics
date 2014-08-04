package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import thaumicenergistics.parts.AEPartEssentiaLevelEmitter;

public class PacketServerEssentiaEmitter extends AbstractServerPacket
{
	private static final byte MODE_REQUEST_UPDATE = 0;
	
	private static final byte MODE_SEND_WANTED = 1;
	
	private static final byte MODE_ADJUST_WANTED = 2;
	
	private static final byte MODE_TOGGLE_REDSTONE = 3;
	
	private AEPartEssentiaLevelEmitter part;
	
	private long wantedAmount;
	
	private int adjustmentAmount;
	
	/**
	 * Creates a packet to let the server know a client would like a full update.
	 * @param part
	 * @param player
	 * @return
	 */
	public PacketServerEssentiaEmitter createUpdateRequest( AEPartEssentiaLevelEmitter part, EntityPlayer player )
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketServerEssentiaEmitter.MODE_REQUEST_UPDATE;
		
		// Set the part
		this.part = part;
		
		return this;
	}
	
	/**
	 * Creates a packet to update the wanted amount on the server
	 * @param wantedAmount
	 * @param part
	 * @param player
	 * @return
	 */
	public PacketServerEssentiaEmitter createWantedAmountUpdate( long wantedAmount, AEPartEssentiaLevelEmitter part, EntityPlayer player)
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketServerEssentiaEmitter.MODE_SEND_WANTED;
		
		// Set the part
		this.part = part;
		
		// Set the wanted amount
		this.wantedAmount = wantedAmount;
		
		return this;
	}
	
	/**
	 * Creates a packet to adjust the wanted amount on the server
	 * @param adjustmentAmount
	 * @param part
	 * @param player
	 * @return
	 */
	public PacketServerEssentiaEmitter createWantedAmountAdjustment( int adjustmentAmount, AEPartEssentiaLevelEmitter part, EntityPlayer player )
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketServerEssentiaEmitter.MODE_ADJUST_WANTED;
		
		// Set the part
		this.part = part;
		
		// Set the adjustment
		this.adjustmentAmount = adjustmentAmount;
		
		return this;
	}

	public PacketServerEssentiaEmitter createRedstoneModeToggle( AEPartEssentiaLevelEmitter part, EntityPlayer player )
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketServerEssentiaEmitter.MODE_TOGGLE_REDSTONE;
		
		// Set the part
		this.part = part;
		
		return this;
	}
	
	@Override
	public void execute()
	{
		switch( this.mode )
		{
			case PacketServerEssentiaEmitter.MODE_REQUEST_UPDATE:
				// Request the full update
				this.part.onClientUpdateRequest( this.player );
				break;
				
			case PacketServerEssentiaEmitter.MODE_SEND_WANTED:
				// Set the wanted amount
				this.part.onClientSetWantedAmount( this.wantedAmount, this.player );
				break;
				
			case PacketServerEssentiaEmitter.MODE_ADJUST_WANTED:
				// Set the adjustment amount
				this.part.onClientAdjustWantedAmount( this.adjustmentAmount, this.player );
				break;
				
			case PacketServerEssentiaEmitter.MODE_TOGGLE_REDSTONE:
				// Toggle the redstone mode
				this.part.onClientToggleRedstoneMode( this.player );
				break;
		}
	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch( this.mode )
		{
			case PacketServerEssentiaEmitter.MODE_REQUEST_UPDATE:
				// Read the part
				this.part = (AEPartEssentiaLevelEmitter)AbstractPacket.readPart( stream );
				break;
				
			case PacketServerEssentiaEmitter.MODE_SEND_WANTED:
				// Read the part
				this.part = (AEPartEssentiaLevelEmitter)AbstractPacket.readPart( stream );
				
				// Read the wanted amount
				this.wantedAmount = stream.readLong();
				break;
				
			case PacketServerEssentiaEmitter.MODE_ADJUST_WANTED:
				// Read the part
				this.part = (AEPartEssentiaLevelEmitter)AbstractPacket.readPart( stream );
				
				// Read the adjustment amount
				this.adjustmentAmount = stream.readInt();
				break;
				
			case PacketServerEssentiaEmitter.MODE_TOGGLE_REDSTONE:
				// Read the part
				this.part = (AEPartEssentiaLevelEmitter)AbstractPacket.readPart( stream );
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch( this.mode )
		{
			case PacketServerEssentiaEmitter.MODE_REQUEST_UPDATE:
				// Write the part
				AbstractPacket.writePart( this.part, stream );
				break;
				
			case PacketServerEssentiaEmitter.MODE_SEND_WANTED:
				// Write the part
				AbstractPacket.writePart( this.part, stream );
				
				// Write wanted amount
				stream.writeLong( this.wantedAmount );
				break;
				
			case PacketServerEssentiaEmitter.MODE_ADJUST_WANTED:
				// Write the part
				AbstractPacket.writePart( this.part, stream );
				
				// Write the adjustment amount
				stream.writeInt( this.adjustmentAmount );
				break;
				
			case PacketServerEssentiaEmitter.MODE_TOGGLE_REDSTONE:
				// Write the part
				AbstractPacket.writePart( this.part, stream );
				break;
		}
	}
	

}
