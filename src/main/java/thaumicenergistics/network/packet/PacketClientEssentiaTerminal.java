package thaumicenergistics.network.packet;

import java.util.ArrayList;
import java.util.List;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.ContainerEssentiaTerminal;
import thaumicenergistics.gui.GuiEssentiaTerminal;
import thaumicenergistics.network.AbstractPacket;

public class PacketClientEssentiaTerminal
	extends AbstractPacket
{
	public static final int MODE_UPDATE_LIST = 0;
	public static final int MODE_SET_CURRENT = 1;
	
	private List<AspectStack> aspectStackList;
	private Aspect currentAspect;

	public PacketClientEssentiaTerminal()
	{
	}

	public PacketClientEssentiaTerminal(EntityPlayer player, List<AspectStack> list)
	{
		super( player);
		
		this.mode = PacketClientEssentiaTerminal.MODE_UPDATE_LIST;
		
		this.aspectStackList = list;
	}

	public PacketClientEssentiaTerminal(EntityPlayer player, Aspect currentAspect)
	{
		super( player );
		
		this.mode = PacketClientEssentiaTerminal.MODE_SET_CURRENT;
		
		this.currentAspect = currentAspect;
	}

	@Override
	public void execute()
	{	
		switch ( this.mode )
		{
			case PacketClientEssentiaTerminal.MODE_UPDATE_LIST:
				if( this.player != null )
				{
					Gui gui = Minecraft.getMinecraft().currentScreen;
					
					if( gui instanceof GuiEssentiaTerminal )
					{
						ContainerEssentiaTerminal container = (ContainerEssentiaTerminal)( (GuiEssentiaTerminal) gui ).inventorySlots;
						
						container.updateAspectList( this.aspectStackList );
					}
				}
				break;
				
			case PacketClientEssentiaTerminal.MODE_SET_CURRENT:
				if( ( this.player != null ) && ( Minecraft.getMinecraft().currentScreen instanceof GuiEssentiaTerminal ) )
				{
					GuiEssentiaTerminal gui = (GuiEssentiaTerminal) Minecraft.getMinecraft().currentScreen;
					
					( (ContainerEssentiaTerminal)gui.getContainer() ).receiveSelectedAspect( this.currentAspect );
				}
				
				break;
		}

	}

	@Override
	public void readData( ByteBuf stream )
	{
		
		switch ( this.mode )
		{
			case PacketClientEssentiaTerminal.MODE_UPDATE_LIST:
				this.aspectStackList = new ArrayList<AspectStack>();

				while ( stream.readableBytes() > 0 )
				{
					this.aspectStackList.add( new AspectStack( AbstractPacket.readAspect( stream ), stream.readLong() ) );
				}
				break;
				
			case PacketClientEssentiaTerminal.MODE_SET_CURRENT:
				this.currentAspect = AbstractPacket.readAspect( stream );
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		
		switch ( this.mode )
		{
			case PacketClientEssentiaTerminal.MODE_UPDATE_LIST:
				for( AspectStack stack : this.aspectStackList )
				{
					AbstractPacket.writeAspect( stack.aspect, stream );

					stream.writeLong( stack.amount );
				}
				break;
				
			case PacketClientEssentiaTerminal.MODE_SET_CURRENT:
				AbstractPacket.writeAspect( this.currentAspect, stream );
				break;
		}
	}
}
