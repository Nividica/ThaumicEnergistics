package thaumicenergistics.blocks;

import net.minecraft.util.IIcon;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.texture.BlockTextureManager;

public class BlockGearBox
	extends BlockGearBoxBase
{
	@Override
	public IIcon getIcon( final int side, final int meta )
	{
		return BlockTextureManager.GEAR_BOX.getTextures()[0];
	}

	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.IRON_GEAR_BOX.getUnlocalizedName();
	}

}
