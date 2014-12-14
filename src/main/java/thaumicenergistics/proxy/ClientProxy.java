package thaumicenergistics.proxy;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy
	extends CommonProxy
{
	public ClientProxy()
	{
		MinecraftForge.EVENT_BUS.register( this );
	}

	@Override
	public void registerRenderers()
	{
		// Register the custom block renderers
		Renderers.registerRenderers();
	}

	@SubscribeEvent
	public void registerTextures( final TextureStitchEvent.Pre event )
	{
		// Register all block textures
		for( BlockTextureManager texture : BlockTextureManager.VALUES )
		{
			texture.registerTexture( event.map );
		}

	}
}
