package thaumicenergistics.common.network.packet.server;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEItemStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;
import thaumicenergistics.common.registries.EnumCache;

/**
 * {@link PartArcaneCraftingTerminal} server-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_S_ArcaneCraftingTerminal
	extends ThEServerPacket
{

	private static final byte MODE_REQUEST_FULL_LIST = 1;
	private static final byte MODE_REQUEST_EXTRACTION = 2;
	private static final byte MODE_REQUEST_DEPOSIT = 3;
	private static final byte MODE_REQUEST_CLEAR_GRID = 4;
	private static final byte MODE_REQUEST_DEPOSIT_REGION = 5;
	private static final byte MODE_REQUEST_SET_SORT = 6;
	private static final byte MODE_REQUEST_SET_GRID = 7;
	private static final byte MODE_REQUEST_AUTO_CRAFT = 8;
	private static final byte MODE_REQUEST_SWAP_ARMOR = 9;

	private static final int ITEM_GRID_SIZE = 9;

	/**
	 * Extracted or deposited item.
	 */
	private IAEItemStack itemStack;

	/**
	 * Mouse button being held.
	 */
	private int mouseButton;

	/**
	 * True if shift is being held.
	 */
	private boolean isShiftHeld;

	/**
	 * Identifies a region by a slot inside of it.
	 */
	private int slotNumber;

	/**
	 * Order to sort the items.
	 */
	private SortOrder sortingOrder;

	/**
	 * Direction to sort the items.
	 */
	private SortDir sortingDirection;

	/**
	 * What mode is used to view the items.
	 */
	private ViewItems viewMode;

	/**
	 * Items to set the crafting grid to.
	 */
	private IAEItemStack[] gridItems;

	/**
	 * Creates the packet
	 *
	 * @param player
	 * @param mode
	 * @return
	 */
	private static Packet_S_ArcaneCraftingTerminal newPacket( final EntityPlayer player, final byte mode )
	{
		// Create the packet
		Packet_S_ArcaneCraftingTerminal packet = new Packet_S_ArcaneCraftingTerminal();

		// Set the player & mode
		packet.player = player;
		packet.mode = mode;

		return packet;
	}

	/**
	 * Create a packet to request to autocraft the specified item.
	 *
	 * @param player
	 * @param result
	 * @return
	 */
	public static void sendAutoCraft( final EntityPlayer player, final IAEItemStack result )
	{
		Packet_S_ArcaneCraftingTerminal packet = newPacket( player, MODE_REQUEST_AUTO_CRAFT );

		// Set the result
		packet.itemStack = result;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Create a packet to request that the crafting grid be cleared.
	 * the ME network. Use only when needed.
	 *
	 * @param player
	 */
	public static void sendClearGrid( final EntityPlayer player )
	{
		Packet_S_ArcaneCraftingTerminal packet = newPacket( player, MODE_REQUEST_CLEAR_GRID );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Creates a packet letting the server know the user would like to
	 * deposition whatever they are holding into the ME network.
	 *
	 * @param player
	 * @return
	 */
	public static void sendDeposit( final EntityPlayer player, final int mouseButton )
	{
		Packet_S_ArcaneCraftingTerminal packet = newPacket( player, MODE_REQUEST_DEPOSIT );

		// Set the button
		packet.mouseButton = mouseButton;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Create a packet requesting that a region(inventory) be deposited into the
	 * ME network.
	 *
	 * @param player
	 * @param slotNumber
	 * @return
	 */
	public static void sendDepositRegion( final EntityPlayer player, final int slotNumber )
	{
		Packet_S_ArcaneCraftingTerminal packet = newPacket( player, MODE_REQUEST_DEPOSIT_REGION );

		// Set the slot number
		packet.slotNumber = slotNumber;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Creates a packet letting the server know the user would like to
	 * extract the specified itemstack from the ME network.
	 *
	 * @param player
	 * @param itemStack
	 * @param mouseButton
	 * @return
	 */
	public static void sendExtract(	final EntityPlayer player, final IAEItemStack itemStack, final int mouseButton,
									final boolean isShiftHeld )
	{
		Packet_S_ArcaneCraftingTerminal packet = newPacket( player, MODE_REQUEST_EXTRACTION );

		// Set stack
		packet.itemStack = itemStack;

		// Set mouse button
		packet.mouseButton = mouseButton;

		// Set shift
		packet.isShiftHeld = isShiftHeld;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Create a packet in full list mode.
	 * This will request a full list of all items in
	 * the ME network. Use only when needed.
	 *
	 * @param player
	 */
	public static void sendFullListRequest( final EntityPlayer player )
	{
		Packet_S_ArcaneCraftingTerminal packet = newPacket( player, MODE_REQUEST_FULL_LIST );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Create a packet to request the sorting order and direction.
	 *
	 * @param player
	 * @param order
	 * @param direction
	 * @return
	 */
	public static void sendMode(	final EntityPlayer player, final SortOrder order, final SortDir direction,
									final ViewItems viewMode )
	{
		Packet_S_ArcaneCraftingTerminal packet = newPacket( player, MODE_REQUEST_SET_SORT );

		// Set the sorts
		packet.sortingDirection = direction;
		packet.sortingOrder = order;
		packet.viewMode = viewMode;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	/**
	 * Create a packet to request that the crafting grid be set to these items.
	 *
	 * @param player
	 * @param items
	 * Must be at least length of 9
	 * @return
	 */
	public static void sendSetCrafting_NEI( final EntityPlayer player, final IAEItemStack[] items )
	{
		Packet_S_ArcaneCraftingTerminal packet = newPacket( player, MODE_REQUEST_SET_GRID );

		// Set the items
		packet.gridItems = items;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	public static void sendSwapArmor( final EntityPlayer player )
	{
		Packet_S_ArcaneCraftingTerminal packet = newPacket( player, MODE_REQUEST_SWAP_ARMOR );

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	public void execute()
	{
		// If the player is not null, and they have the ACT container open
		if( ( this.player != null ) && ( this.player.openContainer instanceof ContainerPartArcaneCraftingTerminal ) )
		{
			switch ( this.mode )
			{
			case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_FULL_LIST:
				// Request the full list
				( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestFullUpdate( this.player );
				break;

			case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION:
				// Request extraction
				( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestExtract( this.player, this.itemStack,
					this.mouseButton, this.isShiftHeld );
				break;

			case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT:
				// Request deposit
				( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestDeposit( this.player, this.mouseButton );
				break;

			case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_CLEAR_GRID:
				// Request clear grid
				( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestClearCraftingGrid( this.player );
				break;

			case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT_REGION:
				// Request deposit region
				( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestDepositRegion( this.player, this.slotNumber );
				break;

			case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_SET_SORT:
				// Request set sort
				( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestSetSort( this.sortingOrder,
					this.sortingDirection, this.viewMode );
				break;

			case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_SET_GRID:
				// Request set grid
				( (ContainerPartArcaneCraftingTerminal)this.player.openContainer )
								.onClientNEIRequestSetCraftingGrid( this.player, this.gridItems );
				break;

			case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_AUTO_CRAFT:
				// Request auto-crafting
				( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestAutoCraft( this.player, this.itemStack );
				break;

			case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_SWAP_ARMOR:
				// Request armor swap
				( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestSwapArmor( this.player );
				break;
			}
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION:
			// Read the item
			this.itemStack = ThEBasePacket.readAEItemStack( stream );

			// Read the mouse button
			this.mouseButton = stream.readInt();

			// Read the shift status
			this.isShiftHeld = stream.readBoolean();
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT:
			// Read the mouse button
			this.mouseButton = stream.readInt();
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT_REGION:
			// Read the slot number
			this.slotNumber = stream.readInt();
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_SET_SORT:
			// Read sorts
			this.sortingDirection = EnumCache.AE_SORT_DIRECTIONS[stream.readInt()];
			this.sortingOrder = EnumCache.AE_SORT_ORDERS[stream.readInt()];
			this.viewMode = EnumCache.AE_VIEW_ITEMS[stream.readInt()];
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_SET_GRID:
			// Init the items
			this.gridItems = new IAEItemStack[Packet_S_ArcaneCraftingTerminal.ITEM_GRID_SIZE];

			// Read the items
			for( int slotIndex = 0; slotIndex < 9; slotIndex++ )
			{
				// Do we have an item to read?
				if( stream.readBoolean() )
				{
					// Set the item
					this.gridItems[slotIndex] = ThEBasePacket.readAEItemStack( stream );
				}
			}
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_AUTO_CRAFT:
			// Read the requested item
			this.itemStack = ThEBasePacket.readAEItemStack( stream );
			break;
		}

	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION:
			// Write the itemstack
			ThEBasePacket.writeAEItemStack( this.itemStack, stream );

			// Write the mouse button
			stream.writeInt( this.mouseButton );

			// Write the shift status
			stream.writeBoolean( this.isShiftHeld );
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT:
			// Write the mouse button
			stream.writeInt( this.mouseButton );
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT_REGION:
			// Write the slot number to the stream
			stream.writeInt( this.slotNumber );
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_SET_SORT:
			// Write the sorts
			stream.writeInt( this.sortingDirection.ordinal() );
			stream.writeInt( this.sortingOrder.ordinal() );
			stream.writeInt( this.viewMode.ordinal() );
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_SET_GRID:
			// Write each non-null item
			for( int slotIndex = 0; slotIndex < Packet_S_ArcaneCraftingTerminal.ITEM_GRID_SIZE; slotIndex++ )
			{
				// Get the item
				IAEItemStack slotItem = this.gridItems[slotIndex];

				// Write if the slot is not null
				boolean hasItem = slotItem != null;
				stream.writeBoolean( hasItem );

				if( hasItem )
				{
					// Write the item
					ThEBasePacket.writeAEItemStack( slotItem, stream );
				}
			}
			break;

		case Packet_S_ArcaneCraftingTerminal.MODE_REQUEST_AUTO_CRAFT:
			// Write the requested item
			ThEBasePacket.writeAEItemStack( this.itemStack, stream );
			break;
		}
	}

}
