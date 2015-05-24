package thaumicenergistics.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.common.items.wands.ItemWandCasting;

/**
 * Houses commonly used methods.
 * 
 * @author Nividica
 * 
 */
public final class ThEUtils
{

	/**
	 * Returns true if the item stack is a wand, scepter, or staff if they are
	 * allowed.
	 * 
	 * @param stack
	 * @param allowStaves
	 * @return
	 */
	public static boolean isItemValidWand( final ItemStack stack, final boolean allowStaves )
	{
		Item potentialWand;

		// Ensure it is not null
		if( ( stack == null ) || ( ( potentialWand = stack.getItem() ) == null ) )
		{
			return false;
		}

		// Ensure it is a casting wand
		if( !( potentialWand instanceof ItemWandCasting ) )
		{
			return false;
		}

		// Ensure it is not a staff
		if( ( !allowStaves ) && ( ( (ItemWandCasting)potentialWand ).isStaff( stack ) ) )
		{
			return false;
		}

		// Get the wand
		ItemWandCasting wand = (ItemWandCasting)stack.getItem();

		// Validate internals
		try
		{
			wand.getAspectsWithRoom( stack );
		}
		catch( Exception e )
		{
			// Internal failure
			return false;
		}

		// Valid wand
		return true;
	}

}
