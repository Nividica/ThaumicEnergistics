package thaumicenergistics.api.grid;

import thaumicenergistics.api.storage.IAspectStack;

public interface IMEEssentiaMonitorReceiver
{
	/**
	 * Verifies that the receiver is still valid.<br>
	 * If returning false the receiver will be removed from the monitor
	 * and the receiver should NOT call removeListener itself.
	 * 
	 * @param verificationToken
	 * @return False if the receiver should no longer get updates.
	 */
	boolean isValid( Object verificationToken );

	/**
	 * Called when a change to the stored essentia occurs.
	 * 
	 * @param fromMonitor
	 * @param changes
	 */
	void postChange( IMEEssentiaMonitor fromMonitor, Iterable<IAspectStack> changes );
}
