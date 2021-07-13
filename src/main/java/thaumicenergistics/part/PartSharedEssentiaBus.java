package thaumicenergistics.part;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;

import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.item.ItemPartBase;
import thaumicenergistics.util.EssentiaFilter;
import thaumicenergistics.util.inventory.ThEUpgradeInventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author BrockWS
 * @author Alex811
 */
public abstract class PartSharedEssentiaBus extends PartBase implements IGridTickable, IUpgradeableHost {

    public EssentiaFilter config;
    public ThEUpgradeInventory upgrades;
    protected boolean lastRedstone = true;
    public List<Runnable> upgradeChangeListeners = new ArrayList<>();

    public PartSharedEssentiaBus(ItemPartBase item) {
        this(item, 9, 4);
    }

    public PartSharedEssentiaBus(ItemPartBase item, int configSlots, int upgradeSlots) {
        super(item);
        this.config = new EssentiaFilter(configSlots) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                PartSharedEssentiaBus.this.host.markForSave();
            }
        };
        this.upgrades = new ThEUpgradeInventory("upgrades", upgradeSlots, 1, this.getItemStack(PartItemStack.NETWORK)) {
            @Override
            public void markDirty() {
                super.markDirty();
                PartSharedEssentiaBus.this.host.markForSave();
                upgradeChangeListeners.forEach(Runnable::run);
            }
        };
    }

    protected int calculateAmountToSend() {
        // A jar can hold 250 essentia
        // TODO: Get feedback on these values
        switch (this.getInstalledUpgrades(Upgrades.SPEED)) {
            case 4:
                return 128;
            case 3:
                return 64;
            case 2:
                return 16;
            case 1:
                return 4;
            default:
                return 1;
        }
    }

    public boolean hasInverterCard() {
        return this.getInstalledUpgrades(Upgrades.INVERTER) > 0;
    }

    public boolean hasRedstoneCard() {
        return this.getInstalledUpgrades(Upgrades.REDSTONE) > 0;
    }

    @Override
    public boolean canConnectRedstone() {
        return this.hasRedstoneCard();
    }

    @Override
    public double getIdlePowerUsage() {
        return 1;
    }

    public TileEntity getConnectedTE() {
        TileEntity self = this.host.getTile();
        World w = self.getWorld();
        BlockPos pos = self.getPos().offset(this.side.getFacing());
        if (w.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null) {
            return w.getTileEntity(pos);
        }
        return null;
    }

    protected IEssentiaStorageChannel getChannel() {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class);
    }

    public EssentiaFilter getConfig() {
        return this.config;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("config"))
            this.config.deserializeNBT(tag.getCompoundTag("config"));
        if (tag.hasKey("upgrades"))
            this.upgrades.deserializeNBT(tag.getTagList("upgrades", 10));
        this.getConfigManager().readFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("config", this.config.serializeNBT());
        tag.setTag("upgrades", this.upgrades.serializeNBT());
        this.getConfigManager().writeToNBT(tag);
    }

    @Override
    public int getInstalledUpgrades(Upgrades upgrade) {
        return this.upgrades.getUpgrades(upgrade);
    }

    @Override
    public TileEntity getTile() {
        return this.hostTile;
    }

    @Override
    public IItemHandler getInventoryByName(String s) {
        if (s.equalsIgnoreCase("upgrades")) {
            return new InvWrapper(this.upgrades);
        }
        return null;
    }

    @Override
    public float getCableConnectionLength(AECableType aeCableType) {
        return 5;
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        return (this.canWork() && this.workAllowedByRedstone()) ? this.doWork() : TickRateModulation.IDLE;
    }

    protected abstract TickRateModulation doWork();

    protected RedstoneMode getRSMode(){
        if (!hasRedstoneCard())
            return RedstoneMode.IGNORE;
        return (RedstoneMode) this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
    }

    protected boolean hasRedstone(){
        return this.host.hasRedstone(this.side);
    }

    protected boolean workAllowedByRedstone(){
        boolean hasRedstone = this.hasRedstone();
        RedstoneMode mode = this.getRSMode();
        return !hasRedstoneCard() ||
                (mode == RedstoneMode.IGNORE) ||
                (mode == RedstoneMode.HIGH_SIGNAL && hasRedstone) ||
                (mode == RedstoneMode.LOW_SIGNAL && !hasRedstone);
    }

    @Override
    public void onNeighborChanged(IBlockAccess iBlockAccess, BlockPos blockPos, BlockPos blockPos1) {
        super.onNeighborChanged(iBlockAccess, blockPos, blockPos1);
        if(this.lastRedstone != this.hasRedstone()){
            this.lastRedstone = !this.lastRedstone;
            if(this.lastRedstone && this.canWork() && this.getRSMode() == RedstoneMode.SIGNAL_PULSE)
                this.doWork();
        }
    }
}
