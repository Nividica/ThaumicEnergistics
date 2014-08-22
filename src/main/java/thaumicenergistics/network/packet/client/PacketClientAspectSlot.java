package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.network.IAspectSlotGui;
import thaumicenergistics.network.packet.AbstractClientPacket;
import thaumicenergistics.network.packet.AbstractPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketClientAspectSlot
	extends AbstractClientPacket
{
	private static final byte MODE_LIST_UPDATE = 0;

	private List<Aspect> filterAspects;

	@SideOnly(Side.CLIENT)
	@Override
	protected void wrappedExecute()
	{
		// Get the gui
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Ensure it is an IAspectSlotGui
		if( !( gui instanceof IAspectSlotGui ) )
		{
			return;
		}

		switch ( this.mode )
		{
			case PacketClientAspectSlot.MODE_LIST_UPDATE:
				( (IAspectSlotGui)gui ).updateAspects( this.filterAspects );
				break;
		}

	}

	public PacketClientAspectSlot createFilterListUpdate( List<Aspect> filterAspects, EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientAspectSlot.MODE_LIST_UPDATE;

		// Mark to use compression
		this.useCompression = true;

		// Set the list
		this.filterAspects = filterAspects;

		return this;
	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientAspectSlot.MODE_LIST_UPDATE:
				// Read the size
				int size = stream.readInt();

				// Create the list
				this.filterAspects = new ArrayList( size );

				// Read each aspect
				for( int i = 0; i < size; i++ )
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
			case PacketClientAspectSlot.MODE_LIST_UPDATE:
				// Write the size of the list
				stream.writeInt( this.filterAspects.size() );

				// Write each aspect
				for( int index = 0; index < this.filterAspects.size(); index++ )
				{
					AbstractPacket.writeAspect( this.filterAspects.get( index ), stream );
				}
				break;
		}
	}

}
