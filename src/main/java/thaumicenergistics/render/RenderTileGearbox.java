package thaumicenergistics.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.ThaumicEnergistics;
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
	private final ModelGearbox gearboxModel = new ModelGearbox();

	/**
	 * Textures
	 */
	private final ResourceLocation TEX_IRON = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/models/gearbox.iron.png" ),
					TEX_THAUMIUM = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/models/gearbox.thaumium.png" );

	/**
	 * Cache of the gearbox block.
	 */
	private final Block gearboxBlock = ThEApi.instance().blocks().IronGearBox.getBlock();

	/**
	 * Renders the gearbox.
	 * 
	 * @param gearboxTile
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param block
	 */
	private void renderGearbox( final TileGearBox gearboxTile, final World world, final int x, final int y, final int z )
	{
		// Ensure there is a world object
		if( world != null )
		{
			// Get the block lightning
			float mixedBrightness = this.gearboxBlock.getMixedBrightnessForBlock( world, x, y, z );
			int light = world.getLightBrightnessForSkyBlocks( x, y, z, 0 );

			int l1 = light % 65536;
			int l2 = light / 65536;

			// Set the color based on the mixed brightness
			Tessellator.instance.setColorOpaque_F( mixedBrightness, mixedBrightness, mixedBrightness );

			// Set the lightmap coords
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, l1, l2 );
		}
		else
		{
			// No world object, render at full brightness
			Tessellator.instance.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
		}

		// Push the matrix
		GL11.glPushMatrix();

		// Center the model
		GL11.glTranslatef( 0.5F, 0.5F, 0.5F );

		// Bind the model texture
		if( gearboxTile.isThaumiumGearbox() )
		{
			Minecraft.getMinecraft().renderEngine.bindTexture( this.TEX_THAUMIUM );
		}
		else
		{
			Minecraft.getMinecraft().renderEngine.bindTexture( this.TEX_IRON );
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
		this.renderGearbox( gearBox, tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord );

		// Pop the GL matrix
		GL11.glPopMatrix();
	}
}