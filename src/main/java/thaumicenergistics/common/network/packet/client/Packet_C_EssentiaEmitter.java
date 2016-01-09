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
	/**
	 * Packet modes.
	 */
	private static final byte MODE_UPDATE_WANTED = 1,
					MODE_UPDATE_REDSTONE = 2;

	private RedstoneMode redstoneMode;
	private long threshold;

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
	 * @param threshold
	 * @param player
	 * @return
	 */
	public static void sendThresholdValue( final long threshold, final EntityPlayer player )
	{
		Packet_C_EssentiaEmitter packet = newPacket( player, MODE_UPDATE_WANTED );

		// Set the wanted amount
		packet.threshold = threshold;

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
			case Packet_C_EssentiaEmitter.MODE_UPDATE_WANTED:
				// Update wanted amount
				( (GuiEssentiaLevelEmitter)gui ).onReceiveThresholdValue( this.threshold );
				break;

			case Packet_C_EssentiaEmitter.MODE_UPDATE_REDSTONE:
				// Update redstone mode
				( (GuiEssentiaLevelEmitter)gui ).onReceiveRedstoneMode( this.redstoneMode );
				break;
			}
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case Packet_C_EssentiaEmitter.MODE_UPDATE_WANTED:
			// Read the wanted amount
			this.threshold = stream.readLong();
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
		case Packet_C_EssentiaEmitter.MODE_UPDATE_WANTED:
			// Write the wanted amount
			stream.writeLong( this.threshold );
			break;

		case Packet_C_EssentiaEmitter.MODE_UPDATE_REDSTONE:
			// Write the redstone mode ordinal
			stream.writeInt( this.redstoneMode.ordinal() );

			break;
		}
	}
}
