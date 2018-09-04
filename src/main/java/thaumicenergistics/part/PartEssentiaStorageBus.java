package thaumicenergistics.part;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.*;
import appeng.api.util.AECableType;

import thaumcraft.api.aspects.IAspectContainer;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.integration.appeng.grid.EssentiaContainerAdapter;
import thaumicenergistics.item.part.ItemEssentiaStorageBus;

/**
 * @author BrockWS
 */
public class PartEssentiaStorageBus extends PartSharedEssentiaBus implements ICellContainer, IMEMonitorHandlerReceiver<IAEEssentiaStack> {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_storage_bus/base"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_storage_bus/on"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_storage_bus/off"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_storage_bus/has_channel")
    };

    private static IPartModel MODEL_ON = new ThEPartModel(MODELS[0], MODELS[1]);
    private static IPartModel MODEL_OFF = new ThEPartModel(MODELS[0], MODELS[2]);
    private static IPartModel MODEL_HAS_CHANNEL = new ThEPartModel(MODELS[0], MODELS[3]);

    private IMEInventoryHandler<IAEEssentiaStack> handler;
    private boolean wasActive = false;

    public PartEssentiaStorageBus(ItemEssentiaStorageBus item) {
        super(item);
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(5, 60, false, false);
    }

    @Override
    public boolean canWork() {
        return false;
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        if (!this.canWork())
            return TickRateModulation.IDLE;
        return TickRateModulation.SLOWER;
    }

    @Override
    public void postChange(IBaseMonitor<IAEEssentiaStack> monitor, Iterable<IAEEssentiaStack> change, IActionSource actionSource) {
        // TODO: Probably should send off an update like PartStorageBus?
        // Won't get anything here util Platform#postChanges is fixed #3644
        // https://github.com/AppliedEnergistics/Applied-Energistics-2/pull/3644
    }

    @Override
    public void onListUpdate() {
        // Ignored
    }

    @Override
    public void saveChanges(ICellInventory<?> iCellInventory) {
        // TODO: Ignored Maybe?
    }

    @Override
    public void onNeighborChanged(IBlockAccess access, BlockPos pos, BlockPos neighbor) {
        if (pos == null || neighbor == null)
            return;
        if (pos.offset(this.side.getFacing()).equals(neighbor) && this.getGridNode() != null) {
            IGrid grid = this.getGridNode().getGrid();
            if (grid != null) { // Might want to check if something was changed
                //ThELog.info("MENetworkCellArrayUpdate");
                grid.postEvent(new MENetworkCellArrayUpdate());
            }
        }
        super.onNeighborChanged(access, pos, neighbor);
    }

    @Override
    public void blinkCell(int slot) {
        // Ignored
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(IStorageChannel<?> channel) {
        //ThELog.info("getCellArray");
        if (channel != this.getChannel() || this.getHandler() == null)
            return Collections.emptyList();
        // We need to "open" the connected IAspectContainer as a "cell" (IMEInventoryHandler)
        return Collections.singletonList(this.getHandler());
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return this.handler == verificationToken;
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

    private IMEInventoryHandler<IAEEssentiaStack> getHandler() {
        if (/*this.handler == null &&*/ this.getConnectedContainer() != null) // TODO: Allow cache
            return this.handler = new EssentiaContainerAdapter(this.getConnectedContainer());
        return null;
    }

    private IAspectContainer getConnectedContainer() {
        return this.getConnectedTE() instanceof IAspectContainer ? (IAspectContainer) this.getConnectedTE() : null;
    }

    @Override
    public void getBoxes(IPartCollisionHelper box) {
        box.addBox(3, 3, 15, 13, 13, 16);
        box.addBox(2, 2, 14, 14, 14, 15);
        box.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public float getCableConnectionLength(AECableType aeCableType) {
        return 4;
    }

    @MENetworkEventSubscribe
    public void updateChannels(final MENetworkChannelsChanged changedChannels) {
        final boolean currentActive = this.getGridNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.gridNode.getGrid().postEvent(new MENetworkCellArrayUpdate());
            this.host.markForUpdate();
        }
    }
}
