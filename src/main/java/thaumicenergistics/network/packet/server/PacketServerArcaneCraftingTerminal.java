package thaumicenergistics.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.AbstractServerPacket;
import appeng.api.storage.data.IAEItemStack;

public class PacketServerArcaneCraftingTerminal
	extends AbstractServerPacket
{

	private static final byte MODE_REQUEST_FULL_LIST = 1;
	private static final byte MODE_REQUEST_EXTRACTION = 2;
	private static final byte MODE_REQUEST_DEPOSIT = 3;
	private static final byte MODE_REQUEST_CLEAR_GRID = 4;

	private IAEItemStack itemStack;

	private int mouseButton;

	/**
	 * Create a packet in full list mode.
	 * This will request a full list of all items in
	 * the ME network. Use only when needed.
	 * 
	 * @param player
	 */
	public PacketServerArcaneCraftingTerminal createRequestFullList( EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerArcaneCraftingTerminal.MODE_REQUEST_FULL_LIST;

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
	public PacketServerArcaneCraftingTerminal createRequestExtract( EntityPlayer player, IAEItemStack itemStack, int mouseButton )
	{
		// Set player
		this.player = player;

		// Set mode
		this.mode = PacketServerArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION;

		// Set stack
		this.itemStack = itemStack;

		// Set mouse button
		this.mouseButton = mouseButton;

		return this;
	}

	/**
	 * Creates a packet letting the server know the user would like to
	 * deposition whatever they are holding into the ME network.
	 * 
	 * @param player
	 * @return
	 */
	public PacketServerArcaneCraftingTerminal createRequestDeposit( EntityPlayer player, int mouseButton )
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
	 * Create a packet to request that the crafting grid be cleared.
	 * the ME network. Use only when needed.
	 * 
	 * @param player
	 */
	public PacketServerArcaneCraftingTerminal createRequestClearGrid( EntityPlayer player )
	{
		// Set the player
		this.player = player;

		// Set the mode
		this.mode = PacketServerArcaneCraftingTerminal.MODE_REQUEST_CLEAR_GRID;

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
					( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestExtract( this.player, this.itemStack, this.mouseButton );
					break;
					
				case PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT:
					// Request deposit
					( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestDeposit( this.player, this.mouseButton );
					break;
					
				case PacketServerArcaneCraftingTerminal.MODE_REQUEST_CLEAR_GRID:
					// Request clear grid
					( (ContainerPartArcaneCraftingTerminal)this.player.openContainer ).onClientRequestClearCraftingGrid( this.player );
					break;
			}
		}
	}

	@Override
	public void readData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION:
				// Read the item
				this.itemStack = AbstractPacket.readAEItemStack( stream );

				// Read the mouse button
				this.mouseButton = stream.readInt();
				break;
				
			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT:
				// Read the mouse button
				this.mouseButton = stream.readInt();
				break;
		}

	}

	@Override
	public void writeData( ByteBuf stream )
	{
		switch ( this.mode )
		{
			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_EXTRACTION:
				// Write the itemstack
				AbstractPacket.writeAEItemStack( this.itemStack, stream );

				// Write the mouse button
				stream.writeInt( this.mouseButton );
				break;
				
			case PacketServerArcaneCraftingTerminal.MODE_REQUEST_DEPOSIT:
				// Write the mouse button
				stream.writeInt( this.mouseButton );
				break;
		}
	}

}
