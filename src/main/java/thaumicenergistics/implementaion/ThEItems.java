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
		this.CoalescenceCore = new ThEItemDescription( MaterialTypes.COALESCENCE_CORE.getStack() );
		this.DiffusionCore = new ThEItemDescription( MaterialTypes.DIFFUSION_CORE.getStack() );
		this.EssentiaCell_16k = new ThEItemDescription( ItemEnum.ESSENTIA_CELL.getDMGStack( AbstractStorageBase.INDEX_16K ) );
		this.EssentiaCell_1k = new ThEItemDescription( ItemEnum.ESSENTIA_CELL.getDMGStack( AbstractStorageBase.INDEX_1K ) );
		this.EssentiaCell_4k = new ThEItemDescription( ItemEnum.ESSENTIA_CELL.getDMGStack( AbstractStorageBase.INDEX_4K ) );
		this.EssentiaCell_64k = new ThEItemDescription( ItemEnum.ESSENTIA_CELL.getDMGStack( AbstractStorageBase.INDEX_64K ) );
		this.EssentiaCell_Casing = new ThEItemDescription( ItemEnum.STORAGE_CASING.getStack() );
		this.EssentiaStorageComponent_16k = new ThEItemDescription( ItemEnum.STORAGE_COMPONENT.getDMGStack( AbstractStorageBase.INDEX_16K ) );
		this.EssentiaStorageComponent_1k = new ThEItemDescription( ItemEnum.STORAGE_COMPONENT.getDMGStack( AbstractStorageBase.INDEX_1K ) );
		this.EssentiaStorageComponent_4k = new ThEItemDescription( ItemEnum.STORAGE_COMPONENT.getDMGStack( AbstractStorageBase.INDEX_4K ) );
		this.EssentiaStorageComponent_64k = new ThEItemDescription( ItemEnum.STORAGE_COMPONENT.getDMGStack( AbstractStorageBase.INDEX_64K ) );
		this.IronGear = new ThEItemDescription( MaterialTypes.IRON_GEAR.getStack() );
		this.WirelessEssentiaTerminal = new ThEItemDescription( ItemEnum.WIRELESS_TERMINAL.getStack() );
		this.KnowledgeCore = new ThEItemDescription( ItemEnum.KNOWLEDGE_CORE.getStack() );
		this.WandFocusAEWrench = new ThEItemDescription( ItemEnum.FOCUS_AEWRENCH.getStack() );
	}
}
