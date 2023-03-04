package thaumicenergistics.implementaion;

import thaumicenergistics.api.IThEItems;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.items.ItemMaterial.MaterialTypes;
import thaumicenergistics.common.storage.EnumEssentiaStorageTypes;

/**
 * Implements {@link IThEItems}.
 *
 * @author Nividica
 *
 */
class ThEItems extends IThEItems {

    ThEItems() {
        // Cores
        this.CoalescenceCore = new ThEItemDescription(MaterialTypes.COALESCENCE_CORE.getStack());
        this.DiffusionCore = new ThEItemDescription(MaterialTypes.DIFFUSION_CORE.getStack());

        // Cells
        this.EssentiaCell_1k = new ThEItemDescription(EnumEssentiaStorageTypes.Type_1K.getCell());
        this.EssentiaCell_4k = new ThEItemDescription(EnumEssentiaStorageTypes.Type_4K.getCell());
        this.EssentiaCell_16k = new ThEItemDescription(EnumEssentiaStorageTypes.Type_16K.getCell());
        this.EssentiaCell_64k = new ThEItemDescription(EnumEssentiaStorageTypes.Type_64K.getCell());
        this.EssentiaCell_Creative = new ThEItemDescription(EnumEssentiaStorageTypes.Type_Creative.getCell());
        this.EssentiaCell_Casing = new ThEItemDescription(ItemEnum.STORAGE_CASING.getStack());

        // Components
        this.EssentiaStorageComponent_1k = new ThEItemDescription(EnumEssentiaStorageTypes.Type_1K.getComponent(1));
        this.EssentiaStorageComponent_4k = new ThEItemDescription(EnumEssentiaStorageTypes.Type_4K.getComponent(1));
        this.EssentiaStorageComponent_16k = new ThEItemDescription(EnumEssentiaStorageTypes.Type_16K.getComponent(1));
        this.EssentiaStorageComponent_64k = new ThEItemDescription(EnumEssentiaStorageTypes.Type_64K.getComponent(1));

        // Misc
        this.IronGear = new ThEItemDescription(MaterialTypes.IRON_GEAR.getStack());
        this.WirelessEssentiaTerminal = new ThEItemDescription(ItemEnum.WIRELESS_TERMINAL.getStack());
        this.KnowledgeCore = new ThEItemDescription(ItemEnum.KNOWLEDGE_CORE.getStack());
        this.WandFocusAEWrench = new ThEItemDescription(ItemEnum.FOCUS_AEWRENCH.getStack());
        this.GolemWifiBackpack = new ThEItemDescription(ItemEnum.GOLEM_WIFI_BACKPACK.getStack());

        // Post-release
        this.CellMicroscope = new ThEItemDescription(ItemEnum.CELL_MICROSCOPE.getStack());
    }
}
