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
		super( BlockTextureManager.INFUSION_PROVIDER );
	}

	@Override
	public int getRenderId()
	{
		return Renderers.InfusionProviderRenderID;
	}

}
