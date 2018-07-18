package thaumicenergistics.part;

import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.item.ItemPartBase;
import thaumicenergistics.util.EssentiaFilter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.networking.ticking.IGridTickable;

/**
 * @author BrockWS
 */
public abstract class PartSharedEssentiaBus extends PartBase implements IGridTickable {

    private EssentiaFilter config;

    public PartSharedEssentiaBus(ItemPartBase item) {
        super(item);
        this.config = new EssentiaFilter(9);
    }

    protected int calculateAmountToSend() {
        return 2;
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
        if (tag.hasKey("config"))
            this.config.deserializeNBT(tag.getCompoundTag("config"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setTag("config", this.config.serializeNBT());
    }
}
