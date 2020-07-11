package thaumicenergistics.common.network.packet.client;

import appeng.api.config.RedstoneMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.client.gui.GuiEssentiaIO;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.parts.PartEssentiaExportBus;
import thaumicenergistics.common.parts.PartEssentiaImportBus;
import thaumicenergistics.common.registries.EnumCache;

/**
 * {@link PartEssentiaImportBus}, {@link PartEssentiaExportBus} client-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_C_EssentiaIOBus
	extends ThEClientPacket
{
	/**
	 * Packet modes.
	 */
	private static final byte MODE_SET_REDSTONE_CONTROLLED = 0,
					MODE_SET_REDSTONE_MODE = 1,
					MODE_SET_FILTER_SIZE = 2,
					MODE_SEND_FULL_UPDATE = 3,
					MODE_SEND_VOID_MODE = 4,
					MODE_SEND_HAS_CRAFTINFG = 5,
					MODE_SET_CRAFTING_MODE = 6;

	private RedstoneMode redstoneMode;

	private byte filterSize;

	private boolean redstoneControlled, isVoidAllowed, hasCraftingCard, craftingOnly;

	/**
	 * Creates the packet
	 *
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_C_EssentiaIOBus newPacket( final EntityPlayer player, final byte mode )
	{
		// Create the packet
		Packet_C_EssentiaIOBus packet = new Packet_C_EssentiaIOBus();

		// Set the player & mode
		packet.player = player;
		packet.mode = mode;

		return packet;
	}

	public static void sendBusState(	final EntityPlayer player, final RedstoneMode redstoneMode, final byte filterSize,
										final boolean redstoneControlled, final boolean hasCraftingCard, final boolean isCraftingOnly )
	{
		Packet_C_EssentiaIOBus packet = newPacket( player, MODE_SEND_FULL_UPDATE );

		// Set the redstone mode
		packet.redstoneMode = redstoneMode;

		// Set the filter size
		packet.filterSize = filterSize;

		// Set controlled
		packet.redstoneControlled = redstoneControlled;
		packet.hasCraftingCard = hasCraftingCard;
		packet.craftingOnly = isCraftingOnly;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Create a packet to update the clients filter size.
	 *
	 * @param player
	 * @param filterSize
	 * @return
	 */
	public static void sendFilterSize( final EntityPlayer player, final byte filterSize )
	{
		Packet_C_EssentiaIOBus packet = newPacket( player, MODE_SET_FILTER_SIZE );

		// Set the filter size
		packet.filterSize = filterSize;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Create a packet to update the client whether the bus is controlled
	 * by redstone or not.
	 *
	 * @param player
	 * @param redstoneControlled
	 * @return
	 */
	public static void sendRedstoneControlled( final EntityPlayer player, final boolean redstoneControlled )
	{
		Packet_C_EssentiaIOBus packet = newPacket( player, MODE_SET_REDSTONE_CONTROLLED );

		// Set controlled
		packet.redstoneControlled = redstoneControlled;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	public static void sendHasCraftingCard( final EntityPlayer player, final boolean hasCraftingCard )
	{
		Packet_C_EssentiaIOBus packet = newPacket( player, MODE_SEND_HAS_CRAFTINFG );

		// Set controlled
		packet.hasCraftingCard = hasCraftingCard;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Create a packet to update the clients redstone mode.
	 *
	 * @param player
	 * @param redstoneMode
	 * @return
	 */
	public static void sendRedstoneMode( final EntityPlayer player, final RedstoneMode redstoneMode )
	{
		Packet_C_EssentiaIOBus packet = newPacket( player, MODE_SET_REDSTONE_MODE );

		// Set the redstone mode
		packet.redstoneMode = redstoneMode;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Sends an update the client informing it of the void mode.
	 *
	 * @param player
	 * @param isVoidAllowed
	 * @return
	 */
	public static void sendVoidMode( final EntityPlayer player, final boolean isVoidAllowed )
	{
		Packet_C_EssentiaIOBus packet = newPacket( player, MODE_SEND_VOID_MODE );

		// Set the void mode
		packet.isVoidAllowed = isVoidAllowed;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	public static void sendCraftingMode( final EntityPlayer player, final boolean isCraftingOnly )
	{
		Packet_C_EssentiaIOBus packet = newPacket( player, MODE_SET_CRAFTING_MODE );

		// Set the void mode
		packet.craftingOnly = isCraftingOnly;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}


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
		case Packet_C_EssentiaIOBus.MODE_SET_REDSTONE_CONTROLLED:
			// Set redstone controlled
			( (GuiEssentiaIO)gui ).onReceiveRedstoneControlled( this.redstoneControlled );
			break;

		case Packet_C_EssentiaIOBus.MODE_SET_REDSTONE_MODE:
			// Set redstone mode
			( (GuiEssentiaIO)gui ).onReceiveRedstoneMode( this.redstoneMode );
			break;

		case Packet_C_EssentiaIOBus.MODE_SET_FILTER_SIZE:
			// Set filter size
			( (GuiEssentiaIO)gui ).onReceiveFilterSize( this.filterSize );
			break;

		case Packet_C_EssentiaIOBus.MODE_SEND_FULL_UPDATE:
			// Set redstone mode
			( (GuiEssentiaIO)gui ).onReceiveRedstoneMode( this.redstoneMode );

			// Set redstone controlled
			( (GuiEssentiaIO)gui ).onReceiveRedstoneControlled( this.redstoneControlled );

			// Set filter size
			( (GuiEssentiaIO)gui ).onReceiveFilterSize( this.filterSize );
			( (GuiEssentiaIO)gui ).onReceiveHasCrafting(this.hasCraftingCard);
			( (GuiEssentiaIO)gui ).onReceiveCraftingOnly(this.craftingOnly);
			break;

		case Packet_C_EssentiaIOBus.MODE_SEND_VOID_MODE:
			// Set void mode
			( (GuiEssentiaIO)gui ).onServerSendVoidMode( this.isVoidAllowed );
			break;

			case Packet_C_EssentiaIOBus.MODE_SET_CRAFTING_MODE:
				( (GuiEssentiaIO)gui ).onReceiveCraftingOnly(this.craftingOnly);
				break;

			case Packet_C_EssentiaIOBus.MODE_SEND_HAS_CRAFTINFG:
				( (GuiEssentiaIO)gui ).onReceiveHasCrafting(this.hasCraftingCard);
				break;

		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case Packet_C_EssentiaIOBus.MODE_SET_REDSTONE_CONTROLLED:
			// Read redstone controlled
			this.redstoneControlled = stream.readBoolean();
			break;

		case Packet_C_EssentiaIOBus.MODE_SET_REDSTONE_MODE:
			// Read the redstone mode ordinal
			this.redstoneMode = EnumCache.AE_REDSTONE_MODES[stream.readByte()];
			break;

		case Packet_C_EssentiaIOBus.MODE_SET_FILTER_SIZE:
			// Read the filter size
			this.filterSize = stream.readByte();
			break;

		case Packet_C_EssentiaIOBus.MODE_SEND_FULL_UPDATE:
			// Read redstone controlled
			this.redstoneControlled = stream.readBoolean();

			// Read the redstone mode ordinal
			this.redstoneMode = EnumCache.AE_REDSTONE_MODES[stream.readByte()];

			// Read the filter size
			this.filterSize = stream.readByte();

			this.hasCraftingCard = stream.readBoolean();
			this.craftingOnly = stream.readBoolean();
			break;

		case Packet_C_EssentiaIOBus.MODE_SEND_VOID_MODE:
			// Read void mode
			this.isVoidAllowed = stream.readBoolean();
			break;

			case Packet_C_EssentiaIOBus.MODE_SET_CRAFTING_MODE:
				this.craftingOnly = stream.readBoolean();
				break;

			case Packet_C_EssentiaIOBus.MODE_SEND_HAS_CRAFTINFG:
				this.hasCraftingCard = stream.readBoolean();
				break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case Packet_C_EssentiaIOBus.MODE_SET_REDSTONE_CONTROLLED:
			// Write redstone controlled
			stream.writeBoolean( this.redstoneControlled );
			break;

		case Packet_C_EssentiaIOBus.MODE_SET_REDSTONE_MODE:
			// Write the redstone mode ordinal
			stream.writeByte( (byte)this.redstoneMode.ordinal() );
			break;

		case Packet_C_EssentiaIOBus.MODE_SET_FILTER_SIZE:
			// Write the filter size
			stream.writeByte( this.filterSize );
			break;

		case Packet_C_EssentiaIOBus.MODE_SEND_FULL_UPDATE:
			// Write redstone controlled
			stream.writeBoolean( this.redstoneControlled );

			// Write the redstone mode ordinal
			stream.writeByte( (byte)this.redstoneMode.ordinal() );

			// Write the filter size
			stream.writeByte( this.filterSize );

			stream.writeBoolean(this.hasCraftingCard);
			stream.writeBoolean(this.craftingOnly);
			break;

		case Packet_C_EssentiaIOBus.MODE_SEND_VOID_MODE:
			// Write void mode
			stream.writeBoolean( this.isVoidAllowed );
			break;

			case Packet_C_EssentiaIOBus.MODE_SET_CRAFTING_MODE:
				stream.writeBoolean(this.craftingOnly);
				break;

			case Packet_C_EssentiaIOBus.MODE_SEND_HAS_CRAFTINFG:
				stream.writeBoolean(this.hasCraftingCard);
				break;
		}
	}

}
