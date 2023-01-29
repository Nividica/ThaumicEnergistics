package thaumicenergistics.common.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import thaumcraft.api.IVisDiscountGear;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Slot that holds armor.
 *
 * @author Nividica
 *
 */
public class SlotArmor extends Slot {

    /**
     * The armor type.
     */
    private final int armorType;

    /**
     * If true will only accept vis discount armor.
     */
    private final boolean restrictToVisArmor;

    /**
     *
     * @param inventory
     * @param index
     * @param x
     * @param y
     * @param armorType 0: Helmet, 1: Chest, 2: Legs, 3: Boots
     */
    public SlotArmor(final IInventory inventory, final int index, final int x, final int y, final int armorType,
            final boolean restrictToVisArmor) {
        // Call super
        super(inventory, index, x, y);

        // Set the armor type
        this.armorType = armorType;

        // Set if the armor is restricted
        this.restrictToVisArmor = restrictToVisArmor;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getBackgroundIconIndex() {
        return ItemArmor.func_94602_b(this.armorType);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValid(final ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        // Vis armor?
        if ((this.restrictToVisArmor && !(itemStack.getItem() instanceof IVisDiscountGear))) {
            return false;
        }

        // Valid armor for this slot?
        return itemStack.getItem().isValidArmor(itemStack, this.armorType, null);
    }
}
