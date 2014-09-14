package thaumicenergistics.integration.tc;

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
import cpw.mods.fml.common.FMLCommonHandler;

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
	 * Holds a list of tiles that we are allowed to extract from.
	 */
	private final List<Class<? extends TileEntity>> extractWhiteList = new ArrayList<Class<? extends TileEntity>>();

	/**
	 * Holds a list of tiles that we are allowed to inject into.
	 */
	private final List<Class<? extends TileEntity>> injectWhiteList = new ArrayList<Class<? extends TileEntity>>();

	/**
	 * Adds a tile entity to the extract whitelist.
	 * The tile must implement the interface {@link IAspectContainer}
	 * 
	 * @param tile
	 * @return True if added to the list, False if not.
	 */
	public boolean addTileToExtractWhitelist( final Class<? extends TileEntity> tile )
	{
		// Ensure we have a tile
		if( tile != null )
		{
			// Ensure it is a container
			if( IAspectContainer.class.isAssignableFrom( tile ) )
			{
				// Is it already registered?
				if( !this.extractWhiteList.contains( tile ) )
				{
					// Add to the list
					this.extractWhiteList.add( tile );

					// Log the addition
					FMLCommonHandler.instance().getFMLLogger().info( "Adding " + tile.toString() + " to extraction whitelist." );
				}

				return true;
			}
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
	public boolean addTileToInjectWhitelist( final Class<? extends TileEntity> tile )
	{
		// Ensure we have a tile
		if( tile != null )
		{
			// Ensure it is a container
			if( IAspectContainer.class.isAssignableFrom( tile ) )
			{
				// Is it already registered?
				if( !this.injectWhiteList.contains( tile ) )
				{
					// Add to the list
					this.injectWhiteList.add( tile );

					// Log the addition
					FMLCommonHandler.instance().getFMLLogger().info( "Adding " + tile.toString() + " to injection whitelist." );
				}

				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the container can be extracted from
	 * 
	 * @param container
	 * @return
	 */
	public boolean canExtract( final IAspectContainer container )
	{
		// Loop over blacklist
		for( Class<? extends TileEntity> whiteClass : this.extractWhiteList )
		{
			// Is the container an instance of this whitelisted class?
			if( whiteClass.isInstance( container ) )
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
	public boolean canInject( final IAspectContainer container )
	{
		// Loop over blacklist
		for( Class<? extends TileEntity> whiteClass : this.injectWhiteList )
		{
			// Is the container an instance of this whitelisted class?
			if( whiteClass.isInstance( container ) )
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
	public FluidStack extractFromContainer( final IAspectContainer container, final FluidStack fluidStack, final Actionable mode )
	{
		// Can we extract?
		if( !this.canExtract( container ) )
		{
			return null;
		}

		if( fluidStack == null )
		{
			return null;
		}

		Fluid fluid = fluidStack.getFluid();

		if( fluid instanceof GaseousEssentia )
		{
			// Is the requested amount > 0?
			if( fluidStack.amount > 0 )
			{
				Aspect gasAspect = ( (GaseousEssentia)fluid ).getAssociatedAspect();

				long amountToDrain_EU = EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( fluidStack.amount );

				return this.extractFromContainer( container, (int)amountToDrain_EU, gasAspect, mode );
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
	public FluidStack extractFromContainer( final IAspectContainer container, int amountToDrain_EU, final Aspect aspectToDrain, final Actionable mode )
	{
		// Can we extract?
		if( !this.canExtract( container ) )
		{
			return null;
		}

		// Get what aspect and how much the container is holding
		AspectStack containerStack = this.getAspectStackFromContainer( container );

		// Is the container holding the correct aspect?
		if( ( containerStack == null ) || ( aspectToDrain != containerStack.aspect ) )
		{
			return null;
		}

		// Is there a fluid form of the essentia?
		GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( aspectToDrain );
		if( essentiaGas == null )
		{
			return null;
		}

		// Get how much is in the container
		int containerAmount = (int)containerStack.amount;

		// Is the container empty?
		if( containerAmount == 0 )
		{
			return null;
		}
		// Is the request empty?
		if( amountToDrain_EU == 0 )
		{
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
		aspectStack.aspect = aspectList.getAspects()[0];

		if( aspectStack.aspect == null )
		{
			return null;
		}

		// Set the amount
		aspectStack.amount = aspectList.getAmount( aspectStack.aspect );

		return aspectStack;
	}

	public int getContainerCapacity( final IAspectContainer container )
	{
		if( container instanceof TileJarFillable )
		{
			return ( (TileJarFillable)container ).maxAmount;
		}
		else if( container instanceof TileAlembic )
		{
			return ( (TileAlembic)container ).maxAmount;
		}

		return 0;
	}

	public int getContainerStoredAmount( final IAspectContainer container )
	{
		if( container instanceof TileJarFillable )
		{
			return ( (TileJarFillable)container ).amount;
		}
		else if( container instanceof TileAlembic )
		{
			return ( (TileAlembic)container ).amount;
		}

		return 0;
	}

	public long injectIntoContainer( final IAspectContainer container, final IAEFluidStack fluidStack, final Actionable mode )
	{
		// Can we inject?
		if( !this.canInject( container ) )
		{
			return 0;
		}

		if( fluidStack == null )
		{
			return 0;
		}

		Fluid fluid = fluidStack.getFluid();

		if( fluid instanceof GaseousEssentia )
		{

			Aspect gasAspect = ( (GaseousEssentia)fluid ).getAssociatedAspect();

			long amountToFill = EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() );

			return this.injectIntoContainer( container, (int)amountToFill, gasAspect, mode );
		}

		return 0;
	}

	public long injectIntoContainer( final IAspectContainer container, int amountToFillInEssentiaUnits, final Aspect aspectToFill,
										final Actionable mode )
	{
		// Can we inject?
		if( !this.canInject( container ) )
		{
			return 0;
		}

		int containerAmount = 0;

		// Get what aspect and how much the container is holding
		AspectStack containerStack = this.getAspectStackFromContainer( container );

		// Is there anything in the container?
		if( containerStack != null )
		{
			// Do the aspects match?
			if( aspectToFill != containerStack.aspect )
			{
				// Aspects do not match;
				return 0;
			}

			// Get how much is in the container
			containerAmount = (int)containerStack.amount;

		}
		else if( !( container.doesContainerAccept( aspectToFill ) ) )
		{
			// Container will not accept this aspect
			return 0;
		}

		// Get how much the container can hold
		int containerCurrentCapacity = this.getContainerCapacity( container ) - containerAmount;

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
	 * Setup the standard white lists
	 */
	public void registerThaumcraftContainers()
	{
		// Alembic
		this.addTileToExtractWhitelist( TileAlembic.class );

		// Centrifuge
		this.addTileToExtractWhitelist( TileCentrifuge.class );

		// Jars
		this.addTileToExtractWhitelist( TileJarFillable.class );
		this.addTileToInjectWhitelist( TileJarFillable.class );
	}

}
