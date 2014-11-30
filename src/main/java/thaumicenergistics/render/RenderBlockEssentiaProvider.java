package thaumicenergistics.render;

import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockEssentiaProvider
	extends RenderBlockProviderBase
{
	public RenderBlockEssentiaProvider()
	{
		super( BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[0], BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[1] );
	}

	@Override
	public int getRenderId()
	{
		// Return the ID of the essentia provider
		return Renderers.EssentiaProviderRenderID;
	}

}
