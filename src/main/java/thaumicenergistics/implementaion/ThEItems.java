package thaumicenergistics.implementaion;

import thaumicenergistics.api.IThEItems;
import thaumicenergistics.items.AbstractStorageBase;
import thaumicenergistics.items.ItemMaterial.MaterialTypes;
import thaumicenergistics.registries.ItemEnum;

class ThEItems
	extends IThEItems
{
	ThEItems()
	{
		this.CoalescenceCore = new ThEItemDescription( ItemEnum.MATERIAL.getItemStackWithDamage( MaterialTypes.COALESCENCE_CORE.getID() ) );
		this.DiffusionCore = new ThEItemDescription( ItemEnum.MATERIAL.getItemStackWithDamage( MaterialTypes.DIFFUSION_CORE.getID() ) );
		this.EssentiaCell_16k = new ThEItemDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( AbstractStorageBase.INDEX_16K ) );
		this.EssentiaCell_1k = new ThEItemDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( AbstractStorageBase.INDEX_1K ) );
		this.EssentiaCell_4k = new ThEItemDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( AbstractStorageBase.INDEX_4K ) );
		this.EssentiaCell_64k = new ThEItemDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( AbstractStorageBase.INDEX_64K ) );
		this.EssentiaCell_Casing = new ThEItemDescription( ItemEnum.STORAGE_CASING.getItemStackWithSize( 1 ) );
		this.EssentiaStorageComponent_16k = new ThEItemDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( AbstractStorageBase.INDEX_16K ) );
		this.EssentiaStorageComponent_1k = new ThEItemDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( AbstractStorageBase.INDEX_1K ) );
		this.EssentiaStorageComponent_4k = new ThEItemDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( AbstractStorageBase.INDEX_4K ) );
		this.EssentiaStorageComponent_64k = new ThEItemDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( AbstractStorageBase.INDEX_64K ) );
		this.IronGear = new ThEItemDescription( ItemEnum.MATERIAL.getItemStackWithDamage( MaterialTypes.IRON_GEAR.getID() ) );
		this.WirelessEssentiaTerminal = new ThEItemDescription( ItemEnum.WIRELESS_TERMINAL.getItemStackWithSize( 1 ) );
		this.KnowledgeCore = new ThEItemDescription( ItemEnum.KNOWLEDGE_CORE.getItemStackWithSize( 1 ) );
		this.WandFocusAEWrench = new ThEItemDescription( ItemEnum.FOCUS_AEWRENCH.getItemStackWithSize( 1 ) );
	}
}
