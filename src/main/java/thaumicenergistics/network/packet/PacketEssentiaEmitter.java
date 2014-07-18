package thaumicenergistics.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.gui.GuiEssentiaLevelEmitter;
import thaumicenergistics.network.AbstractPacket;
import thaumicenergistics.parts.AEPartEssentiaLevelEmitter;
import appeng.api.config.RedstoneMode;

public class PacketEssentiaEmitter extends AbstractPacket
{
	private long wantedAmount;
	private AEPartEssentiaLevelEmitter part;
	private RedstoneMode redstoneMode;
	private boolean toggle;

	public PacketEssentiaEmitter()
	{
	}

	public PacketEssentiaEmitter(boolean toggle, AEPartEssentiaLevelEmitter part, EntityPlayer player)
	{
		this.mode = 3;
		this.toggle = toggle;
		this.part = part;
		this.player = player;
	}

	public PacketEssentiaEmitter(int wantedAmount, AEPartEssentiaLevelEmitter part, EntityPlayer player)
	{
		this.mode = 0;
		this.wantedAmount = wantedAmount;
		this.part = part;
		this.player = player;
	}

	public PacketEssentiaEmitter(RedstoneMode redstoneMode, EntityPlayer player)
	{
		this.mode = 4;
		this.redstoneMode = redstoneMode;
		this.player = player;
	}

	public PacketEssentiaEmitter(String textField, AEPartEssentiaLevelEmitter part, EntityPlayer player)
	{
		this.mode = 1;
		this.wantedAmount = ( textField.isEmpty() ? 0L : Long.parseLong( textField ) );
		this.part = part;
		this.player = player;
	}
	
	public PacketEssentiaEmitter(long wantedAmount, EntityPlayer player)
	{
		this.mode = 2;
		this.player = player;
		this.wantedAmount = wantedAmount;
	}
	

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
			case 0:
				this.part.changeWantedAmount( (int) this.wantedAmount, this.player );
				break;

			case 1:
				this.part.setWantedAmount( this.wantedAmount, this.player );
				break;

			case 2:
				if ( ( this.player != null ) && ( this.player.isClientWorld() ) )
				{
					Gui gui = Minecraft.getMinecraft().currentScreen;
					if ( gui instanceof GuiEssentiaLevelEmitter )
					{
						( (GuiEssentiaLevelEmitter) gui ).setAmountField( this.wantedAmount );
					}
				}
				
				break;

			case 3:
				if ( this.toggle )
				{
					this.part.toggleMode( this.player );
				}
				else
				{
					this.part.syncClientGui( this.player );
				}
				break;

			case 4:
				if ( ( this.player != null ) && ( this.player.isClientWorld() ) )
				{
					Gui gui = Minecraft.getMinecraft().currentScreen;
					if ( gui instanceof GuiEssentiaLevelEmitter )
					{
						( (GuiEssentiaLevelEmitter) gui ).setRedstoneMode( this.redstoneMode );
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
			case 0:
			case 1:
				this.wantedAmount = stream.readLong();
				this.part = ( (AEPartEssentiaLevelEmitter) AbstractPacket.readPart( stream ) );
				break;

			case 2:
				this.wantedAmount = stream.readLong();
				break;

			case 3:
				this.toggle = stream.readBoolean();
				this.part = ( (AEPartEssentiaLevelEmitter) AbstractPacket.readPart( stream ) );
				break;

			case 4:
				this.redstoneMode = RedstoneMode.values()[stream.readInt()];
				break;
				
		}
	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case 0:
			case 1:
				stream.writeLong( this.wantedAmount );
				AbstractPacket.writePart( this.part, stream );
				break;

			case 2:
				stream.writeLong( this.wantedAmount );
				break;

			case 3:
				stream.writeBoolean( this.toggle );
				AbstractPacket.writePart( this.part, stream );
				break;

			case 4:
				stream.writeInt( this.redstoneMode.ordinal() );
				break;
		}
	}

}
