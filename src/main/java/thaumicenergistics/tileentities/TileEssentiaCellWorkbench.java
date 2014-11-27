package thaumicenergistics.tileentities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.container.ContainerEssentiaCellWorkbench;
import thaumicenergistics.inventory.HandlerItemEssentiaCell;
import thaumicenergistics.items.ItemEssentiaCell;
import thaumicenergistics.network.packet.client.PacketClientAspectSlot;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ISaveProvider;

public class TileEssentiaCellWorkbench
	extends TileEntity
	implements IInventory, ISaveProvider
{
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
	private final List<ContainerEssentiaCellWorkbench> openContainers = new ArrayList<ContainerEssentiaCellWorkbench>();

	public TileEssentiaCellWorkbench()
	{
	}

	/**
	 * Forces all containers to sync the cell itemstack
	 */
	private void forceContainerSyncCell()
	{
		for( ContainerEssentiaCellWorkbench container : this.openContainers )
		{
			container.onForceSyncCell();
		}
	}

	/**
	 * Notifies the open containers that the cell changed.
	 */
	private void onCellChanged()
	{
		for( ContainerEssentiaCellWorkbench container : this.openContainers )
		{
			container.onCellChanged();
		}
	}

	/**
	 * Workbench does not need ticks.
	 */
	@Override
	public boolean canUpdate()
	{
		return false;
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
		return true;
	}

	/**
	 * A client requests to add an aspect to the partition list.
	 * 
	 * @param aspect
	 */
	public boolean onClientRequestAddAspectToPartitionList( final EntityPlayer player, final Aspect aspect )
	{
		if( ( this.eCellHandler != null ) )
		{
			if( this.eCellHandler.addAspectToPartitionList( aspect ) )
			{
				// Re-send the list
				this.onClientRequestPartitionList( player );

				return true;
			}
		}

		return false;
	}

	/**
	 * A client requests all partition data be removed.
	 * 
	 * @param player
	 */
	public void onClientRequestClearPartitioning( final EntityPlayer player )
	{
		if( this.eCellHandler != null )
		{
			this.eCellHandler.clearPartitioning();

			// Re-send the list
			this.onClientRequestPartitionList( player );

			// Force containers to update the cell item.
			this.forceContainerSyncCell();
		}
	}

	/**
	 * A client requests the partition list.
	 * 
	 * @param player
	 */
	public void onClientRequestPartitionList( final EntityPlayer player )
	{
		List<Aspect> partitionList;

		if( ( this.eCellHandler != null ) )
		{
			partitionList = this.eCellHandler.getPartitionAspects();
		}
		else
		{
			partitionList = new ArrayList<Aspect>();
		}

		// Send to client
		new PacketClientAspectSlot().createFilterListUpdate( partitionList, player ).sendPacketToPlayer();
	}

	/**
	 * A client requests the cell partition list be set to match
	 * the contents of the cell.
	 * 
	 * @param player
	 */
	public void onClientRequestPartitionToContents( final EntityPlayer player )
	{
		if( this.eCellHandler != null )
		{
			this.eCellHandler.partitionToCellContents();

			// Re-send the list
			this.onClientRequestPartitionList( player );
		}
	}

	/**
	 * A client requests to remove an aspect from the partition list.
	 * 
	 * @param aspect
	 */
	public void onClientRequestRemoveAspectFromPartitionList( final EntityPlayer player, final Aspect aspect )
	{
		if( ( this.eCellHandler != null ) )
		{
			if( this.eCellHandler.removeAspectFromPartitionList( aspect ) )
			{
				// Re-send the list
				this.onClientRequestPartitionList( player );
			}
		}
	}

	/**
	 * A client requests to replace an aspect from the partition list.
	 * 
	 * @param aspect
	 */
	public void onClientRequestReplaceAspectFromPartitionList( final EntityPlayer player, final Aspect fromAspect, final Aspect toAspect )
	{
		if( this.eCellHandler != null )
		{
			if( this.eCellHandler.replaceAspectInPartitionList( fromAspect, toAspect ) )
			{
				// Re-send the list
				this.onClientRequestPartitionList( player );
			}
		}
	}

	@Override
	public void openInventory()
	{
		// Ignored
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
	public void registerContainer( final ContainerEssentiaCellWorkbench container )
	{
		if( !this.openContainers.contains( container ) )
		{
			this.openContainers.add( container );
		}

	}

	/**
	 * Removes a container.
	 * It will no longer receive notifications when the cell changes.
	 * 
	 * @param container
	 */
	public void removeContainer( final ContainerEssentiaCellWorkbench container )
	{
		this.openContainers.remove( container );
	}

	/**
	 * Called when the cell changes
	 */
	@Override
	public void saveChanges( final IMEInventory cellInventory )
	{
		// Mark the chunk as needing to be saved.( less invasive than this.markDirty() )
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
				this.onCellChanged();
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
