package thaumicenergistics.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import cpw.mods.fml.common.FMLLog;

public class EssentiaGasTexture
	extends TextureAtlasSprite
{
	/**
	 * Helper class to adjust the color of an ARGB int packed color.
	 * 
	 * @author Nividica
	 * 
	 */
	private class ARGB_Color
	{
		/**
		 * Array index of each color
		 */
		private static final int ALPHA = 0, RED = 1, GREEN = 2, BLUE = 3;

		/**
		 * Array of 'bytes', where each byte is a color
		 */
		private int[] colorBytes;

		public ARGB_Color()
		{
			/**
			 * Create the array
			 */
			this.colorBytes = new int[4];
		}

		public ARGB_Color( final int color )
		{
			// Call the default constructor
			this();

			/**
			 * Set the color
			 */
			this.setFromPackedColor( color );
		}

		/**
		 * Clamps all bytes to the range of 0 to 155
		 */
		private void clamp()
		{
			for( int i = 0; i < this.colorBytes.length; i++ )
			{
				if( this.colorBytes[i] > 255 )
				{
					this.colorBytes[i] = 255;
				}
				else if( this.colorBytes[i] < 0 )
				{
					this.colorBytes[i] = 0;
				}
			}
		}

		/**
		 * Applies screen blending to this pixel.
		 * 
		 * @param otherPixel
		 */
		public void applyScreen( final ARGB_Color otherPixel )
		{

			// Calculated the inverted & multiplied values
			int redInverted = ( ( 255 - this.colorBytes[RED] ) * ( 255 - otherPixel.colorBytes[RED] ) ) / 255;
			int greenInverted = ( ( 255 - this.colorBytes[GREEN] ) * ( 255 - otherPixel.colorBytes[GREEN] ) ) / 255;
			int blueInverted = ( ( 255 - this.colorBytes[BLUE] ) * ( 255 - otherPixel.colorBytes[BLUE] ) ) / 255;

			// Calculate the new color by inverting the colors
			this.colorBytes[RED] = 255 - redInverted;
			this.colorBytes[GREEN] = 255 - greenInverted;
			this.colorBytes[BLUE] = 255 - blueInverted;

			// Clamp to valid color ranges.
			this.clamp();
		}

		/**
		 * Gets the ARGB packed integer color.
		 * 
		 * @return
		 */
		public int getPackedColor()
		{
			return( ( this.colorBytes[ALPHA] << 24 ) | ( this.colorBytes[RED] << 16 ) | ( this.colorBytes[GREEN] << 8 ) | this.colorBytes[BLUE] );
		}

		/**
		 * Set's the stored color
		 * 
		 * @param color
		 */
		public void setFromPackedColor( final int color )
		{
			this.colorBytes[ALPHA] = ( ( color & 0xFF000000 ) >>> 24 );
			this.colorBytes[RED] = ( ( color & 0x00FF0000 ) >>> 16 );
			this.colorBytes[GREEN] = ( ( color & 0x0000FF00 ) >>> 8 );
			this.colorBytes[BLUE] = ( color & 0x000000FF );

			this.clamp();
		}
	}

	/**
	 * Mod identifier.
	 */
	private static final String MOD_LOC = "thaumicenergistics";

	/**
	 * Path to the texture.
	 */
	private static final String BASE_PATH = "textures/blocks";

	/**
	 * Name of the texture file.
	 */
	private static final String GAS_FILE_NAME = "essentia.gas";

	/**
	 * Number of mipmap levels
	 */
	public static int mipmapLevels = 0;

	/**
	 * The color we want to apply to the texture
	 */
	private int aspectColor;

	public EssentiaGasTexture( final Aspect aspect )
	{
		super( ThaumicEnergistics.MOD_ID + ":" + GAS_FILE_NAME + "_" + aspect.getName() );

		this.aspectColor = aspect.getColor();
	}

	/**
	 * Screens our color onto the image.
	 * 
	 * @param image
	 */
	private void applyScreen( final BufferedImage image )
	{
		if( image == null )
		{
			return;
		}

		// Get the width and height of the image
		int width = image.getWidth(), height = image.getHeight();

		// Get a copy of the colors that comprise the image
		int[] imagePixels = image.getRGB( 0, 0, width, height, null, 0, width );

		// Create a helper for the image and the aspect color
		ARGB_Color imagePixel = new ARGB_Color(), aspectColor = new ARGB_Color( this.aspectColor );

		for( int i = 0; i < imagePixels.length; i++ )
		{
			// Set the pixel from the image color
			imagePixel.setFromPackedColor( imagePixels[i] );

			// Screen the aspect color onto it
			imagePixel.applyScreen( aspectColor );

			// Set the image color to the screened color
			imagePixels[i] = imagePixel.getPackedColor();
		}

		// Set the image to the new colors
		image.setRGB( 0, 0, width, height, imagePixels, 0, width );
	}

	/**
	 * Builds a completed resource path for the specified mipmap level.
	 * 
	 * @param location
	 * @param mipLevel
	 * @return
	 */
	private ResourceLocation completeResourceLocation( final ResourceLocation location, final int mipLevel )
	{
		if( mipLevel == 0 )
		{
			return new ResourceLocation( EssentiaGasTexture.MOD_LOC, String.format( "%s/%s%s", new Object[] { EssentiaGasTexture.BASE_PATH,
							GAS_FILE_NAME, ".png" } ) );
		}

		return new ResourceLocation( EssentiaGasTexture.MOD_LOC, String.format( "%s/mipmaps/%s.%d%s", new Object[] { EssentiaGasTexture.BASE_PATH,
						EssentiaGasTexture.GAS_FILE_NAME, Integer.valueOf( mipLevel ), ".png" } ) );
	}

	/**
	 * Loads the mipmaps for the specified texture resource
	 * 
	 * @param resourceManager
	 * @param baseResourcelocation
	 * @param textureResource
	 * @param images
	 */
	private void loadMipmaps( final IResourceManager resourceManager, final ResourceLocation baseResourcelocation, final IResource textureResource,
								final BufferedImage[] images )
	{
		// Read the metadata
		TextureMetadataSection mipmapMetaData = (TextureMetadataSection)textureResource.getMetadata( "texture" );

		// Is there mipmap meta data?
		if( mipmapMetaData != null )
		{
			// Get the mipmap levels
			List mipmapLevels = mipmapMetaData.getListMipmaps();

			// Are there any mipmaps?
			if( !mipmapLevels.isEmpty() )
			{
				// Get the width and height of the texture
				int textureWidth = images[0].getWidth(), textureHeight = images[0].getHeight();

				// Ensure the texture ratio is correct
				if( ( MathHelper.roundUpToPowerOfTwo( textureWidth ) != textureWidth ) ||
								( MathHelper.roundUpToPowerOfTwo( textureHeight ) != textureHeight ) )
				{
					// Invalid width or height
					throw new RuntimeException( "Unable to load extra miplevels, source-texture is not power of two" );
				}

				// Get the list iterator for the mipmap levels
				Iterator mmIterator = mipmapLevels.iterator();

				// Load each mipmap
				while( mmIterator.hasNext() )
				{
					// Get the next mipmap level
					int currentMipmapLevel = ( (Integer)mmIterator.next() ).intValue();

					// Bounds check the level
					if( ( currentMipmapLevel <= 0 ) || ( currentMipmapLevel >= images.length ) )
					{
						// Skip, Level is out of bounds.
						continue;
					}

					// Ensure the mipmap has not already been loaded
					if( images[currentMipmapLevel] != null )
					{
						// Skip, Mipmap already loaded.
						continue;
					}

					// Get the mipmap location
					ResourceLocation mmLocation = this.completeResourceLocation( baseResourcelocation, currentMipmapLevel );

					// Attempt to load the mipmap image
					try
					{
						// Load the mipmap
						images[currentMipmapLevel] = ImageIO.read( resourceManager.getResource( mmLocation ).getInputStream() );
					}
					catch( IOException ioexception )
					{
						FMLLog.warning( "Unable to load miplevel %d from: %s", currentMipmapLevel, mmLocation.toString() );
					}
				}
			}
		}
	}

	@Override
	public final boolean hasCustomLoader( final IResourceManager manager, final ResourceLocation location )
	{
		return true;
	}

	/**
	 * Load the gas texture and apply the aspect color.
	 */
	@Override
	public boolean load( final IResourceManager resourceManager, final ResourceLocation baseResourcelocation )
	{
		// Get the texture location
		ResourceLocation textureLocation = this.completeResourceLocation( baseResourcelocation, 0 );

		// Attempt to load the texture
		try
		{
			// Get the texture as a resource
			IResource textureResource = resourceManager.getResource( textureLocation );

			// Create the image array
			BufferedImage[] images = new BufferedImage[1 + EssentiaGasTexture.mipmapLevels];

			// Load the texture
			images[0] = ImageIO.read( textureResource.getInputStream() );

			// Load any mipmaps
			this.loadMipmaps( resourceManager, baseResourcelocation, textureResource, images );

			// Apply the aspect color to each texture
			for( int index = 0; index < images.length; index++ )
			{
				this.applyScreen( images[index] );
			}

			// Get the animation metadata
			AnimationMetadataSection animMetaData = (AnimationMetadataSection)textureResource.getMetadata( "animation" );

			// Load the sprite
			super.loadSprite( images, animMetaData, false );
		}
		catch( RuntimeException runtimeexception )
		{
			FMLLog.warning( "Unable to parse metadata from %s", textureLocation.toString() );
		}
		catch( IOException ioexception1 )
		{
			FMLLog.warning( "Using missing texture, unable to load %s", textureLocation.toString() );
		}

		// Do not attempt to stitch into the master texture, causes MISSION_TEXTURE to show in-game.
		return false;
	}
}
