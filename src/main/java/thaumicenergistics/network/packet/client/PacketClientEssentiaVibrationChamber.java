package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import thaumicenergistics.container.ContainerEssentiaVibrationChamber;
import thaumicenergistics.gui.GuiEssentiaVibrationChamber;
import thaumicenergistics.network.packet.AbstractClientPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketClientEssentiaVibrationChamber
	extends AbstractClientPacket
{
	private static final byte MODE_UPDATE = 1;

	private float powerPerTick, maxPowerPerTick;
	private int ticksRemaining, totalTicks;

	@Override
	protected void readData( final ByteBuf stream )
	{
		if( this.mode == PacketClientEssentiaVibrationChamber.MODE_UPDATE )
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

		if( this.mode == PacketClientEssentiaVibrationChamber.MODE_UPDATE )
		{
			// Call update
			( (ContainerEssentiaVibrationChamber)container ).onChamberUpdate( this.powerPerTick, this.maxPowerPerTick, this.ticksRemaining,
				this.totalTicks );
		}
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		if( this.mode == PacketClientEssentiaVibrationChamber.MODE_UPDATE )
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

	/**
	 * Creates an update packet.
	 * 
	 * @param player
	 * @param powerPerTick
	 * @param ticksRemaining
	 * @param totalTicks
	 * @return
	 */
	public PacketClientEssentiaVibrationChamber createUpdatePacket( final EntityPlayer player, final float powerPerTick, final float maxPowerPerTick,
																	final int ticksRemaining, final int totalTicks )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaVibrationChamber.MODE_UPDATE;

		// Set values
		this.powerPerTick = powerPerTick;
		this.maxPowerPerTick = maxPowerPerTick;
		this.ticksRemaining = ticksRemaining;
		this.totalTicks = totalTicks;

		return this;
	}

}
