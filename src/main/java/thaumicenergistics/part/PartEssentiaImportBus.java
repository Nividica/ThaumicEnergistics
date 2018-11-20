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
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;

import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.ThEApi;
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
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_import_bus/base"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_import_bus/on"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_import_bus/off"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_import_bus/has_channel")
    };

    private static IPartModel MODEL_ON = new ThEPartModel(MODELS[0], MODELS[1]);
    private static IPartModel MODEL_OFF = new ThEPartModel(MODELS[0], MODELS[2]);
    private static IPartModel MODEL_HAS_CHANNEL = new ThEPartModel(MODELS[0], MODELS[3]);

    public PartEssentiaImportBus(ItemEssentiaImportBus item) {
        super(item);
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(ThEApi.instance().config().tickTimeEssentiaImportBusMin(), ThEApi.instance().config().tickTimeEssentiaImportBusMax(), false, false);
    }

    @Override
    public boolean canWork() {
        // TODO: Improve
        return this.getConnectedTE() instanceof IAspectContainer;
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
        if (this.isPowered())
            if (this.isActive())
                return MODEL_HAS_CHANNEL;
            else
                return MODEL_ON;
        return MODEL_OFF;
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d vec3d) {
        GuiHandler.openGUI(ModGUIs.ESSENTIA_IMPORT_BUS, player, this.hostTile.getPos(), this.side);
        return true;
    }

    @Override
    public void getBoxes(IPartCollisionHelper box) {
        box.addBox(6, 6, 11, 10, 10, 13);
        box.addBox(5, 5, 13, 11, 11, 14);
        box.addBox(4, 4, 14, 12, 12, 16);
    }
}
