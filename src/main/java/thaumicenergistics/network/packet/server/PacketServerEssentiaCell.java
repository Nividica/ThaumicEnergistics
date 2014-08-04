package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerEssentiaCell;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;

public class PacketServerEssentiaCell
	extends AbstractServerPacket
{
	private static final int MODE_SELECTED_ASPECT = 0;
	private static final int MODE_FULL_UPDATE = 1;
	
	protected Aspect selectedAspect;

	public PacketServerEssentiaCell createFullUpdateRequest( EntityPlayer player )
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketServerEssentiaCell.MODE_FULL_UPDATE;
		
		return this;
	}

	public PacketServerEssentiaCell createUpdateSelectedAspect( EntityPlayer player, Aspect selectedAspect )
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
		switch ( this.mode )
		{
			case PacketServerEssentiaCell.MODE_SELECTED_ASPECT:
				// If the player is not null, and they have the cell container open
				if ( ( this.player != null ) && ( this.player.openContainer instanceof ContainerEssentiaCell ) )
				{
					// Inform the cell container of the selected aspect
					( (ContainerEssentiaCell)this.player.openContainer ).receiveSelectedAspect( this.selectedAspect );
				}
				break;

			case PacketServerEssentiaCell.MODE_FULL_UPDATE:
				// If the player is not null, and they have the cell container open
				if ( ( this.player != null ) && ( this.player.openContainer instanceof ContainerEssentiaCell ) )
				{
					// Force the cell container to send the aspect list to the client
					( (ContainerEssentiaCell)this.player.openContainer ).forceAspectUpdate();
				}
				break;
		}

	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaCell.MODE_SELECTED_ASPECT:
				// Read in the aspect from the stream
				this.selectedAspect = AbstractPacket.readAspect( stream );
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaCell.MODE_SELECTED_ASPECT:
				AbstractPacket.writeAspect( this.selectedAspect, stream );
				break;
		}
	}

}
