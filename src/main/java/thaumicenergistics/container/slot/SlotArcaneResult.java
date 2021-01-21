package thaumicenergistics.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import thaumicenergistics.container.ICraftingContainer;

/**
 * @author BrockWS
 */
public class SlotArcaneResult extends ThESlot {

    private ICraftingContainer container;
    private EntityPlayer player;

    public SlotArcaneResult(ICraftingContainer container, EntityPlayer player, int index, int xPosition, int yPosition) {
        super(container.getInventory("result"), index, xPosition, yPosition);
        this.player = player;
        this.container = container;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }

    @Override
    public boolean getHasStack() {
        return false;
    }

    @Override
    public void putStack(ItemStack stack) {
        super.putStack(stack);
    }
}
