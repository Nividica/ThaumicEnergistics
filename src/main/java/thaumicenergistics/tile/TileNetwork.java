package thaumicenergistics.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.api.AEApi;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;

import appeng.me.GridAccessException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.integration.appeng.grid.GridUtil;
import thaumicenergistics.integration.appeng.grid.IThEGridHost;
import thaumicenergistics.integration.appeng.grid.ThEGridBlock;
import thaumicenergistics.integration.appeng.util.ThEActionSource;
import thaumicenergistics.util.ForgeUtil;
import thaumicenergistics.util.IThEGridNodeBlock;
import thaumicenergistics.util.IThEOwnable;

import java.util.function.Consumer;

/**
 * @author BrockWS
 * @author Alex811
 */
public abstract class TileNetwork extends TileBase implements IThEGridHost, IActionHost, IPowerChannelState, IThEOwnable, IThEGridNodeBlock {

    protected ThEGridBlock gridBlock;
    protected IGridNode gridNode;
    protected ThEActionSource src;
    protected EntityPlayer owner;
    protected boolean isPowered = false;
    protected boolean isActive = false;

    public TileNetwork() {
        this.gridBlock = new ThEGridBlock(this, this, true);
        this.src = new ThEActionSource(this);
    }

    public ThEGridBlock getGridBlock() {
        return this.gridBlock;
    }

    @Override
    public IGridNode getGridNode() {
        return this.gridNode;
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
            this.initGridNodeOwner();
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
    public void setOwner(EntityPlayer player) {
        this.owner = player;
    }

    @Override
    public EntityPlayer getOwner() {
        return this.owner;
    }

    @Override
    public void securityBreak() {
        this.getWorld().destroyBlock(this.getPos(), true);
    }

    @Override
    public boolean isPowered() {
        return this.isPowered;
    }

    @Override
    public boolean isActive() {
        if(ForgeUtil.isServer())
            return this.gridNode != null && this.gridNode.isActive();
        else
            return this.isActive;
    }

    @Override
    public void markDirty() {   // server-side, initiate client sync
        super.markDirty();
        if (world == null) return;
        IBlockState state = world.getBlockState(this.getPos());
        world.notifyBlockUpdate(this.getPos(), state, state, 2);
    }

    @MENetworkEventSubscribe
    public final void updateBootStatus(MENetworkBootingStatusChange event) {   // sync client
        this.markDirty();
    }

    @MENetworkEventSubscribe
    public void updatePowerStatus(MENetworkPowerStatusChange event) {   // sync client
        try {
            this.isPowered = GridUtil.getEnergyGrid(this).isNetworkPowered();
            this.markDirty();
        } catch (GridAccessException e) {
            // should ignore?
            this.isPowered = false;
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {  // sync, server-side, returns what to send to the client when the TileEntity's chunk gets loaded by it
        NBTTagCompound nbtTagCompound = super.getUpdateTag();
        nbtTagCompound.setBoolean("powered", this.isPowered());
        nbtTagCompound.setBoolean("active", this.isActive());
        return nbtTagCompound;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {   // sync, client-side, receives from getUpdateTag()
        super.handleUpdateTag(tag);
        isPowered = tag.getBoolean("powered");
        isActive = tag.getBoolean("active");
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() { // sync, server-side, returns what to send to the client on block update, triggered by markDirty()
        return new SPacketUpdateTileEntity(this.getPos(), 1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) { // sync, client-side, receives from getUpdatePacket()
        handleUpdateTag(packet.getNbtCompound());
    }

    public void withPowerStateText(Consumer<String> consumer){
        if(this.isPowered()){
            if(this.isActive())
                consumer.accept(ThEApi.instance().lang().deviceOnline().getLocalizedKey());
            else
                consumer.accept(ThEApi.instance().lang().deviceMissingChannel().getLocalizedKey());
        }else
            consumer.accept(ThEApi.instance().lang().deviceOffline().getLocalizedKey());
    }
}
