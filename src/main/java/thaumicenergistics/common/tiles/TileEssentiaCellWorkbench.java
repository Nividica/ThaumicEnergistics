package thaumicenergistics.common.tiles;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.container.ContainerEssentiaCellWorkbench;
import thaumicenergistics.common.inventory.HandlerItemEssentiaCell;
import thaumicenergistics.common.items.ItemEssentiaCell;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.common.utils.ThEUtils;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ISaveProvider;

public class TileEssentiaCellWorkbench
	extends TileEntity
	implements IInventory, ISaveProvider
{
	/**
	 * NBT Keys
	 */
	private static String NBT_KEY_CELL = "EssentiaCell";

	/**
	 * The stored essentia cell.
	 */
	private ItemStack eCell = null;

	/**
	 * Cell handler
	 */
	private HandlerItemEssentiaCell eCellHandler = null;

	/**
	 * List of containers that are open.
	 */
	private final List<ContainerEssentiaCellWorkbench> listeners = new ArrayList<ContainerEssentiaCellWorkbench>();

	public TileEssentiaCellWorkbench()
	{
	}

	/**
	 * Notifies listeners that the partition list has changed.
	 */
	private void notifyListenersOfPartitionChange()
	{
		ArrayList<Aspect> partitionList = this.getPartitionList();

		// Update each listener
		for( ContainerEssentiaCellWorkbench container : this.listeners )
		{
			container.onPartitionChanged( partitionList );
		}
	}

	/**
	 * Attempts to add an aspect to the partition.
	 * 
	 * @param aspect
	 */
	public boolean addAspectToPartition( final Aspect aspect )
	{
		if( ( this.eCellHandler != null ) )
		{
			if( this.eCellHandler.addAspectToPartitionList( aspect ) )
			{
				// Update listeners
				this.notifyListenersOfPartitionChange();
				return true;
			}
		}

		return false;
	}

	/**
	 * Workbench does not need ticks.
	 */
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	/**
	 * Clears all partitioning.
	 * 
	 * @param player
	 */
	public void clearAllPartitioning()
	{
		// Ensure there is a handler
		if( this.eCellHandler != null )
		{
			// Clear the partitioning
			this.eCellHandler.clearPartitioning();

			// Update listeners
			this.notifyListenersOfPartitionChange();
		}
	}

	@Override
	public void closeInventory()
	{
		// Ignored
	}

	/**
	 * Gets and removes the cell.
	 */
	@Override
	public ItemStack decrStackSize( final int slotIndex, final int amount )
	{
		ItemStack rtn = null;

		if( this.hasEssentiaCell() )
		{
			// Get the cell
			rtn = this.eCell;

			// Null the cell
			this.setInventorySlotContents( slotIndex, null );
		}
		return rtn;
	}

	@Override
	public String getInventoryName()
	{
		return ThaumicEnergistics.MOD_ID + ".essentia.cell.workbench.inventory";
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	/**
	 * Gets the partition list.
	 * 
	 * @return
	 */
	public ArrayList<Aspect> getPartitionList()
	{
		ArrayList<Aspect> partitionList;

		// Ensure there is a handler
		if( ( this.eCellHandler != null ) )
		{
			// Get the partition list
			partitionList = this.eCellHandler.getPartitionAspects();
		}
		else
		{
			// Create an empty list
			partitionList = new ArrayList<Aspect>();
		}

		return partitionList;
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	/**
	 * Gets the cell.
	 */
	@Override
	public ItemStack getStackInSlot( final int slotIndex )
	{
		return this.eCell;
	}

	/**
	 * Gets the cell.
	 */
	@Override
	public ItemStack getStackInSlotOnClosing( final int slotIndex )
	{
		return this.eCell;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	/**
	 * Checks if the workbench has a cell.
	 * 
	 * @return
	 */
	public boolean hasEssentiaCell()
	{
		return( this.eCell != null );
	}

	/**
	 * Ensures the stack is an essentia cell.
	 */
	@Override
	public boolean isItemValidForSlot( final int slotIndex, final ItemStack stack )
	{
		return( ( stack == null ) || ( stack.getItem() instanceof ItemEssentiaCell ) );
	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer player )
	{
		return ThEUtils.canPlayerInteractWith( player, this );
	}

	@Override
	public void openInventory()
	{
		// Ignored
	}

	/**
	 * Partitions the cell be set to the contents of the cell.
	 * 
	 * @param player
	 */
	public void partitionToCellContents()
	{
		// Ensure there is a handler
		if( this.eCellHandler != null )
		{
			// Partition the cell
			this.eCellHandler.partitionToCellContents();

			// Update listeners
			this.notifyListenersOfPartitionChange();
		}
	}

	/**
	 * Loads the workbench.
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Is there a cell to read?
		if( data.hasKey( TileEssentiaCellWorkbench.NBT_KEY_CELL ) )
		{
			// Read the cell
			this.setInventorySlotContents( 0, ItemStack.loadItemStackFromNBT( data.getCompoundTag( TileEssentiaCellWorkbench.NBT_KEY_CELL ) ) );
		}
	}

	/**
	 * Registers a container.
	 * The container will be notified when the cell changes.
	 * 
	 * @param container
	 */
	public void registerListener( final ContainerEssentiaCellWorkbench container )
	{
		if( !this.listeners.contains( container ) )
		{
			this.listeners.add( container );
		}

	}

	/**
	 * Removes an aspect from the partition.
	 * 
	 * @param aspect
	 */
	public void removeAspectFromPartition( final Aspect aspect )
	{
		// Ensure there is a handler
		if( ( this.eCellHandler != null ) )
		{
			// Remove from the partition list
			if( this.eCellHandler.removeAspectFromPartitionList( aspect ) )
			{
				// Update listeners
				this.notifyListenersOfPartitionChange();
			}
		}
	}

	/**
	 * Removes a container.
	 * It will no longer receive notifications when the cell changes.
	 * 
	 * @param container
	 */
	public void removeListener( final ContainerEssentiaCellWorkbench container )
	{
		this.listeners.remove( container );
	}

	/**
	 * Called when the cell changes
	 */
	@Override
	public void saveChanges( final IMEInventory cellInventory )
	{
		// Mark the chunk as needing to be saved.
		// Much less invasive than markDirty(), which issues onNeighborChanged events to a 12 block radius...
		this.worldObj.markTileEntityChunkModified( this.xCoord, this.yCoord, this.zCoord, this );
	}

	/**
	 * Sets the stored cell
	 * 
	 * @param cell
	 */
	@Override
	public void setInventorySlotContents( final int slotIndex, final ItemStack stack )
	{
		if( this.isItemValidForSlot( slotIndex, stack ) )
		{
			// Set the cell
			this.eCell = stack;

			if( EffectiveSide.isServerSide() )
			{
				if( stack == null )
				{
					// Null the handler
					this.eCellHandler = null;
				}
				else
				{
					// Get the handler
					this.eCellHandler = new HandlerItemEssentiaCell( stack, this );
				}

				// Update containers
				this.notifyListenersOfPartitionChange();
			}
		}
	}

	/**
	 * Replaces one aspect with another.
	 * 
	 * @param aspect
	 */
	public void swapPartitionedAspect( final Aspect fromAspect, final Aspect toAspect )
	{
		// Ensure there is a handler
		if( this.eCellHandler != null )
		{
			// Replace the aspect
			if( this.eCellHandler.replaceAspectInPartitionList( fromAspect, toAspect ) )
			{
				// Update listeners
				this.notifyListenersOfPartitionChange();
			}
		}
	}

	/**
	 * Saves the workbench.
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Call super
		super.writeToNBT( data );

		// Is there a cell?
		if( this.hasEssentiaCell() )
		{
			// Write the cell data
			NBTTagCompound cellData = this.eCell.writeToNBT( new NBTTagCompound() );

			// Add the cell data
			data.setTag( TileEssentiaCellWorkbench.NBT_KEY_CELL, cellData );
		}
	}

}
