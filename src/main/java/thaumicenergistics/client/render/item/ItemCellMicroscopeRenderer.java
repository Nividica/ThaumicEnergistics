package thaumicenergistics.client.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import thaumcraft.client.lib.UtilsFX;

public class ItemCellMicroscopeRenderer implements IItemRenderer {

    private IModelCustom model;
    private static final ResourceLocation SCANNER = new ResourceLocation(
            "thaumicenergistics",
            "textures/models/microscope.obj");

    public ItemCellMicroscopeRenderer() {
        this.model = net.minecraftforge.client.model.AdvancedModelLoader.loadModel(SCANNER);
    }

    public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
        return true;
    }

    public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item,
            IItemRenderer.ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Minecraft mc = Minecraft.getMinecraft();
        int rve_id = 0;
        int player_id = 0;
        if (type == ItemRenderType.EQUIPPED) {
            rve_id = mc.renderViewEntity.getEntityId();
            player_id = ((EntityLivingBase) data[1]).getEntityId();
        }
        EntityPlayer playermp = mc.thePlayer;
        float partialTicks = UtilsFX.getTimer(mc).renderPartialTicks;
        float unknownVar = 0.8F;
        EntityPlayerSP playersp = (EntityPlayerSP) playermp;
        GL11.glPushMatrix();
        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON && player_id == rve_id
                && mc.gameSettings.thirdPersonView == 0) {
            GL11.glTranslatef(1.0F, 0.75F, -1.0F);
            GL11.glRotatef(135.0F, 0.0F, -1.0F, 0.0F);
            float f3 = playersp.prevRenderArmPitch
                    + (playersp.renderArmPitch - playersp.prevRenderArmPitch) * partialTicks;
            float f4 = playersp.prevRenderArmYaw + (playersp.renderArmYaw - playersp.prevRenderArmYaw) * partialTicks;
            GL11.glRotatef((playermp.rotationPitch - f3) * 0.1F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef((playermp.rotationYaw - f4) * 0.1F, 0.0F, 1.0F, 0.0F);
            float var10000 = playermp.prevRotationPitch
                    + (playermp.rotationPitch - playermp.prevRotationPitch) * partialTicks;
            float newProgress = UtilsFX.getPrevEquippedProgress(mc.entityRenderer.itemRenderer)
                    + (UtilsFX.getEquippedProgress(mc.entityRenderer.itemRenderer)
                            - UtilsFX.getPrevEquippedProgress(mc.entityRenderer.itemRenderer)) * partialTicks;
            GL11.glTranslatef(
                    -0.7F * unknownVar,
                    -(-0.65F * unknownVar) + (1.0F - newProgress) * 1.5F,
                    0.9F * unknownVar);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(0.0F, 0.0F * unknownVar, -0.9F * unknownVar);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);

            GL11.glEnable(55);
            GL11.glPushMatrix();
            GL11.glScalef(5.0F, 5.0F, 5.0F);
            mc.renderEngine.bindTexture(mc.thePlayer.getLocationSkin());

            for (int x = 0; x < 2; ++x) {
                int var22 = x * 2 - 1;
                GL11.glPushMatrix();
                GL11.glTranslatef(-0.0F, -0.6F, 1.1F * (float) var22);
                GL11.glRotatef((float) (-45 * var22), 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(59.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef((float) (-65 * var22), 0.0F, 1.0F, 0.0F);
                Render var24 = RenderManager.instance.getEntityRenderObject(mc.thePlayer);
                RenderPlayer var26 = (RenderPlayer) var24;
                float var13 = 1.0F;
                GL11.glScalef(var13, var13, var13);
                var26.renderFirstPersonArm(mc.thePlayer);
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(0.4F, -0.8F, 0.0F);
            GL11.glEnable(55);
            GL11.glScalef(2.0F, 2.0F, 2.0F);
        } else {
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            if (type == ItemRenderType.EQUIPPED) {
                GL11.glTranslatef(1.6F, 0.3F, 2.0F);
                GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
                GL11.glRotatef(30.0F, 0.0F, 0.0F, -1.0F);
            } else if (type == ItemRenderType.INVENTORY) {
                GL11.glScalef(2.5F, 2.5F, 2.5F);
                GL11.glRotatef(60.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(30.0F, 0.0F, 0.0F, -1.0F);
                GL11.glRotatef(248.0F, 0.0F, -1.0F, 0.0F);
            }
        }
        UtilsFX.bindTexture("textures/models/scanner.png");
        this.model.renderAll();
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.11F, 0.0F);
        GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        UtilsFX.renderQuadCenteredFromTexture(
                (String) "textures/models/scanscreen.png",
                0.8F,
                1.0F,
                1.0F,
                1.0F,
                (int) (190.0F
                        + MathHelper.sin((float) (playermp.ticksExisted - playermp.worldObj.rand.nextInt(2))) * 10.0F
                        + 10.0F),
                771,
                1.0F);
        GL11.glTranslatef(0.0F, 0.0F, -2.5F);
        UtilsFX.renderQuadCenteredFromTexture(
                (String) "textures/models/scanscreen.png",
                0.6F,
                1.0F,
                1.0F,
                1.0F,
                (int) (190.0F
                        + MathHelper.sin((float) (playermp.ticksExisted - playermp.worldObj.rand.nextInt(2))) * 10.0F
                        + 10.0F),
                771,
                1.0F);
        if (playermp instanceof EntityPlayer && type == ItemRenderType.EQUIPPED_FIRST_PERSON
                && player_id == rve_id
                && mc.gameSettings.thirdPersonView == 0) {
            RenderHelper.disableStandardItemLighting();
            int j = (int) (190.0F
                    + MathHelper.sin((float) (playermp.ticksExisted - playermp.worldObj.rand.nextInt(2))) * 10.0F
                    + 10.0F);
            int k = j % 65536;
            int l = j / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k / 1.0F, (float) l / 1.0F);
            RenderHelper.enableGUIStandardItemLighting();
        }
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
