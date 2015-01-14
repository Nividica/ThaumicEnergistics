package thaumicenergistics.integration.tc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;

public class VisCraftingHelper
{

	public static final VisCraftingHelper instance = new VisCraftingHelper();

	private ItemStack craftingScepter = null;

	/**
	 * Private constructor
	 */
	private VisCraftingHelper()
	{
		// Intentionally Empty
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
