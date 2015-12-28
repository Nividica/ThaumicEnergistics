package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.container.AbstractContainerCellTerminalBase;
import thaumicenergistics.network.NetworkHandler;
import thaumicenergistics.network.packet.ThEBasePacket;
import thaumicenergistics.network.packet.ThEServerPacket;

public class Packet_S_EssentiaCellTerminal
	extends ThEServerPacket
{
	private static final byte MODE_SELECTED_ASPECT = 0;
	private static final byte MODE_FULL_UPDATE = 1;
	private static final byte MODE_SORT_CHANGE = 2;

	private Aspect currentAspect;
	private ComparatorMode sortMode;

	/**
	 * Creates the packet
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_S_EssentiaCellTerminal newPacket( final EntityPlayer player, final byte mode )
	{
		// Create the packet
		Packet_S_EssentiaCellTerminal packet = new Packet_S_EssentiaCellTerminal();

		// Set the player & mode
		packet.player = player;
		packet.mode = mode;

		return packet;
	}

	public static void sendFullUpdateRequest( final EntityPlayer player )
	{
		Packet_S_EssentiaCellTerminal packet = newPacket( player, MODE_FULL_UPDATE );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendSelectedAspect( final EntityPlayer player, final Aspect currentAspect )
	{
		Packet_S_EssentiaCellTerminal packet = newPacket( player, MODE_SELECTED_ASPECT );

		// Set the current aspect
		packet.currentAspect = currentAspect;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendSortMode( final EntityPlayer player, final ComparatorMode sortMode )
	{
		Packet_S_EssentiaCellTerminal packet = newPacket( player, MODE_SORT_CHANGE );

		// Set the sort mode
		packet.sortMode = sortMode;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
		case Packet_S_EssentiaCellTerminal.MODE_SELECTED_ASPECT:
			if( ( this.player != null ) && ( this.player.openContainer instanceof AbstractContainerCellTerminalBase ) )
			{
				( (AbstractContainerCellTerminalBase)this.player.openContainer ).onReceivedSelectedAspect( this.currentAspect );
			}
			break;

		case Packet_S_EssentiaCellTerminal.MODE_FULL_UPDATE:
			if( ( this.player != null ) && ( this.player.openContainer instanceof AbstractContainerCellTerminalBase ) )
			{
				( (AbstractContainerCellTerminalBase)this.player.openContainer ).onClientRequestFullUpdate();
			}
			break;

		case Packet_S_EssentiaCellTerminal.MODE_SORT_CHANGE:
			if( ( this.player != null ) && ( this.player.openContainer instanceof AbstractContainerCellTerminalBase ) )
			{
				( (AbstractContainerCellTerminalBase)this.player.openContainer ).onClientRequestSortModeChange( this.sortMode, this.player );
			}
		}

	}

	@Override
	public void readData( final ByteBuf stream )
	{

		switch ( this.mode )
		{

		case Packet_S_EssentiaCellTerminal.MODE_SELECTED_ASPECT:
			this.currentAspect = ThEBasePacket.readAspect( stream );
			break;

		case Packet_S_EssentiaCellTerminal.MODE_SORT_CHANGE:
			// Read the mode ordinal
			this.sortMode = ComparatorMode.VALUES[stream.readInt()];
			break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{

		switch ( this.mode )
		{

		case Packet_S_EssentiaCellTerminal.MODE_SELECTED_ASPECT:
			ThEBasePacket.writeAspect( this.currentAspect, stream );
			break;

		case Packet_S_EssentiaCellTerminal.MODE_SORT_CHANGE:
			// Write the mode ordinal
			stream.writeInt( this.sortMode.ordinal() );
			break;
		}
	}
}
