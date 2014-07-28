package thaumicenergistics.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PrivateInventory implements IInventory
{

	public ItemStack[] slots;
	public String customName;
	private int stackLimit;
	private IInventoryUpdateReceiver receiver = null;

	public PrivateInventory(String customName, int size, int stackLimit )
	{
		this.slots = new ItemStack[size];
		this.customName = customName;
		this.stackLimit = stackLimit;
	}
	
	public PrivateInventory( String customName, int size, int stackLimit , IInventoryUpdateReceiver receiver )
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

		if ( this.slots[slotId] != null )
		{
			if ( this.slots[slotId].stackSize <= amount )
			{
				slotStack = this.slots[slotId];

				this.slots[slotId] = null;
			}
			else
			{
				ItemStack tmp = this.slots[slotId];

				slotStack = tmp.splitStack( amount );

				this.slots[slotId] = tmp;

				if ( this.slots[slotId].stackSize == 0 )
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

		if ( slotStack != null )
		{
			int stackLimit = this.getInventoryStackLimit();

			if ( stackLimit > slotStack.getMaxStackSize() )
			{
				stackLimit = slotStack.getMaxStackSize();
			}

			int addedAmount = ( slotStack.stackSize + amount );

			if ( addedAmount <= stackLimit )
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
		if ( this.receiver != null )
		{
			this.receiver.onInventoryChanged( this );
		}
	}

	@Override
	public void openInventory()
	{
	}

	public void readFromNBT( NBTTagList nbtList )
	{
		for( int i = 0; i < nbtList.tagCount(); i++ )
		{
			NBTTagCompound nbtCompound = nbtList.getCompoundTagAt( i );

			int slotId = nbtCompound.getByte( "Slot" ) & 0xFF;

			if ( ( slotId >= 0 ) && ( slotId < this.slots.length ) )
			{
				this.slots[slotId] = ItemStack.loadItemStackFromNBT( nbtCompound );
			}
		}
	}

	@Override
	public void setInventorySlotContents( int slotId, ItemStack itemStack )
	{
		if ( ( itemStack != null ) && ( itemStack.stackSize > this.getInventoryStackLimit() ) )
		{
			itemStack.stackSize = this.getInventoryStackLimit();
		}

		this.slots[slotId] = itemStack;

		this.markDirty();
	}

	public NBTTagList writeToNBT()
	{
		NBTTagList nbtList = new NBTTagList();

		for( int slotId = 0; slotId < this.slots.length; slotId++ )
		{
			if ( this.slots[slotId] != null )
			{
				NBTTagCompound nbtCompound = new NBTTagCompound();

				nbtCompound.setByte( "Slot", (byte) slotId );

				this.slots[slotId].writeToNBT( nbtCompound );

				nbtList.appendTag( nbtCompound );
			}
		}

		return nbtList;
	}
	
	public void setReceiver( IInventoryUpdateReceiver receiver )
	{
		this.receiver = receiver;
	}

}
