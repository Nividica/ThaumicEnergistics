package thaumicenergistics.implementaion;

import thaumicenergistics.api.Blocks;
import thaumicenergistics.registries.BlockEnum;

class ThEBlocks
	extends Blocks
{
	ThEBlocks()
	{
		this.EssentiaProvider = new ThEDescription( BlockEnum.ESSENTIA_PROVIDER.getBlock() );
		this.InfusionProvider = new ThEDescription( BlockEnum.INFUSION_PROVIDER.getBlock() );
		this.IronGearBox = new ThEDescription( BlockEnum.IRON_GEAR_BOX.getBlock() );
		this.ThaumiumGearBox = new ThEDescription( BlockEnum.THAUMIUM_GEAR_BOX.getBlock() );
		this.EssentiaCellWorkbench = new ThEDescription( BlockEnum.ESSENTIA_CELL_WORKBENCH.getBlock() );
		this.ArcaneAssembler = new ThEDescription( BlockEnum.ARCANE_ASSEMBLER.getBlock() );
		this.KnowledgeInscriber = new ThEDescription( BlockEnum.KNOWLEDGE_INSCRIBER.getBlock() );
		this.EssentiaVibrationChamber = new ThEDescription( BlockEnum.ESSENTIA_VIBRATION_CHAMBER.getBlock() );
	}
}
