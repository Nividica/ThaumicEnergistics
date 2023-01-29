package thaumicenergistics.client.render;

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
import thaumicenergistics.client.render.model.ModelArcaneAssembler;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.tiles.TileArcaneAssembler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Renders the {@link TileArcaneAssembler}
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class RenderTileArcaneAssembler extends TileEntitySpecialRenderer {

    /**
     * Assembler model.
     */
    private final ModelArcaneAssembler assemblerModel = new ModelArcaneAssembler();

    /**
     * Texture
     */
    private final ResourceLocation assemblerTexture = new ResourceLocation(
            ThaumicEnergistics.MOD_ID,
            "textures/models/arcane.assembler.png");

    /**
     * Cache of the assembler block.
     */
    private final Block assemblerBlock = ThEApi.instance().blocks().ArcaneAssembler.getBlock();

    private void renderAssembler(final TileArcaneAssembler assemblerTile, final World world, final int x, final int y,
            final int z) {
        // Ensure there is a world object
        if (world != null) {
            // Get the block lightning
            float mixedBrightness = this.assemblerBlock.getMixedBrightnessForBlock(world, x, y, z);
            int light = world.getLightBrightnessForSkyBlocks(x, y, z, 0);

            int l1 = light % 65536;
            int l2 = light / 65536;

            // Set the color based on the mixed brightness
            Tessellator.instance.setColorOpaque_F(mixedBrightness, mixedBrightness, mixedBrightness);

            // Set the lightmap coords
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, l1, l2);
        } else {
            // No world object, render at full brightness
            Tessellator.instance.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        }

        // Push the matrix
        GL11.glPushMatrix();

        // Center the model
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);

        // Bind the model texture
        Minecraft.getMinecraft().renderEngine.bindTexture(this.assemblerTexture);

        // Scale down
        GL11.glScalef(0.047F, 0.047F, 0.047F);

        // Render the assembler
        this.assemblerModel.render(null, 0, 0, -0.1F, 0, 0, 0.625F);

        // Pop the matrix
        GL11.glPopMatrix();
    }

    /**
     * Called when the assembler needs to be rendered.
     */
    @Override
    public void renderTileEntityAt(final TileEntity tileEntity, final double d, final double d1, final double d2,
            final float f) {
        // Push the GL matrix
        GL11.glPushMatrix();

        // Computes the proper place to draw
        GL11.glTranslatef((float) d, (float) d1, (float) d2);

        // Get the assembler
        TileArcaneAssembler assemblerTile = (TileArcaneAssembler) tileEntity;

        // Render the gearbox
        this.renderAssembler(
                assemblerTile,
                tileEntity.getWorldObj(),
                tileEntity.xCoord,
                tileEntity.yCoord,
                tileEntity.zCoord);

        // Pop the GL matrix
        GL11.glPopMatrix();
    }
}
