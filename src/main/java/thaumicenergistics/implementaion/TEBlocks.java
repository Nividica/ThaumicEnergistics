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
	}
}
