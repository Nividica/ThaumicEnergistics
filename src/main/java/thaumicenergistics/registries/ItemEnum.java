package thaumicenergistics.registries;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.items.ItemAEPart;
import thaumicenergistics.items.ItemEssentiaCell;
import thaumicenergistics.items.ItemMaterial;
import thaumicenergistics.items.ItemStorageCasing;
import thaumicenergistics.items.ItemStorageComponent;

public enum ItemEnum
{
		PART_ITEM ("part.base", new ItemAEPart()),
		ESSENTIA_CELL ("storage.essentia", new ItemEssentiaCell()),
		STORAGE_COMPONENT ("storage.component", new ItemStorageComponent()),
		STORAGE_CASING ("storage.casing", new ItemStorageCasing()),
		MATERIAL ("material", new ItemMaterial());

	private final String internalName;
	private Item item;

	/**
	 * Cache of the enum values
	 */
	public static final ItemEnum[] VALUES = ItemEnum.values();

	private ItemEnum( final String internalName, final Item item )
	{
		this.internalName = internalName;

		this.item = item;

		this.item.setUnlocalizedName( ThaumicEnergistics.MOD_ID + "." + this.internalName );

		this.item.setCreativeTab( ThaumicEnergistics.ModTab );
	}

	public String getInternalName()
	{
		return this.internalName;
	}

	public Item getItem()
	{
		return this.item;
	}

	public ItemStack getItemStackWithDamage( final int damageValue )
	{
		return new ItemStack( this.item, 1, damageValue );
	}

	public ItemStack getItemStackWithSize( final int size )
	{
		return new ItemStack( this.item, size );
	}

	public String getStatName()
	{
		return StatCollector.translateToLocal( this.item.getUnlocalizedName() );
	}

}
