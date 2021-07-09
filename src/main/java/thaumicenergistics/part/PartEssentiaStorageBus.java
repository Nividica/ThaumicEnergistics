package thaumicenergistics.part;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.core.sync.GuiBridge;
import appeng.helpers.IPriorityHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.*;
import appeng.api.util.AECableType;

import thaumcraft.api.aspects.IAspectContainer;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.config.AESettings;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.integration.appeng.grid.EssentiaContainerAdapter;
import thaumicenergistics.item.part.ItemEssentiaStorageBus;
import thaumicenergistics.util.AEUtil;
import thaumicenergistics.util.ForgeUtil;

/**
 * @author BrockWS
 * @author Alex811
 */
public class PartEssentiaStorageBus extends PartSharedEssentiaBus implements ICellContainer, IMEMonitorHandlerReceiver<IAEEssentiaStack>, IPriorityHost {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_storage_bus/base"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_storage_bus/on"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_storage_bus/off"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_storage_bus/has_channel")
    };

    private static IPartModel MODEL_ON = new ThEPartModel(MODELS[0], MODELS[1]);
    private static IPartModel MODEL_OFF = new ThEPartModel(MODELS[0], MODELS[2]);
    private static IPartModel MODEL_HAS_CHANNEL = new ThEPartModel(MODELS[0], MODELS[3]);

    private EssentiaContainerAdapter handler;
    private boolean wasActive = false;
    private IAspectContainer lastConnectedContainer = null;
    private int priority = 0;

    public PartEssentiaStorageBus(ItemEssentiaStorageBus item) {
        super(item, 63, 5);
    }

    @Override
    protected AESettings.SUBJECT getAESettingSubject() {
        return AESettings.SUBJECT.ESSENTIA_STORAGE_BUS;
    }

    @Override
    public void settingChanged(Settings setting) {
        super.settingChanged(setting);
        EssentiaContainerAdapter handler = this.handler;
        if (handler != null) {
            if(setting == Settings.ACCESS)
                handler.setBaseAccess((AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS));
            else if(setting == Settings.STORAGE_FILTER)
                handler.setReportInaccessible((StorageFilter) this.getConfigManager().getSetting(Settings.STORAGE_FILTER));
            else
                return;
            this.triggerUpdate();
        }
    }

    protected void upgradesChanged(){
        EssentiaContainerAdapter handler = this.getHandler();
        if(handler != null)
            handler.setWhitelist(!this.hasInverterCard());
        this.triggerUpdate();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.lastConnectedContainer = this.getConnectedContainer();
        this.upgradeChangeListeners.add(this::upgradesChanged);
        this.upgradesChanged();
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.upgradeChangeListeners.clear();
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(ThEApi.instance().config().tickTimeEssentiaStorageBusMin(), ThEApi.instance().config().tickTimeEssentiaStorageBusMax(), false, false);
    }

    @Override
    public boolean canWork() {
        return false;
    }

    @Override
    protected TickRateModulation doWork() {
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
        // Ignored
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
        IAspectContainer connectedContainer = this.getConnectedContainer();
        if (this.lastConnectedContainer != connectedContainer){
            this.lastConnectedContainer = connectedContainer;
            this.handler = null;   // wipe cached handler, so it gets reconstructed
        }
        super.onNeighborChanged(access, pos, neighbor);
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d vec3d) {
        if ((player.isSneaking() && AEUtil.isWrench(player.getHeldItem(hand), player, this.getTile().getPos())))
            return false;

        if (ForgeUtil.isServer())
            GuiHandler.openGUI(ModGUIs.ESSENTIA_STORAGE_BUS, player, this.hostTile.getPos(), this.side);

        return true;
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
        return this.priority;
    }

    @Override
    public void setPriority(int i) {
        this.priority = i;
        if(this.handler != null)
            this.handler.setPriority(i);
        this.host.markForSave();
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return this.getRepr();
    }

    @Override
    public GuiBridge getGuiBridge() {
        return null;
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

    @Nullable
    private EssentiaContainerAdapter getHandler() {
        if(this.handler == null){
            IAspectContainer connectedContainer = this.getConnectedContainer();
            if(connectedContainer != null)
                return this.handler = new EssentiaContainerAdapter(connectedContainer, this.config,
                        !this.hasInverterCard(),
                        (AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS),
                        (StorageFilter) this.getConfigManager().getSetting(Settings.STORAGE_FILTER),
                        this.priority
                ); // init and cache handler
            return null;
        }
        return this.handler;    // return cached handler
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

    @Override
    @MENetworkEventSubscribe
    public void updateBootStatus(MENetworkBootingStatusChange event) {
        super.updateBootStatus(event);
        this.triggerBootUpdate();
    }

    @Override
    @MENetworkEventSubscribe
    public void updatePowerStatus(MENetworkPowerStatusChange event) {
        super.updatePowerStatus(event);
        this.triggerBootUpdate();
    }

    public void triggerBootUpdate(){
        final boolean currentActive = this.getGridNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.triggerUpdate();
        }
    }

    public void triggerUpdate(){
        this.gridNode.getGrid().postEvent(new MENetworkCellArrayUpdate());
        this.host.markForUpdate();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.priority = tag.getInteger("priority");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("priority", this.priority);
    }
}
