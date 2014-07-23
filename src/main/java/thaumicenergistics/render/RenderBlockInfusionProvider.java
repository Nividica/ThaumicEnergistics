package thaumicenergistics.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileInfusionProvider;
import appeng.api.util.AEColor;

public class RenderBlockInfusionProvider
	extends RenderBlockProviderBase
{

	@Override
	public int getRenderId()
	{
		return Renderers.InfusionProviderRenderID;
	}

	@Override
	protected IIcon getInventoryIcon()
	{
		return BlockTextureManager.INFUSION_PROVIDER.getTextures()[1];
	}

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
			AEColor overlayColor = ( (TileInfusionProvider)world.getTileEntity( x, y, z ) ).getGridColor();

			// Render the overlay
			this.renderBlock( x, y, z, overlayColor, blockBrightness );
		}
	}

	private void renderBlock( double x, double y, double z, AEColor overlayColor, int blockBrightness )
	{
		// Slightly offsets the overlay so no z-fighting
		double negativeOffset = -.0001D;

		// Get the tessellator instance
		Tessellator tessellator = Tessellator.instance;

		// Textures
		IIcon topTexture = BlockTextureManager.INFUSION_PROVIDER.getTextures()[4];

		IIcon bottomTexture = BlockTextureManager.INFUSION_PROVIDER.getTextures()[0];

		// Determined below
		IIcon sideTexture;

		// Is this the opaque pass?
		if ( Renderers.currentRenderPass == Renderers.PASS_OPAQUE )
		{
			// Set texture to base
			sideTexture = BlockTextureManager.INFUSION_PROVIDER.getTextures()[1];

			// Set the drawing color to full white
			tessellator.setColorRGBA( 255, 255, 255, 255 );

			// Reset offset
			negativeOffset = 0.0D;
		}
		// Does the tile have a color?
		else if ( overlayColor != AEColor.Transparent )
		{
			// Set the texture to the color-able version
			sideTexture = BlockTextureManager.INFUSION_PROVIDER.getTextures()[2];

			// Set the drawing color
			tessellator.setColorOpaque_I( overlayColor.mediumVariant );
		}
		else
		{
			// Set the texture to the pre-colored version
			sideTexture = BlockTextureManager.INFUSION_PROVIDER.getTextures()[3];

			// Set the drawing color to full white
			tessellator.setColorRGBA( 255, 255, 255, 255 );
		}

		// Calculate the positive offset
		double positiveOffset = 1.0D - negativeOffset;

		// Set the brightness
		tessellator.setBrightness( blockBrightness );

		// Get the UV bounds
		double sideMinU = sideTexture.getMinU();
		double sideMaxU = sideTexture.getMaxU();
		double sideMinV = sideTexture.getMinV();
		double sideMaxV = sideTexture.getMaxV();

		double topMinU = topTexture.getMinU();
		double topMaxU = topTexture.getMaxU();
		double topMinV = topTexture.getMinV();
		double topMaxV = topTexture.getMaxV();

		double bottomMinU = bottomTexture.getMinU();
		double bottomMaxU = bottomTexture.getMaxU();
		double bottomMinV = bottomTexture.getMinV();
		double bottomMaxV = bottomTexture.getMaxV();

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
		tessellator.addVertexWithUV( x, y, zSouth, sideMinU, sideMaxV );
		tessellator.addVertexWithUV( x1, y, zSouth, sideMaxU, sideMaxV );
		tessellator.addVertexWithUV( x1, y1, zSouth, sideMaxU, sideMinV );
		tessellator.addVertexWithUV( x, y1, zSouth, sideMinU, sideMinV );

		// South face
		tessellator.addVertexWithUV( x, y1, zNorth, sideMaxU, sideMinV );
		tessellator.addVertexWithUV( x1, y1, zNorth, sideMinU, sideMinV );
		tessellator.addVertexWithUV( x1, y, zNorth, sideMinU, sideMaxV );
		tessellator.addVertexWithUV( x, y, zNorth, sideMaxU, sideMaxV );

		// East face
		tessellator.addVertexWithUV( xWest, y, z, sideMaxU, sideMaxV );
		tessellator.addVertexWithUV( xWest, y1, z, sideMaxU, sideMinV );
		tessellator.addVertexWithUV( xWest, y1, z1, sideMinU, sideMinV );
		tessellator.addVertexWithUV( xWest, y, z1, sideMinU, sideMaxV );

		// West face
		tessellator.addVertexWithUV( xEast, y, z1, sideMaxU, sideMaxV );
		tessellator.addVertexWithUV( xEast, y1, z1, sideMaxU, sideMinV );
		tessellator.addVertexWithUV( xEast, y1, z, sideMinU, sideMinV  );
		tessellator.addVertexWithUV( xEast, y, z, sideMinU, sideMaxV );

		if( Renderers.currentRenderPass == Renderers.PASS_OPAQUE )
		{
			// Set the drawing color to full white
			tessellator.setColorRGBA( 255, 255, 255, 255 );
	
			// Up face
			tessellator.addVertexWithUV( x, yUp, z1, topMaxU, topMinV );
			tessellator.addVertexWithUV( x1, yUp, z1, topMinU, topMinV );
			tessellator.addVertexWithUV( x1, yUp, z, topMinU, topMaxV );
			tessellator.addVertexWithUV( x, yUp, z, topMaxU, topMaxV );
	
			// Down face
			tessellator.addVertexWithUV( x, yDown, z, bottomMinU, bottomMaxV );
			tessellator.addVertexWithUV( x1, yDown, z, bottomMaxU, bottomMaxV );
			tessellator.addVertexWithUV( x1, yDown, z1, bottomMaxU, bottomMinV );
			tessellator.addVertexWithUV( x, yDown, z1, bottomMinU, bottomMinV );
		}
	}

}
