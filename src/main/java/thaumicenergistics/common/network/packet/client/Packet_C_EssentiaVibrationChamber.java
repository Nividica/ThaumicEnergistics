package thaumicenergistics.common.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import thaumicenergistics.client.gui.GuiEssentiaVibrationChamber;
import thaumicenergistics.common.container.ContainerEssentiaVibrationChamber;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.tiles.TileEssentiaVibrationChamber;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * {@link TileEssentiaVibrationChamber} client-bound packet.
 * 
 * @author Nividica
 * 
 */
public class Packet_C_EssentiaVibrationChamber
	extends ThEClientPacket
{
	private static final byte MODE_UPDATE = 1;

	private float powerPerTick, maxPowerPerTick;
	private int ticksRemaining, totalTicks;

	/**
	 * Creates an update packet.
	 * 
	 * @param player
	 * @param powerPerTick
	 * @param ticksRemaining
	 * @param totalTicks
	 * @return
	 */
	public static void sendUpdate( final EntityPlayer player, final float powerPerTick, final float maxPowerPerTick,
									final int ticksRemaining, final int totalTicks )
	{
		Packet_C_EssentiaVibrationChamber packet = new Packet_C_EssentiaVibrationChamber();

		// Set the player
		packet.player = player;

		// Set the mode
		packet.mode = Packet_C_EssentiaVibrationChamber.MODE_UPDATE;

		// Set values
		packet.powerPerTick = powerPerTick;
		packet.maxPowerPerTick = maxPowerPerTick;
		packet.ticksRemaining = ticksRemaining;
		packet.totalTicks = totalTicks;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
		if( this.mode == Packet_C_EssentiaVibrationChamber.MODE_UPDATE )
		{
			// Read PPT
			this.powerPerTick = stream.readFloat();

			// Read MPPT
			this.maxPowerPerTick = stream.readFloat();

			// Read TR
			this.ticksRemaining = stream.readInt();

			// Read TT
			this.totalTicks = stream.readInt();
		}

	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void wrappedExecute()
	{
		// Get the gui
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Ensure the gui is the EVC
		if( !( gui instanceof GuiEssentiaVibrationChamber ) )
		{
			return;
		}

		// Get the container
		Container container = ( (GuiEssentiaVibrationChamber)gui ).inventorySlots;

		if( this.mode == Packet_C_EssentiaVibrationChamber.MODE_UPDATE )
		{
			// Call update
			( (ContainerEssentiaVibrationChamber)container ).onChamberUpdate( this.powerPerTick, this.maxPowerPerTick, this.ticksRemaining,
				this.totalTicks );
		}
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		if( this.mode == Packet_C_EssentiaVibrationChamber.MODE_UPDATE )
		{
			// Write PPT
			stream.writeFloat( this.powerPerTick );

			// Write MPPT
			stream.writeFloat( this.maxPowerPerTick );

			// Write TR
			stream.writeInt( this.ticksRemaining );

			// Write TT
			stream.writeInt( this.totalTicks );
		}
	}

}
