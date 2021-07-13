package thaumicenergistics.container.slot;

import javax.annotation.Nullable;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

/**
 * @author BrockWS
 */
public class SlotArmor extends ThESlot {

    private final EntityPlayer player;

    public SlotArmor(EntityPlayer player, IItemHandler handler, int index, int xPosition, int yPosition) {
        this(player, handler, index, xPosition, yPosition, true);
    }

    public SlotArmor(EntityPlayer player, IItemHandler handler, int index, int xPosition, int yPosition, boolean affectedBySlotCount) {
        super(handler, index, xPosition, yPosition, affectedBySlotCount);
        this.player = player;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        EntityEquipmentSlot slot;
        switch (this.getSlotIndex()) {
            case 0:
                slot = EntityEquipmentSlot.FEET;
                break;
            case 1:
                slot = EntityEquipmentSlot.LEGS;
                break;
            case 2:
                slot = EntityEquipmentSlot.CHEST;
                break;
            case 3:
                slot = EntityEquipmentSlot.HEAD;
                break;
            default:
                return false;
        }
        return stack.getItem().isValidArmor(stack, slot, this.player);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return (this.getStack().isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(this.getStack())) && super.canTakeStack(player);
    }

    @Nullable
    @Override
    public String getSlotTexture() {
        return ItemArmor.EMPTY_SLOT_NAMES[this.getSlotIndex()];
    }
}
