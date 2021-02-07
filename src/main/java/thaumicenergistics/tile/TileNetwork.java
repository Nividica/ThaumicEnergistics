package thaumicenergistics.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.api.AEApi;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;

import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.integration.appeng.grid.IThEGridHost;
import thaumicenergistics.integration.appeng.grid.ThEGridBlock;
import thaumicenergistics.integration.appeng.util.ThEActionSource;
import thaumicenergistics.util.ForgeUtil;

/**
 * @author BrockWS
 */
public abstract class TileNetwork extends TileBase implements IThEGridHost, IActionHost, IPowerChannelState {

    protected ThEGridBlock gridBlock;
    protected IGridNode gridNode;
    protected ThEActionSource src;

    public TileNetwork() {
        this.gridBlock = new ThEGridBlock(this, this, true);
        this.src = new ThEActionSource(this);
    }

    public ThEGridBlock getGridBlock() {
        return this.gridBlock;
    }

    @Override
    public void invalidate() {
        if (this.gridNode != null) {
            this.gridNode.destroy();
            this.gridNode = null;
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        this.invalidate();
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull AEPartLocation aePartLocation) {
        return this.getActionableNode();
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation aePartLocation) {
        return AECableType.SMART;
    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        if (this.gridNode == null && ForgeUtil.isServer()) {
            this.gridNode = AEApi.instance().grid().createGridNode(this.getGridBlock());
            this.gridNode.updateState();
        }
        return this.gridNode;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    protected IEssentiaStorageChannel getChannel() {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class);
    }

    @Override
    public void gridChanged() {

    }

    @Override
    public void securityBreak() {
        this.getWorld().destroyBlock(this.getPos(), true);
    }

    @Override
    public boolean isPowered() {
        return isActive();
    }

    @Override
    public boolean isActive() {
        return this.gridNode != null && this.gridNode.isActive();
    }
}
