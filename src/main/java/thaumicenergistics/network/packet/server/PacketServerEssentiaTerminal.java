package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.container.ContainerEssentiaTerminal;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import thaumicenergistics.parts.AEPartEssentiaTerminal;

public class PacketServerEssentiaTerminal
	extends AbstractServerPacket
{
	private static final byte MODE_SELECTED_ASPECT = 0;
	private static final byte MODE_FULL_UPDATE = 1;
	private static final byte MODE_SORT_CHANGE = 2;
	
	private static final ComparatorMode[] SORT_MODES = ComparatorMode.values();

	private Aspect currentAspect;
	private AEPartEssentiaTerminal terminal;
	private ComparatorMode sortMode;

	public PacketServerEssentiaTerminal createFullUpdateRequest( EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaTerminal.MODE_FULL_UPDATE;

		return this;
	}

	public PacketServerEssentiaTerminal createUpdateSelectedAspect( EntityPlayer player, Aspect currentAspect )
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketServerEssentiaTerminal.MODE_SELECTED_ASPECT;

		// Set the current aspect
		this.currentAspect = currentAspect;
		
		return this;
	}

	public PacketServerEssentiaTerminal createRequestChangeSortMode( EntityPlayer player, AEPartEssentiaTerminal terminal, ComparatorMode sortMode )
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketServerEssentiaTerminal.MODE_SORT_CHANGE;

		// Set the terminal
		this.terminal = terminal;
		
		// Set the sort mode
		this.sortMode = sortMode;
		
		return this;
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaTerminal.MODE_SELECTED_ASPECT:
				if( ( this.player != null ) && ( this.player.openContainer instanceof ContainerEssentiaTerminal ) )
				{
					ContainerEssentiaTerminal container = (ContainerEssentiaTerminal)this.player.openContainer;

					container.onReceiveSelectedAspect( this.currentAspect );
				}
				break;

			case PacketServerEssentiaTerminal.MODE_FULL_UPDATE:
				if( ( this.player != null ) && ( this.player.openContainer instanceof ContainerEssentiaTerminal ) )
				{
					ContainerEssentiaTerminal container = (ContainerEssentiaTerminal)this.player.openContainer;

					container.onClientRequestFullUpdate();
				}
				break;
				
			case PacketServerEssentiaTerminal.MODE_SORT_CHANGE:
				// Inform the part about the request
				this.terminal.onClientRequestSortingModeChange( this.sortMode );
		}

	}

	@Override
	public void readData( ByteBuf stream )
	{

		switch ( this.mode )
		{

			case PacketServerEssentiaTerminal.MODE_SELECTED_ASPECT:
				this.currentAspect = AbstractPacket.readAspect( stream );
				break;
				
			case PacketServerEssentiaTerminal.MODE_SORT_CHANGE:
				// Read the part
				this.terminal = (AEPartEssentiaTerminal)AbstractPacket.readPart( stream );
				
				// Read the mode ordinal
				this.sortMode = SORT_MODES[stream.readInt()];
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{

		switch ( this.mode )
		{

			case PacketServerEssentiaTerminal.MODE_SELECTED_ASPECT:
				AbstractPacket.writeAspect( this.currentAspect, stream );
				break;
				
			case PacketServerEssentiaTerminal.MODE_SORT_CHANGE:
				// Write the part
				AbstractPacket.writePart( this.terminal, stream );
				
				// Write the mode ordinal
				stream.writeInt( this.sortMode.ordinal() );
				break;
		}
	}
}
