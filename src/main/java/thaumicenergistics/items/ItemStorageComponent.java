package thaumicenergistics.items;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import thaumicenergistics.ThaumicEnergistics;
import appeng.api.implementations.items.IStorageComponent;

public class ItemStorageComponent
	extends ItemStorageBase
	implements IStorageComponent
{

	private IIcon[] icons;

	public ItemStorageComponent()
	{
		this.setMaxDamage( 0 );
		this.setHasSubtypes( true );
		this.setCreativeTab( ThaumicEnergistics.ThETab );
	}

	@Override
	public int getBytes( final ItemStack itemStack )
	{
		return ItemStorageBase.SIZES[itemStack.getItemDamage()];
	}

	@Override
	public IIcon getIconFromDamage( final int damage )
	{
		int index = MathHelper.clamp_int( damage, 0, ItemStorageBase.SUFFIXES.length );

		return this.icons[index];
	}

	@Override
	public EnumRarity getRarity( final ItemStack itemStack )
	{
		// Get the index based off of the meta data
		int index = MathHelper.clamp_int( itemStack.getItemDamage(), 0, ItemStorageBase.RARITIES.length );

		// Return the rarity
		return ItemStorageBase.RARITIES[index];
	}

	@Override
	public void getSubItems( final Item item, final CreativeTabs creativeTab, final List itemList )
	{
		for( int i = 0; i < ItemStorageBase.SUFFIXES.length; i++ )
		{
			// Skip the creative cell
			if( i == ItemStorageBase.INDEX_CREATIVE )
			{
				continue;
			}

			itemList.add( new ItemStack( item, 1, i ) );
		}
	}

	@Override
	public String getUnlocalizedName( final ItemStack itemStack )
	{
		return ThaumicEnergistics.MOD_ID + ".item.storage.component." + ItemStorageBase.SUFFIXES[itemStack.getItemDamage()];
	}

	@Override
	public boolean isStorageComponent( final ItemStack itemStack )
	{
		return( itemStack.getItem() == this );
	}

	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		this.icons = new IIcon[ItemStorageBase.SUFFIXES.length];

		for( int i = 0; i < ItemStorageBase.SUFFIXES.length; i++ )
		{
			this.icons[i] = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":storage.component." + ItemStorageBase.SUFFIXES[i] );
		}
	}

}
