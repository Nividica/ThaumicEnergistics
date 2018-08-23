package thaumicenergistics.part;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;

import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.item.ItemPartBase;
import thaumicenergistics.util.EssentiaFilter;
import thaumicenergistics.util.inventory.ThEUpgradeInventory;
import thaumicenergistics.util.inventory.WrapperInventoryItemHandler;

/**
 * @author BrockWS
 */
public abstract class PartSharedEssentiaBus extends PartBase implements IGridTickable, IUpgradeableHost {

    public EssentiaFilter config;
    public ThEUpgradeInventory upgrades;

    public PartSharedEssentiaBus(ItemPartBase item) {
        super(item);
        this.config = new EssentiaFilter(9) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                PartSharedEssentiaBus.this.host.markForSave();
            }
        };
        this.upgrades = new ThEUpgradeInventory("", 4, 1) {
            @Override
            public void markDirty() {
                super.markDirty();
                PartSharedEssentiaBus.this.host.markForSave();
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
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("config", this.config.serializeNBT());
        tag.setTag("upgrades", this.upgrades.serializeNBT());
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
            return new WrapperInventoryItemHandler(this.upgrades);
        }
        return null;
    }

    @Override
    public IConfigManager getConfigManager() {
        // TODO
        return null;
    }

    @Override
    public float getCableConnectionLength(AECableType aeCableType) {
        return 5;
    }
}
