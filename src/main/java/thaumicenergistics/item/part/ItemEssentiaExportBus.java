package thaumicenergistics.item.part;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.parts.IPart;

import thaumicenergistics.item.ItemPartBase;
import thaumicenergistics.part.PartEssentiaExportBus;

/**
 * @author BrockWS
 */
public class ItemEssentiaExportBus extends ItemPartBase {

    public ItemEssentiaExportBus(String id) {
        super(id);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack stack) {
        return new PartEssentiaExportBus(this);
    }

    @Override
    public void initModel() {
        AEApi.instance().registries().partModels().registerModels(PartEssentiaExportBus.MODELS);
    }
}
