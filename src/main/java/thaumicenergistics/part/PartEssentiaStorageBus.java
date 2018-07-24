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
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.*;

import thaumcraft.api.aspects.IAspectContainer;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.integration.appeng.grid.EssentiaContainerAdapter;
import thaumicenergistics.item.part.ItemEssentiaStorageBus;
import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 */
public class PartEssentiaStorageBus extends PartSharedEssentiaBus implements ICellContainer, IMEMonitorHandlerReceiver<IAEEssentiaStack> {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_storage_bus_base")
    };

    private static IPartModel MODEL_BASE = new ThEPartModel(MODELS[0]);

    private IMEInventoryHandler<IAEEssentiaStack> handler;

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
        ThELog.info("PartEssentiaStorageBus postChange");
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
        if (access == null || pos == null || neighbor == null)
            return;
        if (pos.offset(this.side.getFacing()).equals(neighbor) && this.getGridNode() != null) {
            IGrid grid = this.getGridNode().getGrid();
            if (grid != null) { // Might want to check if something was changed
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
        if (channel != this.getChannel())
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
        return MODEL_BASE;
    }

    private IMEInventoryHandler<IAEEssentiaStack> getHandler() {
        if (this.handler == null && this.getConnectedContainer() != null) // TODO: Allow cache resetting
            this.handler = new EssentiaContainerAdapter(this.getConnectedContainer());
        return this.handler;
    }

    private IAspectContainer getConnectedContainer() {
        return this.getConnectedTE() instanceof IAspectContainer ? (IAspectContainer) this.getConnectedTE() : null;
    }

    @Override
    public void getBoxes(IPartCollisionHelper box) {
        // Face
        box.addBox(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);

        // Mid
        box.addBox(4.0D, 4.0D, 14.0D, 12.0D, 12.0D, 15.0D);

        // Back
        box.addBox(5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D);
    }
}
