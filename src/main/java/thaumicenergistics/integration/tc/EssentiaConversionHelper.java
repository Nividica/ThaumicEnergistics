package thaumicenergistics.integration.tc;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;

public final class EssentiaConversionHelper
{
	/**
	 * Controls the conversion ratio of essentia/fluid. <BR>
	 * 1 essentia unit is converted to this many mb's of fluid.
	 */
	public static int CONVERSION_MULTIPLIER = 250;

	/**
	 * Singleton
	 */
	public static final EssentiaConversionHelper instance = new EssentiaConversionHelper();

	/**
	 * Private constructor
	 */
	private EssentiaConversionHelper()
	{

	}

	/**
	 * Converts an AE fluid stack into an AspectStack.
	 * 
	 * @param fluidStack
	 * @return Aspect stack if converted, null otherwise.
	 */
	public AspectStack convertAEFluidStackToAspectStack( final IAEFluidStack fluidStack )
	{
		// Is the fluid an essentia gas?
		if( fluidStack.getFluid() instanceof GaseousEssentia )
		{
			// Create an aspect stack to match the fluid
			return new AspectStack( ( (GaseousEssentia)fluidStack.getFluid() ).getAssociatedAspect(),
							this.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() ) );
		}

		return null;
	}

	/**
	 * Converts an essentia amount into a fluid amount(mb).
	 * 
	 * @param essentiaAmount
	 * @return
	 */
	public long convertEssentiaAmountToFluidAmount( final long essentiaAmount )
	{
		return essentiaAmount * EssentiaConversionHelper.CONVERSION_MULTIPLIER;
	}

	/**
	 * Converts a fluid amount(mb) into an essentia amount.
	 * 
	 * @param fluidAmount
	 * @return
	 */
	public long convertFluidAmountToEssentiaAmount( final long fluidAmount )
	{
		return fluidAmount / EssentiaConversionHelper.CONVERSION_MULTIPLIER;
	}

	/**
	 * Converts an AE fluidstack list into a list of AspectStacks.
	 * 
	 * @param fluidStackList
	 * @return
	 */
	public List<AspectStack> convertIIAEFluidStackListToAspectStackList( final IItemList<IAEFluidStack> fluidStackList )
	{
		List<AspectStack> aspectStackList = new ArrayList<AspectStack>();

		if( fluidStackList != null )
		{
			for( IAEFluidStack fluidStack : fluidStackList )
			{
				// Convert
				AspectStack aspectStack = this.convertAEFluidStackToAspectStack( fluidStack );

				// Was the fluid an essentia gas?
				if( aspectStack != null )
				{
					// Add to the stack
					aspectStackList.add( aspectStack );
				}

			}
		}

		return aspectStackList;
	}

	/**
	 * Creates an AE fluidstack from the aspects in the specified contianer.
	 * 
	 * @param container
	 * @return Fluidstack if valid, null otherwise.
	 */
	public IAEFluidStack createAEFluidStackFromItemEssentiaContainer( final ItemStack container )
	{
		// Do we have an item?
		if( container == null )
		{
			return null;
		}

		// Is the item a container?
		if( !EssentiaItemContainerHelper.instance.isContainer( container ) )
		{
			return null;
		}

		// What aspect is in it?
		Aspect containerAspect = EssentiaItemContainerHelper.instance.getAspectInContainer( container );

		// Is there an aspect in it?
		if( containerAspect == null )
		{
			return null;
		}

		// Convert to gas
		GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( containerAspect );

		// Is there a fluid form of the aspect?
		if( essentiaGas == null )
		{
			return null;
		}

		// Get how much is in the container
		long containerAmount_EU = EssentiaItemContainerHelper.instance.getContainerStoredAmount( container );

		// Create and return the stack
		return this.createAEFluidStackInEssentiaUnits( essentiaGas, containerAmount_EU );

	}

	/**
	 * Creates an AE fluid stack from the specified essentia gas. This will
	 * convert the specified amount from essentia units to fluid units(mb).
	 * 
	 * @param essentiaGas
	 * @param essentiaAmount
	 * @return
	 */
	public IAEFluidStack createAEFluidStackInEssentiaUnits( final GaseousEssentia essentiaGas, final long essentiaAmount )
	{
		return this.createAEFluidStackInFluidUnits( essentiaGas, this.convertEssentiaAmountToFluidAmount( essentiaAmount ) );
	}

	/**
	 * Creates an AE fluid stack from the specified essentia gas with the amount
	 * specified.
	 * 
	 * @param essentiaGas
	 * @param fluidAmount
	 * @return
	 */
	public IAEFluidStack createAEFluidStackInFluidUnits( final GaseousEssentia essentiaGas, final long fluidAmount )
	{
		IAEFluidStack ret = AEApi.instance().storage().createFluidStack( new FluidStack( essentiaGas, 1 ) );

		ret.setStackSize( fluidAmount );

		return ret;
	}

}
