package thaumicenergistics.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.registries.ItemEnum;

public class ItemStorageCasing
	extends Item
{
	public ItemStorageCasing()
	{
		this.setMaxDamage( 0 );

		this.setHasSubtypes( false );

		//ThaumcraftApi.registerObjectTag( new ItemStack( this ), new AspectList().add( Aspect.MECHANISM, 5 ) );
	}

	@Override
	public String getUnlocalizedName( ItemStack itemStack )
	{
		return ThaumicEnergistics.MODID + ".item." + ItemEnum.STORAGE_CASING.getInternalName();
	}

	@Override
	public void registerIcons( IIconRegister iconRegister )
	{
		this.itemIcon = iconRegister.registerIcon( ThaumicEnergistics.MODID + ":essentia.cell.casing" );
	}

}
