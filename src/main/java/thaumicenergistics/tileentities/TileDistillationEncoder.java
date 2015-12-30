package thaumicenergistics.tileentities;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumicenergistics.items.ItemCraftingAspect;
import thaumicenergistics.util.IInventoryUpdateReceiver;
import thaumicenergistics.util.PrivateInventory;
import appeng.items.misc.ItemEncodedPattern;

public class TileDistillationEncoder
	extends TileEntity
	implements IInventoryUpdateReceiver
{
	/**
	 * NBT Keys
	 */
	private static String NBTKEY_INVENTORY = "inventory";

	/**
	 * Slot counts
	 */
	public static int SLOT_SOURCE_ASPECTS_COUNT = 6,
					SLOT_SAVED_ASPECT_COUNT = 1,
					SLOT_PATTERNS_COUNT = 2,
					SLOT_INPUT_ITEM_COUNT = 1,
					SLOT_TOTAL_COUNT = SLOT_INPUT_ITEM_COUNT + SLOT_SOURCE_ASPECTS_COUNT + SLOT_SAVED_ASPECT_COUNT + SLOT_PATTERNS_COUNT;
	/**
	 * Slot ID's
	 */
	public static int SLOT_SOURCE_ITEM = 0,
					SLOT_EMPTY_PATTERNS = 1,
					SLOT_ENCODED_PATTERN = 2,
					SLOT_SOURCE_ASPECTS = 3,
					SLOT_SAVED_ASPECT = SLOT_SOURCE_ASPECTS + SLOT_SOURCE_ASPECTS_COUNT;

	/**
	 * Stores the inventory for this tile.
	 */
	private PrivateInventory internalInventory = new PrivateInventory( "distillation.inscriber", SLOT_TOTAL_COUNT, 64, this )
	{
		@Override
		public boolean isItemValidForSlot( final int slotId, final ItemStack itemStack )
		{
			// Can always clear a slot
			if( itemStack == null )
			{
				return true;
			}

			// Empty pattern slot?
			if( slotId == SLOT_EMPTY_PATTERNS )
			{
				return( itemStack.getItem() instanceof ItemEncodedPattern );
			}

			// Encoded pattern slot?
			if( slotId == SLOT_ENCODED_PATTERN )
			{
				return( itemStack.getItem() instanceof ItemEncodedPattern );
			}

			// Aspect slot?
			if( ( slotId >= SLOT_SOURCE_ASPECTS ) && ( slotId <= SLOT_SAVED_ASPECT ) )
			{
				return( itemStack.getItem() instanceof ItemCraftingAspect );
			}

			return true;
		}
	};

	/**
	 * Default constructor.
	 */
	public TileDistillationEncoder()
	{
	}

	/**
	 * Gets the inscriber's inventory.
	 * 
	 * @return
	 */
	public IInventory getInventory()
	{
		return this.internalInventory;
	}

	/**
	 * True if there is a pattern to encode onto.
	 * 
	 * @return
	 */
	public boolean hasPattern()
	{
		// Is there anything in the pattern slots?
		return( ( this.internalInventory.slots[SLOT_ENCODED_PATTERN] != null )
		|| ( this.internalInventory.slots[SLOT_EMPTY_PATTERNS] != null ) );

	}

	/**
	 * Called when the internal inventory changes
	 */
	@Override
	public void onInventoryChanged( final IInventory sourceInventory )
	{
		// Mark the tile dirty
		this.markDirty();
	}

	/**
	 * Read tile state from NBT.
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Has saved inventory?
		if( data.hasKey( NBTKEY_INVENTORY ) )
		{
			this.internalInventory.readFromNBT( data, NBTKEY_INVENTORY );
		}

	}

	/**
	 * Write tile state to NBT.
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Call super
		super.writeToNBT( data );

		// Write the inventory
		this.internalInventory.writeToNBT( data, NBTKEY_INVENTORY );

	}
}
