package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import thaumicenergistics.registries.EnumCache;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.storage.data.IAEItemStack;

public class PacketServerArcaneCraftingTerminal
	extends AbstractServerPacket
{

	private static final byte MODE_REQUEST_FULL_LIST = 1;
	private static final byte MODE_REQUEST_EXTRACTION = 2;
	private static final byte MODE_REQUEST_DEPOSIT = 3;
	private static final byte MODE_REQUEST_CLEAR_GRID = 4;
	private static final byte MODE_REQUEST_DEPOSIT_REGION = 5;
	private static final byte MODE_REQUEST_SET_SORT = 6;

	private IAEItemStack itemStack;
	private int mouseButton;
	private boolean isShiftHeld;
	private int slotNumber;
	private SortOrder sortingOrder;
	private SortDir sortingDirection;

	/**
	 * Create a packet to request that the crafting grid be cleared.
	 * the ME network. Use only when needed.
	 * 
	 * @param player
	 */
	public PacketServerArcaneCraftingTerminal createRequestClearGrid( final EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerArcaneCraftingTerminal.MODE_REQUEST_CLEAR_GRID;

		return this;
	}

	/**
	 * Creates a packet letting the server know the user would like to
	 * deposition whatever they are holding into the ME network.
	 * 
	 * @param player
	 * @return
	 */
	public PacketServerArcaneCraftingTerminal createRequestDeposit( final EntityPlayer player, final int mouseButton )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT;

		// Set the button
		this.mouseButton = mouseButton;

		return this;
	}

	/**
	 * Create a packet requesting that a region(inventory) be deposited into the
	 * ME network.
	 * 
	 * @param player
	 * @param slotNumber
	 * @return
	 */
	public PacketServerArcaneCraftingTerminal createRequestDepositRegion( final EntityPlayer player, final int slotNumber )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT_REGION;

		// Set the slot number
		this.slotNumber = slotNumber;

		return this;
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
	public PacketServerArcaneCraftingTerminal createRequestExtract( final EntityPlayer player, final IAEItemStack itemStack, final int mouseButton,
																	final boolean isShiftHeld )
	{
		// Set player
		this.player = player;

		// Set mode
		this.mode = PacketServerArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION;

		// Set stack
		this.itemStack = itemStack;

		// Set mouse button
		this.mouseButton = mouseButton;

		// Set shift
		this.isShiftHeld = isShiftHeld;

		return this;
	}

	/**
	 * Create a packet in full list mode.
	 * This will request a full list of all items in
	 * the ME network. Use only when needed.
	 * 
	 * @param player
	 */
	public PacketServerArcaneCraftingTerminal createRequestFullList( final EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerArcaneCraftingTerminal.MODE_REQUEST_FULL_LIST;

		return this;
	}

	/**
	 * Create a packet to request the sorting order and direction.
	 * 
	 * @param player
	 * @param order
	 * @param direction
	 * @return
	 */
	public PacketServerArcaneCraftingTerminal createRequestSetSort( final EntityPlayer player, final SortOrder order, final SortDir direction )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerArcaneCraftingTerminal.MODE_REQUEST_SET_SORT;

		// Set the sorts
		this.sortingDirection = direction;
		this.sortingOrder = order;

		return this;
	}

	@Override
	public void execute()
	{
		// If the player is not null, and they have the ACT container open
		if( ( this.player != null ) && ( this.player.openContainer instanceof ContainerPartArcaneCraftingTerminal ) )
		{
			switch ( this.mode )
			{
				case PacketServerArcaneCraftingTerminal.MODE_REQUEST_FULL_LIST:
					// Request the full list
					( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestFullUpdate( this.player );
					break;

				case PacketServerArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION:
					// Request extraction
					( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestExtract( this.player, this.itemStack,
						this.mouseButton, this.isShiftHeld );
					break;

				case PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT:
					// Request deposit
					( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestDeposit( this.player, this.mouseButton );
					break;

				case PacketServerArcaneCraftingTerminal.MODE_REQUEST_CLEAR_GRID:
					// Request clear grid
					( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestClearCraftingGrid( this.player );
					break;

				case PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT_REGION:
					// Request deposit region
					( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestDepositRegion( this.player, this.slotNumber );
					break;

				case PacketServerArcaneCraftingTerminal.MODE_REQUEST_SET_SORT:
					// Reqeust set sort
					( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestSetSort( this.sortingOrder,
						this.sortingDirection );
					break;
			}
		}
	}

	@Override
	public void readData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION:
				// Read the item
				this.itemStack = AbstractPacket.readAEItemStack( stream );

				// Read the mouse button
				this.mouseButton = stream.readInt();

				// Read the shift status
				this.isShiftHeld = stream.readBoolean();
				break;

			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT:
				// Read the mouse button
				this.mouseButton = stream.readInt();
				break;

			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT_REGION:
				// Read the slot number
				this.slotNumber = stream.readInt();
				break;

			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_SET_SORT:
				// Read sorts
				this.sortingDirection = EnumCache.AE_SORT_DIRECTIONS[stream.readInt()];
				this.sortingOrder = EnumCache.AE_SORT_ORDERS[stream.readInt()];
				break;
		}

	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION:
				// Write the itemstack
				AbstractPacket.writeAEItemStack( this.itemStack, stream );

				// Write the mouse button
				stream.writeInt( this.mouseButton );

				// Write the shift status
				stream.writeBoolean( this.isShiftHeld );
				break;

			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT:
				// Write the mouse button
				stream.writeInt( this.mouseButton );
				break;

			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT_REGION:
				// Write the slot number to the stream
				stream.writeInt( this.slotNumber );
				break;

			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_SET_SORT:
				// Write the sorts
				stream.writeInt( this.sortingDirection.ordinal() );
				stream.writeInt( this.sortingOrder.ordinal() );
				break;
		}
	}

}
