package thaumicenergistics.tile;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.Actionable;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.integration.appeng.grid.GridUtil;
import thaumicenergistics.util.AEUtil;
import thaumicenergistics.util.ForgeUtil;

/**
 * @author BrockWS
 */
public class TileInfusionProvider extends TileNetwork implements IAspectSource {

    // Client side only, for rendering aspect icons with goggles
    private AspectList clientAspects = new AspectList();

    public TileInfusionProvider() {
        super();
    }

    public IItemList<IAEEssentiaStack> getStoredAspects() {
        IEssentiaStorageChannel channel = this.getChannel();
        IItemList<IAEEssentiaStack> list;
        try {
            IStorageGrid storage = GridUtil.getStorageGrid(this);
            list = storage.getInventory(channel).getStorageList();
        } catch (GridAccessException e) {
            // ignore, create an empty list
            list = channel.createList();
        }
        return list;
    }

    @Override
    public AspectList getAspects() {
        if (ForgeUtil.isClient())
            return this.clientAspects;
        AspectList list = new AspectList();
        IItemList<IAEEssentiaStack> stored = this.getStoredAspects();
        for (IAEEssentiaStack stack : stored)
            list.add(stack.getAspect(), stack.getStackSize() >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) stack.getStackSize());
        return list;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int i) {
        try {
            IStorageGrid storage = GridUtil.getStorageGrid(this);
            IMEMonitor<IAEEssentiaStack> monitor = storage.getInventory(this.getChannel());
            IAEEssentiaStack canExtract = monitor.extractItems(AEUtil.getAEStackFromAspect(aspect, i), Actionable.SIMULATE, this.src);
            if (canExtract == null || canExtract.getStackSize() != i)
                return false;
            monitor.extractItems(canExtract, Actionable.MODULATE, this.src);
            this.markDirty();
        } catch (GridAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(super.getUpdateTag());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        if (ForgeUtil.isClient())
            return super.writeToNBT(tag);
        super.writeToNBT(tag);
        this.getAspects().writeToNBT(tag, "storedAspects");
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("storedAspects")) {
            this.clientAspects = new AspectList();
            this.clientAspects.readFromNBT(tag, "storedAspects");
        }
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int i) {
        return this.containerContains(aspect) >= i;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return this.getAspects().getAmount(aspect);
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return false;
    }

    @Override
    public void setAspects(AspectList aspectList) {
        // Ignored
    }

    @Override
    public int addToContainer(Aspect aspect, int i) {
        // Ignored
        return i;
    }

    @Override
    @Deprecated
    public boolean takeFromContainer(AspectList aspectList) {
        return false;
    }

    @Override
    @Deprecated
    public boolean doesContainerContain(AspectList aspectList) {
        return false;
    }
}
