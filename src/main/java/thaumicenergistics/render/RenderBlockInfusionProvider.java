package thaumicenergistics.render;

import net.minecraft.util.IIcon;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;

public class RenderBlockInfusionProvider
	extends RenderBlockProviderBase
{

	@Override
	public int getRenderId()
	{
		return Renderers.InfusionProviderRenderID;
	}
	
	@Override
	protected IIcon getBaseTexture()
	{
		return BlockTextureManager.INFUSION_PROVIDER.getTextures()[0];
	}
	
	@Override
	protected IIcon getOverlayTexture()
	{
		return BlockTextureManager.INFUSION_PROVIDER.getTextures()[1];
	}

}
