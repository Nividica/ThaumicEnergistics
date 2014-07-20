package thaumicenergistics.render;

import org.lwjgl.opengl.GL11;
import appeng.api.util.AEColor;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderBlockEssentiaProvider
	implements ISimpleBlockRenderingHandler
{
	private static final double NEG_OFFSET = -.0001D;
	private static final double POS_OFFSET = 1.0D - NEG_OFFSET;
	
	@Override
	public void renderInventoryBlock( Block block, int metadata, int modelId, RenderBlocks renderer )
	{
		// Get the tessellator instance
		Tessellator tessellator = Tessellator.instance;
		// What pass is this?
		
		IIcon texture = BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[0];

		GL11.glTranslatef( -0.5F, -0.5F, -0.5F );

		tessellator.startDrawingQuads();
		tessellator.setNormal( 0.0F, -1.0F, 0.0F );
		renderer.renderFaceYNeg( block, 0.0D, 0.0D, 0.0D, texture );
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal( 0.0F, 1.0F, 0.0F );
		renderer.renderFaceYPos( block, 0.0D, 0.0D, 0.0D, texture );
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal( 0.0F, 0.0F, -1.0F );
		renderer.renderFaceZNeg( block, 0.0D, 0.0D, 0.0D, texture );
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal( 0.0F, 0.0F, 1.0F );
		renderer.renderFaceZPos( block, 0.0D, 0.0D, 0.0D, texture );
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal( -1.0F, 0.0F, 0.0F );
		renderer.renderFaceXNeg( block, 0.0D, 0.0D, 0.0D, texture );
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal( 1.0F, 0.0F, 0.0F );
		renderer.renderFaceXPos( block, 0.0D, 0.0D, 0.0D, texture );
		tessellator.draw();

		GL11.glTranslatef( 0.5F, 0.5F, 0.5F );
	}

	@Override
	public boolean renderWorldBlock( IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer )
	{
		// What pass is this?
		if ( Renderers.currentRenderPass == Renderers.PASS_OPAQUE )
		{
			// Opaque pass, render the standard block
			renderer.renderStandardBlock( block, x, y, z );

		}
		else
		{
			// Alpha pass, get the providers color
			AEColor pColor = ( (TileEssentiaProvider)world.getTileEntity( x, y, z ) ).getGridColor();
			
			// Is the color not transparent?
			if( pColor != AEColor.Transparent )
			{
				// Render the overlay
				this.renderOverlay( world, x, y, z, pColor.mediumVariant );
			}
		}

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory( int modelId )
	{
		// Show the 3D model in the inventory
		return true;
	}

	@Override
	public int getRenderId()
	{
		// Return the ID of the essentia provider
		return Renderers.EssentiaProviderRenderID;
	}

	private void renderOverlay( IBlockAccess world, double x, double y, double z, int color )
	{
		// Get the tessellator instance
		Tessellator tessellator = Tessellator.instance;

		// Get the texture
		IIcon texture = BlockTextureManager.ESSENTIA_PROVIDER.getTextures()[1];

		// Set the drawing color
		tessellator.setColorOpaque_I( color );
		
		// Get the UV bounds
		double minU = texture.getMinU();
		double maxU = texture.getMaxU();
		double minV = texture.getMinV();
		double maxV = texture.getMaxV();

		// Calculate positions
		double x1 = x + 1.0D;
		double y1 = y + 1.0D;
		double z1 = z + 1.0D;
		double zSouth = z + POS_OFFSET;
		double zNorth = z + NEG_OFFSET;
		double xWest = x + POS_OFFSET;
		double xEast = x + NEG_OFFSET;
		double yUp = y + POS_OFFSET;
		double yDown = y + NEG_OFFSET;

		// South face
		tessellator.addVertexWithUV( x, y, zSouth, minU, maxV );
		tessellator.addVertexWithUV( x1, y, zSouth, maxU, maxV );
		tessellator.addVertexWithUV( x1, y1, zSouth, maxU, minV );
		tessellator.addVertexWithUV( x, y1, zSouth, minU, minV );

		// North face
		tessellator.addVertexWithUV( x, y1, zNorth, minU, minV );
		tessellator.addVertexWithUV( x1, y1, zNorth, maxU, minV );
		tessellator.addVertexWithUV( x1, y, zNorth, maxU, maxV );
		tessellator.addVertexWithUV( x, y, zNorth, minU, maxV );

		// West face
		tessellator.addVertexWithUV( xWest, y, z, maxU, minV );
		tessellator.addVertexWithUV( xWest, y1, z, maxU, maxV );
		tessellator.addVertexWithUV( xWest, y1, z1, minU, maxV );
		tessellator.addVertexWithUV( xWest, y, z1, minU, minV );

		// East face
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
		tessellator.addVertexWithUV( x, yDown, z, maxU, maxV );
		tessellator.addVertexWithUV( x1, yDown, z, minU, maxV );
		tessellator.addVertexWithUV( x1, yDown, z1, minU, minV );
		tessellator.addVertexWithUV( x, yDown, z1, maxU, minV );

	}

}
