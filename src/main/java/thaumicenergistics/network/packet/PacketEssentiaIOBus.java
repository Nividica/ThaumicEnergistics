package thaumicenergistics.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.gui.GuiEssentiatIO;
import thaumicenergistics.network.AbstractPacket;
import thaumicenergistics.parts.AEPartEssentiaIO;
import appeng.api.config.RedstoneMode;

public class PacketEssentiaIOBus extends AbstractPacket
{
	private AEPartEssentiaIO part;
	private byte action;
	private byte ordinal;
	private byte filterSize;
	private boolean redstoneControlled;

	public PacketEssentiaIOBus()
	{
	}

	public PacketEssentiaIOBus(boolean redstoneControlled)
	{
		this.mode = 4;

		this.redstoneControlled = redstoneControlled;
	}

	public PacketEssentiaIOBus(byte filterSize)
	{
		this.mode = 3;

		this.filterSize = filterSize;
	}

	public PacketEssentiaIOBus(EntityPlayer player, AEPartEssentiaIO part)
	{
		super( player );

		this.mode = 2;

		this.part = part;
	}

	public PacketEssentiaIOBus(EntityPlayer player, byte action, AEPartEssentiaIO part)
	{
		super( player );

		this.mode = 0;

		this.action = action;

		this.part = part;
	}

	public PacketEssentiaIOBus(RedstoneMode redstoneMode)
	{
		this.mode = 1;

		this.ordinal = ( (byte) redstoneMode.ordinal() );
	}

	@Override
	public void execute()
	{
		Gui gui;

		switch ( this.mode )
		{
			case 0:
				this.part.loopRedstoneMode( this.player );
				break;

			case 1:
				gui = Minecraft.getMinecraft().currentScreen;
				if ( gui instanceof GuiEssentiatIO )
				{
					GuiEssentiatIO partGui = (GuiEssentiatIO) gui;

					partGui.updateRedstoneMode( RedstoneMode.values()[this.ordinal] );
				}
				break;

			case 2:
				this.part.sendInformation( this.player );
				break;

			case 3:
				gui = Minecraft.getMinecraft().currentScreen;

				if ( gui instanceof GuiEssentiatIO )
				{
					GuiEssentiatIO partGui = (GuiEssentiatIO) gui;

					partGui.changeConfig( this.filterSize );
				}

				break;

			case 4:
				gui = Minecraft.getMinecraft().currentScreen;

				if ( gui instanceof GuiEssentiatIO )
				{
					GuiEssentiatIO partGui = (GuiEssentiatIO) gui;

					partGui.setRedstoneControlled( this.redstoneControlled );
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
				this.part = ( (AEPartEssentiaIO) AbstractPacket.readPart( stream ) );
				this.action = stream.readByte();
				break;

			case 1:
				this.ordinal = stream.readByte();
				break;

			case 2:
				this.part = ( (AEPartEssentiaIO) AbstractPacket.readPart( stream ) );
				break;

			case 3:
				this.filterSize = stream.readByte();
				break;

			case 4:
				this.redstoneControlled = stream.readBoolean();
				break;
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case 0:
				AbstractPacket.writePart( this.part, stream );
				stream.writeByte( this.action );
				break;

			case 1:
				stream.writeByte( this.ordinal );
				break;

			case 2:
				AbstractPacket.writePart( this.part, stream );
				break;

			case 3:
				stream.writeByte( this.filterSize );
				break;

			case 4:
				stream.writeBoolean( this.redstoneControlled );
				break;
		}
	}

}
