package thaumicenergistics.implementaion;

import thaumicenergistics.api.Parts;
import thaumicenergistics.registries.AEPartsEnum;

class ThEParts
	extends Parts
{

	ThEParts()
	{
		this.ArcaneCrafting_Terminal = new ThEDescription( AEPartsEnum.ArcaneCraftingTerminal.getStack() );
		this.Essentia_ExportBus = new ThEDescription( AEPartsEnum.EssentiaExportBus.getStack() );
		this.Essentia_ImportBus = new ThEDescription( AEPartsEnum.EssentiaImportBus.getStack() );
		this.Essentia_LevelEmitter = new ThEDescription( AEPartsEnum.EssentiaLevelEmitter.getStack() );
		this.Essentia_StorageBus = new ThEDescription( AEPartsEnum.EssentiaStorageBus.getStack() );
		this.Essentia_Terminal = new ThEDescription( AEPartsEnum.EssentiaTerminal.getStack() );
		this.VisRelay_Interface = new ThEDescription( AEPartsEnum.VisInterface.getStack() );
		this.Essentia_StorageMonitor = new ThEDescription( AEPartsEnum.EssentiaStorageMonitor.getStack() );
		this.Essentia_ConversionMonitor = new ThEDescription( AEPartsEnum.EssentiaConversionMonitor.getStack() );
	}

}
