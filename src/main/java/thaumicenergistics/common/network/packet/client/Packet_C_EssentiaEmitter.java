package thaumicenergistics.common.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.client.gui.GuiEssentiaLevelEmitter;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.registries.EnumCache;
import appeng.api.config.RedstoneMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Packet_C_EssentiaEmitter
	extends ThEClientPacket
{
	private static final byte MODE_FULL_UPDATE = 0;

	private static final byte MODE_UPDATE_WANTED = 1;

	private static final byte MODE_UPDATE_REDSTONE = 2;

	private RedstoneMode redstoneMode;

	private long wantedAmount;

	/**
	 * Creates the packet
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_C_EssentiaEmitter newPacket( final EntityPlayer player, final byte mode )
	{
		// Create the packet
		Packet_C_EssentiaEmitter packet = new Packet_C_EssentiaEmitter();

		// Set the player & mode
		packet.player = player;
		packet.mode = mode;

		return packet;
	}

	/**
	 * Creates a packet containing the emitter state.
	 * 
	 * @param redstoneMode
	 * @param wantedAmount
	 * @param player
	 * @return
	 */
	public static void sendEmitterState( final RedstoneMode redstoneMode, final long wantedAmount, final EntityPlayer player )
	{
		Packet_C_EssentiaEmitter packet = newPacket( player, MODE_FULL_UPDATE );

		// Set the redstone mode
		packet.redstoneMode = redstoneMode;

		// Set the wanted amount
		packet.wantedAmount = wantedAmount;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	public static void sendRedstoneMode( final RedstoneMode redstoneMode, final EntityPlayer player )
	{
		Packet_C_EssentiaEmitter packet = newPacket( player, MODE_UPDATE_REDSTONE );

		// Set the redstone mode
		packet.redstoneMode = redstoneMode;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Create a packet to update a client with a new wanted amount.
	 * 
	 * @param wantedAmount
	 * @param player
	 * @return
	 */
	public static void setWantedAmount( final long wantedAmount, final EntityPlayer player )
	{
		Packet_C_EssentiaEmitter packet = newPacket( player, MODE_UPDATE_WANTED );

		// Set the wanted amount
		packet.wantedAmount = wantedAmount;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void wrappedExecute()
	{
		// Get the current screen being displayed to the user
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Ensure it is a GuiEssentiaLevelEmitter
		if( gui instanceof GuiEssentiaLevelEmitter )
		{

			switch ( this.mode )
			{
			case Packet_C_EssentiaEmitter.MODE_FULL_UPDATE:
				// Full update
				( (GuiEssentiaLevelEmitter)gui ).onServerUpdateWantedAmount( this.wantedAmount );
				( (GuiEssentiaLevelEmitter)gui ).onServerUpdateRedstoneMode( this.redstoneMode );
				break;

			case Packet_C_EssentiaEmitter.MODE_UPDATE_WANTED:
				// Update wanted amount
				( (GuiEssentiaLevelEmitter)gui ).onServerUpdateWantedAmount( this.wantedAmount );
				break;

			case Packet_C_EssentiaEmitter.MODE_UPDATE_REDSTONE:
				// Update redstone mode
				( (GuiEssentiaLevelEmitter)gui ).onServerUpdateRedstoneMode( this.redstoneMode );
				break;
			}
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case Packet_C_EssentiaEmitter.MODE_FULL_UPDATE:
			// Read the redstone mode ordinal
			this.redstoneMode = EnumCache.AE_REDSTONE_MODES[stream.readInt()];

			// Read the wanted amount
			this.wantedAmount = stream.readLong();
			break;

		case Packet_C_EssentiaEmitter.MODE_UPDATE_WANTED:
			// Read the wanted amount
			this.wantedAmount = stream.readLong();
			break;

		case Packet_C_EssentiaEmitter.MODE_UPDATE_REDSTONE:
			// Read the redstone mode ordinal
			this.redstoneMode = EnumCache.AE_REDSTONE_MODES[stream.readInt()];
			break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case Packet_C_EssentiaEmitter.MODE_FULL_UPDATE:
			// Write the redstone mode ordinal
			stream.writeInt( this.redstoneMode.ordinal() );

			// Write the wanted amount
			stream.writeLong( this.wantedAmount );
			break;

		case Packet_C_EssentiaEmitter.MODE_UPDATE_WANTED:
			// Write the wanted amount
			stream.writeLong( this.wantedAmount );
			break;

		case Packet_C_EssentiaEmitter.MODE_UPDATE_REDSTONE:
			// Write the redstone mode ordinal
			stream.writeInt( this.redstoneMode.ordinal() );

			break;
		}
	}
}
