package thaumicenergistics.implementaion;

import thaumicenergistics.api.IThEParts;
import thaumicenergistics.common.registries.AEPartsEnum;

class ThEParts
	extends IThEParts
{

	ThEParts()
	{
		this.ArcaneCrafting_Terminal = new ThEItemDescription( AEPartsEnum.ArcaneCraftingTerminal.getStack() );
		this.Essentia_ExportBus = new ThEItemDescription( AEPartsEnum.EssentiaExportBus.getStack() );
		this.Essentia_ImportBus = new ThEItemDescription( AEPartsEnum.EssentiaImportBus.getStack() );
		this.Essentia_LevelEmitter = new ThEItemDescription( AEPartsEnum.EssentiaLevelEmitter.getStack() );
		this.Essentia_StorageBus = new ThEItemDescription( AEPartsEnum.EssentiaStorageBus.getStack() );
		this.Essentia_Terminal = new ThEItemDescription( AEPartsEnum.EssentiaTerminal.getStack() );
		this.VisRelay_Interface = new ThEItemDescription( AEPartsEnum.VisInterface.getStack() );
		this.Essentia_StorageMonitor = new ThEItemDescription( AEPartsEnum.EssentiaStorageMonitor.getStack() );
		this.Essentia_ConversionMonitor = new ThEItemDescription( AEPartsEnum.EssentiaConversionMonitor.getStack() );
	}

}
