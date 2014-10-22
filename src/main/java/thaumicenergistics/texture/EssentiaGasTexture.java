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
	private class PixelColor
	{
		/**
		 * Array index of alpha.
		 */
		private static final int ALPHA = 0;

		/**
		 * Array index of red.
		 */
		private static final int RED = 1;

		/**
		 * Array index of green.
		 */
		private static final int GREEN = 2;

		/**
		 * Array index of blue.
		 */
		private static final int BLUE = 3;

		/**
		 * Array of 'bytes', where each byte is a color
		 */
		private int[] colorBytes;

		public PixelColor()
		{
			/**
			 * Create the array
			 */
			this.colorBytes = new int[4];
		}

		public PixelColor( final int color )
		{
			// Call the default constructor
			this();

			/**
			 * Set the color
			 */
			this.setFromColor( color );
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
		 * Gets the ARGB packed integer color.
		 * 
		 * @return
		 */
		public int getColor()
		{
			return( ( this.colorBytes[ALPHA] << 24 ) | ( this.colorBytes[RED] << 16 ) | ( this.colorBytes[GREEN] << 8 ) | this.colorBytes[BLUE] );
		}

		/**
		 * Applies screen blending to this pixel.
		 * 
		 * @param otherPixel
		 */
		public void screen( final PixelColor otherPixel )
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
		 * Set's the stored color
		 * 
		 * @param color
		 */
		public void setFromColor( final int color )
		{
			this.colorBytes[ALPHA] = ( ( color & 0xFF000000 ) >>> 24 );
			this.colorBytes[RED] = ( ( color & 0x00FF0000 ) >>> 16 );
			this.colorBytes[GREEN] = ( ( color & 0x0000FF00 ) >>> 8 );
			this.colorBytes[BLUE] = ( color & 0x000000FF );

			this.clamp();
		}

		/*
		@Override
		public String toString()
		{
			return "Pixel Values: [" + this.colorBytes[ALPHA] + ", " + this.colorBytes[RED] + ", " + this.colorBytes[GREEN] + ", " +
							this.colorBytes[BLUE] + "]";
		}
		*/
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

	public static int mipmapLevels;

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

		int width = image.getWidth();
		int height = image.getHeight();

		// Create an array to hold the pixels
		int[] pixels = image.getRGB( 0, 0, width, height, null, 0, width );
		PixelColor pixel = new PixelColor();

		for( int i = 0; i < pixels.length; i++ )
		{
			// Get the pixel
			pixel.setFromColor( pixels[i] );

			// Multiply
			pixel.screen( new PixelColor( this.aspectColor ) );

			// Set to pixel to our color
			pixels[i] = pixel.getColor();
		}

		// Set the pixels
		image.setRGB( 0, 0, width, height, pixels, 0, width );
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

	@Override
	public final boolean hasCustomLoader( final IResourceManager manager, final ResourceLocation location )
	{
		return true;
	}

	// TODO This is a mess.
	@Override
	public boolean load( final IResourceManager par1ResourceManager, final ResourceLocation resourcelocation )
	{
		ResourceLocation resourcelocation1 = this.completeResourceLocation( resourcelocation, 0 );

		try
		{
			IResource iresource = par1ResourceManager.getResource( resourcelocation1 );
			BufferedImage[] abufferedimage = new BufferedImage[1 + EssentiaGasTexture.mipmapLevels];
			abufferedimage[0] = ImageIO.read( iresource.getInputStream() );
			TextureMetadataSection texturemetadatasection = (TextureMetadataSection)iresource.getMetadata( "texture" );

			if( texturemetadatasection != null )
			{
				List list = texturemetadatasection.getListMipmaps();
				int l;

				if( !list.isEmpty() )
				{
					int k = abufferedimage[0].getWidth();
					l = abufferedimage[0].getHeight();

					if( ( MathHelper.roundUpToPowerOfTwo( k ) != k ) || ( MathHelper.roundUpToPowerOfTwo( l ) != l ) )
					{
						throw new RuntimeException( "Unable to load extra miplevels, source-texture is not power of two" );
					}
				}

				Iterator iterator3 = list.iterator();

				while( iterator3.hasNext() )
				{
					l = ( (Integer)iterator3.next() ).intValue();

					if( ( l > 0 ) && ( l < ( abufferedimage.length - 1 ) ) && ( abufferedimage[l] == null ) )
					{
						ResourceLocation resourcelocation2 = this.completeResourceLocation( resourcelocation, l );

						try
						{
							abufferedimage[l] = ImageIO.read( par1ResourceManager.getResource( resourcelocation2 ).getInputStream() );
						}
						catch( IOException ioexception )
						{
							FMLLog.warning( "Unable to load miplevel %d from: %s", l, resourcelocation2.toString() );
						}
					}
				}
			}

			for( int i = 0; i < abufferedimage.length; i++ )
			{
				this.applyScreen( abufferedimage[i] );
			}

			AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection)iresource.getMetadata( "animation" );

			this.loadSprite( abufferedimage, animationmetadatasection, false );
		}
		catch( RuntimeException runtimeexception )
		{
			FMLLog.warning( "Unable to parse metadata from %s", resourcelocation1.toString() );
		}
		catch( IOException ioexception1 )
		{
			FMLLog.warning( "Using missing texture, unable to load %s", resourcelocation1.toString() );
		}

		return false;
	}

}
