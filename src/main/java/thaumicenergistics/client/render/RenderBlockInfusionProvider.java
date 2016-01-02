package thaumicenergistics.client.render;

import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.registries.Renderers;
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
