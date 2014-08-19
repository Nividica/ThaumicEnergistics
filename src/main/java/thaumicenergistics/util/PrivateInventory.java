package thaumicenergistics.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PrivateInventory
	implements IInventory
{

	public ItemStack[] slots;
	public String customName;
	private int stackLimit;
	private IInventoryUpdateReceiver receiver = null;

	public PrivateInventory( String customName, int size, int stackLimit )
	{
		this.slots = new ItemStack[size];
		this.customName = customName;
		this.stackLimit = stackLimit;
	}

	public PrivateInventory( String customName, int size, int stackLimit, IInventoryUpdateReceiver receiver )
	{
		this( customName, size, stackLimit );

		this.receiver = receiver;
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public ItemStack decrStackSize( int slotId, int amount )
	{
		ItemStack slotStack = null;

		if( this.slots[slotId] != null )
		{
			if( this.slots[slotId].stackSize <= amount )
			{
				slotStack = this.slots[slotId];

				this.slots[slotId] = null;
			}
			else
			{
				ItemStack tmp = this.slots[slotId];

				slotStack = tmp.splitStack( amount );

				this.slots[slotId] = tmp;

				if( this.slots[slotId].stackSize == 0 )
				{
					this.slots[slotId] = null;
				}
			}

			this.markDirty();
		}

		return slotStack;
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
	public ItemStack getStackInSlot( int index )
	{
		return this.slots[index];
	}

	@Override
	public ItemStack getStackInSlotOnClosing( int slotId )
	{
		return this.slots[slotId];
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	public ItemStack incrStackSize( int slotId, int amount )
	{
		ItemStack slotStack = this.slots[slotId];
		ItemStack added = null;

		if( slotStack != null )
		{
			int stackLimit = this.getInventoryStackLimit();

			if( stackLimit > slotStack.getMaxStackSize() )
			{
				stackLimit = slotStack.getMaxStackSize();
			}

			int addedAmount = ( slotStack.stackSize + amount );

			if( addedAmount <= stackLimit )
			{
				added = slotStack.copy();

				added.stackSize = amount;

				slotStack.stackSize += added.stackSize;
			}
		}

		return added;
	}

	@Override
	public boolean isItemValidForSlot( int slotId, ItemStack itemStack )
	{
		// Assume it is not valid
		return false;
	}

	@Override
	public boolean isUseableByPlayer( EntityPlayer player )
	{
		return true;
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

	public final void loadFromNBT( NBTTagCompound data, String tagName )
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
			int slotIndex = nbtCompound.getByte( "Slot" ) & 0xFF;

			// Validate the slot number
			if( ( slotIndex >= 0 ) && ( slotIndex < this.slots.length ) )
			{
				// Load the itemstack
				this.slots[slotIndex] = ItemStack.loadItemStackFromNBT( nbtCompound );
			}
		}
	}

	@Override
	public void setInventorySlotContents( int slotId, ItemStack itemStack )
	{
		// Is the stack size to large?
		if( ( itemStack != null ) && ( itemStack.stackSize > this.getInventoryStackLimit() ) )
		{
			itemStack.stackSize = this.getInventoryStackLimit();
		}

		this.slots[slotId] = itemStack;

		this.markDirty();
	}

	public final void saveToNBT( NBTTagCompound data, String tagName )
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
				nbtCompound.setByte( "Slot", (byte)slotIndex );

				// Save the inventory
				this.slots[slotIndex].writeToNBT( nbtCompound );
				

				// Add to the list
				invList.appendTag( nbtCompound );
			}
		}

		// Write the list into the data
		data.setTag( tagName, invList );
	}

	public void setReceiver( IInventoryUpdateReceiver receiver )
	{
		this.receiver = receiver;
	}

}
