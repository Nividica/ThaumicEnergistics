package thaumicenergistics.network.packet.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import io.netty.buffer.ByteBuf;
import thaumicenergistics.gui.GuiPriority;
import thaumicenergistics.network.packet.AbstractClientPacket;

public class PacketClientPriority
	extends AbstractClientPacket
{
	private static final byte MODE_SEND = 0;
	
	private int priority;
	
	public PacketClientPriority createSendPriority( int priority, EntityPlayer player )
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketClientPriority.MODE_SEND;
		
		// Set the priority
		this.priority = priority;
		
		return this;
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

		if( this.mode == PacketClientPriority.MODE_SEND )
		{
			// Set the priority
			( (GuiPriority)gui ).onServerSendPriority( this.priority );
		}
	}

	@Override
	protected void readData( ByteBuf stream )
	{
		if( this.mode == PacketClientPriority.MODE_SEND )
		{
			// Read the priority
			this.priority = stream.readInt();
		}
	}

	@Override
	protected void writeData( ByteBuf stream )
	{
		if( this.mode == PacketClientPriority.MODE_SEND )
		{
			// Write the priority
			stream.writeInt( this.priority );
		}
	}

}
