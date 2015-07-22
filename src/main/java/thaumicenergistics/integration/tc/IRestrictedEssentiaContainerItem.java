package thaumicenergistics.integration.tc;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaContainerItem;

public interface IRestrictedEssentiaContainerItem
	extends IEssentiaContainerItem
{
	/**
	 * Returns true if the container accepts the specified aspect.
	 * 
	 * @param aspect
	 * @return
	 */
	public boolean acceptsAspect( Aspect aspect );
}
