package thaumicenergistics.implementaion;

import thaumicenergistics.api.Items;
import thaumicenergistics.items.ItemMaterial.MaterialTypes;
import thaumicenergistics.items.ItemStorageBase;
import thaumicenergistics.registries.ItemEnum;

class ThEItems
	extends Items
{
	ThEItems()
	{
		this.CoalescenceCore = new ThEDescription( ItemEnum.MATERIAL.getItemStackWithDamage( MaterialTypes.COALESCENCE_CORE.getID() ) );
		this.DiffusionCore = new ThEDescription( ItemEnum.MATERIAL.getItemStackWithDamage( MaterialTypes.DIFFUSION_CORE.getID() ) );
		this.EssentiaCell_16k = new ThEDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_16K ) );
		this.EssentiaCell_1k = new ThEDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_1K ) );
		this.EssentiaCell_4k = new ThEDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_4K ) );
		this.EssentiaCell_64k = new ThEDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_64K ) );
		this.EssentiaCell_Casing = new ThEDescription( ItemEnum.STORAGE_CASING.getItemStackWithSize( 1 ) );
		this.EssentiaStorageComponent_16k = new ThEDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_16K ) );
		this.EssentiaStorageComponent_1k = new ThEDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_1K ) );
		this.EssentiaStorageComponent_4k = new ThEDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_4K ) );
		this.EssentiaStorageComponent_64k = new ThEDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_64K ) );
		this.IronGear = new ThEDescription( ItemEnum.MATERIAL.getItemStackWithDamage( MaterialTypes.IRON_GEAR.getID() ) );
		this.WirelessEssentiaTerminal = new ThEDescription( ItemEnum.WIRELESS_TERMINAL.getItemStackWithSize( 1 ) );
		this.KnowledgeCore = new ThEDescription( ItemEnum.KNOWLEDGE_CORE.getItemStackWithSize( 1 ) );
	}
}
