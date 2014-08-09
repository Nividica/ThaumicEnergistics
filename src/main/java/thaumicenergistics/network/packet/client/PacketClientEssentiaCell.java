package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.ContainerEssentiaCell;
import thaumicenergistics.gui.GuiEssentiaCell;
import thaumicenergistics.network.packet.AbstractClientPacket;
import thaumicenergistics.network.packet.AbstractPacket;

public class PacketClientEssentiaCell
	extends AbstractClientPacket
{
	private static final int MODE_FULL_LIST = 0;
	private static final int MODE_SELECTED_ASPECT = 1;
	private static final int MODE_LIST_CHANGED = 2;

	private List<AspectStack> aspectStackList;
	private Aspect selectedAspect;
	private AspectStack change;

	public PacketClientEssentiaCell createUpdateFullList( EntityPlayer player, List<AspectStack> list )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaCell.MODE_FULL_LIST;

		// Mark to use compression
		this.useCompression = true;

		// Set the list
		this.aspectStackList = list;

		return this;
	}

	public PacketClientEssentiaCell createSelectedAspectUpdate( EntityPlayer player, Aspect selectedAspect )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaCell.MODE_SELECTED_ASPECT;

		// Set the aspect
		this.selectedAspect = selectedAspect;

		return this;
	}
	
	public PacketClientEssentiaCell createListChanged( EntityPlayer player, AspectStack change )
	{
		// Set the player
		this.player = player;
		
		// Set the mode
		this.mode = PacketClientEssentiaCell.MODE_LIST_CHANGED;
		
		// Set the change
		this.change = change;
		
		return this;
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void wrappedExecute()
	{
		// Get the current screen being displayed to the user
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Is that screen the gui for the cell?
		if( gui instanceof GuiEssentiaCell )
		{
			// Get the gui's container
			ContainerEssentiaCell container = (ContainerEssentiaCell)( ( (GuiEssentiaCell)gui ).inventorySlots );

			switch ( this.mode )
			{
				case PacketClientEssentiaCell.MODE_FULL_LIST:

					// Update the aspect list
					container.onReceiveAspectList( this.aspectStackList );
					break;

				case PacketClientEssentiaCell.MODE_SELECTED_ASPECT:

					// Set the selected aspect
					container.onReceiveSelectedAspect( this.selectedAspect );
					break;
					
				case PacketClientEssentiaCell.MODE_LIST_CHANGED:
					// Update the list
					container.onReceiveAspectListChange( this.change );
					break;
			}
		}
	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientEssentiaCell.MODE_FULL_LIST:
				// Create a new list
				this.aspectStackList = new ArrayList<AspectStack>();

				// Read in the aspect list from the stream
				while( stream.readableBytes() > 0 )
				{
					this.aspectStackList.add( new AspectStack( AbstractPacket.readAspect( stream ), stream.readLong() ) );
				}
				break;

			case PacketClientEssentiaCell.MODE_SELECTED_ASPECT:
				// Read the aspect from the stream
				this.selectedAspect = AbstractPacket.readAspect( stream );
				break;
				
			case PacketClientEssentiaCell.MODE_LIST_CHANGED:
				// Read the stack
				this.change = new AspectStack( AbstractPacket.readAspect( stream ), stream.readLong() );
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientEssentiaCell.MODE_FULL_LIST:
				// Write the aspect list to the stream
				for( AspectStack stack : this.aspectStackList )
				{
					AbstractPacket.writeAspect( stack.aspect, stream );

					stream.writeLong( stack.amount );
				}
				break;

			case PacketClientEssentiaCell.MODE_SELECTED_ASPECT:
				// Write the aspect to the stream
				AbstractPacket.writeAspect( this.selectedAspect, stream );
				break;
				
			case PacketClientEssentiaCell.MODE_LIST_CHANGED:
				// Write the aspect
				AbstractPacket.writeAspect( this.change.aspect, stream );
				
				// Write the amount
				stream.writeLong( this.change.amount );
		}
	}

}
