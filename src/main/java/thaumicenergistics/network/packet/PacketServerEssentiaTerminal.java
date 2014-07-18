package thaumicenergistics.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerEssentiaTerminal;
import thaumicenergistics.network.AbstractPacket;

public class PacketServerEssentiaTerminal
	extends AbstractPacket
{
	private final static int MODE_SET_CURRENT = 0;
	private final static int MODE_FORCE_UPDATE = 1;
	
	private Aspect currentAspect;

	public PacketServerEssentiaTerminal()
	{
	}

	public PacketServerEssentiaTerminal(EntityPlayer player )
	{
		super( player );

		this.mode = PacketServerEssentiaTerminal.MODE_FORCE_UPDATE;
	}

	public PacketServerEssentiaTerminal(EntityPlayer player, Aspect currentAspect )
	{
		super( player );

		this.mode = PacketServerEssentiaTerminal.MODE_SET_CURRENT;
		
		this.currentAspect = currentAspect;
	}

	@Override
	public void execute()
	{	
		switch ( this.mode )
		{
			case PacketServerEssentiaTerminal.MODE_SET_CURRENT:
				if ( ( this.player != null ) && ( this.player.openContainer instanceof ContainerEssentiaTerminal ) )
				{
					ContainerEssentiaTerminal container = (ContainerEssentiaTerminal)this.player.openContainer;
					
					container.receiveSelectedAspect( this.currentAspect );
				}
				break;

			case PacketServerEssentiaTerminal.MODE_FORCE_UPDATE:
				if ( ( this.player != null ) && ( this.player.openContainer instanceof ContainerEssentiaTerminal ) )
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
				
			case PacketServerEssentiaTerminal.MODE_SET_CURRENT:
				this.currentAspect = AbstractPacket.readAspect( stream );
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		
		switch ( this.mode )
		{
				
			case PacketServerEssentiaTerminal.MODE_SET_CURRENT:
				AbstractPacket.writeAspect( this.currentAspect, stream );
				break;
		}
	}
}
