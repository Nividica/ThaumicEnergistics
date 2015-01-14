package thaumicenergistics.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.registries.ThEStrings;

public class ItemStorageCasing
	extends Item
{
	public ItemStorageCasing()
	{
		this.setMaxDamage( 0 );

		this.setHasSubtypes( false );
	}

	@Override
	public String getUnlocalizedName()
	{
		return ThEStrings.Item_EssentiaCellHousing.getUnlocalized();
	}

	@Override
	public String getUnlocalizedName( final ItemStack itemStack )
	{
		return ThEStrings.Item_EssentiaCellHousing.getUnlocalized();
	}

	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		this.itemIcon = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":essentia.cell.casing" );
	}

}
