package thaumicenergistics.client;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEColor;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.item.ItemDummyAspect;

/**
 * Contains all implementations of IItemColor
 *
 * @author BrockWS
 */
@SideOnly(Side.CLIENT)
public class ThEItemColors {

    public static void registerItemColors() {
        IItemColor dummyItemColor = new ThEItemColors.DummyAspectItemColors();
        ThEApi.instance().items().dummyAspect().maybeItem()
                .ifPresent(item -> Minecraft.getMinecraft().getItemColors().registerItemColorHandler(dummyItemColor, item));

        IItemColor terminalItemColor = new ThEItemColors.TerminalItemColor();
        //IBlockColor terminalBlockColor = new ThEItemColors.TerminalBlockColor();
        ThEApi.instance().items().arcaneTerminal().maybeItem().ifPresent(item -> {
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(terminalItemColor, item);
            // Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(terminalBlockColor, item);
        });
        ThEApi.instance().items().essentiaTerminal().maybeItem()
                .ifPresent(item -> Minecraft.getMinecraft().getItemColors().registerItemColorHandler(terminalItemColor, item));
    }

    @SideOnly(Side.CLIENT)
    public static class TerminalBlockColor implements IBlockColor {
        @Override
        public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            if (tintIndex == 10)
                return AEColor.LIME.blackVariant;
            return AEColor.TRANSPARENT.getVariantByTintIndex(tintIndex);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class TerminalItemColor implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            return AEColor.TRANSPARENT.getVariantByTintIndex(tintIndex);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class DummyAspectItemColors implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            ItemDummyAspect item = (ItemDummyAspect) stack.getItem();
            return item.getAspect(stack).getColor();
        }
    }
}
