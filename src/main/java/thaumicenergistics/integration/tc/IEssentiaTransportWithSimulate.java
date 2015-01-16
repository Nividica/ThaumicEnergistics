package thaumicenergistics.integration.tc;

import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import appeng.api.config.Actionable;

public interface IEssentiaTransportWithSimulate
	extends IEssentiaTransport
{
	/**
	 * Adds essentia to the transport.
	 * 
	 * @param aspect
	 * @param amount
	 * @param side
	 * @param mode
	 * @return
	 */
	public int addEssentia( final Aspect aspect, final int amount, final ForgeDirection side, Actionable mode );
}
