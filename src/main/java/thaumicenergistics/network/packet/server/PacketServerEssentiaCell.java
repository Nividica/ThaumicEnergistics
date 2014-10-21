package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.container.ContainerEssentiaCell;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;

public class PacketServerEssentiaCell
	extends AbstractServerPacket
{
	private static final byte MODE_SELECTED_ASPECT = 0;
	private static final byte MODE_FULL_UPDATE = 1;
	private static final byte MODE_SORT_CHANGE = 2;

	protected Aspect selectedAspect;
	private ComparatorMode sortMode;

	public PacketServerEssentiaCell createFullUpdateRequest( final EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCell.MODE_FULL_UPDATE;

		return this;
	}

	public PacketServerEssentiaCell createRequestChangeSortMode( final EntityPlayer player, final ComparatorMode sortMode )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCell.MODE_SORT_CHANGE;

		// Set the sort mode
		this.sortMode = sortMode;

		return this;
	}

	public PacketServerEssentiaCell createUpdateSelectedAspect( final EntityPlayer player, final Aspect selectedAspect )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCell.MODE_SELECTED_ASPECT;

		// Set the selected aspect
		this.selectedAspect = selectedAspect;

		return this;
	}

	@Override
	public void execute()
	{
		// Ensure we have a player
		if( this.player == null )
		{
			return;
		}

		// Ensure they are looking at the cell
		if( !( this.player.openContainer instanceof ContainerEssentiaCell ) )
		{
			return;
		}

		// Get the container
		ContainerEssentiaCell container = ( (ContainerEssentiaCell)this.player.openContainer );

		switch ( this.mode )
		{
			case PacketServerEssentiaCell.MODE_SELECTED_ASPECT:
				// Inform the cell container of the selected aspect
				container.onReceiveSelectedAspect( this.selectedAspect );
				break;

			case PacketServerEssentiaCell.MODE_FULL_UPDATE:
				// Force the cell container to send the aspect list to the client
				container.onClientRequestFullUpdate();
				break;

			case PacketServerEssentiaCell.MODE_SORT_CHANGE:
				// Request a sort mode change
				container.onClientRequestSortModeChange( this.sortMode, this.player );
				break;
		}

	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaCell.MODE_SELECTED_ASPECT:
				// Read in the aspect from the stream
				this.selectedAspect = AbstractPacket.readAspect( stream );
				break;

			case PacketServerEssentiaCell.MODE_SORT_CHANGE:
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
			case PacketServerEssentiaCell.MODE_SELECTED_ASPECT:
				// Write the aspect
				AbstractPacket.writeAspect( this.selectedAspect, stream );
				break;

			case PacketServerEssentiaCell.MODE_SORT_CHANGE:
				// Write the mode ordinal
				stream.writeInt( this.sortMode.ordinal() );
				break;

		}
	}

}
