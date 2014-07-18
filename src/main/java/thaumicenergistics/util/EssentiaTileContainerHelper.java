package thaumicenergistics.util;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileJarFillable;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import appeng.api.storage.data.IAEFluidStack;


public final class EssentiaTileContainerHelper
{
	public static FluidStack drainContainer( IAspectContainer container, FluidStack fluidStack, boolean doDrain )
	{
		if ( fluidStack == null )
		{
			return null;
		}

		Fluid fluid = fluidStack.getFluid();

		if ( fluid instanceof GaseousEssentia )
		{
			// Is the requested amount > 0?
			if ( fluidStack.amount > 0 )
			{
				Aspect gasAspect = ( (GaseousEssentia) fluid ).getAssociatedAspect();

				long amountToDrain_EU = EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( fluidStack.amount );

				return EssentiaTileContainerHelper.drainContainer( container, (int) amountToDrain_EU, gasAspect, doDrain );
			}
		}

		return null;
	}

	public static FluidStack drainContainer( IAspectContainer container, int amountToDrain_EU, Aspect aspectToDrain, boolean doDrain )
	{
		// Get what aspect and how much the container is holding
		AspectStack containerStack = EssentiaTileContainerHelper.getAspectStackFromContainer( container );

		// Is the container holding the correct aspect?
		if ( ( containerStack == null ) || ( aspectToDrain != containerStack.aspect ) )
		{
			return null;
		}

		// Get how much is in the container
		int containerAmount = (int) containerStack.amount;

		// Is the container empty?
		if ( containerAmount == 0 )
		{
			return null;
		}
		// Is the request empty?
		if ( amountToDrain_EU == 0 )
		{
			return null;
		}
		// Is the drain for more than is in the container?
		else if ( amountToDrain_EU > containerAmount )
		{
			amountToDrain_EU = containerAmount;
		}

		// Are we really draining, or just simulating?
		if ( doDrain )
		{
			container.takeFromContainer( aspectToDrain, amountToDrain_EU );
		}

		// Return the amount drained with conversion
		return new FluidStack( GaseousEssentia.getGasFromAspect( aspectToDrain ),
						(int) EssentiaConversionHelper.convertEssentiaAmountToFluidAmount( amountToDrain_EU ) );

	}

	public static long fillContainer( IAspectContainer container, IAEFluidStack fluidStack, boolean doFill )
	{
		if ( fluidStack == null )
		{
			return 0;
		}

		Fluid fluid = fluidStack.getFluid();

		if ( fluid instanceof GaseousEssentia )
		{

			Aspect gasAspect = ( (GaseousEssentia) fluid ).getAssociatedAspect();

			long amountToFill = EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() );

			return EssentiaTileContainerHelper.fillContainer( container, (int) amountToFill, gasAspect, doFill );
		}

		return 0;
	}

	public static long fillContainer( IAspectContainer container, int amountToFillInEssentiaUnits, Aspect aspectToFill, boolean doFill )
	{
		int containerAmount = 0;

		// Get what aspect and how much the container is holding
		AspectStack containerStack = EssentiaTileContainerHelper.getAspectStackFromContainer( container );

		// Is there anything in the container?
		if ( containerStack != null )
		{
			// Do the aspects match?
			if ( aspectToFill != containerStack.aspect )
			{
				// Aspects do not match;
				return 0;
			}

			// Get how much is in the container
			containerAmount = (int) containerStack.amount;

		}
		else if ( !( container.doesContainerAccept( aspectToFill ) ) )
		{
			// Container will not accept this aspect
			return 0;
		}

		// Get how much the container can hold
		int containerCurrentCapacity = EssentiaTileContainerHelper.getContainerCapacity( container ) - containerAmount;

		// Is there more to fill than the container will hold?
		if ( amountToFillInEssentiaUnits > containerCurrentCapacity )
		{
			amountToFillInEssentiaUnits = containerCurrentCapacity;
		}

		// Are we really filling, or simulating?
		if ( doFill )
		{
			container.addToContainer( aspectToFill, amountToFillInEssentiaUnits );
		}

		// Convert to fluid units
		return EssentiaConversionHelper.convertEssentiaAmountToFluidAmount( amountToFillInEssentiaUnits );
	}

	public static AspectStack getAspectStackFromContainer( IAspectContainer container )
	{
		// Get the list of aspects in the container
		AspectList aspectList = container.getAspects();

		if ( aspectList == null )
		{
			return null;
		}

		// Create the stack
		AspectStack aspectStack = new AspectStack();

		// Set the aspect
		aspectStack.aspect = aspectList.getAspects()[0];

		if ( aspectStack.aspect == null )
		{
			return null;
		}

		// Set the amount
		aspectStack.amount = aspectList.getAmount( aspectStack.aspect );

		return aspectStack;
	}

	public static int getContainerCapacity( IAspectContainer container )
	{
		if ( container instanceof TileJarFillable )
		{
			return ( (TileJarFillable) container ).maxAmount;
		}
		else if ( container instanceof TileAlembic )
		{
			return ( (TileAlembic) container ).maxAmount;
		}

		return 0;
	}

	public static int getContainerStoredAmount( IAspectContainer container )
	{
		if ( container instanceof TileJarFillable )
		{
			return ( (TileJarFillable) container ).amount;
		}
		else if ( container instanceof TileAlembic )
		{
			return ( (TileAlembic) container ).amount;
		}

		return 0;
	}

}
