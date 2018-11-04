package thaumicenergistics.item.part;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;

import appeng.api.AEApi;
import appeng.api.parts.IPart;

import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.item.ItemPartBase;
import thaumicenergistics.part.PartArcaneTerminal;

/**
 * @author BrockWS
 */
public class ItemArcaneTerminal extends ItemPartBase {

    public ItemArcaneTerminal(String id) {
        super(id);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack stack) {
        return new PartArcaneTerminal(this);
    }

    @Override
    public void initModel() {
        AEApi.instance().registries().partModels().registerModels(PartArcaneTerminal.MODELS);
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":part/arcane_terminal"));
    }
}
