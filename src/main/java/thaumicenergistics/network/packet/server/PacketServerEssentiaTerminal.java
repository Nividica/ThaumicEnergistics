package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerEssentiaTerminal;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;

public class PacketServerEssentiaTerminal
	extends AbstractServerPacket
{
	private static final int MODE_SELECTED_ASPECT = 0;
	private static final int MODE_FULL_UPDATE = 1;

	private Aspect currentAspect;

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

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
			case PacketServerEssentiaTerminal.MODE_SELECTED_ASPECT:
				if( ( this.player != null ) && ( this.player.openContainer instanceof ContainerEssentiaTerminal ) )
				{
					ContainerEssentiaTerminal container = (ContainerEssentiaTerminal)this.player.openContainer;

					container.receiveSelectedAspect( this.currentAspect );
				}
				break;

			case PacketServerEssentiaTerminal.MODE_FULL_UPDATE:
				if( ( this.player != null ) && ( this.player.openContainer instanceof ContainerEssentiaTerminal ) )
				{
					ContainerEssentiaTerminal container = (ContainerEssentiaTerminal)this.player.openContainer;

					container.forceAspectUpdate();
				}
				break;
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
		}
	}
}
