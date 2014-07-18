package thaumicenergistics.proxy;

import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.render.EssentiaGasTexture;
import thaumicenergistics.render.BlockTextureManager;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy
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

	@SubscribeEvent
	public void registerTextures( TextureStitchEvent.Pre event )
	{
		TextureMap map = event.map;

		for( BlockTextureManager texture : BlockTextureManager.values() )
		{
			texture.registerTexture( map );
		}

		try
		{
			Field mapField = TextureMap.class.getDeclaredField( "mapRegisteredSprites" );
			Field mipmapField = TextureMap.class.getDeclaredField( "mipmapLevels" );

			mapField.setAccessible( true );
			mipmapField.setAccessible( true );

			Map internalMap = (Map) mapField.get( map );
			int mipmap = (Integer) mipmapField.get( map );

			this.injectGeneratedTextures( internalMap, mipmap );
		}
		catch( Throwable e )
		{
			FMLLog.severe( "Unable to generate essentia gas textures.", new Object[0] );
			e.printStackTrace();
		}

	}
}
