package thaumicenergistics.api.networking;

import java.util.Collection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.storage.IAspectStack;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;

public interface IMEEssentiaMonitor
{
	/**
	 * Adds a listener to the essentia grid.
	 * 
	 * @param listener
	 * @param verificationToken
	 */
	public void addListener( IMEEssentiaMonitorReceiver listener, Object verificationToken );

	/**
	 * Extract the specified essentia from the network.<br>
	 * 
	 * @param aspect
	 * @param amount
	 * @param mode
	 * @param source
	 * @param powered
	 * If true will take the required power for the extraction, respecting the mode setting.
	 * @return The amount extracted.
	 */
	public long extractEssentia( Aspect aspect, long amount, Actionable mode, BaseActionSource source, boolean powered );

	/**
	 * Returns the how much of the specified essentia is in the network.<br>
	 * If the aspect is null, returns 0.
	 * 
	 * @param aspect
	 * @return
	 */
	public long getEssentiaAmount( final Aspect aspect );

	/**
	 * Gets the list of essentia in the network.
	 * 
	 * @return
	 */
	public Collection<IAspectStack> getEssentiaList();

	/**
	 * Inject the specified essentia into the network.<br>
	 * 
	 * @param aspect
	 * @param amount
	 * @param mode
	 * @param source
	 * @param powered
	 * If true will take the required power for the injection, respecting the mode setting.
	 * @return The amount that could <strong>not</strong> be injected.
	 */
	public long injectEssentia( Aspect aspect, long amount, Actionable mode, BaseActionSource source, boolean powered );

	/**
	 * Removes a listener from the essentia grid.
	 * 
	 * @param listener
	 */
	public void removeListener( IMEEssentiaMonitorReceiver listener );
}
