package thaumicenergistics.common.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;

/**
 * Packed used to send misc. sync events to the client.
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
	private static final byte MODE_PLAYER_HELD = 1;

	private ItemStack itemStack;

	private boolean flag;

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
		packet.itemStack = heldItem;

		// Is the player holding anything?
		packet.flag = ( heldItem != null );

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
			this.flag = stream.readBoolean();
			if( this.flag )
			{
				// Read stack
				this.itemStack = ThEBasePacket.readItemstack( stream );
			}
			else
			{
				this.itemStack = null;
			}

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
			this.player.inventory.setItemStack( this.itemStack );
			break;
		}
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case MODE_PLAYER_HELD:
			// Write if the held item is null
			stream.writeBoolean( this.flag );
			if( this.flag )
			{
				// Write the stack
				ThEBasePacket.writeItemstack( this.itemStack, stream );
			}

			break;
		}
	}

}
