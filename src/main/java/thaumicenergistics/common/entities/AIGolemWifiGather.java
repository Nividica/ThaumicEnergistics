package thaumicenergistics.common.entities;

import net.minecraft.item.ItemStack;
import thaumcraft.common.entities.golems.EntityGolemBase;

public class AIGolemWifiGather
	extends AIAENetworkGolem
{
	public AIGolemWifiGather( final EntityGolemBase golem, final String encKey )
	{
		super( golem, encKey );
	}

	@Override
	public boolean needsNetworkInteraction()
	{
		// Execute when the golem is holding something
		return( this.golem.getCarried() != null );
	}

	@Override
	public void updateTask()
	{
		ItemStack heldItem = this.golem.getCarried();

		// Ensure the golem is holding something.
		if( heldItem != null )
		{
			// Deposit the item
			this.network.depositStack( heldItem );

			// Was the stack drained?
			if( heldItem.stackSize == 0 )
			{
				// Clear what the golem is holding
				this.golem.setCarried( null );

				// Throw his arms up for good measure.
				this.golem.startActionTimer();
			}
		}
	}

}
