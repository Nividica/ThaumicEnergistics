package thaumicenergistics.util;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.INBTSerializable;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;

/**
 * @author BrockWS
 * @author Alex811
 */
public class EssentiaFilter implements INBTSerializable<NBTTagCompound>, Iterable<Aspect> {

    private Aspect[] aspects;

    public EssentiaFilter(int slots) {
        this.aspects = new Aspect[slots];
    }

    public void setAspect(Aspect aspect, int slot) {
        this.aspects[slot] = aspect;
        this.onContentsChanged();
    }

    public Aspect getAspect(int slot) {
        if (slot < 0 || slot > this.aspects.length)
            return null;
        return this.aspects[slot];
    }

    public boolean isInFilter(Aspect aspect) {
        return this.isInFilter(aspect.getTag());
    }

    public boolean isInFilter(String aspect) {
        for (Aspect af : this.aspects)
            if (af != null && af.getTag().equalsIgnoreCase(aspect))
                return true;
        return false;
    }

    public boolean hasAspects() {
        for (Aspect a : this.aspects)
            if (a != null)
                return true;
        return false;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("count", this.aspects.length);
        for (int i = 0; i < this.aspects.length; i++)
            if (this.aspects[i] != null) // Only set if there is a aspect in the slot
                tag.setString("aspect#" + i, this.aspects[i].getTag());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag.hasKey("count"))
            this.aspects = new Aspect[tag.getInteger("count")];
        for (int i = 0; i < this.aspects.length; i++)
            if (tag.hasKey("aspect#" + i))
                this.aspects[i] = Aspect.getAspect(tag.getString("aspect#" + i));
        this.onContentsChanged();
    }

    public void clear(){
        Arrays.fill(this.aspects, null);
        this.onContentsChanged();
    }

    public void partition(IAspectContainer aspectContainer){
        Aspect[] aspects = aspectContainer.getAspects().getAspects();
        Arrays.fill(this.aspects, null);
        System.arraycopy(aspects, 0, this.aspects, 0, aspects.length);
        this.onContentsChanged();
    }

    protected void onContentsChanged() {

    }

    @Override
    @Nonnull
    public Iterator<Aspect> iterator() {
        return Arrays.asList(this.aspects).iterator();
    }
}
