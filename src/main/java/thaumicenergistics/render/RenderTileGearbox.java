package thaumicenergistics.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.render.model.ModelGearbox;
import thaumicenergistics.tileentities.TileGearBox;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTileGearbox
	extends TileEntitySpecialRenderer
{
	/**
	 * Gearbox model
	 */
	private ModelGearbox gearboxModel = new ModelGearbox();

	/**
	 * Textures
	 */
	private static final ResourceLocation TEX_IRON = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/models/gearbox.iron.png" ),
					TEX_THAUMIUM = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/models/gearbox.thaumium.png" );

	/**
	 * Renders the gearbox.
	 * 
	 * @param gearboxTile
	 * @param world
	 * @param i
	 * @param j
	 * @param k
	 * @param block
	 */
	private void renderGearbox( final TileGearBox gearboxTile, final World world, final int i, final int j, final int k, final Block block )
	{
		// Get the block lightning
		float mixedBrightness = block.getMixedBrightnessForBlock( world, i, j, k );
		int light = world.getLightBrightnessForSkyBlocks( i, j, k, 0 );

		int l1 = light % 65536;
		int l2 = light / 65536;

		// Set the color based on the mixed brightness
		Tessellator.instance.setColorOpaque_F( mixedBrightness, mixedBrightness, mixedBrightness );

		// Set the lightmap coords
		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, l1, l2 );

		// Push the matrix
		GL11.glPushMatrix();

		// Center the model
		GL11.glTranslatef( 0.5F, 0.5F, 0.5F );

		// Bind the model texture
		if( gearboxTile.isThaumiumGearbox() )
		{
			this.bindTexture( RenderTileGearbox.TEX_THAUMIUM );
		}
		else
		{
			this.bindTexture( RenderTileGearbox.TEX_IRON );
		}

		// Scale down
		GL11.glScalef( 0.12F, 0.12F, 0.12F );

		// Update the model.
		this.gearboxModel.updateToTileEntity( gearboxTile );

		// Render the gearbox
		this.gearboxModel.render( null, 0, 0, -0.1F, 0, 0, 0.625F );

		// Pop the matrix
		GL11.glPopMatrix();
	}

	/**
	 * Called when a gearbox tile entity needs to be rendered.
	 * 
	 * @param tileEntity
	 * @param d
	 * @param d1
	 * @param d2
	 * @param f
	 */
	@Override
	public void renderTileEntityAt( final TileEntity tileEntity, final double d, final double d1, final double d2, final float f )
	{
		// Push the GL matrix
		GL11.glPushMatrix();

		// Computes the proper place to draw
		GL11.glTranslatef( (float)d, (float)d1, (float)d2 );

		// Get the gearbox
		TileGearBox gearBox = (TileGearBox)tileEntity;

		// Render the gearbox
		this.renderGearbox( gearBox, tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord,
			ThEApi.instance().blocks().IronGearBox.getBlock() );

		// Pop the GL matrix
		GL11.glPopMatrix();
	}
}