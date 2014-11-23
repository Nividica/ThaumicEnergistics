package thaumicenergistics.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumcraft.common.tiles.TileJarFillableVoid;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import com.google.common.collect.ImmutableList;

class HandlerEssentiaStorageBusContainer
	extends AbstractHandlerEssentiaStorageBus
{
	/**
	 * The amount of power required to transfer 1 essentia.
	 */
	private static final double POWER_DRAIN_PER_ESSENTIA = 0.5;

	/**
	 * Thaumcraft aspect container attracted to the storage bus.
	 */
	private IAspectContainer aspectContainer;

	public HandlerEssentiaStorageBusContainer( final AEPartEssentiaStorageBus part )
	{
		super( part );
	}

	/**
	 * Returns true if there are no filters.
	 * 
	 * @return
	 */
	private boolean allowAny()
	{
		// Are all filters null?
		for( Aspect filteredAspect : this.filteredAspects )
		{
			if( filteredAspect != null )
			{
				// There is a filter
				return false;
			}
		}

		// No filters
		return true;
	}

	/**
	 * Gets essentia stored in the container.
	 */
	private List<AspectStack> getAvailableEssentia()
	{
		// Ensure there is a container
		if( this.aspectContainer == null )
		{
			// No container
			return null;
		}

		// Ensure the container is whitelisted
		if( !TEAPI.transportPermissions().canExtractFromAspectContainerTile( this.aspectContainer ) )
		{
			// Not whitelisted
			return null;
		}

		// Get the essentia and amounts in the container
		List<AspectStack> containerStacks = EssentiaTileContainerHelper.instance.getAspectStacksFromContainer( this.aspectContainer );

		// Ensure there is essentia in the container
		if( ( containerStacks == null ) || containerStacks.isEmpty() )
		{
			// Empty
			return null;
		}

		List<AspectStack> essentiaList = new ArrayList<AspectStack>();

		// Skipping the filter check?
		boolean skipFilterCheck = this.allowAny();

		// Add the essentia
		for( AspectStack essentia : containerStacks )
		{
			// Is the aspect in the filter?
			if( skipFilterCheck || ( this.filteredAspects.contains( essentia.aspect ) ) )
			{
				// Convert to fluid
				GaseousEssentia gas = GaseousEssentia.getGasFromAspect( essentia.aspect );

				// Is there a fluid form of the aspect?
				if( gas != null )
				{
					// Add to the list
					essentiaList.add( new AspectStack( essentia.aspect, essentia.amount ) );
				}
			}
		}

		return essentiaList;
	}

	/**
	 * Takes power from the AE network.
	 * 
	 * @param essentiaAmount
	 * @param mode
	 * @return True if power can/was taken. False otherwise.
	 */
	private boolean takePowerFromNetwork( final int essentiaAmount, final Actionable mode )
	{
		// Get the energy grid
		IEnergyGrid eGrid = this.partStorageBus.getGridBlock().getEnergyGrid();

		// Ensure we have a grid
		if( eGrid == null )
		{
			return false;
		}

		// Calculate amount of power to take
		double powerDrain = HandlerEssentiaStorageBusContainer.POWER_DRAIN_PER_ESSENTIA * essentiaAmount;

		// Extract
		return( eGrid.extractAEPower( powerDrain, mode, PowerMultiplier.CONFIG ) >= powerDrain );
	}

	@Override
	protected boolean canTransferGas( final GaseousEssentia essentiaGas )
	{
		// Are there no filters set?
		if( this.allowAny() )
		{
			// Allow the storage bus to store anything when no filters set.
			return true;
		}

		// Pass to super
		return super.canTransferGas( essentiaGas );
	}

	@Override
	public boolean canAccept( final IAEFluidStack fluidStack )
	{
		// Ensure we have an aspect container
		if( this.aspectContainer == null )
		{
			// No container
			return false;
		}

		// Ensure the bus has security access
		if( !hasSecurityPermission() )
		{
			// The bus does not have security access.
			return false;
		}

		// Ensure the fluid is an essentia gas
		if( !this.isFluidEssentiaGas( fluidStack ) )
		{
			// Not essentia gas.
			return false;
		}

		// Ensure the container is whitelisted
		if( !( TEAPI.transportPermissions().canInjectToAspectContainerTile( this.aspectContainer ) ) )
		{
			// Invalid container
			return false;
		}

		// Ensure we are allowed to transfer this fluid
		if( !this.canTransferGas( (GaseousEssentia)fluidStack.getFluid() ) )
		{
			/*
			 * Either: Not on whitelist or is on blacklist
			 */
			return false;
		}

		// Is the container a reservoir
		if( this.aspectContainer instanceof TileEssentiaReservoir )
		{
			// Reservoir can accept any type
			return true;
		}

		// Get the essentia, if any, in the container
		AspectStack containerStack = EssentiaTileContainerHelper.instance.getAspectStackFromContainer( this.aspectContainer );

		// Is the container empty?
		if( containerStack == null )
		{
			// Container is empty, can accept any aspect.
			return true;
		}

		// Get the gas aspect
		Aspect gasAspect = ( (GaseousEssentia)fluidStack.getFluid() ).getAspect();

		// Does the aspect in the container match the gas aspect?
		return gasAspect == containerStack.aspect;
	}

	/**
	 * Extracts essentia from the container.
	 * returns the number of items extracted, null
	 */
	@Override
	public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable mode, final BaseActionSource source )
	{
		if( ( this.aspectContainer == null ) || ( request == null ) )
		{
			// Nothing to drain from, or empty request
			return null;
		}

		// Ensure the fluid is an essentia gas
		if( !this.isFluidEssentiaGas( request ) )
		{
			// Not essentia gas fluid.
			return null;
		}

		// Get the fluid stack from the request
		FluidStack toDrain = request.getFluidStack();

		// Can this gas be transfered?
		if( !this.canTransferGas( (GaseousEssentia)toDrain.getFluid() ) )
		{
			// Can not transfer this gas.
			return null;
		}

		// Simulate draining the container
		FluidStack drained = EssentiaTileContainerHelper.instance.extractFromContainer( this.aspectContainer, toDrain, Actionable.SIMULATE );

		// Was any drained?
		if( ( drained == null ) || ( drained.amount == 0 ) )
		{
			// None was drained from the container.
			return null;
		}

		// Convert the drain amount to essentia units
		int drainedAmount_EU = (int)EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( drained.amount );

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
			EssentiaTileContainerHelper.instance.extractFromContainer( this.aspectContainer, toDrain, Actionable.MODULATE );

			// Take power
			this.takePowerFromNetwork( drainedAmount_EU, Actionable.MODULATE );
		}

		// Copy the request
		IAEFluidStack extractedFluid = request.copy();

		// Set the amount extracted
		extractedFluid.setStackSize( drained.amount );

		return extractedFluid;
	}

	/**
	 * Gets the list of fluids from the container.
	 */
	@Override
	public IItemList<IAEFluidStack> getAvailableItems( final IItemList<IAEFluidStack> out )
	{
		if( this.aspectContainer != null )
		{
			List<AspectStack> essentiaList = this.getAvailableEssentia();

			if( essentiaList != null )
			{
				for( AspectStack essentia : essentiaList )
				{
					// Convert to fluid
					GaseousEssentia gas = GaseousEssentia.getGasFromAspect( essentia.aspect );

					// Add to the item list
					out.add( EssentiaConversionHelper.instance.createAEFluidStackInEssentiaUnits( gas, essentia.amount ) );
				}
			}
		}

		return out;
	}

	/**
	 * Inserts essentia into the container.
	 * returns the number of items not added.
	 */
	@Override
	public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable mode, final BaseActionSource source )
	{
		// Ensure input and output
		if( ( this.aspectContainer == null ) || ( input == null ) || ( !this.canAccept( input ) ) )
		{
			return input;
		}

		// Get the fluid stack from the input
		FluidStack toFill = input.getFluidStack();

		// Ensure the bus can transfer the gas.
		if( !this.canTransferGas( (GaseousEssentia)toFill.getFluid() ) )
		{
			// Can not inject this gas.
			return input;
		}

		// Is void allowed, and we are attached to a void jar?
		boolean canVoid = this.isVoidAllowed() && ( this.aspectContainer instanceof TileJarFillableVoid );

		// How much can be filled, in fluid units
		int filled_FU;

		// Can we void any leftovers?
		if( canVoid )
		{
			// Say that we can take it all
			filled_FU = (int)input.getStackSize();
		}
		else
		{
			// Simulate filling the container
			filled_FU = (int)EssentiaTileContainerHelper.instance.injectIntoContainer( this.aspectContainer, input, Actionable.SIMULATE );

			// Was any filled?
			if( filled_FU == 0 )
			{
				// Can not inject partial essentia amounts.
				return input;
			}
		}

		// Get how much the filled amount is in essentia units
		int filled_EU = (int)EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( filled_FU );

		// Do we have the power to complete this operation?
		if( !this.takePowerFromNetwork( filled_EU, Actionable.SIMULATE ) )
		{
			// Not enough power
			return input;
		}

		// Are we modulating?
		if( mode == Actionable.MODULATE )
		{
			// Inject, and set the actual amount injected
			filled_FU = (int)EssentiaTileContainerHelper.instance.injectIntoContainer( this.aspectContainer, input, Actionable.MODULATE );

			// Take power for as much as we claimed we could take.
			this.takePowerFromNetwork( filled_EU, Actionable.MODULATE );

			// Convert the actual amount injected into Essentia units.
			filled_EU = (int)EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( filled_FU );
		}

		// Calculate how much was left over
		int remaining_FU = toFill.amount - filled_FU;

		// Did we completely drain the input stack?
		if( remaining_FU == 0 )
		{
			// Nothing left over
			return null;
		}
		// Are we voiding the remainder?
		else if( canVoid )
		{
			// Update the grid so that it doesn't thing we have stored the voided amount.
			this.postAlterationToHostGrid( ImmutableList.of( AEApi.instance().storage()
							.createFluidStack( new FluidStack( toFill.getFluid(), -remaining_FU ) ) ) );

			// Report that we accepted it all.
			return null;
		}

		// Return what was left over
		IAEFluidStack remainingFluid = input.copy();
		remainingFluid.setStackSize( remaining_FU );

		return remainingFluid;
	}

	/**
	 * Checks if we are still facing a valid container.
	 * 
	 * @return
	 */
	@Override
	public boolean onNeighborChange()
	{
		// Get the tile we are facing
		TileEntity tileEntity = getFaceingTile();

		// Are we facing an essentia container?
		if( tileEntity instanceof IAspectContainer )
		{
			// Has the container changed?
			if( this.aspectContainer != tileEntity )
			{
				// Set the container
				this.aspectContainer = (IAspectContainer)tileEntity;
			}

			return true;
		}

		if( this.aspectContainer != null )
		{
			this.aspectContainer = null;
			return true;
		}

		return false;
	}

	/**
	 * Checks the Thaumcraft container.
	 */
	@Override
	public void tickingRequest( final IGridNode node, final int TicksSinceLastCall )
	{
		// Ignored
	}

}
