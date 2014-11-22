package thaumicenergistics.render;

import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;

public class RenderBlockInfusionProvider
	extends RenderBlockProviderBase
{

	public RenderBlockInfusionProvider()
	{
		super( BlockTextureManager.INFUSION_PROVIDER.getTextures()[0], BlockTextureManager.INFUSION_PROVIDER.getTextures()[1] );
	}

	@Override
	public int getRenderId()
	{
		return Renderers.InfusionProviderRenderID;
	}

}
