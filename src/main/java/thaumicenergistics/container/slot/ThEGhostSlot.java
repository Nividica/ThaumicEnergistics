package thaumicenergistics.container.slot;

import appeng.util.helpers.ItemHandlerUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

/**
 * @author BrockWS
 * @author Alex811
 */
public class ThEGhostSlot extends SlotGhost {

    private static IInventory EMPTY = new InventoryBasic("[Null]", true, 0);

    private IItemHandler itemHandler;
    private int x;
    private int y;
    private boolean affectedBySlotCount;

    public ThEGhostSlot(IItemHandler handler, int index, int xPosition, int yPosition) {
        this(handler, index, xPosition, yPosition, true);
    }

    public ThEGhostSlot(IItemHandler handler, int index, int xPosition, int yPosition, boolean affectedBySlotCount) {
        super(ThEGhostSlot.EMPTY, index, xPosition, yPosition);
        this.itemHandler = handler;
        this.x = xPosition;
        this.y = yPosition;
        this.affectedBySlotCount = affectedBySlotCount;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return this.itemHandler.isItemValid(this.getSlotIndex(), stack);
    }

    @Override
    public void putStack(ItemStack stack) {
        if (this.itemHandler == null)
            return;
        ItemHandlerUtil.setStackInSlot(this.itemHandler, this.getSlotIndex(), stack);
        this.onSlotChanged();
    }

    @Override
    public ItemStack getStack() {
        if (this.itemHandler == null)
            return ItemStack.EMPTY;
        return this.itemHandler.getStackInSlot(this.getSlotIndex());
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        if (this.itemHandler == null)
            return ItemStack.EMPTY;
        return this.itemHandler.extractItem(this.getSlotIndex(), amount, false);
    }

    @SideOnly(Side.CLIENT)
    public int getBackgroundIconIndex() {
        return -1;
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation getBackgroundIcon() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasBackgroundIcon() {
        return this.getBackgroundIcon() != null && this.getBackgroundIconIndex() > -1;
    }

    public void recalculateY(int slots) {
        this.yPos = this.y;
        if (!this.affectedBySlotCount)
            return;
        this.yPos += slots * 18;
    }
}
