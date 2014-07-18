package thaumicenergistics.network.packet;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.ContainerEssentiaCell;
import thaumicenergistics.gui.GuiEssentiaCell;
import thaumicenergistics.network.AbstractPacket;

public class PacketClientEssentiaCell
	extends AbstractPacket
{
	private static final int MODE_UPDATE_LIST = 0;
	private static final int MODE_SELECTED_ASPECT = 1;
	
	protected List<AspectStack> aspectStackList;
	protected Aspect currentAspect;

	public PacketClientEssentiaCell()
	{
	}

	public PacketClientEssentiaCell( EntityPlayer player, List<AspectStack> list )
	{
		super( player );

		this.mode = PacketClientEssentiaCell.MODE_UPDATE_LIST;

		this.aspectStackList = list;
	}

	public PacketClientEssentiaCell( EntityPlayer player, Aspect currentAspect )
	{
		super( player );

		this.mode = PacketClientEssentiaCell.MODE_SELECTED_ASPECT;

		this.currentAspect = currentAspect;
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
			case PacketClientEssentiaCell.MODE_UPDATE_LIST:
				// Ensure we have a player
				if ( ( this.player != null ) )
				{
					// Get the current screen being displayed to the user
					Gui gui = Minecraft.getMinecraft().currentScreen;
					
					// Is that screen the gui for the cell?
					if ( gui instanceof GuiEssentiaCell )
					{	
						// Get the gui's container
						ContainerEssentiaCell container = (ContainerEssentiaCell)( ( (GuiEssentiaCell)gui ).inventorySlots );

						// Update the aspect list
						container.updateAspectList( this.aspectStackList );
					}
				}
				break;

			case PacketClientEssentiaCell.MODE_SELECTED_ASPECT:
				// Ensure we have a player
				if ( ( this.player != null ) )
				{
					// Get the current screen being displayed to the user
					Gui gui = Minecraft.getMinecraft().currentScreen;
					
					// Is that screen the gui for the cell?
					if ( gui instanceof GuiEssentiaCell )
					{	
						// Get the gui's container
						ContainerEssentiaCell container = (ContainerEssentiaCell)( ( (GuiEssentiaCell)gui ).inventorySlots );

						// Set the selected aspect
						container.receiveSelectedAspect( this.currentAspect );
					}
				}
				break;
		}

	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientEssentiaCell.MODE_UPDATE_LIST:
				// Create a new list
				this.aspectStackList = new ArrayList<AspectStack>();

				// Read in the aspect list from the stream
				while ( stream.readableBytes() > 0 )
				{
					this.aspectStackList.add( new AspectStack( AbstractPacket.readAspect( stream ), stream.readLong() ) );
				}
				break;

			case PacketClientEssentiaCell.MODE_SELECTED_ASPECT:
				// Read the aspect from the stream
				this.currentAspect = AbstractPacket.readAspect( stream );
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientEssentiaCell.MODE_UPDATE_LIST:
				// Write the aspect list to the stream
				for( AspectStack stack : this.aspectStackList )
				{
					AbstractPacket.writeAspect( stack.aspect, stream );

					stream.writeLong( stack.amount );
				}
				break;

			case PacketClientEssentiaCell.MODE_SELECTED_ASPECT:
				// Write the aspect to the stream
				AbstractPacket.writeAspect( this.currentAspect, stream );
				break;
		}
	}

}
