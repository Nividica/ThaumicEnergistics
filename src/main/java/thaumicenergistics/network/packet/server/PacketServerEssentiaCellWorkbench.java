package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import thaumicenergistics.tileentities.TileEssentiaCellWorkbench;

public class PacketServerEssentiaCellWorkbench
	extends AbstractServerPacket
{
	/**
	 * Packet modes.
	 */
	private static final byte MODE_REQUEST_ADD_ASPECT = 0, MODE_REQUEST_REMOVE_ASPECT = 1, MODE_REQUEST_REPLACE_ASPECT = 2,
					MODE_REQUEST_FULL_LIST = 4, MODE_REQUEST_CLEAR = 5, MODE_REQUEST_PARITION_CONTENTS = 6;

	private Aspect arAspect, replaceAspect;

	private TileEssentiaCellWorkbench workbench;

	@Override
	protected void readData( final ByteBuf stream )
	{
		// Read the workbench
		this.workbench = (TileEssentiaCellWorkbench)AbstractPacket.readTileEntity( stream );

		switch ( this.mode )
		{
		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_ADD_ASPECT:
		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_REMOVE_ASPECT:
			// Read the aspect
			this.arAspect = AbstractPacket.readAspect( stream );
			break;

		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_REPLACE_ASPECT:
			// Read the original aspect
			this.arAspect = AbstractPacket.readAspect( stream );

			// Read the replacement aspect
			this.replaceAspect = AbstractPacket.readAspect( stream );
			break;
		}
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		// Write the workbench
		AbstractPacket.writeTileEntity( this.workbench, stream );

		switch ( this.mode )
		{
		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_ADD_ASPECT:
		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_REMOVE_ASPECT:
			// Write the aspect
			AbstractPacket.writeAspect( this.arAspect, stream );
			break;

		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_REPLACE_ASPECT:
			// Write the original aspect
			AbstractPacket.writeAspect( this.arAspect, stream );

			// Write the replacement aspect
			AbstractPacket.writeAspect( this.replaceAspect, stream );
			break;
		}

	}

	public PacketServerEssentiaCellWorkbench createRequestAddAspect( final EntityPlayer player, final TileEssentiaCellWorkbench workbench,
																		final Aspect aspect )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCellWorkbench.MODE_REQUEST_ADD_ASPECT;

		// Set the aspect
		this.arAspect = aspect;

		// Set the workbench
		this.workbench = workbench;

		return this;
	}

	public PacketServerEssentiaCellWorkbench createRequestClearPartitioning( final EntityPlayer player, final TileEssentiaCellWorkbench workbench )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCellWorkbench.MODE_REQUEST_CLEAR;

		// Set the workbench
		this.workbench = workbench;

		return this;
	}

	public PacketServerEssentiaCellWorkbench createRequestGetPartitionList( final EntityPlayer player, final TileEssentiaCellWorkbench workbench )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCellWorkbench.MODE_REQUEST_FULL_LIST;

		// Set the workbench
		this.workbench = workbench;

		return this;
	}

	public PacketServerEssentiaCellWorkbench createRequestPartitionToContents( final EntityPlayer player, final TileEssentiaCellWorkbench workbench )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCellWorkbench.MODE_REQUEST_PARITION_CONTENTS;

		// Set the workbench
		this.workbench = workbench;

		return this;
	}

	public PacketServerEssentiaCellWorkbench createRequestRemoveAspect( final EntityPlayer player, final TileEssentiaCellWorkbench workbench,
																		final Aspect aspect )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCellWorkbench.MODE_REQUEST_REMOVE_ASPECT;

		// Set the aspect
		this.arAspect = aspect;

		// Set the workbench
		this.workbench = workbench;

		return this;
	}

	public PacketServerEssentiaCellWorkbench createRequestReplaceAspect( final EntityPlayer player, final TileEssentiaCellWorkbench workbench,
																			final Aspect originalAspect, final Aspect newAspect )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerEssentiaCellWorkbench.MODE_REQUEST_REPLACE_ASPECT;

		// Set the aspects
		this.arAspect = originalAspect;
		this.replaceAspect = newAspect;

		// Set the workbench
		this.workbench = workbench;

		return this;
	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_ADD_ASPECT:
			// Request the aspect be added
			this.workbench.onClientRequestAddAspectToPartitionList( this.player, this.arAspect );
			break;

		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_REMOVE_ASPECT:
			// Request the aspect be removed
			this.workbench.onClientRequestRemoveAspectFromPartitionList( this.player, this.arAspect );
			break;

		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_REPLACE_ASPECT:
			// Request the aspect be replaced
			this.workbench.onClientRequestReplaceAspectFromPartitionList( this.player, this.arAspect, this.replaceAspect );
			break;

		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_FULL_LIST:
			// Request the full list
			this.workbench.onClientRequestPartitionList( this.player );
			break;

		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_CLEAR:
			// Request the clear
			this.workbench.onClientRequestClearPartitioning( this.player );
			break;

		case PacketServerEssentiaCellWorkbench.MODE_REQUEST_PARITION_CONTENTS:
			this.workbench.onClientRequestPartitionToContents( this.player );
			break;
		}
	}

}
