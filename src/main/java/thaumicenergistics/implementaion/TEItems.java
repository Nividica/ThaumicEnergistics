package thaumicenergistics.implementaion;

import thaumicenergistics.api.Items;
import thaumicenergistics.items.ItemMaterial.MaterialTypes;
import thaumicenergistics.items.ItemStorageBase;
import thaumicenergistics.registries.ItemEnum;

class TEItems
	extends Items
{
	TEItems()
	{
		this.CoalescenceCore = new TEDescription( ItemEnum.MATERIAL.getItemStackWithDamage( MaterialTypes.COALESCENCE_CORE.getID() ) );
		this.DiffusionCore = new TEDescription( ItemEnum.MATERIAL.getItemStackWithDamage( MaterialTypes.DIFFUSION_CORE.getID() ) );
		this.EssentiaCell_16k = new TEDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_16K ) );
		this.EssentiaCell_1k = new TEDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_1K ) );
		this.EssentiaCell_4k = new TEDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_4K ) );
		this.EssentiaCell_64k = new TEDescription( ItemEnum.ESSENTIA_CELL.getItemStackWithDamage( ItemStorageBase.INDEX_64K ) );
		this.EssentiaCell_Casing = new TEDescription( ItemEnum.STORAGE_CASING.getItemStackWithSize( 1 ) );
		this.EssentiaStorageComponent_16k = new TEDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_16K ) );
		this.EssentiaStorageComponent_1k = new TEDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_1K ) );
		this.EssentiaStorageComponent_4k = new TEDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_4K ) );
		this.EssentiaStorageComponent_64k = new TEDescription( ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( ItemStorageBase.INDEX_64K ) );
		this.IronGear = new TEDescription( ItemEnum.MATERIAL.getItemStackWithDamage( MaterialTypes.IRON_GEAR.getID() ) );
	}
}
