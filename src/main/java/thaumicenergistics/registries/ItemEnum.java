package thaumicenergistics.registries;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.items.*;

public enum ItemEnum
{
		ITEM_AEPART ("part.base", new ItemAEPart()),
		ESSENTIA_CELL ("storage.essentia", new ItemEssentiaCell()),
		STORAGE_COMPONENT ("storage.component", new ItemStorageComponent()),
		STORAGE_CASING ("storage.casing", new ItemStorageCasing()),
		MATERIAL ("material", new ItemMaterial()),
		WIRELESS_TERMINAL ("wireless.essentia.terminal", new ItemWirelessEssentiaTerminal()),
		KNOWLEDGE_CORE ("knowledge.core", new ItemKnowledgeCore()),
		FOCUS_AEWRENCH ("focus.aewrench", new ItemFocusAEWrench()),
		CRAFTING_ASPECT ("crafting.aspect", new ItemCraftingAspect());

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

	/**
	 * Gets an item stack of size 1 with the specified damage value.
	 * 
	 * @param damageValue
	 * @return
	 */
	public ItemStack getDMGStack( final int damageValue )
	{
		return this.getDMGStack( damageValue, 1 );
	}

	/**
	 * Gets an item stack of the specified size and damage value.
	 * 
	 * @param damageValue
	 * @param size
	 * @return
	 */
	public ItemStack getDMGStack( final int damageValue, final int size )
	{
		return new ItemStack( this.item, size, damageValue );
	}

	public String getInternalName()
	{
		return this.internalName;
	}

	public Item getItem()
	{
		return this.item;
	}

	/**
	 * Gets an item stack of size 1.
	 * 
	 * @return
	 */
	public ItemStack getStack()
	{
		return this.getStack( 1 );
	}

	/**
	 * Gets an item stack of the specified size.
	 * 
	 * @param size
	 * @return
	 */
	public ItemStack getStack( final int size )
	{
		return new ItemStack( this.item, size );
	}

}
