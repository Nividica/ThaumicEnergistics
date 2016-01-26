package thaumicenergistics.common.inventory;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.common.parts.PartEssentiaStorageBus;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.tile.misc.TileCondenser;
import com.google.common.collect.ImmutableList;

/**
 * Handles interaction between {@link PartEssentiaStorageBus} and {@link TileCondenser}.
 * 
 * @author Nividica
 * 
 */
public class HandlerEssentiaStorageBusCondenser
	extends HandlerEssentiaStorageBusBase
{

	private TileCondenser condenser = null;

	public HandlerEssentiaStorageBusCondenser( final PartEssentiaStorageBus part )
	{
		super( part );
	}

	@Override
	public boolean canAccept( final IAEFluidStack fluidStack )
	{
		// Ensure there is a condenser
		if( this.condenser == null )
		{
			return false;
		}

		// Ensure the bus has security access
		if( !this.hasSecurityPermission() )
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

		// Ensure we are allowed to transfer this fluid
		if( !this.canTransferGas( (GaseousEssentia)fluidStack.getFluid() ) )
		{
			/*
			 * Either: Not on whitelist or is on blacklist
			 */
			return false;
		}

		// Can accept the fluid.
		return true;
	}

	@Override
	public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable mode, final BaseActionSource source )
	{
		// Nothing comes out of the condenser.
		return null;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems( final IItemList<IAEFluidStack> out )
	{
		// Nothing is stored in the condenser.
		return out;
	}

	@Override
	public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable mode, final BaseActionSource source )
	{
		// Ensure input and output
		if( ( this.condenser == null ) || ( input == null ) || ( !this.canAccept( input ) ) )
		{
			return input;
		}

		// Ignore if simulation
		if( mode == Actionable.SIMULATE )
		{
			// Condenser can accept all.
			return null;
		}

		// Create the fluidstack
		IAEFluidStack injectStack = input.copy();

		// Set the amount to the Essentia units, NOT the fluid units
		injectStack.setStackSize( 500 * EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( input.getStackSize() ) );

		// Inject the fluid
		this.condenser.fill( this.partStorageBus.getSide().getOpposite(), injectStack.getFluidStack(), ( mode == Actionable.MODULATE ) );

		// Update the grid so that it doesn't thing we have stored the voided amount.
		this.postAlterationToHostGrid( ImmutableList.of( AEApi.instance().storage()
						.createFluidStack( new FluidStack( input.getFluid(), (int) -input.getStackSize() ) ) ) );

		// All fluid accepted.
		return null;
	}

	@Override
	public boolean onNeighborChange()
	{
		// Get the facing tile
		TileEntity te = this.getFaceingTile();

		// Is it a condenser?
		if( te instanceof TileCondenser )
		{
			this.condenser = (TileCondenser)te;
		}
		else
		{
			this.condenser = null;
		}

		// Nothing to update
		return false;
	}

	@Override
	public void tickingRequest( final IGridNode node, final int TicksSinceLastCall )
	{
		// Ignored.
	}

	@Override
	public boolean validForPass( final int pass )
	{
		return true;
	}

}
