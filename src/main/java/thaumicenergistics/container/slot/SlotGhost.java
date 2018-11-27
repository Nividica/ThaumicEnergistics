package thaumicenergistics.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author BrockWS
 */
public class SlotGhost extends Slot implements ISlotOptional {

    private boolean slotEnabled = true;
    private int groupID = 0;

    public SlotGhost(IInventory inventory, int index, int xPosition, int yPosition, int groupID) {
        this(inventory, index, xPosition, yPosition);
        this.groupID = groupID;
    }

    public SlotGhost(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isEnabled() {
        return this.isSlotEnabled();
    }

    @Override
    public boolean isSlotEnabled() {
        return this.slotEnabled;
    }

    @Override
    public void setSlotEnabled(boolean enabled) {
        this.slotEnabled = enabled;
    }

    @Override
    public int getSlotGroup() {
        return this.groupID;
    }
}
