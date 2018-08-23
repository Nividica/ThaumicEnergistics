package thaumicenergistics.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
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
            Minecraft mc = Minecraft.getMinecraft();
            Tessellator tessellator = Tessellator.getInstance();
            Aspect aspect = ((ItemDummyAspect) item).getAspect(stack);
            IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null);
            // I Don't know how to do
            return;
        }
        super.renderByItem(stack, partialTicks);
    }
}
