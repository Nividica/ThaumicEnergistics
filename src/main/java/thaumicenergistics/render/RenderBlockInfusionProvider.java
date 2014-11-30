package thaumicenergistics.render;

import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
