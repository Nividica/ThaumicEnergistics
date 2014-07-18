package thaumicenergistics.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.network.AbstractPacket;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;

public class PacketEssentiaStorageBus extends AbstractPacket
{
	AEPartEssentiaStorageBus part;

	public PacketEssentiaStorageBus()
	{
	}

	public PacketEssentiaStorageBus(EntityPlayer player, AEPartEssentiaStorageBus part)
	{
		super( player );

		this.mode = 0;

		this.part = part;
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
			case 0:
				this.part.sendInformation( this.player );
				break;
		}
	}

	@Override
	public void readData( ByteBuf stream )
	{
		this.part = ( (AEPartEssentiaStorageBus) AbstractPacket.readPart( stream ) );
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		AbstractPacket.writePart( this.part, stream );
	}

}
