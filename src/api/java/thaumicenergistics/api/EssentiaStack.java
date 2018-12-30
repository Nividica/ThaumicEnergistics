package thaumicenergistics.api;

import net.minecraft.nbt.NBTTagCompound;

import thaumcraft.api.aspects.Aspect;

/**
 * Something for Essentia similar to ItemStack and FluidStack, since Thaumcraft doesn't have one
 *
 * @author BrockWS
 */
public class EssentiaStack {

    private String aspect;
    private int amount;

    public EssentiaStack(Aspect aspect, int amount) {
        this(aspect != null ? aspect.getTag() : "", amount);
    }

    public EssentiaStack(String aspect, int amount) {
        if (aspect == null || aspect.isEmpty())
            throw new IllegalArgumentException("Aspect cannot be null");
        this.aspect = aspect;
        this.amount = amount;
    }

    private EssentiaStack(EssentiaStack old) {
        this.aspect = old.getAspectTag();
        this.amount = old.getAmount();
    }

    private EssentiaStack() {

    }

    public String getAspectTag() {
        return this.aspect;
    }

    public Aspect getAspect() {
        return Aspect.getAspect(this.getAspectTag());
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }

    public NBTTagCompound write(NBTTagCompound tag) {
        tag.setString("Aspect", this.getAspectTag());
        tag.setInteger("Amount", this.getAmount());
        return tag;
    }

    public void read(NBTTagCompound tag) {
        this.aspect = tag.getString("Aspect");
        this.amount = tag.getInteger("Amount");
    }

    public EssentiaStack copy() {
        return new EssentiaStack(this);
    }

    public static EssentiaStack readFromNBT(NBTTagCompound tag) {
        if (tag != null && !tag.hasNoTags()) {
            EssentiaStack stack = new EssentiaStack();
            stack.read(tag);
            return stack.getAspect() != null && stack.getAmount() > 0 ? stack : null;
        }
        return null;
    }
}
