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
import thaumicenergistics.gui.GuiEssentiaCellTerminal;
import thaumicenergistics.network.packet.AbstractClientPacket;
import thaumicenergistics.network.packet.AbstractPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketClientEssentiaCellTerminal
	extends AbstractClientPacket
{
	private static final byte MODE_FULL_LIST = 0;
	private static final byte MODE_SELECTED_ASPECT = 1;
	private static final byte MODE_SORT_MODE_CHANGED = 2;
	private static final byte MODE_LIST_CHANGED = 3;

	private List<AspectStack> aspectStackList;
	private Aspect selectedAspect;
	private ComparatorMode sortMode;
	private AspectStack change;

	@SideOnly(Side.CLIENT)
	@Override
	protected void wrappedExecute()
	{
		Gui gui = Minecraft.getMinecraft().currentScreen;

		if( gui instanceof GuiEssentiaCellTerminal )
		{
			switch ( this.mode )
			{
				case PacketClientEssentiaCellTerminal.MODE_FULL_LIST:
					( (GuiEssentiaCellTerminal)gui ).onReceiveAspectList( this.aspectStackList );
					break;

				case PacketClientEssentiaCellTerminal.MODE_SELECTED_ASPECT:
					( (GuiEssentiaCellTerminal)gui ).onReceiveSelectedAspect( this.selectedAspect );
					break;

				case PacketClientEssentiaCellTerminal.MODE_SORT_MODE_CHANGED:
					// Update the sorting mode
					( (GuiEssentiaCellTerminal)gui ).onSortModeChanged( this.sortMode );
					break;

				case PacketClientEssentiaCellTerminal.MODE_LIST_CHANGED:
					// Update the list
					( (GuiEssentiaCellTerminal)gui ).onReceiveAspectListChange( this.change );
					break;
			}
		}
	}

	public PacketClientEssentiaCellTerminal createListChanged( final EntityPlayer player, final AspectStack change )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaCellTerminal.MODE_LIST_CHANGED;

		// Set the change
		this.change = change;

		return this;
	}

	public PacketClientEssentiaCellTerminal createSelectedAspectUpdate( final EntityPlayer player, final Aspect selectedAspect )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaCellTerminal.MODE_SELECTED_ASPECT;

		// Set the selected aspect
		this.selectedAspect = selectedAspect;

		return this;
	}

	public PacketClientEssentiaCellTerminal createSortModeUpdate( final EntityPlayer player, final ComparatorMode sortMode )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaCellTerminal.MODE_SORT_MODE_CHANGED;

		// Set the sort mode
		this.sortMode = sortMode;

		return this;
	}

	public PacketClientEssentiaCellTerminal createUpdateFullList( final EntityPlayer player, final List<AspectStack> list )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientEssentiaCellTerminal.MODE_FULL_LIST;

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
			case PacketClientEssentiaCellTerminal.MODE_FULL_LIST:
				this.aspectStackList = new ArrayList<AspectStack>();

				while( stream.readableBytes() > 0 )
				{
					this.aspectStackList.add( new AspectStack( AbstractPacket.readAspect( stream ), stream.readLong() ) );
				}
				break;

			case PacketClientEssentiaCellTerminal.MODE_SELECTED_ASPECT:
				this.selectedAspect = AbstractPacket.readAspect( stream );
				break;

			case PacketClientEssentiaCellTerminal.MODE_SORT_MODE_CHANGED:
				// Read the mode ordinal
				this.sortMode = ComparatorMode.VALUES[stream.readInt()];
				break;

			case PacketClientEssentiaCellTerminal.MODE_LIST_CHANGED:
				// Read the stack
				this.change = new AspectStack( AbstractPacket.readAspect( stream ), stream.readLong() );
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{

		switch ( this.mode )
		{
			case PacketClientEssentiaCellTerminal.MODE_FULL_LIST:
				for( AspectStack stack : this.aspectStackList )
				{
					AbstractPacket.writeAspect( stack.aspect, stream );

					stream.writeLong( stack.amount );
				}
				break;

			case PacketClientEssentiaCellTerminal.MODE_SELECTED_ASPECT:
				AbstractPacket.writeAspect( this.selectedAspect, stream );
				break;

			case PacketClientEssentiaCellTerminal.MODE_SORT_MODE_CHANGED:
				// Write the mode ordinal
				stream.writeInt( this.sortMode.ordinal() );
				break;

			case PacketClientEssentiaCellTerminal.MODE_LIST_CHANGED:
				// Write the aspect
				AbstractPacket.writeAspect( this.change.aspect, stream );

				// Write the amount
				stream.writeLong( this.change.amount );
		}
	}
}
