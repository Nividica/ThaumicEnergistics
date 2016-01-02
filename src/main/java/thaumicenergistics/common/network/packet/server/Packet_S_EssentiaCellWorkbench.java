package thaumicenergistics.common.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.tiles.TileEssentiaCellWorkbench;

public class Packet_S_EssentiaCellWorkbench
	extends ThEServerPacket
{
	/**
	 * Packet modes.
	 */
	private static final byte MODE_REQUEST_ADD_ASPECT = 0, MODE_REQUEST_REMOVE_ASPECT = 1, MODE_REQUEST_REPLACE_ASPECT = 2,
					MODE_REQUEST_FULL_LIST = 4, MODE_REQUEST_CLEAR = 5, MODE_REQUEST_PARITION_CONTENTS = 6;

	private Aspect arAspect, replaceAspect;

	private TileEssentiaCellWorkbench workbench;

	/**
	 * Creates the packet
	 * 
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_S_EssentiaCellWorkbench newPacket( final EntityPlayer player, final byte mode )
	{
		// Create the packet
		Packet_S_EssentiaCellWorkbench packet = new Packet_S_EssentiaCellWorkbench();

		// Set the player & mode
		packet.player = player;
		packet.mode = mode;

		return packet;
	}

	public static void sendAddAspect( final EntityPlayer player, final TileEssentiaCellWorkbench workbench,
										final Aspect aspect )
	{
		Packet_S_EssentiaCellWorkbench packet = newPacket( player, MODE_REQUEST_ADD_ASPECT );

		// Set the aspect
		packet.arAspect = aspect;

		// Set the workbench
		packet.workbench = workbench;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendClearPartitioning( final EntityPlayer player, final TileEssentiaCellWorkbench workbench )
	{
		Packet_S_EssentiaCellWorkbench packet = newPacket( player, MODE_REQUEST_CLEAR );

		// Set the workbench
		packet.workbench = workbench;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendGetPartitionList( final EntityPlayer player, final TileEssentiaCellWorkbench workbench )
	{
		Packet_S_EssentiaCellWorkbench packet = newPacket( player, MODE_REQUEST_FULL_LIST );

		// Set the workbench
		packet.workbench = workbench;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendPartitionToContents( final EntityPlayer player, final TileEssentiaCellWorkbench workbench )
	{
		Packet_S_EssentiaCellWorkbench packet = newPacket( player, MODE_REQUEST_PARITION_CONTENTS );

		// Set the workbench
		packet.workbench = workbench;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendRemoveAspect( final EntityPlayer player, final TileEssentiaCellWorkbench workbench,
											final Aspect aspect )
	{
		Packet_S_EssentiaCellWorkbench packet = newPacket( player, MODE_REQUEST_REMOVE_ASPECT );

		// Set the aspect
		packet.arAspect = aspect;

		// Set the workbench
		packet.workbench = workbench;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendReplaceAspect( final EntityPlayer player, final TileEssentiaCellWorkbench workbench,
											final Aspect originalAspect, final Aspect newAspect )
	{
		Packet_S_EssentiaCellWorkbench packet = newPacket( player, MODE_REQUEST_REPLACE_ASPECT );

		// Set the aspects
		packet.arAspect = originalAspect;
		packet.replaceAspect = newAspect;

		// Set the workbench
		packet.workbench = workbench;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
		// Read the workbench
		this.workbench = (TileEssentiaCellWorkbench)ThEBasePacket.readTileEntity( stream );

		switch ( this.mode )
		{
		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_ADD_ASPECT:
		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_REMOVE_ASPECT:
			// Read the aspect
			this.arAspect = ThEBasePacket.readAspect( stream );
			break;

		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_REPLACE_ASPECT:
			// Read the original aspect
			this.arAspect = ThEBasePacket.readAspect( stream );

			// Read the replacement aspect
			this.replaceAspect = ThEBasePacket.readAspect( stream );
			break;
		}
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		// Write the workbench
		ThEBasePacket.writeTileEntity( this.workbench, stream );

		switch ( this.mode )
		{
		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_ADD_ASPECT:
		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_REMOVE_ASPECT:
			// Write the aspect
			ThEBasePacket.writeAspect( this.arAspect, stream );
			break;

		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_REPLACE_ASPECT:
			// Write the original aspect
			ThEBasePacket.writeAspect( this.arAspect, stream );

			// Write the replacement aspect
			ThEBasePacket.writeAspect( this.replaceAspect, stream );
			break;
		}

	}

	@Override
	public void execute()
	{
		switch ( this.mode )
		{
		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_ADD_ASPECT:
			// Request the aspect be added
			this.workbench.onClientRequestAddAspectToPartitionList( this.player, this.arAspect );
			break;

		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_REMOVE_ASPECT:
			// Request the aspect be removed
			this.workbench.onClientRequestRemoveAspectFromPartitionList( this.player, this.arAspect );
			break;

		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_REPLACE_ASPECT:
			// Request the aspect be replaced
			this.workbench.onClientRequestReplaceAspectFromPartitionList( this.player, this.arAspect, this.replaceAspect );
			break;

		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_FULL_LIST:
			// Request the full list
			this.workbench.onClientRequestPartitionList( this.player );
			break;

		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_CLEAR:
			// Request the clear
			this.workbench.onClientRequestClearPartitioning( this.player );
			break;

		case Packet_S_EssentiaCellWorkbench.MODE_REQUEST_PARITION_CONTENTS:
			this.workbench.onClientRequestPartitionToContents( this.player );
			break;
		}
	}

}
