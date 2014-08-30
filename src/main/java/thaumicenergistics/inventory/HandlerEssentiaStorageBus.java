package thaumicenergistics.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileJarFillable;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
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

	/**
	 * Storage bus associated with this handler.
	 */
	private AEPartEssentiaStorageBus partStorageBus;

	/**
	 * Thaumcraft aspect container attracted to the storage bus.
	 */
	private IAspectContainer aspectContainer;

	/**
	 * Controls what operations this bus is allowed to perform.
	 */
	private AccessRestriction access;

	/**
	 * Filtered aspects
	 */
	private List<Aspect> filteredAspects = new ArrayList();

	/**
	 * When true the filter becomes a blacklist.
	 */
	private boolean inverted;

	/**
	 * Creates the handler.
	 * 
	 * @param part
	 */
	public HandlerEssentiaStorageBus( AEPartEssentiaStorageBus part )
	{
		// Set the part
		this.partStorageBus = part;

		// Set to full read/write access.
		this.access = AccessRestriction.READ_WRITE;
	}

	/**
	 * Verifies that the requested fluidstack to insert/extract is both an
	 * Essentia Gas and white-listed, or not black-listed.
	 * 
	 * @param fluidRequest
	 * @return True if the bus is allowed to transfer the fluid, false if it can
	 * not.
	 */
	private boolean canTransferFluid( FluidStack fluidRequest )
	{
		// Ensure the request is not null
		if( fluidRequest == null )
		{
			return false;
		}

		// Ensure the fluid an essentia gas
		if( !( fluidRequest.getFluid() instanceof GaseousEssentia ) )
		{
			return false;
		}

		// Get the gas form of the request
		GaseousEssentia gasRequest = (GaseousEssentia)fluidRequest.getFluid();

		// Get the aspect of the request
		Aspect requestAspect = gasRequest.getAssociatedAspect();

		/*
		 * Validate based on if the aspect is filtered and the storage bus is
		 * inverted. See explanation below.
		 */
		return( this.filteredAspects.contains( requestAspect ) == !this.inverted );

		/*
		 * Truth 'table' ---- Conditions: * isFiltered = true * inverted = false
		 * Expected outcome: * valid = true Sequence * valid = ( isFiltered ==
		 * !inverted ); * valid = ( true == !false ) * valid = ( true == true )
		 * * valid = true
		 * 
		 * ---- Conditions: * isFiltered = false * inverted = false Expected
		 * outcome: * valid = false Sequence * valid = ( isFiltered == !inverted
		 * ); * valid = ( false == !false ) * valid = ( false == true ) * valid
		 * = false
		 * 
		 * ---- Conditions: * isFiltered = true * inverted = true Expected
		 * outcome: * valid = false Sequence * valid = ( isFiltered == !inverted
		 * ); * valid = ( true == !true ) * valid = ( true == false ) * valid =
		 * false ----
		 * 
		 * Conditions: * isFiltered = false * inverted = true Expected outcome:
		 * * valid = true Sequence * valid = ( isFiltered == !inverted ); *
		 * valid = ( false == !true ) * valid = ( false == false ) * valid =
		 * true ----
		 */
	}

	/**
	 * Takes power from the AE network.
	 * 
	 * @param essentiaAmount
	 * @param mode
	 * @return True if power can/was taken. False otherwise.
	 */
	private boolean takePowerFromNetwork( int essentiaAmount, Actionable mode )
	{
		// Get the energy grid
		IEnergyGrid eGrid = this.partStorageBus.getGridBlock().getEnergyGrid();

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

	/**
	 * Is the specified fluid allowed to be placed in the container? This does
	 * not take into consideration the amount currently in the container.
	 */
	@Override
	public boolean canAccept( IAEFluidStack fluidStack )
	{
		// Ensure we have an aspect container
		if( this.aspectContainer == null )
		{
			// No container
			return false;
		}

		// Ensure the container is a fillable jar
		if( !( this.aspectContainer instanceof TileJarFillable ) )
		{
			// Invalid container
			return false;
		}

		// Is our access restricted to read-only or no access?
		if( ( this.access == AccessRestriction.READ ) || ( this.access == AccessRestriction.NO_ACCESS ) )
		{
			// Not allowed to insert
			return false;
		}

		// Ensure we are allowed to transfer this fluid
		if( !this.canTransferFluid( fluidStack.getFluidStack() ) )
		{
			/*
			 * Either: Not an essentia gas Not on whitelist Is on blacklist
			 */
			return false;
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
		Aspect gasAspect = ( (GaseousEssentia)fluidStack.getFluid() ).getAssociatedAspect();

		// Does the aspect in the container match the gas aspect?
		return gasAspect == containerStack.aspect;
	}

	/**
	 * Extracts essentia from the container.
	 */
	@Override
	public IAEFluidStack extractItems( IAEFluidStack request, Actionable mode, BaseActionSource source )
	{
		if( ( this.aspectContainer == null ) || ( request == null ) )
		{
			// Nothing to drain from, or empty request
			return null;
		}

		// Get the fluid stack from the request
		FluidStack toDrain = request.getFluidStack();

		// Validate the request
		if( !this.canTransferFluid( toDrain ) )
		{
			return null;
		}

		// Simulate draining the container
		FluidStack drained = EssentiaTileContainerHelper.instance.extractFromContainer( this.aspectContainer, toDrain, Actionable.SIMULATE );

		// Was any drained?
		if( ( drained == null ) || ( drained.amount == 0 ) )
		{
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

		// Inform the storage bus
		this.partStorageBus.onEssentiaTransfered( -drainedAmount_EU );

		// Did fulfill the request fully?
		if( drained.amount == toDrain.amount )
		{
			// Fully satisfied.
			return request;
		}

		// Return how much was drained
		return AEApi.instance().storage().createFluidStack( new FluidStack( toDrain.getFluid(), drained.amount ) );
	}

	/**
	 * Returns the access restrictions, if any, imposed on the storage bus.
	 */
	@Override
	public AccessRestriction getAccess()
	{
		return this.access;
	}

	/**
	 * Gets the list of 'fluids' that can be extracted from this storage bus.
	 */
	@Override
	public IItemList<IAEFluidStack> getAvailableItems( IItemList<IAEFluidStack> out )
	{
		if( this.aspectContainer != null )
		{

			// Only report back items that are extractable
			if( EssentiaTileContainerHelper.instance.canExtract( this.aspectContainer ) )
			{
				// Get the essentia and amount in the container
				AspectStack containerStack = EssentiaTileContainerHelper.instance.getAspectStackFromContainer( this.aspectContainer );

				// Is there an aspect?
				if( containerStack != null )
				{
					// Is the aspect in the filter?
					if( this.filteredAspects.contains( containerStack.aspect ) )
					{
						// Convert to fluid
						GaseousEssentia gas = GaseousEssentia.getGasFromAspect( containerStack.aspect );

						// Add to the item list
						out.add( EssentiaConversionHelper.instance.createAEFluidStackInEssentiaUnits( gas, (int)containerStack.amount ) );
					}
				}
			}
		}

		return out;
	}

	/**
	 * Gets the storage channel for the storage bus.
	 */
	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.FLUIDS;
	}

	/**
	 * Gets the priority of the storage bus.
	 */
	@Override
	public int getPriority()
	{
		return this.partStorageBus.getPriority();
	}

	/**
	 * Dunno
	 */
	@Override
	public int getSlot()
	{
		return 0;
	}

	/**
	 * Inserts essentia into the container.
	 */
	@Override
	public IAEFluidStack injectItems( IAEFluidStack input, Actionable mode, BaseActionSource source )
	{
		if( ( this.aspectContainer == null ) || ( input == null ) || ( !this.canAccept( input ) ) )
		{
			return input;
		}

		// Get the fluid stack from the input
		FluidStack toFill = input.getFluidStack();

		// Validate the request
		if( !this.canTransferFluid( toFill ) )
		{
			return null;
		}

		// Simulate filling the container
		int filled_FU = (int)EssentiaTileContainerHelper.instance.injectIntoContainer( this.aspectContainer, input, Actionable.SIMULATE );

		// Was any filled?
		if( filled_FU == 0 )
		{
			return input;
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
			// Inject
			EssentiaTileContainerHelper.instance.injectIntoContainer( this.aspectContainer, input, Actionable.MODULATE );

			// Take power
			this.takePowerFromNetwork( filled_EU, Actionable.MODULATE );
		}

		// Inform the storage bus
		this.partStorageBus.onEssentiaTransfered( filled_EU );

		// Calculate how much was left over
		int remaining_FU = toFill.amount - filled_FU;

		// Did we completely drain the input stack?
		if( remaining_FU == 0 )
		{
			// Nothing left over
			return null;
		}

		// Return what was left over
		return AEApi.instance().storage().createFluidStack( new FluidStack( toFill.getFluid(), remaining_FU ) );
	}

	/**
	 * Checks if the specified fluid is allowed to be transfered.
	 */
	@Override
	public boolean isPrioritized( IAEFluidStack fluidStack )
	{
		// Ensure the fluid stack is not null
		if( fluidStack == null )
		{
			return false;
		}

		// Validate
		return this.canTransferFluid( fluidStack.getFluidStack() );
	}

	/**
	 * Checks if we are still facing a valid container.
	 */
	public void onNeighborChange()
	{
		this.aspectContainer = null;

		// Get the host
		TileEntity hostTile = this.partStorageBus.getHostTile();

		// Is there a host?
		if( hostTile == null )
		{
			return;
		}

		// Is the host in a loaded world?
		if( hostTile.getWorldObj() == null )
		{
			return;
		}

		// Get what direction we are facing.
		ForgeDirection orientation = this.partStorageBus.getSide();

		// Get the tile entity we are facing
		TileEntity tileEntity = hostTile.getWorldObj().getTileEntity( hostTile.xCoord + orientation.offsetX, hostTile.yCoord + orientation.offsetY,
			hostTile.zCoord + orientation.offsetZ );

		// Are we facing an essentia container?
		if( tileEntity instanceof IAspectContainer )
		{
			this.aspectContainer = (IAspectContainer)tileEntity;
		}
	}

	/**
	 * Sets if we are inverted or not.
	 * 
	 * @param isInverted
	 * True = Blacklist, False = Whitelist.
	 */
	public void setInverted( boolean isInverted )
	{
		this.inverted = isInverted;
	}

	/**
	 * Set's the list of filtered aspects.
	 * 
	 * @param aspectList
	 */
	public void setPrioritizedAspects( List<Aspect> aspectList )
	{
		this.filteredAspects = aspectList;
	}

	@Override
	public boolean validForPass( int pass )
	{
		return true;
	}

}
