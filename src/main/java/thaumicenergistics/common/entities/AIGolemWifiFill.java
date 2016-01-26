package thaumicenergistics.common.entities;

import net.minecraft.item.ItemStack;
import thaumcraft.common.entities.golems.EntityGolemBase;

/**
 * Allows a golem to extract items from the network as it needs them.
 * 
 * @author Nividica
 * 
 */
public class AIGolemWifiFill
	extends AIAENetworkGolem
{

	public AIGolemWifiFill( final EntityGolemBase golem, final WirelessGolemHandler.WirelessServerData wsd )
	{
		super( golem, wsd );
	}

	@Override
	protected boolean needsNetworkNow()
	{
		// Is the golem watching for anything?
		return( this.golem.itemWatched != null );
	}

	@Override
	public void updateTask()
	{
		// Get the stack the golem is watching for.
		ItemStack watched = this.golem.itemWatched;
		if( watched != null )
		{
			// Attempt extraction
			ItemStack extracted = this.network.extractStack( watched );
			if( extracted != null )
			{
				this.golem.setCarried( extracted );
				this.golem.itemWatched = null;
			}
		}
	}

}
