package thaumicenergistics.inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
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
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import com.google.common.collect.ImmutableList;

class HandlerEssentiaStorageBusContainer
	extends AbstractHandlerEssentiaStorageBus
{
	/**
	 * Thaumcraft aspect container attracted to the storage bus.
	 */
	private IAspectContainer aspectContainer;

	/**
	 * Cached contents of the container.
	 */
	Hashtable<Aspect, Long> cachedContainerAspects = new Hashtable<Aspect, Long>();

	public HandlerEssentiaStorageBusContainer( final AEPartEssentiaStorageBus part )
	{
		super( part );
	}

	/**
	 * Adds a list of aspects stacks to a <Aspect,Long> dictionary.
	 * 
	 * @param essentiaList
	 */
	private void addListToDictionary( final List<AspectStack> essentiaList, final Hashtable<Aspect, Long> dictionary )
	{
		// Add each essentia
		if( essentiaList != null )
		{
			for( AspectStack stack : essentiaList )
			{
				dictionary.put( stack.aspect, stack.amount );
			}
		}
	}

	/**
	 * Adjusts the cached aspect amount based on the specified delta.
	 * Positive diff adds to the amount, negative diff removes from the
	 * amount.
	 * 
	 * @param aspect
	 * @param diff
	 */
	private void adjustCache( final Aspect aspect, final long diff )
	{
		long cachedAmount = 0;

		// Does the cache have this aspect?
		if( this.cachedContainerAspects.containsKey( aspect ) )
		{
			cachedAmount = this.cachedContainerAspects.get( aspect );
		}

		// Change the amount
		long newAmount = cachedAmount + diff;

		// Is there any amount left?
		if( newAmount > 0 )
		{
			// Update the cache
			this.cachedContainerAspects.put( aspect, newAmount );
		}
		// None left, was there some?
		else if( cachedAmount > 0 )
		{
			// Remove from the cache
			this.cachedContainerAspects.remove( aspect );
		}
	}

	/**
	 * Gets essentia stored in the container.
	 */
	private List<AspectStack> getContainerEssentia()
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
		List<AspectStack> containerStacks = EssentiaTileContainerHelper.INSTANCE.getAspectStacksFromContainer( this.aspectContainer );

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
		for( AspectStack essentiaStack : containerStacks )
		{
			// Is the aspect in the filter?
			if( skipFilterCheck || ( this.filteredAspects.contains( essentiaStack.aspect ) ) )
			{
				// Convert to fluid
				GaseousEssentia gas = GaseousEssentia.getGasFromAspect( essentiaStack.aspect );

				// Is there a fluid form of the aspect?
				if( gas != null )
				{
					// Add to the list
					essentiaList.add( essentiaStack );
				}
			}
		}

		return essentiaList;
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
		AspectStack containerStack = EssentiaTileContainerHelper.INSTANCE.getAspectStackFromContainer( this.aspectContainer );

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
		FluidStack drained = EssentiaTileContainerHelper.INSTANCE.extractFromContainer( this.aspectContainer, toDrain, Actionable.SIMULATE );

		// Was any drained?
		if( ( drained == null ) || ( drained.amount == 0 ) )
		{
			// None was drained from the container.
			return null;
		}

		// Convert the drain amount to essentia units
		int drainedAmount_EU = (int)EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( drained.amount );

		// Do we have the power to drain this?
		if( !this.partStorageBus.extractPowerForEssentiaTransfer( drainedAmount_EU, Actionable.SIMULATE ) )
		{
			// Not enough power
			return null;
		}

		// Are we modulating?
		if( mode == Actionable.MODULATE )
		{
			// Extract
			EssentiaTileContainerHelper.INSTANCE.extractFromContainer( this.aspectContainer, toDrain, Actionable.MODULATE );

			// Take power
			this.partStorageBus.extractPowerForEssentiaTransfer( drainedAmount_EU, Actionable.MODULATE );

			// Update cache
			this.adjustCache( ( (GaseousEssentia)toDrain.getFluid() ).getAspect(), -drainedAmount_EU );
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
			// Get the contents of the container
			List<AspectStack> essentiaList = this.getContainerEssentia();

			// Update the cache
			this.cachedContainerAspects.clear();
			this.addListToDictionary( essentiaList, this.cachedContainerAspects );

			if( essentiaList != null )
			{
				for( AspectStack essentia : essentiaList )
				{
					// Convert to fluid
					GaseousEssentia gas = GaseousEssentia.getGasFromAspect( essentia.aspect );

					// Add to the item list
					out.add( EssentiaConversionHelper.INSTANCE.createAEFluidStackInEssentiaUnits( gas, essentia.amount ) );
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
			filled_FU = (int)EssentiaTileContainerHelper.INSTANCE.injectIntoContainer( this.aspectContainer, input, Actionable.SIMULATE );

			// Was any filled?
			if( filled_FU == 0 )
			{
				// Can not inject any.
				return input;
			}
		}

		// Get how much the filled amount is in essentia units
		int filled_EU = (int)EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( filled_FU );

		// Do we have the power to complete this operation?
		if( !this.partStorageBus.extractPowerForEssentiaTransfer( filled_EU, Actionable.SIMULATE ) )
		{
			// Not enough power
			return input;
		}

		// Are we modulating?
		if( mode == Actionable.MODULATE )
		{
			// Inject, and set the actual amount injected
			filled_FU = (int)EssentiaTileContainerHelper.INSTANCE.injectIntoContainer( this.aspectContainer, input, Actionable.MODULATE );

			// Take power for as much as we claimed we could take.
			this.partStorageBus.extractPowerForEssentiaTransfer( filled_EU, Actionable.MODULATE );

			// Convert the actual amount injected into Essentia units.
			filled_EU = (int)EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( filled_FU );

			// Update the cache
			this.adjustCache( ( (GaseousEssentia)toFill.getFluid() ).getAspect(), filled_EU );
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

				// Clear the cache
				this.cachedContainerAspects.clear();

				// Container changed
				return true;
			}

			return false;
		}

		// Was the bus facing a container?
		if( this.aspectContainer != null )
		{
			// Clear the reference
			this.aspectContainer = null;

			// Send one last tick
			this.tickingRequest( null, 0 );

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
		// Alteration list
		List<IAEFluidStack> alterations = null;

		// Create the checklist
		HashSet<Aspect> aspectsToCheck = new HashSet<Aspect>();

		// Get the current contents of the container
		List<AspectStack> currentContainerContents = this.getContainerEssentia();

		// Convert to dictionary
		Hashtable<Aspect, Long> currentContainerAspects = new Hashtable<Aspect, Long>();
		if( currentContainerContents != null )
		{
			this.addListToDictionary( currentContainerContents, currentContainerAspects );

			// Add the current aspects to check list
			aspectsToCheck.addAll( currentContainerAspects.keySet() );
		}

		// Add the cached aspects to check list
		aspectsToCheck.addAll( this.cachedContainerAspects.keySet() );

		// Is there anything to check?
		if( aspectsToCheck.size() == 0 )
		{
			// Nothing to check.
			return;
		}

		// Compare all amounts
		for( Aspect aspect : aspectsToCheck )
		{
			// Value cached
			long cachedAmount = 0;
			if( this.cachedContainerAspects.containsKey( aspect ) )
			{
				cachedAmount = this.cachedContainerAspects.get( aspect );
			}

			// Current value
			long currentAmount = 0;
			if( currentContainerAspects.containsKey( aspect ) )
			{
				currentAmount = currentContainerAspects.get( aspect );
			}

			// Calculate the difference
			long diff = currentAmount - cachedAmount;

			// Do they differ?
			if( diff != 0 )
			{
				// First alteration?
				if( alterations == null )
				{
					// Create the list
					alterations = new ArrayList<IAEFluidStack>();
				}

				// Create the alteration
				alterations.add( EssentiaConversionHelper.INSTANCE.createAEFluidStackInEssentiaUnits( aspect, diff ) );

				// Update the cache
				this.adjustCache( aspect, diff );
			}
		}

		// Any alterations?
		if( alterations != null )
		{
			// Post the changes
			this.postAlterationToHostGrid( alterations );
		}

	}

	/**
	 * Valid for pass 1 if there are filters or the container has stored
	 * essentia.
	 * Valid for pass 2 if no filters or stored essentia.
	 * 
	 * @return
	 */
	@Override
	public boolean validForPass( final int pass )
	{
		if( this.aspectContainer != null )
		{
			boolean hasFilters = !this.allowAny();
			boolean hasStored = !EssentiaTileContainerHelper.INSTANCE.getAspectStacksFromContainer( this.aspectContainer ).isEmpty();

			// Is this the priority pass?
			if( pass == 1 )
			{
				// Valid if has filters or container has something in it
				return( hasFilters || hasStored );
			}

			// Valid if has no filters and container is empty.
			return( ( !hasFilters ) && ( !hasStored ) );

		}

		return false;
	}
}
