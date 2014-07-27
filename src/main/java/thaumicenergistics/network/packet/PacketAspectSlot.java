package thaumicenergistics.network.packet;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.network.AbstractPacket;
import thaumicenergistics.network.IAspectSlotGui;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.parts.AEPartBase;

public class PacketAspectSlot extends AbstractPacket
{

	private int index;
	private Aspect aspect;
	private IAspectSlotPart part;
	private List<Aspect> filterAspects;

	public PacketAspectSlot()
	{
	}

	public PacketAspectSlot(IAspectSlotPart part, int index, Aspect aspect, EntityPlayer player)
	{
		super( player );
		this.mode = 0;
		this.index = index;
		this.part = part;
		this.aspect = aspect;
	}

	public PacketAspectSlot(List<Aspect> filterAspects)
	{
		this.mode = 1;
		this.useCompression = true;
		this.filterAspects = filterAspects;
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
			case 0:
				this.part.setAspect( this.index, this.aspect, this.player );
				break;

			case 1:
				Gui gui = Minecraft.getMinecraft().currentScreen;

				if ( gui instanceof IAspectSlotGui )
				{
					IAspectSlotGui partGui = (IAspectSlotGui) gui;

					partGui.updateAspects( this.filterAspects );
				}

				break;
		}
	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case 0:
				this.part = ( (IAspectSlotPart) AbstractPacket.readPart( stream ) );

				this.index = stream.readInt();

				this.aspect = AbstractPacket.readAspect( stream );
				break;

			case 1:
				this.filterAspects = new ArrayList();

				int count = stream.readInt();

				for( int i = 0; i < count; i++ )
				{
					this.filterAspects.add( AbstractPacket.readAspect( stream ) );
				}

				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case 0:
				AbstractPacket.writePart( (AEPartBase) this.part, stream );

				stream.writeInt( this.index );

				AbstractPacket.writeAspect( this.aspect, stream );
				break;

			case 1:
				stream.writeInt( this.filterAspects.size() );

				for( int i = 0; i < this.filterAspects.size(); i++ )
				{
					AbstractPacket.writeAspect( this.filterAspects.get( i ), stream );
				}

				break;
		}
	}

}
