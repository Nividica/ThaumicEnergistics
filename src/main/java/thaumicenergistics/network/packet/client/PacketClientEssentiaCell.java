package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.container.ContainerEssentiaCell;
import thaumicenergistics.gui.GuiEssentiaCell;
import thaumicenergistics.network.packet.AbstractClientPacket;
import thaumicenergistics.network.packet.AbstractPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketClientEssentiaCell
	extends AbstractClientPacket
{
	private static final byte MODE_FULL_LIST = 0;
	private static final byte MODE_SELECTED_ASPECT = 1;
	private static final byte MODE_LIST_CHANGED = 2;
	private static final byte MODE_SORT_MODE_CHANGED = 3;

	private List<AspectStack> aspectStackList;
	private Aspect selectedAspect;
	private AspectStack change;
	private ComparatorMode sortMode;

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

				case PacketClientEssentiaCell.MODE_SORT_MODE_CHANGED:
					// Update the sorting mode
					( (GuiEssentiaCell)gui ).onSortModeChanged( this.sortMode );
					break;
			}
		}
	}

	public PacketClientEssentiaCell createListChanged( final EntityPlayer player, final AspectStack change )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaCell.MODE_LIST_CHANGED;

		// Set the change
		this.change = change;

		return this;
	}

	public PacketClientEssentiaCell createSelectedAspectUpdate( final EntityPlayer player, final Aspect selectedAspect )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaCell.MODE_SELECTED_ASPECT;

		// Set the aspect
		this.selectedAspect = selectedAspect;

		return this;
	}

	public PacketClientEssentiaCell createSortModeUpdate( final EntityPlayer player, final ComparatorMode sortMode )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaCell.MODE_SORT_MODE_CHANGED;

		// Set the sort mode
		this.sortMode = sortMode;

		return this;
	}

	public PacketClientEssentiaCell createUpdateFullList( final EntityPlayer player, final List<AspectStack> list )
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

	@Override
	public void readData( final ByteBuf stream )
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
				break;

			case PacketClientEssentiaCell.MODE_SORT_MODE_CHANGED:
				// Read the mode ordinal
				this.sortMode = ComparatorMode.VALUES[stream.readInt()];
				break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
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
				break;

			case PacketClientEssentiaCell.MODE_SORT_MODE_CHANGED:
				// Write the mode ordinal
				stream.writeInt( this.sortMode.ordinal() );
				break;
		}
	}

}
