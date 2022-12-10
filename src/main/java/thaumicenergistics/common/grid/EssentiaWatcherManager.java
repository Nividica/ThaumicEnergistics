package thaumicenergistics.common.grid;

import appeng.api.networking.IGridNode;
import java.util.HashMap;
import java.util.HashSet;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.IEssentiaWatcher;
import thaumicenergistics.api.grid.IEssentiaWatcherHost;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.api.grid.IMEEssentiaMonitorReceiver;
import thaumicenergistics.api.storage.IAspectStack;

class EssentiaWatcherManager implements IMEEssentiaMonitorReceiver {
    /**
     * Maps Node->Watcher
     */
    private HashMap<IGridNode, IEssentiaWatcher> watchers = new HashMap<IGridNode, IEssentiaWatcher>();

    /**
     * Maps Aspect -> Watchers
     */
    private HashMap<Aspect, HashSet<IEssentiaWatcher>> watchedAspects =
            new HashMap<Aspect, HashSet<IEssentiaWatcher>>();

    /**
     * True when the manager is listening for changes.
     */
    private boolean isListeningForChanges = false;

    private final GridEssentiaCache gridCache;

    public EssentiaWatcherManager(final GridEssentiaCache gridCache) {
        this.gridCache = gridCache;
    }

    /**
     * Adds a watcher.
     *
     * @param node
     * @param watcher
     */
    public void addWatcher(final IGridNode node, final IEssentiaWatcher watcher) {
        // Add the watcher
        this.watchers.put(node, watcher);

        // Is the manager not listening for changes?
        if (!this.isListeningForChanges) {
            // Listen for changes
            this.gridCache.addListener(this, this.gridCache.internalGrid);
            this.isListeningForChanges = true;
        }
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return this.isListeningForChanges && (verificationToken == this.gridCache.internalGrid);
    }

    /**
     * Called by watchers when a new aspect is to be tracked.
     */
    public void onWatcherAddAspect(final IEssentiaWatcher watcher, final Aspect aspect) {
        HashSet<IEssentiaWatcher> aWatchers;

        // Does the set need to be created?
        if (!this.watchedAspects.containsKey(aspect)) {
            // Create the set
            aWatchers = new HashSet<IEssentiaWatcher>();
            this.watchedAspects.put(aspect, aWatchers);
        } else {
            // Get the set
            aWatchers = this.watchedAspects.get(aspect);
        }

        // Add the watcher
        aWatchers.add(watcher);
    }

    /**
     * Called by watchers just before they are cleared.
     *
     * @param watcher
     * @param previouslyTrackedAspects
     */
    public void onWatcherCleared(final IEssentiaWatcher watcher, final HashSet<Aspect> previouslyTrackedAspects) {
        for (Aspect aspect : previouslyTrackedAspects) {
            this.onWatcherRemoveAspect(watcher, aspect);
        }
    }

    /**
     * Called by watchers when an aspect is no longer to be tracked.
     *
     * @param watcher
     * @param aspect
     */
    public void onWatcherRemoveAspect(final IEssentiaWatcher watcher, final Aspect aspect) {
        // Get the set
        HashSet<IEssentiaWatcher> aWatchers = this.watchedAspects.get(aspect);
        if (aWatchers != null) {
            // Remove the watcher
            aWatchers.remove(watcher);

            // Is the set empty?
            if (aWatchers.isEmpty()) {
                // Remove the mapping
                this.watchedAspects.remove(aspect);
            }
        }
    }

    @Override
    public void postChange(final IMEEssentiaMonitor fromMonitor, final Iterable<IAspectStack> changes) {
        // Fast bail
        if (this.watchedAspects.isEmpty()) {
            return;
        }

        // Loop over all changes
        for (IAspectStack change : changes) {
            // Is the change being watched for?
            if (this.watchedAspects.containsKey(change.getAspect())) {
                // Get the set
                HashSet<IEssentiaWatcher> watcherSet = this.watchedAspects.get(change.getAspect());

                // Get the full amount in the system
                long fullAmount = this.gridCache.getEssentiaAmount(change.getAspect());

                // Update each watcher
                for (IEssentiaWatcher watcher : watcherSet) {
                    // Get the watchers host
                    IEssentiaWatcherHost host = watcher.getHost();

                    // Update the host
                    if (host != null) {
                        host.onEssentiaChange(change.getAspect(), fullAmount, change.getStackSize());
                    }
                }
            }
        }
    }

    /**
     * Removes a watcher.
     *
     * @param node
     */
    public void removeWatcher(final IGridNode node) {
        // Get the watcher
        IEssentiaWatcher watcher = this.watchers.get(node);
        if (watcher != null) {
            // Clear the watcher
            watcher.clear();

            // Remove the watcher
            this.watchers.remove(node);

            // Is the list empty?
            if (this.watchers.isEmpty()) {
                // Ensure the watched aspects is also empty
                this.watchedAspects.clear();

                // Stop listening
                this.gridCache.removeListener(this);
                this.isListeningForChanges = false;
            }
        }
    }
}
