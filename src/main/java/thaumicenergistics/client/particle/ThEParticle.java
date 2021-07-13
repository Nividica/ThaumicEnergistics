package thaumicenergistics.client.particle;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.init.ModGlobals;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Color;

/**
 * Makes custom particles!
 * Supports:
 * animated particles {@link #setFrameCount(int)}
 * changing texture hue {@link #setTint(Color, Color, float)}
 * changing texture opacity {@link #setAlpha(float, float, float)}
 * changing particle size {@link #setScale(float, float, float)}
 * you can change the hue, opacity and size progressively and/or randomly
 * @author Alex811
 */
@SideOnly(Side.CLIENT)
public class ThEParticle extends Particle {
    private static final VertexFormat VERTEX_FORMAT = (new VertexFormat()).addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.COLOR_4UB).addElement(DefaultVertexFormats.TEX_2S).addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
    private double[][] texMap = {{1.0F, 1.0F}, {1.0F, 0.0F}, {0.0F, 0.0F}, {0.0F, 1.0F}};
    private float frameHeight;
    private int frameCurr;
    private int frameCount = 1;
    private Color initialTint = Color.WHITE;
    private Color finalTint = Color.WHITE;
    private float initialAlpha = 1.0F;
    private float finalAlpha = 1.0F;
    private float initialScale = 1.0F;
    private float finalScale = 1.0F;
    private float progress;
    private ResourceLocation resourceLocation;

    /**
     * Use this for moving particles, to set overall speed see {@link #multiplyVelocity(float)}
     * @param textureName texture filename, should be inside textures/particle
     * @param worldIn world to spawn in
     * @param xCoordIn x coordinate to spawn in
     * @param yCoordIn y coordinate to spawn in
     * @param zCoordIn z coordinate to spawn in
     * @param xSpeedIn initial speed in x axis
     * @param ySpeedIn initial speed in y axis
     * @param zSpeedIn initial speed in z axis
     */
    public ThEParticle(String textureName, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.motionY -= 0.10000000149011612D;
        init(textureName);
    }

    /**
     * Use this for particles that spawn motionless
     * @param textureName texture filename, should be inside textures/particle
     * @param worldIn world to spawn in
     * @param xCoordIn x coordinate to spawn in
     * @param yCoordIn y coordinate to spawn in
     * @param zCoordIn z coordinate to spawn in
     */
    public ThEParticle(String textureName, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn){
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        init(textureName);
    }

    protected void init(String textureName){
        this.resourceLocation = new ResourceLocation(ModGlobals.MOD_ID, "textures/particle/" + textureName + ".png");
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        processFrame();
    }

    /**
     * Set speed multiplier
     */
    @Override
    @MethodsReturnNonnullByDefault
    public Particle multiplyVelocity(float multiplier) {
        this.motionX *= multiplier;
        this.motionY *= multiplier;
        this.motionZ *= multiplier;
        return this;
    }

    public void processFrame(){
        progress = ((float) this.particleAge) / ((float) this.particleMaxAge);
        if(this.frameCount > 1){
            this.frameCurr = Math.round(progress * (this.frameCount - 1));
            float frameCurrTop = this.frameHeight * this.frameCurr;
            float frameCurrBottom = frameCurrTop + this.frameHeight;
            texMap = new double[][]{{1.0F, frameCurrBottom}, {1.0F, frameCurrTop}, {0.0F, frameCurrTop}, {0.0F, frameCurrBottom}};
        }
        if(this.initialAlpha != this.finalAlpha)
            this.particleAlpha = valFromProgress(this.initialAlpha, this.finalAlpha);
        if(this.initialScale != this.finalScale)
            this.particleScale = valFromProgress(this.initialScale, this.finalScale);
        if(!this.initialTint.equals(this.finalTint))
            setRBGColorF(valFromProgress(this.initialTint.getRed(), this.finalTint.getRed()), valFromProgress(this.initialTint.getGreen(), this.finalTint.getGreen()), valFromProgress(this.initialTint.getBlue(), this.finalTint.getBlue()));
    }

    /**
     * Same as {@link Particle#setRBGColorF(float, float, float)} but for integers 0 - 255
     * @param r Red 0 - 255
     * @param g Green 0 - 255
     * @param b Blue 0 - 255
     */
    public void setRBGColorF(int r, int g, int b){
        setRBGColorF(r / 255.0F, g / 255.0F, b / 255.0F);
    }

    protected float valFromProgress(float init, float fin){
        return init + progress * (fin - init);
    }

    protected float getRand(float deviation){
        return 1.0F - deviation + this.rand.nextFloat() * 2.0F * deviation;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderParticle(BufferBuilder bufferIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if(Minecraft.getMinecraft().isGamePaused()) return;
        processFrame();

        float vecScale = 0.1F * this.particleScale;
        float xInterp = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float yInterp = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float zInterp = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        int brightness = this.getBrightnessForRender(partialTicks);
        int skyL = brightness >> 16 & 65535;
        int blockL = brightness & 65535;

        rotationX *= vecScale;
        rotationXY *= vecScale;
        rotationZ *= vecScale;
        rotationYZ *= vecScale;
        rotationXZ *= vecScale;
        Vec3d[] avec3d = new Vec3d[] {
                new Vec3d(-rotationX - rotationXY,-rotationZ, -rotationYZ - rotationXZ),
                new Vec3d(-rotationX + rotationXY, rotationZ, -rotationYZ + rotationXZ),
                new Vec3d(rotationX + rotationXY, rotationZ, rotationYZ + rotationXZ),
                new Vec3d(rotationX - rotationXY, -rotationZ, rotationYZ - rotationXZ)
        };

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, this.particleAlpha);

        Minecraft.getMinecraft().getTextureManager().bindTexture(this.resourceLocation);

        if (this.particleAngle != 0.0F)
        {
            float angleInterp = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
            float vecMod = MathHelper.cos(angleInterp * 0.5F);
            float vecModSq = vecMod * vecMod;
            float vecMod2 = 2.0F * vecMod;
            float sin = MathHelper.sin(angleInterp * 0.5F);
            float xComponent = sin * (float)cameraViewDir.x;
            float yComponent = sin * (float)cameraViewDir.y;
            float zComponent = sin * (float)cameraViewDir.z;
            Vec3d vec3d = new Vec3d(xComponent, yComponent, zComponent);

            for (int i = 0; i < 4; ++i)
                avec3d[i] = vec3d.scale(2.0D * avec3d[i].dotProduct(vec3d))
                        .add(avec3d[i].scale(vecModSq - vec3d.dotProduct(vec3d)))
                        .add(vec3d.crossProduct(avec3d[i]).scale(vecMod2));
        }

        bufferIn.begin(GL11.GL_QUADS, VERTEX_FORMAT);
        GL11.glColor3f(1,0,0);
        for(int i = 0; i < 4; i++)
            bufferIn.pos(xInterp + avec3d[i].x, yInterp + avec3d[i].y, zInterp + avec3d[i].z)
                    .tex(texMap[i][0], texMap[i][1])
                    .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
                    .lightmap(skyL, blockL)
                    .normal(0.0F, 0.0F, 0.0F)
                    .endVertex();

        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
    }

    public ThEParticle setGravity(float gravity){
        this.particleGravity = gravity;
        return this;
    }

    /**
     * Set opacity
     * @param initialAlpha Alpha to start with
     * @param finalAlpha Alpha to end at
     * @param randomness 0 - 1
     * @return The particle, for convenience
     */
    public ThEParticle setAlpha(float initialAlpha, float finalAlpha, float randomness){
        this.initialAlpha = initialAlpha;
        this.finalAlpha = finalAlpha;
        if(randomness > 0.0F){
            this.initialAlpha *= safeFloat(getRand(randomness));
            this.finalAlpha *= safeFloat(getRand(randomness));
        }
        this.particleAlpha = this.initialAlpha;
        return this;
    }

    /**
     * Set particle scale
     * @param initialScale Scale to start with
     * @param finalScale Scale to end at
     * @param randomness 0 - 1
     * @return The particle, for convenience
     */
    public ThEParticle setScale(float initialScale, float finalScale, float randomness){
        this.initialScale = initialScale;
        this.finalScale = finalScale;
        if(randomness > 0.0F){
            this.initialScale *= getRand(randomness);
            this.finalScale *= getRand(randomness);
        }
        this.particleScale = this.initialScale;
        return this;
    }

    /**
     * Set texture hue
     * @param initialTint Color/tint to start with
     * @param finalTint Color/tint to end at
     * @param randomness 0 - 1
     * @return The particle, for convenience
     */
    public ThEParticle setTint(Color initialTint, Color finalTint, float randomness){
        this.initialTint = initialTint;
        this.finalTint = finalTint;
        if(randomness > 0.0F){
            this.initialTint = randColor(this.initialTint, randomness);
            this.finalTint = randColor(this.finalTint, randomness);
        }
        setRBGColorF(this.initialTint.getRed(), this.initialTint.getGreen(), this.initialTint.getBlue());
        return this;
    }

    /**
     * Randomizes colors
     * @param color Color to randomize
     * @param randomness 0 - 1
     * @return the new Color
     */
    protected Color randColor(Color color, float randomness){
        return safeColor(color.getRed() * getRand(randomness) / 255, color.getGreen() * getRand(randomness) / 255, color.getBlue() * getRand(randomness) / 255);
    }

    /**
     * @param r Red value of the new Color
     * @param g Green value of the new Color
     * @param b Blue value of the new Color
     * @return a new Color, makes sure the RGB values are within the acceptable range (0 - 1)
     */
    protected Color safeColor(float r, float g, float b){
        return new Color(safeFloat(r), safeFloat(g), safeFloat(b));
    }

    /**
     * @param x the float we want to limit
     * @return the passed float, forced into the range 0 - 1
     */
    protected float safeFloat(float x){
        return Math.max(Math.min(x, 1), 0);
    }

    /**
     * Use this to make animated particles!
     * All the frames should be in the same texture, stacked in a column.
     * @param frameCount the number of frames contained in the texture
     */
    public void setFrameCount(int frameCount){
        this.frameHeight = 1.0F / frameCount;
        this.frameCount = frameCount;
        this.frameCurr = 0;
    }
}
