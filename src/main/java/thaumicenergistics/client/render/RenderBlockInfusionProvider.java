package thaumicenergistics.client.render;

import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.registries.Renderers;
import thaumicenergistics.common.tiles.TileInfusionProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Renders the {@link TileInfusionProvider}
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class RenderBlockInfusionProvider extends RenderBlockProviderBase {

    public RenderBlockInfusionProvider() {
        super(BlockTextureManager.INFUSION_PROVIDER);
    }

    @Override
    public int getRenderId() {
        return Renderers.InfusionProviderRenderID;
    }
}
