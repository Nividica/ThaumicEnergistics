package thaumicenergistics.util.inventory;

import java.util.Collections;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import net.minecraftforge.common.util.INBTSerializable;

/**
 * Manages an internal inventory
 *
 * @author BrockWS
 */
public class ThEInternalInventory implements IInventory, INBTSerializable<NBTTagList>, Iterable<ItemStack> {

    /**
     * Stack size limit.
     */
    private final int stackLimit;

    /**
     * The inventory slots.
     */
    private final NonNullList<ItemStack> slots;

    /**
     * Name of the inventory.
     */
    private final String customName;

    public ThEInternalInventory(String customName, int size, int stackLimit) {
        this.slots = NonNullList.withSize(size, ItemStack.EMPTY);
        this.customName = customName;
        this.stackLimit = stackLimit;
    }

    @Override
    public int getSizeInventory() {
        return this.slots.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.slots)
            if (stack != null)
                return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.slots.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = this.getStackInSlot(index);
        if (stack.isEmpty())
            return stack;
        ItemStack toReturn = stack.splitStack(count);
        this.setInventorySlotContents(index, stack);
        return toReturn;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = this.getStackInSlot(index);
        this.setInventorySlotContents(index, ItemStack.EMPTY);
        this.markDirty();
        return stack != null ? stack : ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
            stack.setCount(this.getInventoryStackLimit());
        this.slots.set(index, stack);
        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return this.stackLimit;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0; // ?
    }

    @Override
    public void setField(int id, int value) {
        // ?
    }

    @Override
    public int getFieldCount() {
        return 0; // ?
    }

    @Override
    public void clear() {
        this.slots.clear();
    }

    @Override
    public String getName() {
        return this.customName;
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(this.getName());
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList nbt = new NBTTagList();
        this.slots.forEach(slot -> nbt.appendTag(slot.serializeNBT()));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        for (int i = 0; i < nbt.tagCount(); i++)
            this.slots.set(i, new ItemStack((NBTTagCompound) nbt.get(i)));
        this.markDirty();
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return Collections.unmodifiableCollection(this.slots).iterator();
    }
}
