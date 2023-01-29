package thaumicenergistics.api.grid;

import java.util.Collection;

import thaumcraft.api.aspects.Aspect;

/**
 * Watches the essentia grid for changes, informs the host when changes occur.
 *
 * @author Nividica
 *
 */
public interface IEssentiaWatcher extends Collection<Aspect> {

    /**
     * Return the host for this watcher.
     *
     * @return
     */
    IEssentiaWatcherHost getHost();
}
