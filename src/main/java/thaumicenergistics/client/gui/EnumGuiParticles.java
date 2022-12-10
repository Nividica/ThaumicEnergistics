package thaumicenergistics.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Particles that can be displayed in a GUI.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public enum EnumGuiParticles {
    Orb(thaumcraft.client.fx.ParticleEngine.particleTexture, 0, 128, 16, 16, 16, 0, 16),
    Rune(thaumcraft.client.fx.ParticleEngine.particleTexture, 0, 96, 16, 16, 16, 0, 16),
    Wispy(thaumcraft.client.fx.ParticleEngine.particleTexture, 0, 80, 16, 16, 16, 0, 16),
    Knowledge(thaumcraft.client.fx.ParticleEngine.particleTexture, 0, 64, 16, 16, 16, 0, 4);

    /**
     * Location of the particle in the texture.
     */
    private int txU, txV, width, height;

    /**
     * Amount that U and V change during each animation step.
     */
    private int animationUStep, animationVStep;

    /**
     * Number of frames in the animation.
     * Can not be 0.
     */
    private int FrameCount;

    /**
     * Texture used to draw the particle.
     */
    private ResourceLocation texture;

    private EnumGuiParticles(
            final ResourceLocation textureLocation,
            final int U,
            final int V,
            final int width,
            final int height,
            final int UStep,
            final int VStep,
            final int Frames) {
        this.texture = textureLocation;
        this.txU = U;
        this.txV = V;
        this.width = width;
        this.height = height;
        this.animationUStep = UStep;
        this.animationVStep = VStep;
        this.FrameCount = Frames;
    }

    /**
     * Resets the engine state.
     */
    public static void finishDraw() {
        // Set color to white
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Disable blending
        GL11.glDisable(GL11.GL_BLEND);
    }

    /**
     * Draws the particle.
     * If settings "needsPrepare" to false, prepare and finish must be called manually.
     * However this can be useful if drawing multiple of the same particle.
     *
     * @param gui
     * @param positionX
     * @param positionY
     * @param frameNumber
     * @param red
     * @param green
     * @param blue
     * @param needsPrepare
     */
    public void drawParticle(
            final Gui gui,
            final int positionX,
            final int positionY,
            final int frameNumber,
            final float red,
            final float green,
            final float blue,
            final boolean needsPrepare) {
        if (needsPrepare) {
            this.prepareDraw();
        }

        // Wrap the frame number
        int frameIndex = frameNumber % this.FrameCount;

        // Calculate the U position
        int U = this.txU + (this.animationUStep * frameIndex);

        // Calculate the V position
        int V = this.txV + (this.animationVStep * frameIndex);

        // Set tint color
        GL11.glColor4f(red, green, blue, 0.90f);

        // Draw the particle
        gui.drawTexturedModalRect(positionX, positionY, U, V, this.width, this.height);

        if (needsPrepare) {
            EnumGuiParticles.finishDraw();
        }
    }

    /**
     * Prepares the engine to draw this particle.
     * Must be called before drawing, but can be called once then
     * draw called multiple times for the same particle.
     */
    public void prepareDraw() {
        // Bind the texture
        Minecraft.getMinecraft().renderEngine.bindTexture(this.texture);

        // Enable alpha blending
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    }
}
