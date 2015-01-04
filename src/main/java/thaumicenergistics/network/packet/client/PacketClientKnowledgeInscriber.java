package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.container.ContainerKnowledgeInscriber.CoreSaveState;
import thaumicenergistics.gui.GuiKnowledgeInscriber;
import thaumicenergistics.network.packet.AbstractClientPacket;

public class PacketClientKnowledgeInscriber
	extends AbstractClientPacket
{

	private static final byte MODE_SENDSAVE = 0;

	private static final CoreSaveState[] SAVE_STATES = CoreSaveState.values();

	private CoreSaveState saveState;

	@Override
	protected void readData( final ByteBuf stream )
	{
		this.saveState = PacketClientKnowledgeInscriber.SAVE_STATES[stream.readInt()];
	}

	@Override
	protected void wrappedExecute()
	{
		// Get the gui
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Ensure it is the knowledge inscriber
		if( gui instanceof GuiKnowledgeInscriber )
		{
			( (GuiKnowledgeInscriber)gui ).onReceiveSaveState( this.saveState );
		}

	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		stream.writeInt( this.saveState.ordinal() );
	}

	public PacketClientKnowledgeInscriber createSendSaveState( final EntityPlayer player, final CoreSaveState saveState )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientKnowledgeInscriber.MODE_SENDSAVE;

		// Set the state
		this.saveState = saveState;

		return this;
	}

}
