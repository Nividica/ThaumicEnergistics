package thaumicenergistics.api.grid;

import javax.annotation.Nonnull;

import thaumicenergistics.api.storage.IAspectStack;

/**
 * Defines a class that will attach to a monitor to receive all essentia change events.
 *
 * @author Nividica
 *
 */
public interface IMEEssentiaMonitorReceiver {

    /**
     * Verifies that the receiver is still valid.<br>
     * If returning false the receiver will be removed from the monitor and the receiver should NOT call removeListener
     * itself.
     *
     * @param verificationToken
     * @return False if the receiver should no longer get updates.
     */
    boolean isValid(@Nonnull Object verificationToken);

    /**
     * Called when a change to the stored essentia occurs.
     *
     * @param fromMonitor
     * @param changes
     */
    void postChange(@Nonnull IMEEssentiaMonitor fromMonitor, @Nonnull Iterable<IAspectStack> changes);
}
