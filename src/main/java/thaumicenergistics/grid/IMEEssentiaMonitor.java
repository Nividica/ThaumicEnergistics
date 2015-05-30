package thaumicenergistics.grid;

import java.util.Collection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridCache;
import appeng.api.networking.security.BaseActionSource;

public interface IMEEssentiaMonitor
	extends IGridCache
{
	/**
	 * Adds a listener to the aspect grid.
	 * 
	 * @param listener
	 * @param verificationToken
	 */
	public void addListener( IMEEssentiaMonitorReceiver listener, Object verificationToken );

	/**
	 * Extract the specified essentia from the network.<br>
	 * This method takes power into consideration.
	 * 
	 * @param aspect
	 * @param amount
	 * @param mode
	 * @param source
	 * @return The amount extracted.
	 */
	public long extractEssentia( Aspect aspect, long amount, Actionable mode, BaseActionSource source );

	/**
	 * Returns the how much of the specified essentia is in the network.<br>
	 * If the aspect is null, returns 0.
	 * 
	 * @param aspect
	 * @return
	 */
	public long getEssentiaAmount( final Aspect aspect );

	/**
	 * Gets the list of aspects in the network.
	 * 
	 * @return
	 */
	public Collection<AspectStack> getEssentiaList();

	/**
	 * Inject the specified essentia into the network.<br>
	 * This method takes power into consideration.
	 * 
	 * @param aspect
	 * @param amount
	 * @param mode
	 * @param source
	 * @return The amount that could <strong>not</strong> be injected.
	 */
	public long injectEssentia( Aspect aspect, long amount, Actionable mode, BaseActionSource source );

	/**
	 * Removes a listener from the aspect grid.
	 * 
	 * @param listener
	 */
	public void removeListener( IMEEssentiaMonitorReceiver listener );
}
