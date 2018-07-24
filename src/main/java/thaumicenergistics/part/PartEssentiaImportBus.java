package thaumicenergistics.part;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;

import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.AEEssentiaStack;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.item.part.ItemEssentiaImportBus;

/**
 * @author BrockWS
 */
public class PartEssentiaImportBus extends PartSharedEssentiaBus {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_import_bus_base")
    };

    private static IPartModel MODEL_BASE = new ThEPartModel(MODELS[0]);

    public PartEssentiaImportBus(ItemEssentiaImportBus item) {
        super(item);
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(5, 40, false, false);
    }

    @Override
    public boolean canWork() {
        // TODO: Improve
        return this.getConnectedTE() != null && this.getConnectedTE() instanceof IAspectContainer;
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        if (!this.canWork())
            return TickRateModulation.IDLE;
        IAspectContainer container = (IAspectContainer) this.getConnectedTE();
        for (Aspect aspect : container.getAspects().getAspects()) {
            if (this.config.hasAspects() && !this.config.isInFilter(aspect)) // Check filter
                continue;
            EssentiaStack inContainer = new EssentiaStack(aspect, Math.min(container.containerContains(aspect), this.calculateAmountToSend()));

            IStorageGrid storageGrid = this.getGridNode().getGrid().getCache(IStorageGrid.class);
            IMEMonitor<IAEEssentiaStack> storage = storageGrid.getInventory(this.getChannel());

            AEEssentiaStack toInsert = AEEssentiaStack.fromEssentiaStack(inContainer);
            if (storage.canAccept(toInsert)) {
                IAEEssentiaStack notInserted = storage.injectItems(toInsert, Actionable.SIMULATE, this.source);
                if (notInserted != null && notInserted.getStackSize() > 0) {
                    toInsert.decStackSize(notInserted.getStackSize());
                }
                container.takeFromContainer(toInsert.getAspect(), (int) toInsert.getStackSize());
                storage.injectItems(toInsert, Actionable.MODULATE, this.source);
                return TickRateModulation.FASTER;
            }
        }
        return TickRateModulation.SLOWER;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return MODEL_BASE;
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d vec3d) {
        GuiHandler.openGUI(ModGUIs.ESSENTIA_IMPORT_BUS, player, this.hostTile.getPos(), this.side);
        return true;
    }
}
