package thaumicenergistics.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class DistillationPatternHelper
{
	/**
	 * NBT Keys
	 */
	private static final String NBTKEY_AE_IN = "in",
					NBTKEY_AE_OUT = "out",
					NBTKEY_AE_ISCRAFTING = "crafting",
					NBTKEY_AE_CAN_SUB = "substitute";

	/**
	 * Output of the pattern.
	 * Must be ItemCraftingAspect patterns.
	 */
	public ItemStack output = null;

	/**
	 * Input of the pattern.
	 */
	public ItemStack input = null;

	/**
	 * Loads the values from the pattern
	 * 
	 * @param pattern
	 */
	public void loadFromPattern( final ItemStack pattern )
	{
		// Reset
		this.reset();

		if( !pattern.hasTagCompound() )
		{
			// Nothing to load
			return;
		}

		// Get the NBT tag
		NBTTagCompound data = pattern.getTagCompound();

		// Get the input and output list
		NBTTagList inTags = data.getTagList( NBTKEY_AE_IN, NBT.TAG_COMPOUND );
		NBTTagList outTags = data.getTagList( NBTKEY_AE_OUT, NBT.TAG_COMPOUND );

		// Empty check
		if( ( outTags.tagCount() < 1 ) || ( inTags.tagCount() < 1 ) )
		{
			// Nothing to load.
			return;
		}

		// Read the input and output
		this.output = ItemStack.loadItemStackFromNBT( outTags.getCompoundTagAt( 0 ) );
		this.input = ItemStack.loadItemStackFromNBT( inTags.getCompoundTagAt( 0 ) );

		// Null check
		if( ( this.input == null ) || ( this.output == null ) )
		{
			this.reset();
			return;
		}

	}

	/**
	 * Resets the helper.
	 */
	public void reset()
	{
		this.output = null;
		this.input = null;
	}

	public void writeToPattern( final ItemStack pattern )
	{
		// Check the input & output
		if( ( this.input == null ) || ( this.output == null ) )
		{
			// No input or output
			return;
		}

		// Create a new tag
		NBTTagCompound data = new NBTTagCompound();

		// Write the input
		NBTTagList inTags = new NBTTagList();
		inTags.appendTag( this.input.writeToNBT( new NBTTagCompound() ) );

		// Write the outputs
		NBTTagList outTags = new NBTTagList();
		outTags.appendTag( this.output.writeToNBT( new NBTTagCompound() ) );

		// Write the basics
		data.setBoolean( NBTKEY_AE_CAN_SUB, false );
		data.setBoolean( NBTKEY_AE_ISCRAFTING, false );

		// Write the lists
		data.setTag( NBTKEY_AE_IN, inTags );
		data.setTag( NBTKEY_AE_OUT, outTags );

		// Save into the item
		pattern.setTagCompound( data );

	}
}
