package thaumicenergistics.proxy;

import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.texture.EssentiaGasTexture;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy
	extends CommonProxy
{
	public ClientProxy()
	{
		MinecraftForge.EVENT_BUS.register( this );
	}

	private void injectGeneratedTextures( Map textureMap, int mipmapLevel )
	{
		// Set the mipmap levels
		EssentiaGasTexture.mipmapLevels = mipmapLevel;

		for( GaseousEssentia gas : GaseousEssentia.gasList.values() )
		{
			// Create the texture
			EssentiaGasTexture gasTexture = new EssentiaGasTexture( gas.getAssociatedAspect() );

			// Add to the texturemap
			textureMap.put( gasTexture.getIconName(), gasTexture );

			// Set the texture
			gas.setIcons( gasTexture );
		}

	}

	@Override
	public void registerRenderers()
	{
		// Register the custom block renderers
		Renderers.registerRenderers();
	}

	@SubscribeEvent
	public void registerTextures( TextureStitchEvent.Pre event )
	{
		// Cache the map
		TextureMap map = event.map;

		// Register all block textures
		for( BlockTextureManager texture : BlockTextureManager.values() )
		{
			texture.registerTexture( map );
		}

		// Attempt to generate gas textures
		try
		{
			// Using reflection, allow these fields to become accessible
			Field mapField = TextureMap.class.getDeclaredField( "mapRegisteredSprites" );
			Field mipmapField = TextureMap.class.getDeclaredField( "mipmapLevels" );
			mapField.setAccessible( true );
			mipmapField.setAccessible( true );

			// Access the maps internal map of textures
			Map internalMap = (Map)mapField.get( map );

			// Access the maps internal mipmap value
			int mipmap = (Integer)mipmapField.get( map );

			// Create and inject textures
			this.injectGeneratedTextures( internalMap, mipmap );
		}
		catch( Throwable e )
		{
			FMLLog.severe( "Unable to generate essentia gas textures.", new Object[0] );
			e.printStackTrace();
		}

	}
}
