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
import thaumicenergistics.registries.ThEStrings;
import appeng.api.implementations.items.IStorageComponent;

public class ItemStorageComponent
	extends AbstractStorageBase
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
		return AbstractStorageBase.SIZES[itemStack.getItemDamage()];
	}

	@Override
	public IIcon getIconFromDamage( final int damage )
	{
		int index = MathHelper.clamp_int( damage, 0, AbstractStorageBase.SIZES.length - 1 );

		return this.icons[index];
	}

	@Override
	public EnumRarity getRarity( final ItemStack itemStack )
	{
		// Get the index based off of the meta data
		int index = MathHelper.clamp_int( itemStack.getItemDamage(), 0, AbstractStorageBase.RARITIES.length - 1 );

		// Return the rarity
		return AbstractStorageBase.RARITIES[index];
	}

	@Override
	public void getSubItems( final Item item, final CreativeTabs creativeTab, final List itemList )
	{
		for( int i = 0; i < AbstractStorageBase.SIZES.length; i++ )
		{
			// Skip the creative cell
			if( i == AbstractStorageBase.INDEX_CREATIVE )
			{
				continue;
			}

			itemList.add( new ItemStack( item, 1, i ) );
		}
	}

	@Override
	public String getUnlocalizedName()
	{
		return ThaumicEnergistics.MOD_ID + ".item.storage.component";
	}

	@Override
	public String getUnlocalizedName( final ItemStack itemStack )
	{
		switch ( itemStack.getItemDamage() )
		{
		case 0:
			return ThEStrings.Item_StorageComponent_1k.getUnlocalized();

		case 1:
			return ThEStrings.Item_StorageComponent_4k.getUnlocalized();

		case 2:
			return ThEStrings.Item_StorageComponent_16k.getUnlocalized();

		case 3:
			return ThEStrings.Item_StorageComponent_64k.getUnlocalized();

		default:
			return "";

		}
	}

	@Override
	public boolean isStorageComponent( final ItemStack itemStack )
	{
		return( itemStack.getItem() == this );
	}

	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		this.icons = new IIcon[AbstractStorageBase.SUFFIXES.length];

		for( int i = 0; i < AbstractStorageBase.SUFFIXES.length; i++ )
		{
			// Skip the creative cell
			if( i == AbstractStorageBase.INDEX_CREATIVE )
			{
				continue;
			}

			this.icons[i] = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":storage.component." + AbstractStorageBase.SUFFIXES[i] );
		}
	}

}
