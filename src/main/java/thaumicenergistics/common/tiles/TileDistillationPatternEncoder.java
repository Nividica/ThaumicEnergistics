package thaumicenergistics.common.tiles;

import java.util.ArrayList;
import appeng.api.AEApi;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumicenergistics.common.tiles.abstraction.ThETileInventory;

/**
 * Encodes recipes whose result is essentia.
 *
 * @author Nividica
 *
 */
public class TileDistillationPatternEncoder
	extends ThETileInventory
{
	/**
	 * NBT Keys
	 */
	private static String NBTKEY_INVENTORY = "inventory";

	/**
	 * Slot counts
	 */
	public static int SLOT_PATTERNS_COUNT = 2,
					SLOT_SOURCE_ITEM_COUNT = 1,
					SLOT_TOTAL_COUNT = SLOT_SOURCE_ITEM_COUNT + SLOT_PATTERNS_COUNT;
	/**
	 * Slot ID's
	 */
	public static int SLOT_SOURCE_ITEM = 0,
					SLOT_BLANK_PATTERNS = 1,
					SLOT_ENCODED_PATTERN = 2;

	/**
	 * Default constructor.
	 */
	public TileDistillationPatternEncoder()
	{
		super( "distillation.inscriber", SLOT_TOTAL_COUNT, 64 );
	}

	/**
	 * Does not need ticks.
	 */
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	/**
	 * Returns a list of items to drop when broken.
	 *
	 * @return
	 */
	public ArrayList<ItemStack> getDrops( final ArrayList<ItemStack> drops )
	{
		// Add encoded
		if( this.internalInventory.getHasStack( SLOT_ENCODED_PATTERN ) )
		{
			drops.add( this.internalInventory.getStackInSlot( SLOT_ENCODED_PATTERN ) );
		}

		// Add blank
		if( this.internalInventory.getHasStack( SLOT_BLANK_PATTERNS ) )
		{
			drops.add( this.internalInventory.getStackInSlot( SLOT_BLANK_PATTERNS ) );
		}

		return drops;
	}

	/**
	 * True if there is a pattern to encode onto.
	 *
	 * @return
	 */
	public boolean hasPatterns()
	{
		// Is there anything in the pattern slots?
		return this.internalInventory.getHasStack( SLOT_ENCODED_PATTERN ) || this.internalInventory.getHasStack( SLOT_BLANK_PATTERNS );

	}

	@Override
	public boolean isItemValidForSlot( final int slotId, final ItemStack itemStack )
	{
		// Can always clear a slot
		if( itemStack == null )
		{
			return true;
		}

		// Empty pattern slot?
		if( slotId == SLOT_BLANK_PATTERNS )
		{
			return AEApi.instance().definitions().materials().blankPattern().isSameAs( itemStack );
		}

		// Encoded pattern slot?
		if( slotId == SLOT_ENCODED_PATTERN )
		{
			return AEApi.instance().definitions().items().encodedPattern().isSameAs( itemStack );
		}

		return true;
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
