package thaumicenergistics.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerEssentiaCell;
import thaumicenergistics.network.AbstractPacket;

public class PacketServerEssentiaCell
	extends AbstractPacket
{
	private static final int MODE_SELECTED_ASPECT = 0;
	private static final int MODE_FORCE_UPDATE = 1;
	protected Aspect currentAspect;

	public PacketServerEssentiaCell()
	{
	}

	public PacketServerEssentiaCell( EntityPlayer player, Aspect currentAspect )
	{
		super( player );

		this.mode = PacketServerEssentiaCell.MODE_SELECTED_ASPECT;

		this.currentAspect = currentAspect;
	}

	public PacketServerEssentiaCell( EntityPlayer player )
	{
		super( player );

		this.mode = PacketServerEssentiaCell.MODE_FORCE_UPDATE;
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
					( (ContainerEssentiaCell)this.player.openContainer ).receiveSelectedAspect( this.currentAspect );
				}
				break;

			case PacketServerEssentiaCell.MODE_FORCE_UPDATE:
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
				this.currentAspect = AbstractPacket.readAspect( stream );
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaCell.MODE_SELECTED_ASPECT:
				AbstractPacket.writeAspect( this.currentAspect, stream );
				break;
		}
	}

}
