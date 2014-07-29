package thaumicenergistics.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileJarFillable;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
import thaumicenergistics.util.EssentiaConversionHelper;
import thaumicenergistics.util.EssentiaTileContainerHelper;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;

public class HandlerEssentiaStorageBus
	implements IMEInventoryHandler<IAEFluidStack>
{

	/**
	 * The amount of power required to transfer 1 essentia.
	 */
	private static final double POWER_DRAIN_PER_ESSENTIA = 0.5;
	
	private AEPartEssentiaStorageBus part;
	private IAspectContainer aspectContainer;
	private AccessRestriction access;
	private List<Aspect> prioritizedAspects = new ArrayList();
	private boolean inverted;

	public HandlerEssentiaStorageBus( AEPartEssentiaStorageBus part )
	{
		this.part = part;
	}

	@Override
	public boolean canAccept( IAEFluidStack fluidStack )
	{
		boolean acceptable = false;

		if ( ( this.aspectContainer != null ) && ( this.aspectContainer instanceof TileJarFillable ) && ( this.access != AccessRestriction.WRITE ) &&
						( this.access != AccessRestriction.NO_ACCESS ) )
		{
			// Get the fluid
			Fluid fluid = fluidStack.getFluid();

			// Is the fluid essentia gas?
			if ( fluid instanceof GaseousEssentia )
			{
				// Get the aspect
				Aspect gasAspect = ( (GaseousEssentia)fluid ).getAssociatedAspect();

				// Get the essentia, if any, in the container
				AspectStack containerAspect = EssentiaTileContainerHelper.getAspectStackFromContainer( this.aspectContainer );

				// Does the container contain essentia, if so does it match?
				if ( ( containerAspect == null ) || ( gasAspect == containerAspect.aspect ) )
				{
					if ( this.inverted )
					{
						acceptable = ( !this.prioritizedAspects.isEmpty() || !this.isPrioritized( fluidStack ) );
					}
					else
					{
						acceptable = ( this.prioritizedAspects.isEmpty() || this.isPrioritized( fluidStack ) );
					}
				}

			}

		}

		return acceptable;
	}

	@Override
	public IAEFluidStack extractItems( IAEFluidStack request, Actionable mode, BaseActionSource source )
	{
		if ( ( this.aspectContainer == null ) || ( request == null ) )
		{
			// Nothing to drain from, or empty request
			return null;
		}

		// Get the fluid stack from the request
		FluidStack toDrain = request.getFluidStack();

		// Simulate draining the container
		FluidStack drained = EssentiaTileContainerHelper.extractFromContainer( this.aspectContainer, toDrain, Actionable.SIMULATE );

		// Was any drained?
		if ( ( drained == null ) || ( drained.amount == 0 ) )
		{
			return null;
		}
		
		// Convert the drain amount to essentia units
		int drainedAmount_EU = (int)EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( drained.amount );
		
		// Do we have the power to drain this?
		if( !this.takePowerFromNetwork( drainedAmount_EU, Actionable.SIMULATE ) )
		{
			// Not enough power
			return null;
		}
		
		// Are we modulating?
		if( mode == Actionable.MODULATE )
		{
			// Extract
			EssentiaTileContainerHelper.extractFromContainer( this.aspectContainer, toDrain, Actionable.MODULATE );
			
			// Take power
			this.takePowerFromNetwork( drainedAmount_EU, Actionable.MODULATE );
		}

		// Did fulfill the request fully?
		if ( drained.amount == toDrain.amount )
		{
			// Fully satisfied.
			return request;
		}

		// Return how much was drained
		return AEApi.instance().storage().createFluidStack( new FluidStack( toDrain.getFluid(), drained.amount ) );
	}

	@Override
	public AccessRestriction getAccess()
	{
		return this.access;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems( IItemList<IAEFluidStack> out )
	{
		if ( this.aspectContainer != null )
		{

			// Only report back items that are extractable
			if ( EssentiaTileContainerHelper.canExtract( this.aspectContainer ) )
			{
				// Get the essentia and amount in the container
				AspectStack containerStack = EssentiaTileContainerHelper.getAspectStackFromContainer( this.aspectContainer );

				if ( containerStack != null )
				{
					// Convert to fluid
					GaseousEssentia gas = GaseousEssentia.getGasFromAspect( containerStack.aspect );

					// Add to the item list
					out.add( EssentiaConversionHelper.createAEFluidStackInEssentiaUnits( gas, (int)containerStack.amount ) );
				}
			}
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.FLUIDS;
	}

	@Override
	public int getPriority()
	{
		return this.part.getPriority();
	}

	@Override
	public int getSlot()
	{
		return 0;
	}
	
	private boolean takePowerFromNetwork( int essentiaAmount, Actionable mode )
	{
		// Get the energy grid
		IEnergyGrid eGrid = this.part.getGridBlock().getEnergyGrid();

		// Ensure we have a grid
		if( eGrid == null )
		{
			return false;
		}

		// Calculate amount of power to take
		double powerDrain = HandlerEssentiaStorageBus.POWER_DRAIN_PER_ESSENTIA * essentiaAmount;

		// Extract
		return( eGrid.extractAEPower( powerDrain, mode, PowerMultiplier.CONFIG ) >= powerDrain );
	}

	@Override
	public IAEFluidStack injectItems( IAEFluidStack input, Actionable mode, BaseActionSource source )
	{
		if ( ( this.aspectContainer == null ) || ( input == null ) || ( !this.canAccept( input ) ) )
		{
			return input;
		}

		// Get the fluid stack from the input
		FluidStack toFill = input.getFluidStack();

		// Simulate filling the container
		int filled_FU = (int)EssentiaTileContainerHelper.injectIntoContainer( this.aspectContainer, input, Actionable.SIMULATE );

		// Was any filled?
		if ( filled_FU == 0 )
		{
			return input;
		}
		
		// Get how much the filled amount is in essentia units
		int filled_EU = (int)EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( filled_FU );
		
		// Do we have the power to complete this operation?
		if( !this.takePowerFromNetwork( filled_EU, Actionable.SIMULATE ) )
		{
			// Not enough power
			return input;
		}
		
		// Are we modulating?
		if( mode == Actionable.MODULATE )
		{
			// Inject
			EssentiaTileContainerHelper.injectIntoContainer( this.aspectContainer, input, Actionable.MODULATE );
			
			// Take power
			this.takePowerFromNetwork( filled_EU, Actionable.MODULATE );
		}
		
		// Calculate how much was left over
		int remaining_FU = toFill.amount - filled_FU;

		// Did we completely drain the input stack?
		if ( remaining_FU == 0 )
		{
			// Nothing left over
			return null;
		}

		//  Return what was left over
		return AEApi.instance().storage().createFluidStack( new FluidStack( toFill.getFluid(), remaining_FU ) );
	}

	@Override
	public boolean isPrioritized( IAEFluidStack fluidStack )
	{
		boolean isPriority = false;

		if ( fluidStack != null )
		{
			// Get the fluid
			Fluid fluid = fluidStack.getFluid();

			// Is it an essentia gas?
			if ( fluid instanceof GaseousEssentia )
			{
				// Get the aspect
				Aspect gasAspect = ( (GaseousEssentia)fluid ).getAssociatedAspect();

				// Check the prioritized array
				for( Aspect aspect : this.prioritizedAspects )
				{
					if ( aspect == gasAspect )
					{
						isPriority = true;

						break;
					}
				}
			}
		}

		return isPriority;
	}

	public void onNeighborChange()
	{
		this.aspectContainer = null;

		// Get the host
		TileEntity hostTile = this.part.getHostTile();

		// Is there a host?
		if ( hostTile == null )
		{
			return;
		}

		// Is the host in a loaded world?
		if ( hostTile.getWorldObj() == null )
		{
			return;
		}

		// Get what direction we are facing.
		ForgeDirection orientation = this.part.getSide();

		// Get the tile entity we are facing
		TileEntity tileEntity = hostTile.getWorldObj().getTileEntity( hostTile.xCoord + orientation.offsetX, hostTile.yCoord + orientation.offsetY,
			hostTile.zCoord + orientation.offsetZ );

		// Are we facing an essentia container?
		if ( tileEntity instanceof IAspectContainer )
		{
			this.aspectContainer = (IAspectContainer)tileEntity;
		}
	}

	public void setInverted( boolean isInverted )
	{
		this.inverted = isInverted;
	}

	public void setPrioritizedAspects( List<Aspect> aspectList )
	{
		this.prioritizedAspects = aspectList;
	}

}
