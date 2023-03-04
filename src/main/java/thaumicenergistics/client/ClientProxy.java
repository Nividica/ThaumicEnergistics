package thaumicenergistics.client;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import thaumicenergistics.client.render.item.ItemCellMicroscopeRenderer;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.CommonProxy;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.registries.Renderers;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Client side proxy.
 *
 * @author Nividica
 *
 */
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void registerRenderers() {
        // Register the custom block renderers
        Renderers.registerRenderers();

        // Post-release renderer
        MinecraftForgeClient.registerItemRenderer(ItemEnum.CELL_MICROSCOPE.getItem(), new ItemCellMicroscopeRenderer());
    }

    @SubscribeEvent
    public void registerTextures(final TextureStitchEvent.Pre event) {
        // Register all block textures
        for (BlockTextureManager texture : BlockTextureManager.ALLVALUES) {
            texture.registerTexture(event.map);
        }
    }
}
