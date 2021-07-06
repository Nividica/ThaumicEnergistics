package thaumicenergistics.integration.appeng;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.storage.data.IItemList;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.IThEIntegration;
import thaumicenergistics.integration.appeng.cell.CreativeEssentiaCellHandler;

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
        AEApi.instance().registries().cell().addCellHandler(new CreativeEssentiaCellHandler());

        ThEApi.instance().items().essentiaImportBus().maybeStack(1).ifPresent(stack -> {
            Upgrades.REDSTONE.registerItem(stack, 1);
            Upgrades.CAPACITY.registerItem(stack, 2);
            Upgrades.SPEED.registerItem(stack, 4);
        });
        ThEApi.instance().items().essentiaExportBus().maybeStack(1).ifPresent(stack -> {
            Upgrades.REDSTONE.registerItem(stack, 1);
            Upgrades.CAPACITY.registerItem(stack, 2);
            Upgrades.SPEED.registerItem(stack, 4);
        });
        ThEApi.instance().items().essentiaStorageBus().maybeStack(1).ifPresent(stack -> {
            Upgrades.INVERTER.registerItem(stack, 1);
            Upgrades.CAPACITY.registerItem(stack, 5);
        });
    }

    @Override
    public void postInit() {

    }

    @Override
    public String getModID() {
        return ModGlobals.MOD_ID_AE2;
    }

    @Override
    public boolean isRequired() {
        return true;
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
            if (o instanceof Aspect) {
                return this.createStack(new EssentiaStack((Aspect) o, Integer.MAX_VALUE));
            } else if (o instanceof EssentiaStack) {
                return AEEssentiaStack.fromEssentiaStack((EssentiaStack) o);
            } else if (o instanceof AEEssentiaStack) {
                return ((AEEssentiaStack) o).copy();
            }
            return null;
        }

        @Nullable
        @Override
        public IAEEssentiaStack readFromPacket(@Nonnull ByteBuf buf) {
            return AEEssentiaStack.fromPacket(buf);
        }

        @Nullable
        @Override
        public IAEEssentiaStack createFromNBT(@Nonnull NBTTagCompound tag) {
            return AEEssentiaStack.fromNBT(tag);
        }
    }
}
