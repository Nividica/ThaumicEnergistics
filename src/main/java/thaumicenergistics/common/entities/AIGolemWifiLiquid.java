package thaumicenergistics.common.entities;

import java.util.ArrayList;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;
import thaumicenergistics.common.entities.WirelessGolemHandler.WirelessServerData;

public class AIGolemWifiLiquid
	extends AIAENetworkGolem
{

	private FluidStack wantedFluid;

	public AIGolemWifiLiquid( final EntityGolemBase golem, final WirelessServerData wsd )
	{
		super( golem, wsd );
	}

	@Override
	protected boolean needsNetworkNow()
	{
		// Is the golem in-need of a fluid?
		ArrayList<FluidStack> fluids = GolemHelper.getMissingLiquids( this.golem );

		// Don't bother if the golem is asking for EVERY fluid, only if there is a specific request.
		if( ( fluids != null ) && ( fluids.size() == 1 ) )
		{
			// Set the wanted fluid
			this.wantedFluid = fluids.get( 0 ).copy();

			// Verify that the golem is carrying the fluid, or nothing
			if( ( this.golem.fluidCarried == null ) || ( this.golem.fluidCarried.getFluid() == this.wantedFluid.getFluid() ) )
			{
				return true;
			}
			this.wantedFluid = null;
		}
		return false;
	}

	@Override
	public void updateTask()
	{
		if( this.wantedFluid == null )
		{
			return;
		}

		// Is the golem carrying the fluid?
		int existingSize = 0;
		if( this.golem.fluidCarried != null )
		{
			existingSize = this.golem.fluidCarried.amount;
		}

		// Calculate maximum request size
		this.wantedFluid.amount = 1000 - existingSize;

		// Attempt extraction
		FluidStack extracted = this.network.extractFluid( this.wantedFluid );
		if( extracted == null )
		{
			return;
		}

		// Add to existing?
		if( existingSize > 0 )
		{
			this.golem.fluidCarried.amount += extracted.amount;
		}
		else
		{
			this.golem.fluidCarried = extracted;
		}

		this.wantedFluid = null;
	}

}
