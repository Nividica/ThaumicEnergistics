package thaumicenergistics.common.inventory;

import thaumicenergistics.api.storage.IInventoryUpdateReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PrivateInventory
	implements IInventory
{

	private static final String NBT_KEY_SLOT = "Slot";

	public ItemStack[] slots;
	public String customName;
	private int stackLimit;
	private IInventoryUpdateReceiver receiver = null;

	public PrivateInventory( final String customName, final int size, final int stackLimit )
	{
		this.slots = new ItemStack[size];
		this.customName = customName;
		this.stackLimit = stackLimit;
	}

	public PrivateInventory( final String customName, final int size, final int stackLimit, final IInventoryUpdateReceiver receiver )
	{
		this( customName, size, stackLimit );

		this.receiver = receiver;
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public ItemStack decrStackSize( final int slotId, final int amount )
	{
		// Get the stack
		ItemStack slotStack = this.slots[slotId];
		ItemStack resultStack = null;

		// Is the slot empty?
		if( slotStack == null )
		{
			// Slot is empty
			return null;
		}

		// Calculate the amount to get
		int decAmount = Math.min( amount, slotStack.stackSize );

		// Calculate the remaining amount
		int remAmount = slotStack.stackSize - decAmount;

		// Is there anything remaining in the slot?
		if( remAmount > 0 )
		{
			this.slots[slotId].stackSize = remAmount;
		}
		else
		{
			this.slots[slotId] = null;
		}

		// Was any amount gotten?
		if( decAmount > 0 )
		{
			resultStack = slotStack.copy();
			resultStack.stackSize = decAmount;
		}

		this.markDirty();

		return resultStack;
	}

	@Override
	public String getInventoryName()
	{
		return this.customName;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.stackLimit;
	}

	@Override
	public int getSizeInventory()
	{
		return this.slots.length;
	}

	@Override
	public ItemStack getStackInSlot( final int index )
	{
		return this.slots[index];
	}

	@Override
	public ItemStack getStackInSlotOnClosing( final int slotId )
	{
		return this.slots[slotId];
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	/**
	 * Increases the stack size of the specified slot, and marks the inventory
	 * as dirty.
	 * 
	 * @param slotId
	 * @param amount
	 * @return The amount that was added.
	 */
	public int incrStackSize( final int slotId, final int amount )
	{
		// Get the stack
		ItemStack slotStack = this.slots[slotId];

		// Assume none was added
		int added = 0;

		if( slotStack != null )
		{
			// Calculate the stack limit
			int stackLimit = Math.min( this.getInventoryStackLimit(), slotStack.getMaxStackSize() );

			// Calculate how much can be added
			added = Math.min( amount, stackLimit - slotStack.stackSize );

			// Can any be added?
			if( added > 0 )
			{
				this.slots[slotId].stackSize += added;
				this.markDirty();
			}
		}

		return added;
	}

	/**
	 * True if the inventory is empty.
	 * 
	 * @return
	 */
	public boolean isEmpty()
	{
		// Loop over the slots
		for( int index = 0; index < this.slots.length; ++index )
		{
			// Is this slot occupied?
			if( this.slots[index] != null )
			{
				// Inventory is not empty
				return false;
			}
		}

		// Checked all slots, inventory is empty.
		return true;
	}

	@Override
	public boolean isItemValidForSlot( final int slotId, final ItemStack itemStack )
	{
		// Assume it is not valid
		return false;
	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer player )
	{
		return true;
	}

	public final void readFromNBT( final NBTTagCompound data, final String tagName )
	{
		// Ensure there is a data tag
		if( data == null )
		{
			return;
		}

		// Ensure the data has our inventory
		if( !data.hasKey( tagName ) )
		{
			return;
		}

		// Load the list
		NBTTagList invList = data.getTagList( tagName, (byte)10 );

		// Load the inventory from the list
		for( int index = 0; index < invList.tagCount(); index++ )
		{
			// Get the compound
			NBTTagCompound nbtCompound = invList.getCompoundTagAt( index );

			// Get the slot number
			int slotIndex = nbtCompound.getByte( PrivateInventory.NBT_KEY_SLOT ) & 0xFF;

			// Validate the slot number
			if( ( slotIndex >= 0 ) && ( slotIndex < this.slots.length ) )
			{
				// Load the itemstack
				this.slots[slotIndex] = ItemStack.loadItemStackFromNBT( nbtCompound );
			}
		}
	}

	@Override
	public void markDirty()
	{
		if( this.receiver != null )
		{
			this.receiver.onInventoryChanged( this );
		}
	}

	@Override
	public void openInventory()
	{
	}

	public final void writeToNBT( final NBTTagCompound data, final String tagName )
	{
		// Ensure there is a data tag
		if( data == null )
		{
			return;
		}

		// Create the list
		NBTTagList invList = new NBTTagList();

		// Write each slot into the list
		for( int slotIndex = 0; slotIndex < this.slots.length; slotIndex++ )
		{
			// Ensure the slot has something to write
			if( this.slots[slotIndex] != null )
			{
				// Create the compound
				NBTTagCompound nbtCompound = new NBTTagCompound();

				// Set the slot number
				nbtCompound.setByte( PrivateInventory.NBT_KEY_SLOT, (byte)slotIndex );

				// Save the inventory
				this.slots[slotIndex].writeToNBT( nbtCompound );

				// Add to the list
				invList.appendTag( nbtCompound );
			}
		}

		// Write the list into the data
		if( invList.tagCount() > 0 )
		{
			data.setTag( tagName, invList );
		}
	}

	@Override
	public void setInventorySlotContents( final int slotId, final ItemStack itemStack )
	{
		// Is the stack size too large?
		if( ( itemStack != null ) && ( itemStack.stackSize > this.getInventoryStackLimit() ) )
		{
			itemStack.stackSize = this.getInventoryStackLimit();
		}

		this.slots[slotId] = itemStack;

		this.markDirty();
	}

	public void setReceiver( final IInventoryUpdateReceiver receiver )
	{
		this.receiver = receiver;
	}

}
