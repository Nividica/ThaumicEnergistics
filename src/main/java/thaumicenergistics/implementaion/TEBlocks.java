package thaumicenergistics.implementaion;

import thaumicenergistics.api.Blocks;
import thaumicenergistics.registries.BlockEnum;

class TEBlocks
	extends Blocks
{
	TEBlocks()
	{
		this.EssentiaProvider = new TEDescription( BlockEnum.ESSENTIA_PROVIDER.getBlock() );
		this.InfusionProvider = new TEDescription( BlockEnum.INFUSION_PROVIDER.getBlock() );
		this.IronGearBox = new TEDescription( BlockEnum.IRON_GEAR_BOX.getBlock() );
		this.ThaumiumGearBox = new TEDescription( BlockEnum.THAUMIUM_GEAR_BOX.getBlock() );
		this.EssentiaCellWorkbench = new TEDescription( BlockEnum.ESSENTIA_CELL_WORKBENCH.getBlock() );
		this.ArcaneAssembler = new TEDescription( BlockEnum.ARCANE_ASSEMBLER.getBlock() );
	}
}
