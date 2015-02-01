package thaumicenergistics.integration.tc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileCentrifuge;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumcraft.common.tiles.TileJarFillable;
import thaumcraft.common.tiles.TileTubeBuffer;
import thaumicenergistics.api.ITransportPermissions;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEFluidStack;

/**
 * Helper class for working with Thaumcraft TileEntity essentia containers.
 * 
 * @author Nividica
 * 
 */
public final class EssentiaTileContainerHelper
{
	/**
	 * Singleton
	 */
	public static final EssentiaTileContainerHelper instance = new EssentiaTileContainerHelper();

	/**
	 * Cache the permission class
	 */
	public final ITransportPermissions perms = ThEApi.instance().transportPermissions();

	/**
	 * Extracts essentia from a container based on the specified fluid stack
	 * type and amount.
	 * 
	 * @param container
	 * @param request
	 * @param mode
	 * @return
	 */
	public FluidStack extractFromContainer( final IAspectContainer container, final FluidStack request, final Actionable mode )
	{
		// Is the container whitelisted?
		if( !this.perms.canExtractFromAspectContainerTile( container ) )
		{
			// Not whitelsited
			return null;
		}

		// Do we have a request
		if( ( request == null ) || ( request.getFluid() == null ) || ( request.amount == 0 ) )
		{
			// No request
			return null;
		}

		Fluid fluid = request.getFluid();

		// Ensure it is a gas
		if( !( fluid instanceof GaseousEssentia ) )
		{
			// Not a gas
			return null;
		}

		// Get the gas aspect
		Aspect gasAspect = ( (GaseousEssentia)fluid ).getAspect();

		// Get the amount to extract
		long amountToDrain_EU = EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( request.amount );

		// Extract
		return this.extractFromContainer( container, (int)amountToDrain_EU, gasAspect, mode );
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
	public FluidStack extractFromContainer( final IAspectContainer container, int amountToDrain_EU, final Aspect aspectToDrain, final Actionable mode )
	{
		// Is the container whitelisted?
		if( !this.perms.canExtractFromAspectContainerTile( container ) )
		{
			// Not whitelisted
			return null;
		}

		// Is the request empty?
		if( amountToDrain_EU == 0 )
		{
			// Empty request
			return null;
		}

		// Get how much is in the container
		int containerAmount = 0;

		// Aspect match
		if( container instanceof TileEssentiaReservoir )
		{
			// Get the aspect amount from the reservoir
			containerAmount = container.getAspects().getAmount( aspectToDrain );
		}
		else
		{
			// Get what aspect and how much the container is holding
			AspectStack containerStack = this.getAspectStackFromContainer( container );

			// Is the container holding the correct aspect?
			if( ( containerStack == null ) || ( aspectToDrain != containerStack.aspect ) )
			{
				return null;
			}

			// Get the amount
			containerAmount = (int)containerStack.amount;
		}

		// Is there a fluid form of the essentia?
		GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( aspectToDrain );
		if( essentiaGas == null )
		{
			// Invalid aspect.
			return null;
		}

		// Is the container empty?
		if( containerAmount == 0 )
		{
			// Empty container
			return null;
		}

		// Is the drain for more than is in the container?
		else if( amountToDrain_EU > containerAmount )
		{
			amountToDrain_EU = containerAmount;
		}

		// Are we really draining, or just simulating?
		if( mode == Actionable.MODULATE )
		{
			container.takeFromContainer( aspectToDrain, amountToDrain_EU );
		}

		// Return the amount drained with conversion
		return new FluidStack( essentiaGas, (int)EssentiaConversionHelper.instance.convertEssentiaAmountToFluidAmount( amountToDrain_EU ) );

	}

	public Aspect getAspectInContainer( final IAspectContainer container )
	{
		// Get the aspect list from the container
		AspectStack containerStack = this.getAspectStackFromContainer( container );

		// Did we get a stack?
		if( containerStack == null )
		{
			return null;
		}

		return containerStack.aspect;
	}

	public AspectStack getAspectStackFromContainer( final IAspectContainer container )
	{
		// Ensure we have a container
		if( container == null )
		{
			return null;
		}

		// Get the list of aspects in the container
		AspectList aspectList = container.getAspects();

		if( aspectList == null )
		{
			return null;
		}

		// Create the stack
		AspectStack aspectStack = new AspectStack();

		// Set the aspect
		aspectStack.aspect = aspectList.getAspectsSortedAmount()[0];

		if( aspectStack.aspect == null )
		{
			return null;
		}

		// Set the amount
		aspectStack.amount = aspectList.getAmount( aspectStack.aspect );

		return aspectStack;
	}

	/**
	 * Gets the list of aspects in the container.
	 * 
	 * @param container
	 * @return
	 */
	public List<AspectStack> getAspectStacksFromContainer( final IAspectContainer container )
	{
		List<AspectStack> stacks = new ArrayList<AspectStack>();

		// Ensure we have a container
		if( container == null )
		{
			return stacks;
		}

		// Get the list of aspects in the container
		AspectList aspectList = container.getAspects();

		if( aspectList == null )
		{
			return stacks;
		}

		// Populate the list
		for( Entry<Aspect, Integer> essentia : aspectList.aspects.entrySet() )
		{
			if( ( essentia != null ) && ( essentia.getValue() != 0 ) )
			{
				stacks.add( new AspectStack( essentia.getKey(), essentia.getValue() ) );
			}
		}

		return stacks;

	}

	public int getContainerCapacity( final IAspectContainer container )
	{
		int capacity = 0;

		if( container instanceof TileJarFillable )
		{
			capacity = ( (TileJarFillable)container ).maxAmount;
		}
		else if( container instanceof TileAlembic )
		{
			capacity = ( (TileAlembic)container ).maxAmount;
		}
		else if( container instanceof TileEssentiaReservoir )
		{
			capacity = ( (TileEssentiaReservoir)container ).maxAmount;
		}

		return capacity;
	}

	public int getContainerStoredAmount( final IAspectContainer container )
	{
		int stored = 0;

		if( container instanceof TileJarFillable )
		{
			stored = ( (TileJarFillable)container ).amount;
		}
		else if( container instanceof TileAlembic )
		{
			stored = ( (TileAlembic)container ).amount;
		}
		else if( container instanceof TileEssentiaReservoir )
		{
			// Get the essentia list
			for( AspectStack essentia : this.getAspectStacksFromContainer( container ) )
			{
				if( essentia != null )
				{
					stored += (int)essentia.amount;
				}
			}
		}

		return stored;
	}

	public long injectIntoContainer( final IAspectContainer container, final IAEFluidStack fluidStack, final Actionable mode )
	{
		// Do we have an input?
		if( fluidStack == null )
		{
			// No input
			return 0;
		}

		// Is the container whitelisted?
		if( !this.perms.canInjectToAspectContainerTile( container ) )
		{
			// Not whitelisted
			return 0;
		}

		// Get the fluid.
		Fluid fluid = fluidStack.getFluid();

		// Ensure it is a gas
		if( !( fluid instanceof GaseousEssentia ) )
		{
			// Not essentia gas
			return 0;
		}

		// Get the aspect of the gas
		Aspect gasAspect = ( (GaseousEssentia)fluid ).getAspect();

		// Get the amount to fill
		long amountToFill = EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() );

		// Inject
		return this.injectIntoContainer( container, (int)amountToFill, gasAspect, mode );
	}

