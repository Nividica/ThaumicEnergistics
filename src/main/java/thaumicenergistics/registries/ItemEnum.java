package thaumicenergistics.registries;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.items.ItemAEPart;
import thaumicenergistics.items.ItemEssentiaCell;
import thaumicenergistics.items.ItemKnowledgeCore;
import thaumicenergistics.items.ItemMaterial;
import thaumicenergistics.items.ItemStorageCasing;
import thaumicenergistics.items.ItemStorageComponent;
import thaumicenergistics.items.ItemWirelessEssentiaTerminal;

public enum ItemEnum
{
		ITEM_AEPART ("part.base", new ItemAEPart()),
		ESSENTIA_CELL ("storage.essentia", new ItemEssentiaCell()),
		STORAGE_COMPONENT ("storage.component", new ItemStorageComponent()),
		STORAGE_CASING ("storage.casing", new ItemStorageCasing()),
		MATERIAL ("material", new ItemMaterial()),
		WIRELESS_TERMINAL ("wireless.essentia.terminal", new ItemWirelessEssentiaTerminal()),
		KNOWLEDGE_CORE ("knowledge.core", new ItemKnowledgeCore());

	/**
	 * Internal name of the item.
	 */
	private final String internalName;

	/**
	 * The actual item.
	 */
	private Item item;

	/**
	 * Cache of the enum values
	 */
	public static final ItemEnum[] VALUES = ItemEnum.values();

	private ItemEnum( final String internalName, final Item item )
	{
		this.internalName = internalName;

		this.item = item;

		this.item.setCreativeTab( ThaumicEnergistics.ThETab );
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

}
