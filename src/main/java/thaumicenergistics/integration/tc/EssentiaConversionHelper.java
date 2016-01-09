package thaumicenergistics.integration.tc;

import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.storage.AspectStack;
import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;

/**
 * Aids in converting essentia to and from a fluid.
 * 
 * @author Nividica
 * 
 */
public final class EssentiaConversionHelper
{
	/**
	 * Singleton
	 */
	public static final EssentiaConversionHelper INSTANCE = new EssentiaConversionHelper();

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
			return new AspectStack( ( (GaseousEssentia)fluidStack.getFluid() ).getAspect(), this.convertFluidAmountToEssentiaAmount( fluidStack
							.getStackSize() ) );
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
		return essentiaAmount * ThaumicEnergistics.config.conversionMultiplier();
	}

	/**
	 * Converts a fluid amount(mb) into an essentia amount.
	 * 
	 * @param fluidAmount
	 * @return
	 */
	public long convertFluidAmountToEssentiaAmount( final long fluidAmount )
	{
		return fluidAmount / ThaumicEnergistics.config.conversionMultiplier();
	}

	/**
	 * Creates an AE fluid stack from the specified essentia gas. This will
	 * convert the specified amount from essentia units to fluid units(mb).
	 * 
	 * @param Aspect
	 * @param essentiaAmount
	 * @return
	 */
	public IAEFluidStack createAEFluidStackInEssentiaUnits( final Aspect aspect, final long essentiaAmount )
	{
		GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( aspect );

		if( essentiaGas == null )
		{
			return null;
		}

		return this.createAEFluidStackInFluidUnits( essentiaGas, this.convertEssentiaAmountToFluidAmount( essentiaAmount ) );
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
		IAEFluidStack ret = null;
		try
		{
			ret = AEApi.instance().storage().createFluidStack( new FluidStack( essentiaGas, 1 ) );

			ret.setStackSize( fluidAmount );
		}
		catch( Exception e )
		{
		}

		return ret;
	}
}
