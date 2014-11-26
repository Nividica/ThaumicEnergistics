package thaumicenergistics.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.texture.EssentiaGasTexture;
import thaumicenergistics.util.TELog;
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
		// Cache the map
		TextureMap map = event.map;

		// Register all block textures
		for( BlockTextureManager texture : BlockTextureManager.VALUES )
		{
			texture.registerTexture( map );
		}

		// Set the mipmap levels
		EssentiaGasTexture.mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;

		// Add each gas texture
		for( GaseousEssentia gas : GaseousEssentia.gasList.values() )
		{
			// Create the texture
			EssentiaGasTexture gasTexture = new EssentiaGasTexture( gas.getAspect() );

			// Add to the texture map
			if( !map.setTextureEntry( gasTexture.getIconName(), gasTexture ) )
			{
				TELog.warning( "Unable to register texture for %s", gasTexture.getIconName() );
			}

			// Set the texture
			gas.setIcons( gasTexture );
		}

	}
}
