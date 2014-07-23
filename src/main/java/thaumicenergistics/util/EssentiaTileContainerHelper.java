package thaumicenergistics.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileCentrifuge;
import thaumcraft.common.tiles.TileJarFillable;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEFluidStack;

/**
 * Helper class for working with Thaumcraft TileEntity aspect containers.
 * 
 * @author Nividica
 * 
 */
public final class EssentiaTileContainerHelper
{
	/**
	 * Holds a list of tiles that we are allowed to extract from.
	 */
	private static final List<Class<? extends TileEntity>> extractWhiteList = new ArrayList<Class<? extends TileEntity>>();

	/**
	 * Holds a list of tiles that we are allowed to inject into.
	 */
	private static final List<Class<? extends TileEntity>> injectWhiteList = new ArrayList<Class<? extends TileEntity>>();

	/**
	 * Setup the standard white lists
	 */
	static
	{
		// Alembic
		EssentiaTileContainerHelper.addTileToExtractWhitelist( TileAlembic.class );

		// Centrifuge
		EssentiaTileContainerHelper.addTileToExtractWhitelist( TileCentrifuge.class );

		// Jars
		EssentiaTileContainerHelper.addTileToExtractWhitelist( TileJarFillable.class );
		EssentiaTileContainerHelper.addTileToInjectWhitelist( TileJarFillable.class );
	}

	/**
	 * Adds a tile entity to the extract whitelist.
	 * The tile must implement the interface {@link IAspectContainer}
	 * 
	 * @param tile
	 * @return True if added to the list, False if not.
	 */
	public static boolean addTileToExtractWhitelist( Class<? extends TileEntity> tile )
	{
		// Ensure it is a container
		if ( IAspectContainer.class.isAssignableFrom( tile ) )
		{
			// Is it already registered?
			if ( !EssentiaTileContainerHelper.extractWhiteList.contains( tile ) )
			{
				// Add to the list
				EssentiaTileContainerHelper.extractWhiteList.add( tile );
			}

			return true;
		}

		return false;
	}

	/**
	 * Adds a tile entity to the inject whitelist.
	 * The tile must implement the interface {@link IAspectContainer}
	 * 
	 * @param tile
	 * @return True if added to the list, False if not.
	 */
	public static boolean addTileToInjectWhitelist( Class<? extends TileEntity> tile )
	{
		// Ensure it is a container
		if ( IAspectContainer.class.isAssignableFrom( tile ) )
		{
			// Is it already registered?
			if ( !EssentiaTileContainerHelper.injectWhiteList.contains( tile ) )
			{
				// Add to the list
				EssentiaTileContainerHelper.injectWhiteList.add( tile );
			}

			return true;
		}

		return false;
	}

	/**
	 * Checks if the container can be extracted from
	 * 
	 * @param container
	 * @return
	 */
	public static boolean canExtract( IAspectContainer container )
	{
		// Loop over blacklist
		for( Class<? extends TileEntity> whiteClass : EssentiaTileContainerHelper.extractWhiteList )
		{
			// Is the container an instance of this whitelisted class?
			if ( whiteClass.isInstance( container ) )
			{
				// Return that we can extract
				return true;
			}
		}

		// Return that we can not extract
		return false;
	}

	/**
	 * Checks if the container can be injected into
	 * 
	 * @param container
	 * @return
	 */
	public static boolean canInject( IAspectContainer container )
	{
		// Loop over blacklist
		for( Class<? extends TileEntity> whiteClass : EssentiaTileContainerHelper.injectWhiteList )
		{
			// Is the container an instance of this whitelisted class?
			if ( whiteClass.isInstance( container ) )
			{
				// Return that we can inject
				return true;
			}
		}

		// Return that we can not inject
		return false;
	}

	/**
	 * Extracts essentia from a container based on the specified fluid stack
	 * type and amount.
	 * 
	 * @param container
	 * @param fluidStack
	 * @param mode
	 * @return
	 */
	public static FluidStack extractFromContainer( IAspectContainer container, FluidStack fluidStack, Actionable mode )
	{
		// Can we extract?
		if ( !EssentiaTileContainerHelper.canExtract( container ) )
		{
			return null;
		}

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
				Aspect gasAspect = ( (GaseousEssentia)fluid ).getAssociatedAspect();

				long amountToDrain_EU = EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( fluidStack.amount );

				return EssentiaTileContainerHelper.extractFromContainer( container, (int)amountToDrain_EU, gasAspect, mode );
			}
		}

		return null;
	}

	/**
	 * Extracts essentia from a container based on the specified aspect and
	 * amount.
	 * 
	 * @param container
	 * @param amountToDrain_EU
	 * @param aspectToDrain
	 * @param mode
	 * @return
	 */
	public static FluidStack extractFromContainer( IAspectContainer container, int amountToDrain_EU, Aspect aspectToDrain, Actionable mode )
	{
		// Can we extract?
		if ( !EssentiaTileContainerHelper.canExtract( container ) )
		{
			return null;
		}

		// Get what aspect and how much the container is holding
		AspectStack containerStack = EssentiaTileContainerHelper.getAspectStackFromContainer( container );

		// Is the container holding the correct aspect?
		if ( ( containerStack == null ) || ( aspectToDrain != containerStack.aspect ) )
		{
			return null;
		}

		// Get how much is in the container
		int containerAmount = (int)containerStack.amount;

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
		if ( mode == Actionable.MODULATE )
		{
			container.takeFromContainer( aspectToDrain, amountToDrain_EU );
		}

		// Return the amount drained with conversion
		return new FluidStack( GaseousEssentia.getGasFromAspect( aspectToDrain ),
						(int)EssentiaConversionHelper.convertEssentiaAmountToFluidAmount( amountToDrain_EU ) );

	}

	public static long injectIntoContainer( IAspectContainer container, IAEFluidStack fluidStack, Actionable mode )
	{
		// Can we inject?
		if ( !EssentiaTileContainerHelper.canInject( container ) )
		{
			return 0;
		}

		if ( fluidStack == null )
		{
			return 0;
		}

		Fluid fluid = fluidStack.getFluid();

		if ( fluid instanceof GaseousEssentia )
		{

			Aspect gasAspect = ( (GaseousEssentia)fluid ).getAssociatedAspect();

			long amountToFill = EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() );

			return EssentiaTileContainerHelper.injectIntoContainer( container, (int)amountToFill, gasAspect, mode );
		}

		return 0;
	}

	public static long injectIntoContainer( IAspectContainer container, int amountToFillInEssentiaUnits, Aspect aspectToFill, Actionable mode )
	{
		// Can we inject?
		if ( !EssentiaTileContainerHelper.canInject( container ) )
		{
			return 0;
		}

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
			containerAmount = (int)containerStack.amount;

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
		if ( mode == Actionable.MODULATE )
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
			return ( (TileJarFillable)container ).maxAmount;
		}
		else if ( container instanceof TileAlembic )
		{
			return ( (TileAlembic)container ).maxAmount;
		}

		return 0;
	}

	public static int getContainerStoredAmount( IAspectContainer container )
	{
		if ( container instanceof TileJarFillable )
		{
			return ( (TileJarFillable)container ).amount;
		}
		else if ( container instanceof TileAlembic )
		{
			return ( (TileAlembic)container ).amount;
		}

		return 0;
	}

}
