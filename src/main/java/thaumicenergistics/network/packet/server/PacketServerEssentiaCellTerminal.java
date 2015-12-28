package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.container.AbstractContainerCellTerminalBase;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;

public class PacketServerEssentiaCellTerminal
	extends AbstractServerPacket
{
	private static final byte MODE_SELECTED_ASPECT = 0;
	private static final byte MODE_FULL_UPDATE = 1;
	private static final byte MODE_SORT_CHANGE = 2;

	private Aspect currentAspect;
	private ComparatorMode sortMode;

	public PacketServerEssentiaCellTerminal createFullUpdateRequest( final EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCellTerminal.MODE_FULL_UPDATE;

		return this;
	}

	public PacketServerEssentiaCellTerminal createRequestChangeSortMode( final EntityPlayer player, final ComparatorMode sortMode )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCellTerminal.MODE_SORT_CHANGE;

		// Set the sort mode
		this.sortMode = sortMode;

		return this;
	}

	public PacketServerEssentiaCellTerminal createUpdateSelectedAspect( final EntityPlayer player, final Aspect currentAspect )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCellTerminal.MODE_SELECTED_ASPECT;

		// Set the current aspect
		this.currentAspect = currentAspect;

		return this;
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
		case PacketServerEssentiaCellTerminal.MODE_SELECTED_ASPECT:
			if( ( this.player != null ) && ( this.player.openContainer instanceof AbstractContainerCellTerminalBase ) )
			{
				( (AbstractContainerCellTerminalBase)this.player.openContainer ).onReceivedSelectedAspect( this.currentAspect );
			}
			break;

		case PacketServerEssentiaCellTerminal.MODE_FULL_UPDATE:
			if( ( this.player != null ) && ( this.player.openContainer instanceof AbstractContainerCellTerminalBase ) )
			{
				( (AbstractContainerCellTerminalBase)this.player.openContainer ).onClientRequestFullUpdate();
			}
			break;

		case PacketServerEssentiaCellTerminal.MODE_SORT_CHANGE:
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

		case PacketServerEssentiaCellTerminal.MODE_SELECTED_ASPECT:
			this.currentAspect = AbstractPacket.readAspect( stream );
			break;

		case PacketServerEssentiaCellTerminal.MODE_SORT_CHANGE:
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

		case PacketServerEssentiaCellTerminal.MODE_SELECTED_ASPECT:
			AbstractPacket.writeAspect( this.currentAspect, stream );
			break;

		case PacketServerEssentiaCellTerminal.MODE_SORT_CHANGE:
			// Write the mode ordinal
			stream.writeInt( this.sortMode.ordinal() );
			break;
		}
	}
}
