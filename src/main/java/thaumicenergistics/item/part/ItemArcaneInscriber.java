package thaumicenergistics.item.part;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.part.PartArcaneInscriber;

import javax.annotation.Nullable;

/**
 * @author Alex811
 */
public class ItemArcaneInscriber extends ItemArcaneTerminal {
    public ItemArcaneInscriber(String id) {
        super(id);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack stack) {
        return new PartArcaneInscriber(this);
    }

    @Override
    public void initModel() {
        AEApi.instance().registries().partModels().registerModels(PartArcaneInscriber.MODELS);
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":part/arcane_inscriber"));
    }
}
