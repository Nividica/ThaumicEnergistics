package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.container.ContainerPriority;
import thaumicenergistics.network.packet.AbstractServerPacket;

public class PacketServerPriority
	extends AbstractServerPacket
{
	private static final byte MODE_SET = 0;
	private static final byte MODE_ADJUST = 1;
	private static final byte MODE_REQUEST = 2;

	private int priority;

	@Override
	protected void readData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerPriority.MODE_SET:
			case PacketServerPriority.MODE_ADJUST:
				// Read the priority
				this.priority = stream.readInt();
				break;
		}
	}

	@Override
	protected void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerPriority.MODE_SET:
			case PacketServerPriority.MODE_ADJUST:
				// Write the priority
				stream.writeInt( this.priority );
				break;
		}

	}

	/**
	 * Asks the server to adjust the priority by the specified value.
	 * 
	 * @param priority
	 * @param player
	 * @return
	 */
	public PacketServerPriority createRequestAdjustPriority( int priority, EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerPriority.MODE_ADJUST;

		// Set the priority
		this.priority = priority;

		return this;
	}

	public PacketServerPriority createRequestPriority( EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerPriority.MODE_REQUEST;

		return this;
	}

	/**
	 * Asks the server to set the priority to the specified value.
	 * 
	 * @param priority
	 * @param player
	 * @return
	 */
	public PacketServerPriority createRequestSetPriority( int priority, EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerPriority.MODE_SET;

		// Set the priority
		this.priority = priority;

		return this;
	}

	@Override
	public void execute()
	{
		// Ensure we have a player
		if( this.player == null )
		{
			return;
		}

		// Ensure the player has opened the priority container
		if( !( this.player.openContainer instanceof ContainerPriority ) )
		{
			return;
		}

		switch ( this.mode )
		{
			case PacketServerPriority.MODE_SET:
				// Set the priority
				( (ContainerPriority)this.player.openContainer ).onClientRequestSetPriority( this.priority );
				break;

			case PacketServerPriority.MODE_ADJUST:
				// Adjust the priority
				( (ContainerPriority)this.player.openContainer ).onClientRequestAdjustPriority( this.priority );
				break;

			case PacketServerPriority.MODE_REQUEST:
				// Request the priority
				( (ContainerPriority)this.player.openContainer ).onClientRequestPriority();
				break;
		}
	}

}
