package thaumicenergistics.implementaion;

import thaumicenergistics.api.Parts;
import thaumicenergistics.registries.AEPartsEnum;

class TEParts
	extends Parts
{

	TEParts()
	{
		this.ArcaneCrafting_Terminal = new TEDescription( AEPartsEnum.ArcaneCraftingTerminal.getStack() );
		this.Essentia_ExportBus = new TEDescription( AEPartsEnum.EssentiaExportBus.getStack() );
		this.Essentia_ImportBus = new TEDescription( AEPartsEnum.EssentiaImportBus.getStack() );
		this.Essentia_LevelEmitter = new TEDescription( AEPartsEnum.EssentiaLevelEmitter.getStack() );
		this.Essentia_StorageBus = new TEDescription( AEPartsEnum.EssentiaStorageBus.getStack() );
		this.Essentia_Terminal = new TEDescription( AEPartsEnum.EssentiaTerminal.getStack() );
		this.VisRelay_Interface = new TEDescription( AEPartsEnum.VisInterface.getStack() );
	}

}
