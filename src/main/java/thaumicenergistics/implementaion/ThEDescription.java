package thaumicenergistics.implementaion;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.api.IItemDescription;

public class ThEDescription
	implements IItemDescription
{
	private Block myBlock = null;
	private Item myItem = null;
	private int itemMeta = 0;

	ThEDescription( final Block block )
	{
		this( Item.getItemFromBlock( block ), 0 );
		this.myBlock = block;
	}

	ThEDescription( final Item item, final int meta )
	{
		this.myItem = item;
		this.itemMeta = meta;
	}

	ThEDescription( final ItemStack stack )
	{
		this( stack.getItem(), stack.getItemDamage() );
	}

	@Override
	public Block getBlock()
	{
		return this.myBlock;
	}

	@Override
	public int getDamage()
	{
		return this.itemMeta;
	}

	@Override
	public Item getItem()
	{
		return this.myItem;
	}

	@Override
	public ItemStack getStack()
	{
		return this.getStacks( 1 );
	}

	@Override
	public ItemStack getStacks( final int amount )
	{
		if( this.myItem != null )
		{
			return new ItemStack( this.myItem, amount, this.itemMeta );
		}
		else if( this.myBlock != null )
		{
			return new ItemStack( this.myBlock, amount, this.itemMeta );
		}

		return null;

	}

}
