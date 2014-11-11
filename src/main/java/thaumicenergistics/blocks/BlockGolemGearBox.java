package thaumicenergistics.blocks;

import net.minecraft.util.IIcon;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.texture.BlockTextureManager;

public class BlockGolemGearBox
	extends AbstractBlockGearBoxBase
{

	public BlockGolemGearBox()
	{
		// Set that golems are allowed to interact with the gearbox.
		this.allowGolemInteraction = true;
	}

	@Override
	public IIcon getIcon( final int side, final int meta )
	{
		return BlockTextureManager.GEAR_BOX.getTextures()[1];
	}

	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.THAUMIUM_GEAR_BOX.getUnlocalizedName();
	}

}
