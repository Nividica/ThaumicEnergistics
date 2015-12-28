package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import thaumicenergistics.parts.AbstractAEPartBase;

public class PacketServerAspectSlot
	extends AbstractServerPacket
{
	private static final byte MODE_SET_ASPECT = 0;

	private int index;

	private Aspect aspect;

	private IAspectSlotPart part;

	public PacketServerAspectSlot createUpdatePartAspect( final IAspectSlotPart part, final int index, final Aspect aspect, final EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerAspectSlot.MODE_SET_ASPECT;

		// Set the index
		this.index = index;

		// Set the part
		this.part = part;

		// Set the aspect
		this.aspect = aspect;

		return this;
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
		case PacketServerAspectSlot.MODE_SET_ASPECT:
			// Inform the part of the aspect change
			this.part.setAspect( this.index, this.aspect, this.player );
			break;
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case PacketServerAspectSlot.MODE_SET_ASPECT:
			// Read the part
			this.part = ( (IAspectSlotPart)AbstractPacket.readPart( stream ) );

			// Read the index
			this.index = stream.readInt();

			// Read the aspect
			this.aspect = AbstractPacket.readAspect( stream );
			break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case PacketServerAspectSlot.MODE_SET_ASPECT:
			// Write the part
			AbstractPacket.writePart( (AbstractAEPartBase)this.part, stream );

			// Write the index
			stream.writeInt( this.index );

			// Write the aspect
			AbstractPacket.writeAspect( this.aspect, stream );
			break;
		}
	}

}
