package thaumicenergistics.items;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;

public abstract class ItemStorageBase
	extends Item
{

	public static final int INDEX_1K = 0;

	public static final int INDEX_4K = 1;

	public static final int INDEX_16K = 2;

	public static final int INDEX_64K = 3;

	public static final int INDEX_CREATIVE = 4;

	protected static final String[] SUFFIXES = { "1k", "4k", "16k", "64k", "creative" };

	protected static final int[] SIZES = { 1024, 4096, 16348, 65536, 0 };

	protected static final EnumRarity[] RARITIES = { EnumRarity.uncommon, EnumRarity.uncommon, EnumRarity.rare, EnumRarity.rare, EnumRarity.epic };
}
