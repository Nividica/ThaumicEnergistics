package thaumicenergistics.client.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.registries.Renderers;
import thaumicenergistics.common.tiles.TileEssentiaProvider;

/**
 * Renders the {@link TileEssentiaProvider}
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class RenderBlockEssentiaProvider
	extends RenderBlockProviderBase
{
	public RenderBlockEssentiaProvider()
	{
		super( BlockTextureManager.ESSENTIA_PROVIDER );
	}

	@Override
	public int getRenderId()
	{
		// Return the ID of the essentia provider
		return Renderers.EssentiaProviderRenderID;
	}

}
