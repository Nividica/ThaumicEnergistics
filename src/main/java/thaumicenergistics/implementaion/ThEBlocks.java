package thaumicenergistics.implementaion;

import thaumicenergistics.api.IThEBlocks;
import thaumicenergistics.common.registries.BlockEnum;

class ThEBlocks
	extends IThEBlocks
{
	ThEBlocks()
	{
		this.EssentiaProvider = new ThEItemDescription( BlockEnum.ESSENTIA_PROVIDER.getBlock() );
		this.InfusionProvider = new ThEItemDescription( BlockEnum.INFUSION_PROVIDER.getBlock() );
		this.IronGearBox = new ThEItemDescription( BlockEnum.IRON_GEAR_BOX.getBlock() );
		this.ThaumiumGearBox = new ThEItemDescription( BlockEnum.THAUMIUM_GEAR_BOX.getBlock() );
		this.EssentiaCellWorkbench = new ThEItemDescription( BlockEnum.ESSENTIA_CELL_WORKBENCH.getBlock() );
		this.ArcaneAssembler = new ThEItemDescription( BlockEnum.ARCANE_ASSEMBLER.getBlock() );
		this.KnowledgeInscriber = new ThEItemDescription( BlockEnum.KNOWLEDGE_INSCRIBER.getBlock() );
		this.EssentiaVibrationChamber = new ThEItemDescription( BlockEnum.ESSENTIA_VIBRATION_CHAMBER.getBlock() );
		this.DistillationPatternEncoder = new ThEItemDescription( BlockEnum.DISTILLATION_ENCODER.getBlock() );
	}
}
