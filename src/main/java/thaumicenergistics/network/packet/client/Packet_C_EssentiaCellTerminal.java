package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.gui.GuiEssentiaCellTerminal;
import thaumicenergistics.network.NetworkHandler;
import thaumicenergistics.network.packet.ThEBasePacket;
import thaumicenergistics.network.packet.ThEClientPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Packet_C_EssentiaCellTerminal
	extends ThEClientPacket
{
	private static final byte MODE_FULL_LIST = 0;
	private static final byte MODE_SELECTED_ASPECT = 1;
	private static final byte MODE_SORT_MODE_CHANGED = 2;
	private static final byte MODE_LIST_CHANGED = 3;

	private List<AspectStack> aspectStackList;
	private Aspect selectedAspect;
	private ComparatorMode sortMode;
	private AspectStack change;

	/**
	 * Creates the packet
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_C_EssentiaCellTerminal newPacket( final EntityPlayer player, final byte mode )
	{
		// Create the packet
		Packet_C_EssentiaCellTerminal packet = new Packet_C_EssentiaCellTerminal();

		// Set the player & mode
		packet.player = player;
		packet.mode = mode;

		return packet;
	}

	/**
	 * Called to send the full list of aspects.
	 * 
	 * @param player
	 * @param list
	 */
	public static void sendFullList( final EntityPlayer player, final List<AspectStack> list )
	{
		Packet_C_EssentiaCellTerminal packet = newPacket( player, MODE_FULL_LIST );
		// Set the player

		// Mark to use compression
		packet.useCompression = true;

		// Set the list
		packet.aspectStackList = list;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Called when an aspect amount changes.
	 * 
	 * @param player
	 * @param change
	 */
	public static void setAspectAmount( final EntityPlayer player, final AspectStack change )
	{
		Packet_C_EssentiaCellTerminal packet = newPacket( player, MODE_LIST_CHANGED );

		// Set the change
		packet.change = change;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Called when the selected aspect changes.
	 * 
	 * @param player
	 * @param selectedAspect
	 */
	public static void setSelectedAspect( final EntityPlayer player, final Aspect selectedAspect )
	{
		Packet_C_EssentiaCellTerminal packet = newPacket( player, MODE_SELECTED_ASPECT );

		// Set the selected aspect
		packet.selectedAspect = selectedAspect;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Sends the sorting mode.
	 * 
	 * @param player
	 * @param sortMode
	 */
	public static void setSortMode( final EntityPlayer player, final ComparatorMode sortMode )
	{
		Packet_C_EssentiaCellTerminal packet = newPacket( player, MODE_SORT_MODE_CHANGED );

		// Set the sort mode
		packet.sortMode = sortMode;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void wrappedExecute()
	{
		Gui gui = Minecraft.getMinecraft().currentScreen;

		if( gui instanceof GuiEssentiaCellTerminal )
		{
			switch ( this.mode )
			{
			case Packet_C_EssentiaCellTerminal.MODE_FULL_LIST:
				( (GuiEssentiaCellTerminal)gui ).onReceiveAspectList( this.aspectStackList );
				break;

			case Packet_C_EssentiaCellTerminal.MODE_SELECTED_ASPECT:
				( (GuiEssentiaCellTerminal)gui ).onReceiveSelectedAspect( this.selectedAspect );
				break;

			case Packet_C_EssentiaCellTerminal.MODE_SORT_MODE_CHANGED:
				// Update the sorting mode
				( (GuiEssentiaCellTerminal)gui ).onSortModeChanged( this.sortMode );
				break;

			case Packet_C_EssentiaCellTerminal.MODE_LIST_CHANGED:
				// Update the list
				( (GuiEssentiaCellTerminal)gui ).onReceiveAspectListChange( this.change );
				break;
			}
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{

		switch ( this.mode )
		{
		case Packet_C_EssentiaCellTerminal.MODE_FULL_LIST:
			this.aspectStackList = new ArrayList<AspectStack>();

			while( stream.readableBytes() > 0 )
			{
				this.aspectStackList.add( new AspectStack( ThEBasePacket.readAspect( stream ), stream.readLong() ) );
			}
			break;

		case Packet_C_EssentiaCellTerminal.MODE_SELECTED_ASPECT:
			this.selectedAspect = ThEBasePacket.readAspect( stream );
			break;

		case Packet_C_EssentiaCellTerminal.MODE_SORT_MODE_CHANGED:
			// Read the mode ordinal
			this.sortMode = ComparatorMode.VALUES[stream.readInt()];
			break;

		case Packet_C_EssentiaCellTerminal.MODE_LIST_CHANGED:
			// Read the stack
			this.change = new AspectStack( ThEBasePacket.readAspect( stream ), stream.readLong() );
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{

		switch ( this.mode )
		{
		case Packet_C_EssentiaCellTerminal.MODE_FULL_LIST:
			for( AspectStack stack : this.aspectStackList )
			{
				ThEBasePacket.writeAspect( stack.aspect, stream );

				stream.writeLong( stack.stackSize );
			}
			break;

		case Packet_C_EssentiaCellTerminal.MODE_SELECTED_ASPECT:
			ThEBasePacket.writeAspect( this.selectedAspect, stream );
			break;

		case Packet_C_EssentiaCellTerminal.MODE_SORT_MODE_CHANGED:
			// Write the mode ordinal
			stream.writeInt( this.sortMode.ordinal() );
			break;

		case Packet_C_EssentiaCellTerminal.MODE_LIST_CHANGED:
			// Write the aspect
			ThEBasePacket.writeAspect( this.change.aspect, stream );

			// Write the amount
			stream.writeLong( this.change.stackSize );
		}
	}
}
