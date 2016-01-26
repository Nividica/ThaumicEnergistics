package thaumicenergistics.common.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.client.gui.GuiPriority;
import thaumicenergistics.common.network.NetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Priority GUI client-bound packet.
 * 
 * @author Nividica
 * 
 */
public class Packet_C_Priority
	extends ThEClientPacket
{
	private static final byte MODE_SEND = 0;

	private int priority;

	public static void sendPriority( final int priority, final EntityPlayer player )
	{
		Packet_C_Priority packet = new Packet_C_Priority();

		// Set the player
		packet.player = player;

		// Set the mode
		packet.mode = Packet_C_Priority.MODE_SEND;

		// Set the priority
		packet.priority = priority;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
		if( this.mode == Packet_C_Priority.MODE_SEND )
		{
			// Read the priority
			this.priority = stream.readInt();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void wrappedExecute()
	{
		// Ensure we have a player
		if( this.player == null )
		{
			return;
		}

		// Get the gui
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Ensure they are looking at the priority gui
		if( !( gui instanceof GuiPriority ) )
		{
			return;
		}

		if( this.mode == Packet_C_Priority.MODE_SEND )
		{
			// Set the priority
			( (GuiPriority)gui ).onServerSendPriority( this.priority );
		}
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		if( this.mode == Packet_C_Priority.MODE_SEND )
		{
			// Write the priority
			stream.writeInt( this.priority );
		}
	}

}
