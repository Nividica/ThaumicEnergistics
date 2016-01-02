package thaumicenergistics.common.network.packet.client;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.client.gui.GuiEssentiaCellTerminal;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.registries.EnumCache;
import thaumicenergistics.common.storage.AspectStack;
import thaumicenergistics.common.storage.AspectStackComparator.AspectStackComparatorMode;
import appeng.api.config.ViewItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Packet_C_EssentiaCellTerminal
	extends ThEClientPacket
{
	private static final byte MODE_FULL_LIST = 0;
	private static final byte MODE_SELECTED_ASPECT = 1;
	private static final byte MODE_VIEWING_CHANGED = 2;
	private static final byte MODE_LIST_CHANGED = 3;

	private Collection<IAspectStack> aspectStackList;
	private Aspect selectedAspect;
	private AspectStackComparatorMode sortMode;
	private IAspectStack change;
	private ViewItems viewMode;

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
	public static void sendFullList( final EntityPlayer player, final Collection<IAspectStack> list )
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
	 * Sends the viewing modes.
	 * 
	 * @param player
	 * @param sortMode
	 */
	public static void sendViewingModes( final EntityPlayer player, final AspectStackComparatorMode sortMode, final ViewItems viewMode )
	{
		Packet_C_EssentiaCellTerminal packet = newPacket( player, MODE_VIEWING_CHANGED );

		// Set the sort mode
		packet.sortMode = sortMode;

		// Set the view mode
		packet.viewMode = viewMode;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Called when an aspect amount changes.
	 * 
	 * @param player
	 * @param change
	 */
	public static void setAspectAmount( final EntityPlayer player, final IAspectStack change )
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

			case Packet_C_EssentiaCellTerminal.MODE_VIEWING_CHANGED:
				// Update the modes
				( (GuiEssentiaCellTerminal)gui ).onViewingModesChanged( this.sortMode, this.viewMode );
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
			this.aspectStackList = new ArrayList<IAspectStack>();

			// Read each stack
			while( stream.readableBytes() > 0 )
			{
				this.aspectStackList.add( AspectStack.loadAspectStackFromStream( stream ) );
			}
			break;

		case Packet_C_EssentiaCellTerminal.MODE_SELECTED_ASPECT:
			// Read aspect
			this.selectedAspect = ThEBasePacket.readAspect( stream );
			break;

		case Packet_C_EssentiaCellTerminal.MODE_VIEWING_CHANGED:
			// Read the mode ordinals
			this.sortMode = AspectStackComparatorMode.VALUES[stream.readInt()];
			this.viewMode = EnumCache.AE_VIEW_ITEMS[stream.readInt()];
			break;

		case Packet_C_EssentiaCellTerminal.MODE_LIST_CHANGED:
			// Read the stack
			this.change = AspectStack.loadAspectStackFromStream( stream );
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{

		switch ( this.mode )
		{
		case Packet_C_EssentiaCellTerminal.MODE_FULL_LIST:
			// Write each stack
			for( IAspectStack stack : this.aspectStackList )
			{
				stack.writeToStream( stream );
			}
			break;

		case Packet_C_EssentiaCellTerminal.MODE_SELECTED_ASPECT:
			// Write aspect
			ThEBasePacket.writeAspect( this.selectedAspect, stream );
			break;

		case Packet_C_EssentiaCellTerminal.MODE_VIEWING_CHANGED:
			// Write the mode ordinals
			stream.writeInt( this.sortMode.ordinal() );
			stream.writeInt( this.viewMode.ordinal() );
			break;

		case Packet_C_EssentiaCellTerminal.MODE_LIST_CHANGED:
			// Write the stack
			this.change.writeToStream( stream );
		}
	}
}
