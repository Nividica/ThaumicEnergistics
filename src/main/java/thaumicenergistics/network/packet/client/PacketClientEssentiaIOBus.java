package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.gui.GuiEssentiaIO;
import thaumicenergistics.network.packet.AbstractClientPacket;
import thaumicenergistics.registries.EnumCache;
import appeng.api.config.RedstoneMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketClientEssentiaIOBus
	extends AbstractClientPacket
{
	private static final byte MODE_SET_REDSTONE_CONTROLLED = 0, MODE_SET_REDSTONE_MODE = 1, MODE_SET_FILTER_SIZE = 2, MODE_SEND_FULL_UPDATE = 3,
					MODE_SEND_VOID_MODE = 4;

	private RedstoneMode redstoneMode;

	private byte filterSize;

	private boolean redstoneControlled, isVoidAllowed;

	@SideOnly(Side.CLIENT)
	@Override
	protected void wrappedExecute()
	{
		// Get the gui
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Ensure the gui is a GuiEssentiaIO
		if( !( gui instanceof GuiEssentiaIO ) )
		{
			return;
		}

		switch ( this.mode )
		{
		case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_CONTROLLED:
			// Set redstone controlled
			( (GuiEssentiaIO)gui ).onReceiveRedstoneControlled( this.redstoneControlled );
			break;

		case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_MODE:
			// Set redstone mode
			( (GuiEssentiaIO)gui ).onReceiveRedstoneMode( this.redstoneMode );
			break;

		case PacketClientEssentiaIOBus.MODE_SET_FILTER_SIZE:
			// Set filter size
			( (GuiEssentiaIO)gui ).onReceiveFilterSize( this.filterSize );
			break;

		case PacketClientEssentiaIOBus.MODE_SEND_FULL_UPDATE:
			// Set redstone mode
			( (GuiEssentiaIO)gui ).onReceiveRedstoneMode( this.redstoneMode );

			// Set redstone controlled
			( (GuiEssentiaIO)gui ).onReceiveRedstoneControlled( this.redstoneControlled );

			// Set filter size
			( (GuiEssentiaIO)gui ).onReceiveFilterSize( this.filterSize );
			break;

		case PacketClientEssentiaIOBus.MODE_SEND_VOID_MODE:
			// Set void mode
			( (GuiEssentiaIO)gui ).onServerSendVoidMode( this.isVoidAllowed );
			break;
		}
	}

	public PacketClientEssentiaIOBus createFullUpdate( final EntityPlayer player, final RedstoneMode redstoneMode, final byte filterSize,
														final boolean redstoneControlled )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaIOBus.MODE_SEND_FULL_UPDATE;

		// Set the redstone mode
		this.redstoneMode = redstoneMode;

		// Set the filter size
		this.filterSize = filterSize;

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
	public PacketClientEssentiaIOBus createSetFilterSize( final EntityPlayer player, final byte filterSize )
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
	 * Create a packet to update the client whether the bus is controlled
	 * by redstone or not.
	 * 
	 * @param player
	 * @param redstoneControlled
	 * @return
	 */
	public PacketClientEssentiaIOBus createSetRedstoneControlled( final EntityPlayer player, final boolean redstoneControlled )
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
	 * Create a packet to update the clients redstone mode.
	 * 
	 * @param player
	 * @param redstoneMode
	 * @return
	 */
	public PacketClientEssentiaIOBus createSetRedstoneMode( final EntityPlayer player, final RedstoneMode redstoneMode )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaIOBus.MODE_SET_REDSTONE_MODE;

		// Set the redstone mode
		this.redstoneMode = redstoneMode;

		return this;
	}

	/**
	 * Sends an update the client informing it of the void mode.
	 * 
	 * @param player
	 * @param isVoidAllowed
	 * @return
	 */
	public PacketClientEssentiaIOBus createVoidModeUpdate( final EntityPlayer player, final boolean isVoidAllowed )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaIOBus.MODE_SEND_VOID_MODE;

		// Set the void mode
		this.isVoidAllowed = isVoidAllowed;

		return this;
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_CONTROLLED:
			// Read redstone controlled
			this.redstoneControlled = stream.readBoolean();
			break;

		case PacketClientEssentiaIOBus.MODE_SET_REDSTONE_MODE:
			// Read the redstone mode ordinal
			this.redstoneMode = EnumCache.AE_REDSTONE_MODES[stream.readByte()];
			break;

		case PacketClientEssentiaIOBus.MODE_SET_FILTER_SIZE:
			// Read the filter size
			this.filterSize = stream.readByte();
			break;

		case PacketClientEssentiaIOBus.MODE_SEND_FULL_UPDATE:
			// Read redstone controlled
			this.redstoneControlled = stream.readBoolean();

			// Read the redstone mode ordinal
			this.redstoneMode = EnumCache.AE_REDSTONE_MODES[stream.readByte()];

			// Read the filter size
			this.filterSize = stream.readByte();
			break;

		case PacketClientEssentiaIOBus.MODE_SEND_VOID_MODE:
			// Read void mode
			this.isVoidAllowed = stream.readBoolean();
			break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
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

		case PacketClientEssentiaIOBus.MODE_SEND_VOID_MODE:
			// Write void mode
			stream.writeBoolean( this.isVoidAllowed );
			break;

		}
	}

}
