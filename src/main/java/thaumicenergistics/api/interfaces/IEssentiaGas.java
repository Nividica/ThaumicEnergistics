package thaumicenergistics.api.interfaces;

import net.minecraftforge.fluids.Fluid;
import thaumcraft.api.aspects.Aspect;

public interface IEssentiaGas
{
	/**
	 * Get the aspect this gas is based off of.
	 * 
	 * @return
	 */
	public Aspect getAspect();

	/**
	 * Gets the fluid form of the gas.
	 * 
	 * @return
	 */
	public Fluid getFluid();
}
