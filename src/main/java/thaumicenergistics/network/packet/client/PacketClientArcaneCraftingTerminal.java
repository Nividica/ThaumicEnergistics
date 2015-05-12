package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.network.packet.AbstractClientPacket;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.registries.EnumCache;
import appeng.api.AEApi;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketClientArcaneCraftingTerminal
	extends AbstractClientPacket
{
	/**
	 * Packet modes
	 */
	private static final byte MODE_RECEIVE_CHANGE = 0, MODE_RECEIVE_FULL_LIST = 1, MODE_RECEIVE_PLAYER_HOLDING = 2, MODE_RECEIVE_SORTS = 3,
					MODE_UPDATE_COSTS = 4;

	private IAEItemStack changedStack;
	private IItemList<IAEItemStack> fullList;
	private boolean isHeldEmpty;
	private SortOrder sortingOrder;
	private SortDir sortingDirection;
	private ViewItems viewMode;

	@SideOnly(Side.CLIENT)
	@Override
	protected void wrappedExecute()
	{
		// Get the current screen being displayed to the user
		Gui gui = Minecraft.getMinecraft().currentScreen;

		// Is that screen the gui for the ACT?
		if( gui instanceof GuiArcaneCraftingTerminal )
		{
			switch ( this.mode )
			{
				case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST:
					// Set the item list
					( (GuiArcaneCraftingTerminal)gui ).onReceiveFullList( this.fullList );
					break;

				case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_CHANGE:
					// Update the item list
					( (GuiArcaneCraftingTerminal)gui ).onReceiveChange( this.changedStack );
					break;

				case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_PLAYER_HOLDING:
					// Set the held item
					( (GuiArcaneCraftingTerminal)gui ).onReceivePlayerHeld( this.changedStack );
					break;

				case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_SORTS:
					( (GuiArcaneCraftingTerminal)gui ).onReceiveSorting( this.sortingOrder, this.sortingDirection, this.viewMode );
					break;

				case PacketClientArcaneCraftingTerminal.MODE_UPDATE_COSTS:
					( (GuiArcaneCraftingTerminal)gui ).onServerSendForceUpdateCost();
					break;
			}
		}
	}

	/**
	 * Creates a packet with a changed network stack amount
	 * 
	 * @param player
	 * @param change
	 */
	public PacketClientArcaneCraftingTerminal createChangeUpdate( final EntityPlayer player, final IAEItemStack change )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientArcaneCraftingTerminal.MODE_RECEIVE_CHANGE;

		// Set the change
		this.changedStack = change;

		return this;
	}

	/**
	 * Creates a packet with the full list of items in the AE network.
	 * Only send in response to a request.
	 * 
	 * @param player
	 * @param fullList
	 */
	public PacketClientArcaneCraftingTerminal createFullListUpdate( final EntityPlayer player, final IItemList<IAEItemStack> fullList )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST;

		// Enable compression
		this.useCompression = true;

		// Set the full list
		this.fullList = fullList;

		return this;
	}

	/**
	 * Creates a packet with an update to what itemstack the player is holding.
	 * 
	 * @param player
	 * @param heldItem
	 * @return
	 */
	public PacketClientArcaneCraftingTerminal createPlayerHoldingUpdate( final EntityPlayer player, final IAEItemStack heldItem )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientArcaneCraftingTerminal.MODE_RECEIVE_PLAYER_HOLDING;

		// Set the held item
		this.changedStack = heldItem;

		// Is the player holding anything?
		this.isHeldEmpty = ( heldItem == null );

		return this;
	}

	/**
	 * Creates a packet with an update to the sorting order and direction.
	 * 
	 * @param player
	 * @param order
	 * @param direction
	 * @return
	 */
	public PacketClientArcaneCraftingTerminal createSortingUpdate( final EntityPlayer player, final SortOrder order, final SortDir direction,
																	final ViewItems viewMode )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientArcaneCraftingTerminal.MODE_RECEIVE_SORTS;

		// Set the sorts
		this.sortingDirection = direction;
		this.sortingOrder = order;
		this.viewMode = viewMode;

		return this;
	}

	/**
	 * Forces the client to re-calculate the displayed aspect costs
	 * 
	 * @return
	 */
	public PacketClientArcaneCraftingTerminal createUpdateAspectCost( final EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketClientArcaneCraftingTerminal.MODE_UPDATE_COSTS;

		return this;
	}

	@Override
	public void readData( final ByteBuf stream )
	{

		switch ( this.mode )
		{
			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST:
				// Create a new list
				this.fullList = AEApi.instance().storage().createItemList();

				// Read how many items there are
				int count = stream.readInt();

				for( int i = 0; i < count; i++ )
				{
					// Also ensure there are bytes to read
					if( stream.readableBytes() <= 0 )
					{
						break;
					}

					// Read the itemstack
					IAEItemStack itemStack = AbstractPacket.readAEItemStack( stream );

					// Ensure it is not null
					if( itemStack != null )
					{
						// Add to the list
						this.fullList.add( itemStack );
					}
				}
				break;

			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_CHANGE:
				// Read the change amount
				int changeAmount = stream.readInt();

				// Read the item
				this.changedStack = AbstractPacket.readAEItemStack( stream );

				// Adjust it's size
				this.changedStack.setStackSize( changeAmount );

				break;

			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_PLAYER_HOLDING:
				// Read if the itemstack is empty
				this.isHeldEmpty = stream.readBoolean();

				// Is it not empty?
				if( !this.isHeldEmpty )
				{
					this.changedStack = AbstractPacket.readAEItemStack( stream );
				}
				else
				{
					this.changedStack = null;
				}
				break;

			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_SORTS:
				// Read sorts
				this.sortingDirection = EnumCache.AE_SORT_DIRECTIONS[stream.readInt()];
				this.sortingOrder = EnumCache.AE_SORT_ORDERS[stream.readInt()];
				this.viewMode = EnumCache.AE_VIEW_ITEMS[stream.readInt()];
				break;
		}
	}

	@Override
	public void writeData( final ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST:
				// Is the list null?
				if( this.fullList == null )
				{
					// No items
					stream.writeInt( 0 );
					return;
				}

				// Write how many items there are
				stream.writeInt( this.fullList.size() );

				// Get the iterator
				Iterator<IAEItemStack> listIterator = this.fullList.iterator();

				// Write each item
				while( listIterator.hasNext() )
				{
					AbstractPacket.writeAEItemStack( listIterator.next(), stream );
				}
				break;

			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_CHANGE:
				// Write the change amount
				stream.writeInt( (int)this.changedStack.getStackSize() );

				// Write the change
				AbstractPacket.writeAEItemStack( this.changedStack, stream );
				break;

			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_PLAYER_HOLDING:
				// Write if the held item is empty
				stream.writeBoolean( this.isHeldEmpty );

				// Is it not empty?
				if( !this.isHeldEmpty )
				{
					// Write the stack
					AbstractPacket.writeAEItemStack( this.changedStack, stream );
				}

				break;

			case PacketClientArcaneCraftingTerminal.MODE_RECEIVE_SORTS:
				// Write the sorts
				stream.writeInt( this.sortingDirection.ordinal() );
				stream.writeInt( this.sortingOrder.ordinal() );
				stream.writeInt( this.viewMode.ordinal() );
				break;
		}

	}

}
