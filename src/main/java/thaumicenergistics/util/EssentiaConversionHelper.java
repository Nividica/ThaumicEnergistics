package thaumicenergistics.util;

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
	private static final int CONVERSION_MULTIPLIER = 250;

	public static long convertEssentiaAmountToFluidAmount( long essentiaAmount )
	{
		return essentiaAmount * EssentiaConversionHelper.CONVERSION_MULTIPLIER;
	}

	public static long convertFluidAmountToEssentiaAmount( long fluidAmount )
	{
		return fluidAmount / EssentiaConversionHelper.CONVERSION_MULTIPLIER;
	}

	public static IAEFluidStack createAEFluidStackFromItemEssentiaContainer( ItemStack container )
	{
		// Do we have an item?
		if ( container == null )
		{
			return null;
		}

		// Is the item a container?
		if ( !EssentiaItemContainerHelper.isContainer( container ) )
		{
			return null;
		}

		// What aspect is in it?
		Aspect containerAspect = EssentiaItemContainerHelper.getAspectInContainer( container );

		// Is there an aspect in it?
		if ( containerAspect == null )
		{
			return null;
		}

		// Convert to gas
		GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( containerAspect );

		// Get how much is in the container
		int containerAmount_EU = EssentiaItemContainerHelper.getContainerStoredAmount( container );

		// Create and return the stack
		return EssentiaConversionHelper.createAEFluidStackInEssentiaUnits( essentiaGas, containerAmount_EU );

	}

	public static IAEFluidStack createAEFluidStackInEssentiaUnits( GaseousEssentia essentiaGas, int essentiaAmount )
	{
		return EssentiaConversionHelper.createAEFluidStackInFluidUnits( essentiaGas,
			(int) EssentiaConversionHelper.convertEssentiaAmountToFluidAmount( essentiaAmount ) );
	}

	public static IAEFluidStack createAEFluidStackInFluidUnits( GaseousEssentia essentiaGas, int fluidAmount )
	{
		return AEApi.instance().storage().createFluidStack( new FluidStack( essentiaGas, fluidAmount ) );
	}

	public static AspectStack convertAEFluidStackToAspectStack( IAEFluidStack fluidStack )
	{
		// Is the fluid an essentia gas?
		if ( fluidStack.getFluid() instanceof GaseousEssentia )
		{
			// Create an aspect stack to match the fluid
			return new AspectStack( ( (GaseousEssentia) fluidStack.getFluid() ).getAssociatedAspect(),
							EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() ) );
		}

		return null;
	}
	
	public static List<AspectStack> convertIIAEFluidStackListToAspectStackList( IItemList<IAEFluidStack> fluidStackList )
	{
		List<AspectStack> aspectStackList = new ArrayList<AspectStack>();

		if( fluidStackList != null )
		{
			for( IAEFluidStack fluidStack : fluidStackList )
			{
				// Convert
				AspectStack aspectStack = EssentiaConversionHelper.convertAEFluidStackToAspectStack( fluidStack );
	
				// Was the fluid an essentia gas?
				if ( aspectStack != null )
				{
					// Add to the stack
					aspectStackList.add( aspectStack );
				}
	
			}
		}

		return aspectStackList;
	}

}
