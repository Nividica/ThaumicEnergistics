package thaumicenergistics.common.items;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.storage.EnumEssentiaStorageTypes;
import appeng.api.implementations.items.IStorageComponent;

/**
 * {@link ItemEssentiaCell} storage components.
 * 
 * @author Nividica
 * 
 */
public class ItemStorageComponent
	extends Item
	implements IStorageComponent
{

	/**
	 * Component icons.
	 */
	private IIcon[] icons;

	public ItemStorageComponent()
	{
		// No damage
		this.setMaxDamage( 0 );

		// Has subtypes
		this.setHasSubtypes( true );

		// Goes in ThE's creative tab.
		this.setCreativeTab( ThaumicEnergistics.ThETab );
	}

	@Override
	public int getBytes( final ItemStack itemStack )
	{
		return EnumEssentiaStorageTypes.fromIndex[itemStack.getItemDamage()].capacity;
	}

	@Override
	public IIcon getIconFromDamage( final int damage )
	{
		// Return icon
		return this.icons[damage];
	}

	@Override
	public EnumRarity getRarity( final ItemStack itemStack )
	{
		// Return the rarity
		return EnumEssentiaStorageTypes.fromIndex[itemStack.getItemDamage()].rarity;
	}

	@Override
	public void getSubItems( final Item item, final CreativeTabs creativeTab, final List itemList )
	{
		for( EnumEssentiaStorageTypes type : EnumEssentiaStorageTypes.fromIndex )
		{
			// Skip the creative cell, it has no component
			if( type == EnumEssentiaStorageTypes.Type_Creative )
			{
				continue;
			}
			itemList.add( type.getComponent( 1 ) );

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
		return EnumEssentiaStorageTypes.fromIndex[itemStack.getItemDamage()].componentName.getUnlocalized();
	}

	@Override
	public boolean isStorageComponent( final ItemStack itemStack )
	{
		return( itemStack.getItem() == this );
	}

	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		// Create the icon array
		this.icons = new IIcon[EnumEssentiaStorageTypes.fromIndex.length];

		// Add each type
		for( int i = 0; i < this.icons.length; i++ )
		{
			// Skip creative
			if( i == EnumEssentiaStorageTypes.Type_Creative.index )
			{
				this.icons[i] = null;
			}
			else
			{
				this.icons[i] = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":storage.component." +
								EnumEssentiaStorageTypes.fromIndex[i].suffix );
			}
		}
	}

}
