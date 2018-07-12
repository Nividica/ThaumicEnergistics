package thaumicenergistics.integration.appeng;

import io.netty.buffer.ByteBuf;
import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.IThEIntegration;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

import appeng.api.AEApi;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IItemList;

/**
 * @author BrockWS
 */
public class ThEAppliedEnergistics implements IThEIntegration {

    @Override
    public void preInit() {
        AEApi.instance().storage().registerStorageChannel(IEssentiaStorageChannel.class, new EssentiaStorageChannel());
    }

    @Override
    public void init() {
        AEApi.instance().registries().cell().addCellHandler(new BasicEssentiaCellHandler());
    }

    @Override
    public void postInit() {

    }

    public static class EssentiaStorageChannel implements IEssentiaStorageChannel {

        @Nonnull
        @Override
        public IItemList<IAEEssentiaStack> createList() {
            return new EssentiaList();
        }

        @Nullable
        @Override
        public IAEEssentiaStack createStack(@Nonnull Object o) {
            if (o instanceof EssentiaStack) {
                return AEEssentiaStack.fromEssentiaStack((EssentiaStack) o);
            } else if (o instanceof AEEssentiaStack) {
                return ((AEEssentiaStack) o).copy();
            }
            return null;
        }

        @Nullable
        @Override
        public IAEEssentiaStack readFromPacket(@Nonnull ByteBuf byteBuf) throws IOException {
            // TODO
            return null;
        }

        @Nullable
        @Override
        public IAEEssentiaStack poweredExtraction(@Nonnull IEnergySource iEnergySource, @Nonnull IMEInventory<IAEEssentiaStack> imeInventory, @Nonnull IAEEssentiaStack iaeEssentiaStack, @Nonnull IActionSource iActionSource) {
            // TODO
            return null;
        }

        @Nullable
        @Override
        public IAEEssentiaStack poweredInsert(@Nonnull IEnergySource iEnergySource, @Nonnull IMEInventory<IAEEssentiaStack> imeInventory, @Nonnull IAEEssentiaStack iaeEssentiaStack, @Nonnull IActionSource iActionSource) {
            // TODO
            return null;
        }
    }
}
