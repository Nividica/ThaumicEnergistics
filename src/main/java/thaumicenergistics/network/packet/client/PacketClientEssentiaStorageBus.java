package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.gui.GuiEssentiaStorageBus;
import thaumicenergistics.network.packet.AbstractClientPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketClientEssentiaStorageBus
	extends AbstractClientPacket
{
	private static final byte MODE_SET_VOID = 0;

	private boolean isVoidAllowed;

	@Override
	protected void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientEssentiaStorageBus.MODE_SET_VOID:
				// Read void mode
				this.isVoidAllowed = stream.readBoolean();
				break;
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void wrappedExecute()
	{
		// Get the gui
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Ensure the gui is a GuiEssentiaStorageBus
		if( !( gui instanceof GuiEssentiaStorageBus ) )
		{
			return;
		}

		switch ( this.mode )
		{
			case PacketClientEssentiaStorageBus.MODE_SET_VOID:
				// Set void mode
				( (GuiEssentiaStorageBus)gui ).onServerSentVoidMode( this.isVoidAllowed );
				break;
		}

	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientEssentiaStorageBus.MODE_SET_VOID:
				// Write void mode
				stream.writeBoolean( this.isVoidAllowed );
				break;
		}
	}

	public PacketClientEssentiaStorageBus createSetIsVoidAllowed( final EntityPlayer player, final boolean isVoidAllowed )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaStorageBus.MODE_SET_VOID;

		// Set void
		this.isVoidAllowed = isVoidAllowed;

		return this;
	}

}
