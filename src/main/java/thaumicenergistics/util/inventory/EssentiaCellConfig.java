package thaumicenergistics.util.inventory;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.items.ItemStackHandler;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.item.ItemDummyAspect;
import thaumicenergistics.util.ThEUtil;

/**
 * @author BrockWS
 */
public class EssentiaCellConfig extends ItemStackHandler {

    private ItemStack cell;

    public EssentiaCellConfig(ItemStack stack) {
        super(63);
        this.cell = stack;
        this.deserializeNBT(this.cell.getOrCreateSubCompound("filter"));
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || stack.getItem() instanceof ItemDummyAspect)
            return super.insertItem(slot, stack, simulate);
        if (!(stack.getItem() instanceof IEssentiaContainerItem))
            return stack;

        AspectList list = ((IEssentiaContainerItem) stack.getItem()).getAspects(stack);
        if (list == null || list.size() < 1 || !ThEApi.instance().items().dummyAspect().maybeStack(1).isPresent())
            return stack;
        ItemStack dummyStack = ThEUtil.setAspect(ThEApi.instance().items().dummyAspect().maybeStack(1).get(), list.getAspects()[0]);

        super.insertItem(slot, dummyStack, simulate);
        return stack;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof ItemDummyAspect) {
            super.setStackInSlot(slot, stack);
            return;
        }
        if (!(stack.getItem() instanceof IEssentiaContainerItem))
            return;

        AspectList list = ((IEssentiaContainerItem) stack.getItem()).getAspects(stack);
        if (list == null || list.size() < 1 || !ThEApi.instance().items().dummyAspect().maybeStack(1).isPresent())
            return;
        ItemStack dummyStack = ThEUtil.setAspect(ThEApi.instance().items().dummyAspect().maybeStack(1).get(), list.getAspects()[0]);
        super.setStackInSlot(slot, dummyStack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        NBTTagCompound tag = this.cell.getTagCompound();
        if (tag == null)
            tag = new NBTTagCompound();
        tag.setTag("filter", this.serializeNBT());
        this.cell.setTagCompound(tag);
    }
}
