package thaumicenergistics.integration.tc;

import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import appeng.api.networking.IGrid;
import appeng.api.util.DimensionalCoord;

public interface IDigiVisSource
{
	/**
	 * Drains digivis from this source.
	 * 
	 * @param digiVisAspect
	 * @param amount
	 * @return
	 */
	public int consumeVis( Aspect digiVisAspect, int amount );

	/**
	 * Gets the side of cable the source part is attached to.
	 * If this returns UNKNOWN, it is assumed the source is a whole block.
	 * 
	 * @return
	 */
	public ForgeDirection getCableSide();

	/**
	 * Get's the AE grid the source is attached to.
	 * 
	 * @return
	 */
	public IGrid getGrid();

	/**
	 * Returns the location of the source.
	 * 
	 * @return
	 */
	public DimensionalCoord getLocation();

	/**
	 * Get's the unique identifier for this source.
	 * 
	 * @return
	 */
	public long getUID();

	/**
	 * Is the source active?
	 * 
	 * @return
	 */
	public boolean isActive();
}
