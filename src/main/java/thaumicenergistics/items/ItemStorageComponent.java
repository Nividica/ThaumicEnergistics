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
	private static final EnumRarity[] RARITIES = { EnumRarity.uncommon, EnumRarity.uncommon, EnumRarity.rare, EnumRarity.epic };

	private IIcon[] icons;

	public ItemStorageComponent()
	{
		this.setMaxDamage( 0 );
		this.setHasSubtypes( true );
		this.setCreativeTab( ThaumicEnergistics.TETab );
	}

	@Override
	public int getBytes( ItemStack itemStack )
	{
		return ItemStorageBase.SIZES[itemStack.getItemDamage()];
	}

	@Override
	public IIcon getIconFromDamage( int damage )
	{
		int index = MathHelper.clamp_int( damage, 0, ItemStorageBase.SUFFIXES.length );

		return this.icons[index];
	}

	@Override
	public EnumRarity getRarity( ItemStack itemStack )
	{
		// Get the index based off of the meta data
		int index = MathHelper.clamp_int( itemStack.getItemDamage(), 0, ItemStorageComponent.RARITIES.length );

		// Return the rarity
		return ItemStorageComponent.RARITIES[index];
	}

	@Override
	public void getSubItems( Item item, CreativeTabs creativeTab, List itemList )
	{
		for( int i = 0; i < ItemStorageBase.SUFFIXES.length; i++ )
		{
			itemList.add( new ItemStack( item, 1, i ) );
		}
	}

	@Override
	public String getUnlocalizedName( ItemStack itemStack )
	{
		return ThaumicEnergistics.MOD_ID + ".item.storage.component." + ItemStorageBase.SUFFIXES[itemStack.getItemDamage()];
	}

	@Override
	public boolean isStorageComponent( ItemStack itemStack )
	{
		return( itemStack.getItem() == this );
	}

	@Override
	public void registerIcons( IIconRegister iconRegister )
	{
		this.icons = new IIcon[ItemStorageBase.SUFFIXES.length];

		for( int i = 0; i < ItemStorageBase.SUFFIXES.length; i++ )
		{
			this.icons[i] = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":storage.component." + ItemStorageBase.SUFFIXES[i] );
		}
	}

}
