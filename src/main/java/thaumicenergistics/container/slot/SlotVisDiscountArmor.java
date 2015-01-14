package thaumicenergistics.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import thaumcraft.api.IVisDiscountGear;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SlotVisDiscountArmor
	extends Slot
{

	/**
	 * The armor type.
	 */
	private final int armorType;

	/**
	 * 
	 * @param inventory
	 * @param index
	 * @param x
	 * @param y
	 * @param armorType
	 * 0: Helmet, 1: Chest, 2: Legs, 3: Boots
	 */
	public SlotVisDiscountArmor( final IInventory inventory, final int index, final int x, final int y, final int armorType )
	{
		// Call super
		super( inventory, index, x, y );

		// Set the armor type
		this.armorType = armorType;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBackgroundIconIndex()
	{
		return ItemArmor.func_94602_b( this.armorType );
	}

	@Override
	public int getSlotStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValid( final ItemStack itemStack )
	{
		if( itemStack == null )
		{
			return false;
		}

		return( ( itemStack.getItem() instanceof IVisDiscountGear ) && ( itemStack.getItem().isValidArmor( itemStack, this.armorType, null ) ) );
	}

}
