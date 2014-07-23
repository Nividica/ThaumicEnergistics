package thaumicenergistics.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public abstract class RenderBlockProviderBase
	implements ISimpleBlockRenderingHandler
{

	protected abstract IIcon getInventoryIcon();
	
	@Override
	public final void renderInventoryBlock( Block block, int metadata, int modelId, RenderBlocks renderer )
	{
		// Get the tessellator instance
		Tessellator tessellator = Tessellator.instance;
		// What pass is this?

		IIcon texture = this.getInventoryIcon();

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
	
	public abstract void renderWorldBlock( IBlockAccess world, int x, int y, int z, int blockBrightness );
	
	@Override
	public final boolean renderWorldBlock( IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer )
	{
		// Calculate the brightness based on light hitting each face
		int blockBrightness = world.getLightBrightnessForSkyBlocks(x+1, y, z, 0 )
						| world.getLightBrightnessForSkyBlocks(x-1, y, z, 0 )
						| world.getLightBrightnessForSkyBlocks(x, y+1, z, 0 )
						| world.getLightBrightnessForSkyBlocks(x, y-1, z, 0 )
						| world.getLightBrightnessForSkyBlocks(x, y, z+1, 0 )
						| world.getLightBrightnessForSkyBlocks(x, y, z-1, 0 );
		
		// Pass to sub
		this.renderWorldBlock( world, x, y, z, blockBrightness );

		// Return 
		return true;
	}

	@Override
	public final boolean shouldRender3DInInventory( int modelId )
	{
		return true;
	}

}
