package thaumicenergistics.integration.appeng;

import io.netty.buffer.ByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.item.ItemDummyAspect;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;

/**
 * @author BrockWS
 */
public class AEEssentiaStack implements IAEEssentiaStack, Comparable<AEEssentiaStack> {

    private Aspect aspect;
    private long stackSize;
    private long countRequestable;
    private boolean isCraftable;
    private int hash;

    private AEEssentiaStack(Aspect aspect, long amount) {
        this.aspect = aspect;
        if (this.aspect == null) {
            throw new IllegalArgumentException("Aspect is null");
        }
        this.setStackSize(amount);
        this.setCraftable(false);
        this.setCountRequestable(0);
        this.hash = this.aspect.hashCode();
    }

    private AEEssentiaStack(AEEssentiaStack stack) {
        this.aspect = stack.getAspect();
        if (this.aspect == null)
            throw new IllegalArgumentException("Aspect is null");
        this.setStackSize(stack.getStackSize());
        this.setCraftable(false);
        this.setCountRequestable(0);
        this.hash = stack.hash;
    }

    public static AEEssentiaStack fromEssentiaStack(EssentiaStack stack) {
        if (stack == null)
            return null;
        return new AEEssentiaStack(stack.getAspect(), stack.getAmount());
    }

    public static IAEEssentiaStack fromNBT(NBTTagCompound t) {
        EssentiaStack stack = EssentiaStack.readFromNBT(t);
        if (stack == null)
            return null;
        AEEssentiaStack ae = AEEssentiaStack.fromEssentiaStack(stack);
        ae.setStackSize(t.getLong("AspectAmount"));
        ae.setCountRequestable(t.getLong("Req"));
        ae.setCraftable(t.getBoolean("Craft"));
        return new AEEssentiaStack(stack.getAspect(), stack.getAmount());
    }

    // TODO: fromPacket

    @Override
    public long getStackSize() {
        return this.stackSize;
    }

    @Override
    public IAEEssentiaStack setStackSize(long l) {
        this.stackSize = l;
        return this;
    }

    @Override
    public long getCountRequestable() {
        return this.countRequestable;
    }

    @Override
    public IAEEssentiaStack setCountRequestable(long l) {
        this.countRequestable = l;
        return this;
    }

    @Override
    public boolean isCraftable() {
        return this.isCraftable;
    }

    @Override
    public IAEEssentiaStack setCraftable(boolean b) {
        this.isCraftable = b;
        return this;
    }

    @Override
    public IAEEssentiaStack reset() {
        this.setStackSize(0);
        this.setCountRequestable(0);
        this.setCraftable(false);
        return this;
    }

    @Override
    public boolean isMeaningful() {
        return this.getStackSize() != 0 || this.countRequestable > 0 || this.isCraftable;
    }

    @Override
    public void incStackSize(long l) {
        this.setStackSize(this.getStackSize() + l);
    }

    @Override
    public void decStackSize(long l) {
        this.setStackSize(this.getStackSize() - l);
    }

    @Override
    public void incCountRequestable(long l) {
        this.setCountRequestable(this.getCountRequestable() + l);
    }

    @Override
    public void decCountRequestable(long l) {
        this.setCountRequestable(this.getCountRequestable() - l);
    }

    @Override
    public Aspect getAspect() {
        return this.aspect;
    }

    @Override
    public EssentiaStack getStack() {
        return new EssentiaStack(this.getAspect(), (int) Math.min(Integer.MAX_VALUE, this.stackSize));
    }

    @Override
    public void add(IAEEssentiaStack option) {
        if (option == null) return;
        this.incStackSize(option.getStackSize());
        this.setCountRequestable(this.getCountRequestable() + option.getCountRequestable());
        this.setCraftable(this.isCraftable() || option.isCraftable());
    }

    @Override
    public void writeToNBT(NBTTagCompound t) {
        t.setString("Aspect", this.getAspect().getTag());
        t.setByte("Count", (byte) 0);
        t.setLong("Amount", this.getStackSize());
        t.setLong("Req", this.getCountRequestable());
        t.setBoolean("Craft", this.isCraftable());
    }

    @Override
    public void writeToPacket(ByteBuf buf) throws IOException {
        // TODO
    }

    @Override
    public IAEEssentiaStack copy() {
        return new AEEssentiaStack(this);
    }

    @Override
    public IAEEssentiaStack empty() {
        IAEEssentiaStack copy = this.copy();
        copy.reset();
        return copy;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return false;
    }

    @Override
    public IStorageChannel<IAEEssentiaStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class);
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        // TODO: Test
        return new ItemStack(new ItemDummyAspect(this.aspect));
    }

    @Override
    public boolean fuzzyComparison(IAEEssentiaStack other, FuzzyMode mode) {
        return this.aspect == other.getAspect();
    }

    @Override
    public int compareTo(AEEssentiaStack o) {
        int diff = this.hashCode() - o.hashCode();
        return Integer.compare(diff, 0);
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AEEssentiaStack) {
            return ((AEEssentiaStack) obj).getAspect().getTag().equalsIgnoreCase(this.getAspect().getTag());
        }
        if (obj instanceof EssentiaStack) {
            return ((EssentiaStack) obj).getAspect().getTag().equalsIgnoreCase(this.getAspect().getTag());
        }
        return false;
    }
}
