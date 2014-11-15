package thaumicenergistics.api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

// TODO: Add getBlockTileEntity to API

public interface IItemDescription
{

	/**
	 * Gets the block of this item if it has one.
	 * 
	 * @return
	 */
	public Block getBlock();

	/**
	 * Gets the damage, or meta, value of the item.
	 * 
	 * @return
	 */
	public int getDamage();

	/**
	 * Gets the item.
	 * 
	 * @return
	 */
	public Item getItem();

	/**
	 * Gets a stack of the item.
	 * 
	 * @return
	 */
	public ItemStack getStack();

	/**
	 * Gets multiple stacks of the item.
	 * 
	 * @param amount
	 * @return
	 */
	public ItemStack getStacks( int amount );
}
