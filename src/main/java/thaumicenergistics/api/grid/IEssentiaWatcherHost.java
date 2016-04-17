package thaumicenergistics.api.grid;

import thaumcraft.api.aspects.Aspect;

/**
 * A host that wants to be informed of essentia events.
 *
 * @author Nividica
 *
 */
public interface IEssentiaWatcherHost
{
	/**
	 * Called when essentia levels change in the network.
	 *
	 * @param aspect
	 * Aspect of the changed essentia.
	 * @param storedAmount
	 * The full amount stored in the network.
	 * @param changeAmount
	 * The amount that just changed.
	 */
	void onEssentiaChange( Aspect aspect, long storedAmount, long changeAmount );

	/**
	 * Providers the essentia watcher for your host.
	 *
	 * @param newWatcher
	 */
	void updateWatcher( IEssentiaWatcher newWatcher );
}
