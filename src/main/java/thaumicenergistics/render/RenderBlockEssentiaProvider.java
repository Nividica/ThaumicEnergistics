package thaumicenergistics.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import appeng.api.util.AEColor;

public class RenderBlockEssentiaProvider
	extends RenderBlockProviderBase
{

	@Override
	public void renderWorldBlock( IBlockAccess world, int x, int y, int z, int blockBrightness )
	{
		// What pass is this?
		if ( Renderers.currentRenderPass == Renderers.PASS_OPAQUE )
		{
			// Opaque pass
			this.renderBlock( x, y, z, null, blockBrightness );

		}
		else
		{
			// Alpha pass, get the providers color
			AEColor overlayColor = ( (TileEssentiaProvider)world.getTileEntity( x, y, z ) ).getGridColor();

			// Render the overlay
			this.renderBlock( x, y, z, overlayColor, blockBrightness );
		}
	}

	@Override
	public int getRenderId()
	{
		// Return the ID of the essentia provider
		return Renderers.EssentiaProviderRenderID;
	}

	private void renderBlock( double x, double y, double z, AEColor overlayColor, int blockBrightness )
	{
		// Slightly offsets the overlay so no z-fighting
		double negativeOffset = -.0001D;

		// Get the tessellator instance
		Tessellator tessellator = Tessellator.instance;

		// Get the texture
		IIcon texture;

		// Is this the opaque pass?
		if ( Renderers.currentRenderPass == Renderers.PASS_OPAQUE )
		{
			// Set texture to base
			texture = BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[0];

			// Set the drawing color to full white
			tessellator.setColorRGBA( 255, 255, 255, 255 );

			// Reset offset
			negativeOffset = 0.0D;
		}
		// Does the tile have a color?
		else if ( overlayColor != AEColor.Transparent )
		{
			// Set the texture to the color-able version
			texture = BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[1];

			// Set the drawing color
			tessellator.setColorOpaque_I( overlayColor.mediumVariant );
		}
		else
		{
			// Set the texture to the pre-colored version
			texture = BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[2];

			// Set the drawing color to full white
			tessellator.setColorRGBA( 255, 255, 255, 255 );
		}

		// Calculate the positive offset
		double positiveOffset = 1.0D - negativeOffset;

		// Set the brightness
		tessellator.setBrightness( blockBrightness );

		// Get the UV bounds
		double minU = texture.getMinU();
		double maxU = texture.getMaxU();
		double minV = texture.getMinV();
		double maxV = texture.getMaxV();

		// Calculate positions
		double x1 = x + 1.0D;
		double y1 = y + 1.0D;
		double z1 = z + 1.0D;
		double zSouth = z + positiveOffset;
		double zNorth = z + negativeOffset;
		double xWest = x + positiveOffset;
		double xEast = x + negativeOffset;
		double yUp = y + positiveOffset;
		double yDown = y + negativeOffset;

		// North face
		tessellator.addVertexWithUV( x, y, zSouth, minU, minV );
		tessellator.addVertexWithUV( x1, y, zSouth, minU, maxV );
		tessellator.addVertexWithUV( x1, y1, zSouth, maxU, maxV );
		tessellator.addVertexWithUV( x, y1, zSouth, maxU, minV );

		// South face
		tessellator.addVertexWithUV( x, y1, zNorth, minU, minV );
		tessellator.addVertexWithUV( x1, y1, zNorth, maxU, minV );
		tessellator.addVertexWithUV( x1, y, zNorth, maxU, maxV );
		tessellator.addVertexWithUV( x, y, zNorth, minU, maxV );

		// East face
		tessellator.addVertexWithUV( xWest, y, z, minU, minV );
		tessellator.addVertexWithUV( xWest, y1, z, maxU, minV );
		tessellator.addVertexWithUV( xWest, y1, z1, maxU, maxV );
		tessellator.addVertexWithUV( xWest, y, z1, minU, maxV );

		// West face
		tessellator.addVertexWithUV( xEast, y, z1, minU, minV );
		tessellator.addVertexWithUV( xEast, y1, z1, minU, maxV );
		tessellator.addVertexWithUV( xEast, y1, z, maxU, maxV );
		tessellator.addVertexWithUV( xEast, y, z, maxU, minV );

		// Up face
		tessellator.addVertexWithUV( x, yUp, z1, maxU, minV );
		tessellator.addVertexWithUV( x1, yUp, z1, minU, minV );
		tessellator.addVertexWithUV( x1, yUp, z, minU, maxV );
		tessellator.addVertexWithUV( x, yUp, z, maxU, maxV );

		// Down face
		tessellator.addVertexWithUV( x, yDown, z, minU, maxV );
		tessellator.addVertexWithUV( x1, yDown, z, maxU, maxV );
		tessellator.addVertexWithUV( x1, yDown, z1, maxU, minV );
		tessellator.addVertexWithUV( x, yDown, z1, minU, minV );

	}

	@Override
	protected IIcon getInventoryIcon()
	{
		return BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[0];
	}

}
