package thaumicenergistics.network.packet.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import appeng.api.config.RedstoneMode;
import io.netty.buffer.ByteBuf;
import thaumicenergistics.gui.GuiEssentiatIO;
import thaumicenergistics.network.packet.AbstractClientPacket;

public class PacketClientEssentiaIOBus
	extends AbstractClientPacket
{
	private static final byte MODE_SET_REDSTONE_CONTROLLED = 0;

	private static final byte MODE_SET_REDSTONE_MODE = 1;

	private static final byte MODE_SET_FILTER_SIZE = 2;

	private static final byte MODE_SEND_FULL_UPDATE = 3;

	private static final RedstoneMode[] REDSTONE_MODES = RedstoneMode.values();

	private RedstoneMode redstoneMode;

	private byte filterSize;

	private boolean redstoneControlled;

	/**
	 * Create a packet to update the client whether the bus is controlled
	 * by redstone or not.
	 * 
	 * @param player
	 * @param redstoneControlled
	 * @return
	 */
	public PacketClientEssentiaIOBus createSetRedstoneControlled( EntityPlayer player, boolean redstoneControlled )
	{
		// Set the player 
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaIOBus.MODE_SET_REDSTONE_CONTROLLED;

		// Set controlled
		this.redstoneControlled = redstoneControlled;

		return this;
	}

	/**
	 * Create a packet to update the clients filter size.
	 * 
	 * @param player
	 * @param filterSize
	 * @return
	 */
	public PacketClientEssentiaIOBus createSetFilterSize( EntityPlayer player, byte filterSize )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaIOBus.MODE_SET_FILTER_SIZE;

		// Set the filter size
		this.filterSize = filterSize;

		return this;
	}

	/**
	 * Create a packet to update the clients redstone mode.
	 * 
	 * @param player
	 * @param redstoneMode
	 * @return
	 */
	public PacketClientEssentiaIOBus createSetRedstoneMode( EntityPlayer player, RedstoneMode redstoneMode )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaIOBus.MODE_SET_REDSTONE_MODE;

		// Set the redstone mode
		this.redstoneMode = redstoneMode;

		return this;
	}

	public PacketClientEssentiaIOBus createFullUpdate( EntityPlayer player, RedstoneMode redstoneMode, byte filterSize, boolean redstoneControlled )
	{
		// Set the player
		this.player = player;

		// Set the redstone mode
		this.redstoneMode = redstoneMode;

		// Set the filter size
		this.filterSize = filterSize;

		// Set controlled
		this.redstoneControlled = redstoneControlled;

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

		// Get the gui
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Ensure the gui is a GuiEssentiaIO
		if( !( gui instanceof GuiEssentiatIO ) )
		{
			return;
		}

		switch ( this.mode )
		{
			case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_CONTROLLED:
				// Set redstone controlled
				( (GuiEssentiatIO)gui ).onReceiveRedstoneControlled( this.redstoneControlled );
				break;

			case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_MODE:
				// Set redstone mode
				( (GuiEssentiatIO)gui ).onReceiveRedstoneMode( this.redstoneMode );
				break;

			case PacketClientEssentiaIOBus.MODE_SET_FILTER_SIZE:
				// Set filter size
				( (GuiEssentiatIO)gui ).onReceiveFilterSize( this.filterSize );
				break;
				
			case PacketClientEssentiaIOBus.MODE_SEND_FULL_UPDATE:
				// Set redstone controlled
				( (GuiEssentiatIO)gui ).onReceiveRedstoneControlled( this.redstoneControlled );
				
				// Set redstone mode
				( (GuiEssentiatIO)gui ).onReceiveRedstoneMode( this.redstoneMode );
				
				// Set filter size
				( (GuiEssentiatIO)gui ).onReceiveFilterSize( this.filterSize );
				break;
		}
	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_CONTROLLED:
				// Read redstone controlled
				this.redstoneControlled = stream.readBoolean();				
				break;

			case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_MODE:
				// Read the redstone mode ordinal
				this.redstoneMode = PacketClientEssentiaIOBus.REDSTONE_MODES[stream.readByte()];
				break;

			case PacketClientEssentiaIOBus.MODE_SET_FILTER_SIZE:
				// Read the filter size
				this.filterSize = stream.readByte();
				break;
				
			case PacketClientEssentiaIOBus.MODE_SEND_FULL_UPDATE:
				// Read redstone controlled
				this.redstoneControlled = stream.readBoolean();
				
				// Read the redstone mode ordinal
				this.redstoneMode = PacketClientEssentiaIOBus.REDSTONE_MODES[stream.readByte()];
				
				// Read the filter size
				this.filterSize = stream.readByte();
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_CONTROLLED:
				// Write redstone controlled
				stream.writeBoolean( this.redstoneControlled );
				break;

			case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_MODE:
				// Write the redstone mode ordinal
				stream.writeByte( (byte)this.redstoneMode.ordinal() );
				break;

			case PacketClientEssentiaIOBus.MODE_SET_FILTER_SIZE:
				// Write the filter size
				stream.writeByte( this.filterSize );
				break;
				
			case PacketClientEssentiaIOBus.MODE_SEND_FULL_UPDATE:
				// Write redstone controlled
				stream.writeBoolean( this.redstoneControlled );
				
				// Write the redstone mode ordinal
				stream.writeByte( (byte)this.redstoneMode.ordinal() );
				
				// Write the filter size
				stream.writeByte( this.filterSize );
				break;
				
		}
	}

}
