package thaumicenergistics.implementaion;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.api.IItemDescription;

public class TEDescription
	implements IItemDescription
{
	private Block myBlock = null;
	private Item myItem = null;
	private int itemMeta = 0;

	TEDescription( final Block block )
	{
		this( Item.getItemFromBlock( block ), 0 );
		this.myBlock = block;
	}

	TEDescription( final Item item, final int meta )
	{
		this.myItem = item;
		this.itemMeta = meta;
	}

	TEDescription( final ItemStack stack )
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
		if( this.myItem == null )
		{
			return null;
		}

		return new ItemStack( this.myItem, amount, this.itemMeta );
	}

}
