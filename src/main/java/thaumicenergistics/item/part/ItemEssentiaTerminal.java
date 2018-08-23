package thaumicenergistics.item.part;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.model.ModelLoader;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.util.AEColor;

import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.item.ItemPartBase;
import thaumicenergistics.part.PartEssentiaTerminal;

/**
 * @author BrockWS
 */
public class ItemEssentiaTerminal extends ItemPartBase {

    public ItemEssentiaTerminal(String id) {
        super(id);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack stack) {
        return new PartEssentiaTerminal(this);
    }

    @Override
    public void initModel() {
        AEApi.instance().registries().partModels().registerModels(PartEssentiaTerminal.MODELS);
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":part/essentia_terminal"));
    }

    public static class TerminalItemColor implements IItemColor {

        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            return AEColor.TRANSPARENT.getVariantByTintIndex(tintIndex);

        }
    }
}
