package thaumicenergistics.integration.tc;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;

public class VisCraftingHelper
{

	public static final VisCraftingHelper INSTANCE = new VisCraftingHelper();

	private ItemStack craftingScepter = null;

	/**
	 * Private constructor
	 */
	private VisCraftingHelper()
	{
		// Intentionally Empty
	}

	/**
	 * Calculates the vis discount for the specified armor slots and aspect.
	 * 
	 * @param inventory
	 * @param firstSlotIndex
	 * @param aspect
	 * @return
	 */
	public float calculateArmorDiscount( final IInventory inventory, final int firstSlotIndex, final Aspect aspect )
	{
		float discount = 0.0F;

		for( int index = 0; index < 4; index++ )
		{
			// Get the armor stack
			ItemStack armor = inventory.getStackInSlot( firstSlotIndex + index );

			// Ensure it is valid discount gear
			if( ( armor != null ) && ( armor.getItem() instanceof IVisDiscountGear ) )
			{
				try
				{
					// Get the discount
					discount += ( ( (IVisDiscountGear)armor.getItem() ).getVisDiscount( armor, null, aspect ) / 100.0F );
				}
				catch( Exception e )
				{
				}
			}
		}

		return discount;
	}

	/**
	 * Calculates the amount of warp generated from the specified armor slots.
	 * 
	 * @param inventory
	 * @param firstSlotIndex
	 * @return
	 */
	public int calculateArmorWarp( final IInventory inventory, final int firstSlotIndex )
	{
		int warp = 0;

		for( int index = 0; index < 4; index++ )
		{
			// Get the armor stack
			ItemStack armor = inventory.getStackInSlot( firstSlotIndex + index );

			// Ensure it is valid warp gear
			if( ( armor != null ) && ( armor.getItem() instanceof IWarpingGear ) )
			{
				try
				{
					// Get the warp
					warp += ( (IWarpingGear)armor.getItem() ).getWarp( armor, null );
				}
				catch( Exception e )
				{
				}
			}
		}

		return warp;
	}

	/**
	 * Gets an itemstack representing a fully charged silverwood and thaumium
	 * crafting scepter.
	 * 
	 * @return
	 */
	public ItemStack getCraftingScepter()
	{
		if( this.craftingScepter == null )
		{
			// Create the item
			this.craftingScepter = new ItemStack( ConfigItems.itemWandCasting, 1, 32767 );

			// Set rod type to thaumium
			( (ItemWandCasting)this.craftingScepter.getItem() ).setCap( this.craftingScepter, ConfigItems.WAND_CAP_THAUMIUM );

			// Set cap type to silverwood
			( (ItemWandCasting)this.craftingScepter.getItem() ).setRod( this.craftingScepter, ConfigItems.WAND_ROD_SILVERWOOD );

			// Set that it is a scepter
			this.craftingScepter.setTagInfo( "sceptre", new NBTTagByte( (byte)1 ) );

			// Max out vis storage
			for( Aspect aspect : Aspect.getPrimalAspects() )
			{
				( (ItemWandCasting)this.craftingScepter.getItem() ).addVis( this.craftingScepter, aspect,
					( (ItemWandCasting)this.craftingScepter.getItem() ).getMaxVis( this.craftingScepter ), true );
			}
		}

		return this.craftingScepter;
	}

	/**
	 * Gets the vis discount given by the crafting scepter for the specified
	 * aspect.
	 * 
	 * @param aspect
	 * @return
	 */
	public float getScepterVisModifier( final Aspect aspect )
	{
		// Ensure the scepter has been created
		if( this.craftingScepter == null )
		{
			this.getCraftingScepter();
		}

		return ( (ItemWandCasting)this.craftingScepter.getItem() ).getConsumptionModifier( this.craftingScepter, null, aspect, true );
	}

}
