package thaumicenergistics.inventory;

import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.IAspectContainer;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.tile.misc.TileInterface;

/**
 * Pass-though handler for containers and interfaces.
 * 
 * @author Nividica
 * 
 */
public class HandlerEssentiaStorageBusDuality
	extends AbstractHandlerEssentiaStorageBus
{
	/**
	 * The handler that is actually used, can be null.
	 */
	private AbstractHandlerEssentiaStorageBus internalHandler;

	/**
	 * Handler used when facing an aspect container.
	 */
	private HandlerEssentiaStorageBusContainer containerHandler;

	/**
	 * Handler used when facing an interface.
	 */
	private HandlerEssentiaStorageBusInterface interfaceHandler;

	/**
	 * Creates the handler.
	 * 
	 * @param part
	 */
	public HandlerEssentiaStorageBusDuality( final AEPartEssentiaStorageBus part )
	{
		super( part );
	}

	/**
	 * Updates the internal handler to match the duality handler.
	 */
	private void updateInternalHandler()
	{
		// Ensure there is an internal handler?
		if( this.internalHandler != null )
		{
			// Set the filtered aspects
			this.internalHandler.filteredAspects = this.filteredAspects;

			// Set inverted
			this.internalHandler.inverted = this.inverted;

			// Set void
			this.internalHandler.setVoidAllowed( this.isVoidAllowed() );
		}
	}

	@Override
	public boolean canAccept( final IAEFluidStack fluidStack )
	{
		// Ensure we have an internal handler
		if( this.internalHandler != null )
		{
			// Pass to handler
			return this.internalHandler.canAccept( fluidStack );
		}

		// No handler
		return false;
	}

	@Override
	public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable mode, final BaseActionSource source )
	{
		// Ensure we have an internal handler
		if( this.internalHandler != null )
		{
			// Pass to handler
			return this.internalHandler.extractItems( request, mode, source );
		}

		// No handler
		return null;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems( final IItemList<IAEFluidStack> out )
	{
		// Ensure we have an internal handler
		if( this.internalHandler != null )
		{
			// Pass to handler
			return this.internalHandler.getAvailableItems( out );
		}

		// No handler
		return out;
	}

	@Override
	public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable mode, final BaseActionSource source )
	{
		// Ensure we have an internal handler
		if( this.internalHandler != null )
		{
			// Pass to handler
			return this.internalHandler.injectItems( input, mode, source );
		}

		// No handler
		return input;
	}

	@Override
	public boolean onNeighborChange()
	{
		boolean doUpdate = false;

		// What is the storage bus facing?
		TileEntity tileEntity = this.getFaceingTile();

		AbstractHandlerEssentiaStorageBus newHandler = null;

		if( tileEntity instanceof IAspectContainer )
		{
			// Create the container handler if needed
			if( this.containerHandler == null )
			{
				// Create the handler
				this.containerHandler = new HandlerEssentiaStorageBusContainer( this.partStorageBus );
			}

			// Set internal handler to the container handler
			newHandler = this.containerHandler;
		}
		else if( tileEntity instanceof TileInterface )
		{
			// Create the interface handler if needed
			if( this.interfaceHandler == null )
			{
				// Create the handler
				newHandler = new HandlerEssentiaStorageBusInterface( this.partStorageBus );
			}

			// Set the internal handler to the interface handler
			this.internalHandler = this.interfaceHandler;

		}

		// Has the handler changed?
		if( ( this.internalHandler != null ) && ( this.internalHandler != newHandler ) )
		{
			// Let the old handler know the neighbor changed
			doUpdate |= this.internalHandler.onNeighborChange();
		}

		// Set the handler
		this.internalHandler = newHandler;

		// Pass to handler if has one
		if( this.internalHandler != null )
		{
			// Update it
			this.updateInternalHandler();

			// Pass to the handler
			doUpdate |= this.internalHandler.onNeighborChange();
		}

		return doUpdate;

	}

	@Override
	public void setInverted( final boolean isInverted )
	{
		this.inverted = isInverted;

		if( this.internalHandler != null )
		{
			this.internalHandler.setInverted( isInverted );
		}
	}

	@Override
	public void setVoidAllowed( final boolean isVoidAllowed )
	{
		// Call super
		super.setVoidAllowed( isVoidAllowed );

		// Call handler
		if( this.internalHandler != null )
		{
			this.internalHandler.setVoidAllowed( isVoidAllowed );
		}
	}

	@Override
	public void tickingRequest( final IGridNode node, final int TicksSinceLastCall )
	{
		// Ensure we have an internal handler
		if( this.internalHandler != null )
		{
			// Pass to handler
			this.internalHandler.tickingRequest( node, TicksSinceLastCall );
		}
	}

	@Override
	public boolean validForPass( final int pass )
	{
		// Ensure we have an internal handler
		if( this.internalHandler != null )
		{
			// Pass to handler
			return this.internalHandler.validForPass( pass );
		}

		// No handler
		return false;
	}
}
