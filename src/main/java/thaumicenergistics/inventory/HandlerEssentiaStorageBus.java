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
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;

public class HandlerEssentiaStorageBus
	implements IMEInventoryHandler<IAEFluidStack>
{
	private AEPartEssentiaStorageBus node;
	private IAspectContainer aspectContainer;
	private AccessRestriction access;
	private List<Aspect> prioritizedAspects = new ArrayList();
	private boolean inverted;

	public HandlerEssentiaStorageBus( AEPartEssentiaStorageBus node )
	{
		this.node = node;
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

		// Drain the container
		FluidStack drained = EssentiaTileContainerHelper.extractFromContainer( this.aspectContainer, toDrain, mode );

		// Was any drained?
		if ( ( drained == null ) || ( drained.amount == 0 ) )
		{
			return null;
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
		return this.node.getPriority();
	}

	@Override
	public int getSlot()
	{
		return 0;
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

		// Fill the container
		int filled = (int)EssentiaTileContainerHelper.injectIntoContainer( this.aspectContainer, input, mode );

		// Was any filled?
		if ( filled == 0 )
		{
			return input;
		}

		// Did we completely drain the input stack?
		if ( filled == toFill.amount )
		{
			// Nothing left over
			return null;
		}

		// Calculate how much was left over and return it
		return AEApi.instance().storage().createFluidStack( new FluidStack( toFill.getFluid(), toFill.amount - filled ) );
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
		TileEntity hostTile = this.node.getHostTile();

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
		ForgeDirection orientation = this.node.getSide();

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
