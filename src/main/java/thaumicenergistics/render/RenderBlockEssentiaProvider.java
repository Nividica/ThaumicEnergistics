package thaumicenergistics.render;

import net.minecraft.util.IIcon;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;

public class RenderBlockEssentiaProvider
	extends RenderBlockProviderBase
{

	@Override
	protected IIcon getBaseTexture()
	{
		return BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[0];
	}

	@Override
	protected IIcon getOverlayTexture()
	{
		return BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[1];
	}

	@Override
	public int getRenderId()
	{
		// Return the ID of the essentia provider
		return Renderers.EssentiaProviderRenderID;
	}

}
