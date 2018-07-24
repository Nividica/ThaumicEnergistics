package thaumicenergistics.item.part;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.parts.IPart;

import thaumicenergistics.item.ItemPartBase;
import thaumicenergistics.part.PartEssentiaStorageBus;

/**
 * @author BrockWS
 */
public class ItemEssentiaStorageBus extends ItemPartBase {

    public ItemEssentiaStorageBus(String id) {
        super(id);
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack is) {
        return new PartEssentiaStorageBus(this);
    }

    @Override
    public void initModel() {
        AEApi.instance().registries().partModels().registerModels(PartEssentiaStorageBus.MODELS);
    }
}
