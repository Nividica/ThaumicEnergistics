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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;

public class EssentiaGasTexture extends TextureAtlasSprite
{
	private class Pixel
	{
		private static final int ALPHA = 0;
		private static final int RED = 1;
		private static final int GREEN = 2;
		private static final int BLUE = 3;

		private int[] data;

		public Pixel(int color)
		{
			this.data = new int[4];
			this.setFromColor( color );
		}

		private void clamp()
		{
			for( int i = 0; i < this.data.length; i++ )
			{
				if ( this.data[i] > 255 )
				{
					this.data[i] = 255;
				}
				else if ( this.data[i] < 0 )
				{
					this.data[i] = 0;
				}
			}
		}

		public int getColor()
		{
			return ( ( this.data[ALPHA] << 24 ) | ( this.data[RED] << 16 ) | ( this.data[GREEN] << 8 ) | this.data[BLUE] );
		}

		public void multiplyWith( Pixel otherPixel, boolean preserveAlpha )
		{
			if ( !preserveAlpha )
			{
				this.data[ALPHA] = ( this.data[ALPHA] * otherPixel.data[ALPHA] ) / 255;
			}

			this.data[RED] = ( this.data[RED] * otherPixel.data[RED] ) / 255;
			this.data[GREEN] = ( this.data[GREEN] * otherPixel.data[GREEN] ) / 255;
			this.data[BLUE] = ( this.data[BLUE] * otherPixel.data[BLUE] ) / 255;

			this.clamp();
		}
		
		public void setFromColor( int color )
		{
			this.data[ALPHA] = ( ( color & 0xFF000000 ) >>> 24 );
			this.data[RED] = ( ( color & 0x00FF0000 ) >>> 16 );
			this.data[GREEN] = ( ( color & 0x0000FF00 ) >>> 8 );
			this.data[BLUE] = ( color & 0x000000FF );

			this.clamp();
		}

		@Override
		public String toString()
		{
			return "Pixel Values: [" + this.data[ALPHA] + ", " + this.data[RED] + ", " + this.data[GREEN] + ", " + this.data[BLUE] + "]";
		}
	}

	private static final Logger LOGGER = LogManager.getLogger();
	public static final String GAS_FILE_NAME = "essentia.gas";
	public static final String BASE_PATH = "textures/blocks";

	public static int mipmapLevels;

	private Pixel overlayColor;

	public EssentiaGasTexture(Aspect aspect)
	{
		super( ThaumicEnergistics.MODID + ":" + GAS_FILE_NAME + "_" + aspect.getName() );

		this.overlayColor = new Pixel( aspect.getColor() );
	}

	private ResourceLocation completeResourceLocation( ResourceLocation location, int p_147634_2_ )
	{
		return p_147634_2_ == 0 ? new ResourceLocation( location.getResourceDomain(), String.format( "%s/%s%s",
			new Object[] { EssentiaGasTexture.BASE_PATH, GAS_FILE_NAME, ".png" } ) ) : new ResourceLocation( location.getResourceDomain(),
						String.format( "%s/mipmaps/%s.%d%s",
							new Object[] { EssentiaGasTexture.BASE_PATH, GAS_FILE_NAME, Integer.valueOf( p_147634_2_ ), ".png" } ) );
	}

	protected void drawOverlay( BufferedImage image )
	{
		if ( image == null )
		{
			return;
		}

		int width = image.getWidth();
		int height = image.getHeight();

		// Create an array to hold the pixels
		int[] pixels = image.getRGB( 0, 0, width, height, null, 0, width );
		Pixel pixel = new Pixel( 0 );

		for( int i = 0; i < pixels.length; i++ )
		{
			// Get the pixel
			pixel.setFromColor( pixels[i] );

			// Multiply
			pixel.multiplyWith( this.overlayColor, true );

			// Set to pixel to our color
			pixels[i] = pixel.getColor();
		}

		// Set the pixels
		image.setRGB( 0, 0, width, height, pixels, 0, width );
	}

	@Override
	public final boolean hasCustomLoader( IResourceManager manager, ResourceLocation location )
	{
		return true;
	}

	@Override
	public boolean load( IResourceManager par1ResourceManager, ResourceLocation resourcelocation )
	{
		ResourceLocation resourcelocation1 = this.completeResourceLocation( resourcelocation, 0 );

		try
		{
			IResource iresource = par1ResourceManager.getResource( resourcelocation1 );
			BufferedImage[] abufferedimage = new BufferedImage[1 + EssentiaGasTexture.mipmapLevels];
			abufferedimage[0] = ImageIO.read( iresource.getInputStream() );
			TextureMetadataSection texturemetadatasection = (TextureMetadataSection) iresource.getMetadata( "texture" );

			if ( texturemetadatasection != null )
			{
				List list = texturemetadatasection.getListMipmaps();
				int l;

				if ( !list.isEmpty() )
				{
					int k = abufferedimage[0].getWidth();
					l = abufferedimage[0].getHeight();

					if ( ( MathHelper.roundUpToPowerOfTwo( k ) != k ) || ( MathHelper.roundUpToPowerOfTwo( l ) != l ) )
					{
						throw new RuntimeException( "Unable to load extra miplevels, source-texture is not power of two" );
					}
				}

				Iterator iterator3 = list.iterator();

				while ( iterator3.hasNext() )
				{
					l = ( (Integer) iterator3.next() ).intValue();

					if ( ( l > 0 ) && ( l < ( abufferedimage.length - 1 ) ) && ( abufferedimage[l] == null ) )
					{
						ResourceLocation resourcelocation2 = this.completeResourceLocation( resourcelocation, l );

						try
						{
							abufferedimage[l] = ImageIO.read( par1ResourceManager.getResource( resourcelocation2 ).getInputStream() );
						}
						catch( IOException ioexception )
						{
							LOGGER.error( "Unable to load miplevel {} from: {}",
								new Object[] { Integer.valueOf( l ), resourcelocation2, ioexception } );
						}
					}
				}
			}

			for( int i = 0; i < abufferedimage.length; i++ )
			{
				this.drawOverlay( abufferedimage[i] );
			}

			AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection) iresource.getMetadata( "animation" );

			this.loadSprite( abufferedimage, animationmetadatasection, false );
		}
		catch( RuntimeException runtimeexception )
		{
			LOGGER.error( "Unable to parse metadata from " + resourcelocation1, runtimeexception );
		}
		catch( IOException ioexception1 )
		{
			LOGGER.error( "Using missing texture, unable to load " + resourcelocation1, ioexception1 );
		}

		return false;
	}

}