	public long injectIntoContainer( final IAspectContainer container, int amountToFillInEssentiaUnits, final Aspect aspectToFill,
										final Actionable mode )
	{
		// Is the container whitelisted?
		if( !this.perms.canInjectToAspectContainerTile( container ) )
		{
			// Not whitelisted
			return 0;
		}

		// Get the aspect in the container
		AspectStack storedEssentia = this.getAspectStackFromContainer( container );

		// Match types on jars
		if( ( storedEssentia != null ) && ( container instanceof TileJarFillable ) )
		{
			// Do the aspects match?
			if( aspectToFill != storedEssentia.aspect )
			{
				// Aspects do not match;
				return 0;
			}
		}
		else if( !( container.doesContainerAccept( aspectToFill ) ) )
		{
			// Container will not accept this aspect
			return 0;
		}

		// Get how much the container can hold
		int containerCurrentCapacity = this.getContainerCapacity( container ) - this.getContainerStoredAmount( container );

		// Is there more to fill than the container will hold?
		if( amountToFillInEssentiaUnits > containerCurrentCapacity )
		{
			amountToFillInEssentiaUnits = containerCurrentCapacity;
		}

		// Are we really filling, or simulating?
		if( mode == Actionable.MODULATE )
		{
			container.addToContainer( aspectToFill, amountToFillInEssentiaUnits );
		}

		// Convert to fluid units
		return EssentiaConversionHelper.instance.convertEssentiaAmountToFluidAmount( amountToFillInEssentiaUnits );
	}

	/**
	 * Setup the standard white list
	 */
	public void registerThaumcraftContainers()
	{
		// Alembic
		this.perms.addAspectContainerTileToExtractPermissions( TileAlembic.class );

		// Centrifuge
		this.perms.addAspectContainerTileToExtractPermissions( TileCentrifuge.class );

		// Jars
		this.perms.addAspectContainerTileToExtractPermissions( TileJarFillable.class );
		this.perms.addAspectContainerTileToInjectPermissions( TileJarFillable.class );

		// Essentia buffer
		this.perms.addAspectContainerTileToExtractPermissions( TileTubeBuffer.class );

		// TODO: Drop legacy support at version 1.0
		try
		{
			// Essentia reservoir
			Class<?> ter = Class.forName( "thaumcraft.common.tiles.TileEssentiaReservoir" );
			this.perms.addAspectContainerTileToExtractPermissions( (Class<? extends TileEntity>)ter );
			this.perms.addAspectContainerTileToInjectPermissions( (Class<? extends TileEntity>)ter );
		}
		catch( ClassNotFoundException e )
		{
		}
		try
		{
			// Advanced alchemical furnace
			Class<?> tafan = Class.forName( "thaumcraft.common.tiles.TileAlchemyFurnaceAdvancedNozzle" );
			this.perms.addAspectContainerTileToExtractPermissions( (Class<? extends TileEntity>)tafan );
		}
		catch( ClassNotFoundException e )
		{
		}
	}

}
