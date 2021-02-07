package thaumicenergistics.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.particle.ParticleCrafting;
import thaumicenergistics.tile.TileArcaneAssembler;

/**
 * @author Alex811
 */
@SideOnly(Side.CLIENT)
public class ArcaneAssemblerRenderer extends TileEntitySpecialRenderer<TileArcaneAssembler> {
    private final double particleMultiplier = ThEApi.instance().config().arcaneAssemblerParticleMultiplier();
    private double particleProgress = 0.0F;

    @Override
    public void render(TileArcaneAssembler te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        final ItemStack renderedItem = te.getCraftingInv().getStackInSlot(0);
        if(renderedItem != null && !renderedItem.isEmpty()){
            GlStateManager.pushMatrix();

            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
            float degrees = (float) (Minecraft.getSystemTime() * 0.05D % 360);
            GlStateManager.rotate(degrees, 0, 1, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);

            GlStateManager.disableLighting();
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(renderedItem, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.enableLighting();

            GlStateManager.popMatrix();
            if(!Minecraft.getMinecraft().isGamePaused()){
                particleProgress += particleMultiplier;
                if(particleProgress >= 1){
                    for(int i = 0; i < (int) particleProgress; i++)
                        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCrafting(getWorld(), (double) te.getPos().getX() + 0.5D, (double) te.getPos().getY() + 0.5D, (double) te.getPos().getZ() + 0.5D));
                    particleProgress %= 1.0;
                }
            }
        }
    }
}
