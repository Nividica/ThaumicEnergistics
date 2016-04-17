package thaumicenergistics.common.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.utils.ThEUtils;

/**
 * Packed used to send miscellaneous events to the client.
 *
 * @author Nividica
 *
 */
public class Packet_C_Sync
	extends ThEClientPacket
{
	/**
	 * Packet modes.
	 */
	private static final byte MODE_PLAYER_HELD = 1,
					MODE_SOUND = 2;

	private ItemStack syncStack;

	private boolean syncFlag;

	private String syncString;

	/**
	 * Creates the packet
	 *
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_C_Sync newPacket( final EntityPlayer player, final byte mode )
	{
		// Create the packet
		Packet_C_Sync packet = new Packet_C_Sync();

		// Set the player & mode
		packet.player = player;
		packet.mode = mode;

		return packet;
	}

	/**
	 * Creates a packet with an update to what itemstack the player is holding.
	 *
	 * @param player
	 * @param heldItem
	 * @return
	 */
	public static void sendPlayerHeldItem( final EntityPlayer player, final ItemStack heldItem )
	{
		// Create the packet
		Packet_C_Sync packet = newPacket( player, MODE_PLAYER_HELD );

		// Set the held item
		packet.syncStack = heldItem;

		// Is the player holding anything?
		packet.syncFlag = ( heldItem != null );

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	/**
	 * Sends a sound to the player to play.
	 *
	 * @param player
	 * @param soundLocation
	 */
	public static void sendPlaySound( final EntityPlayer player, final String soundLocation )
	{
		// Create the packet
		Packet_C_Sync packet = newPacket( player, MODE_SOUND );

		// Set the sound location
		packet.syncString = soundLocation;

		// Send it
		NetworkHandler.sendPacketToClient( packet );
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case MODE_PLAYER_HELD:
			// Read if the item is null
			this.syncFlag = stream.readBoolean();
			if( this.syncFlag )
			{
				// Read stack
				this.syncStack = ThEBasePacket.readItemstack( stream );
			}
			else
			{
				this.syncStack = null;
			}
			break;

		case MODE_SOUND:
			// Read the sound location
			this.syncString = ThEBasePacket.readString( stream );
			break;
		}
	}

	@Override
	protected void wrappedExecute()
	{
		switch ( this.mode )
		{
		case MODE_PLAYER_HELD:
			// Set what the player is holding.
			this.player.inventory.setItemStack( this.syncStack );
			break;
		case MODE_SOUND:
			// Play the sound
			ThEUtils.playClientSound( null, this.syncString );
		}
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case MODE_PLAYER_HELD:
			// Write if the held item is null
			stream.writeBoolean( this.syncFlag );
			if( this.syncFlag )
			{
				// Write the stack
				ThEBasePacket.writeItemstack( this.syncStack, stream );
			}
			break;
		case MODE_SOUND:
			// Write the sound location
			ThEBasePacket.writeString( this.syncString, stream );
			break;
		}
	}

}
