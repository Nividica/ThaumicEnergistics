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

public class ItemStorageComponent extends Item implements IStorageComponent
{
	public final String[] suffixes = { "1k", "4k", "16k", "64k" };
	public final int[] size = { 1024, 4096, 16384, 65536 };

	private IIcon[] icons;

	public ItemStorageComponent()
	{
		this.setMaxDamage( 0 );
		this.setHasSubtypes( true );
		this.setCreativeTab( ThaumicEnergistics.ModTab );
	}

	@Override
	public int getBytes( ItemStack itemStack )
	{
		return this.size[itemStack.getItemDamage()];
	}

	@Override
	public IIcon getIconFromDamage( int damage )
	{
		int index = MathHelper.clamp_int( damage, 0, this.suffixes.length );

		return this.icons[index];
	}

	@Override
	public EnumRarity getRarity( ItemStack itemStack )
	{
		return EnumRarity.rare;
	}

	@Override
	public void getSubItems( Item item, CreativeTabs creativeTab, List itemList )
	{
		for( int i = 0; i < this.suffixes.length; i++ )
		{
			itemList.add( new ItemStack( item, 1, i ) );
		}
	}

	@Override
	public String getUnlocalizedName( ItemStack itemStack )
	{
		return ThaumicEnergistics.MOD_ID + ".item.storage.component." + this.suffixes[itemStack.getItemDamage()];
	}

	@Override
	public boolean isStorageComponent( ItemStack itemStack )
	{
		return ( itemStack.getItem() == this );
	}

	@Override
	public void registerIcons( IIconRegister iconRegister )
	{
		this.icons = new IIcon[this.suffixes.length];

		for( int i = 0; i < this.suffixes.length; i++ )
		{
			this.icons[i] = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":storage.component." + this.suffixes[i] );
		}
	}

}
