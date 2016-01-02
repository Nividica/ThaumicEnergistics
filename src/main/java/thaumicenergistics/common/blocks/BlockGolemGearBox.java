package thaumicenergistics.common.blocks;

import net.minecraft.util.IIcon;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.registries.BlockEnum;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGolemGearBox
	extends AbstractBlockGearBoxBase
{

	public BlockGolemGearBox()
	{
		// Set that golems are allowed to interact with the gearbox.
		this.allowGolemInteraction = true;
	}

	@SideOnly(Side.CLIENT)
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
