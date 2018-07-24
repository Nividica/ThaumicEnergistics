package thaumicenergistics.item.part;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.parts.IPart;

import thaumicenergistics.item.ItemPartBase;
import thaumicenergistics.part.PartEssentiaImportBus;

/**
 * @author BrockWS
 */
public class ItemEssentiaImportBus extends ItemPartBase {

    public ItemEssentiaImportBus(String id) {
        super(id);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack stack) {
        return new PartEssentiaImportBus(this);
    }

    @Override
    public void initModel() {
        AEApi.instance().registries().partModels().registerModels(PartEssentiaImportBus.MODELS);
    }
}
