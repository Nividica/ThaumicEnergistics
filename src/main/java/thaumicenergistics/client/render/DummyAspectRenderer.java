package thaumicenergistics.client.render;

import org.lwjgl.opengl.GL11;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.item.ItemDummyAspect;

import static net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

/**
 * @author BrockWS
 */
@SideOnly(Side.CLIENT)
public class DummyAspectRenderer extends TileEntityItemStackRenderer {

    public static DummyAspectRenderer INSTANCE;
    public static TransformType transformType = TransformType.GUI;

    static {
        INSTANCE = new DummyAspectRenderer();
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        Item item = stack.getItem();
        if (item instanceof ItemDummyAspect && ((ItemDummyAspect) item).getAspect(stack) != null) {
            Aspect aspect = ((ItemDummyAspect) item).getAspect(stack);
            GlStateManager.pushMatrix();
            Minecraft.getMinecraft().getTextureManager().bindTexture(aspect.getImage());
            GL11.glBlendFunc(770, 771);
            GlStateManager.disableLighting();
            // GlStateManager.scale(0.063f, 0.063f, 0);
            GlStateManager.rotate(180f, 1, 1, 0);
            GlStateManager.rotate(90f, 0, 0, 1);
            GlStateManager.translate(0f, -1f, 0);

            Color c = new Color(aspect.getColor());
            GlStateManager.color((float) c.getRed() / 255.0F, (float) c.getGreen() / 255.0F, (float) c.getBlue() / 255.0F, 1.0f);

            Tessellator tess = Tessellator.getInstance();
            tess.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            tess.getBuffer().pos(0.0D, 1.0D, 0).tex(0.0D, 1.0D).color((float) c.getRed() / 255.0F, (float) c.getGreen() / 255.0F, (float) c.getBlue() / 255.0F, 1.0f).endVertex();
            tess.getBuffer().pos(1.0D, 1.0D, 0).tex(1.0D, 1.0D).color((float) c.getRed() / 255.0F, (float) c.getGreen() / 255.0F, (float) c.getBlue() / 255.0F, 1.0f).endVertex();
            tess.getBuffer().pos(1.0D, 0.0D, 0).tex(1.0D, 0.0D).color((float) c.getRed() / 255.0F, (float) c.getGreen() / 255.0F, (float) c.getBlue() / 255.0F, 1.0f).endVertex();
            tess.getBuffer().pos(0.0D, 0.0D, 0).tex(0.0D, 0.0D).color((float) c.getRed() / 255.0F, (float) c.getGreen() / 255.0F, (float) c.getBlue() / 255.0F, 1.0f).endVertex();
            tess.draw();
            GlStateManager.enableLighting();

            GlStateManager.popMatrix();
        }
    }
}
