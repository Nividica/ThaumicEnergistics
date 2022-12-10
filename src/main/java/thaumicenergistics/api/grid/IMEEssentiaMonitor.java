package thaumicenergistics.api.grid;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import java.util.Collection;
import javax.annotation.Nonnull;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.storage.IAspectStack;

/**
 * Provides access to essentia storage.
 *
 * @author Nividica
 *
 */
public interface IMEEssentiaMonitor {
    /**
     * Adds a listener to the essentia grid.
     *
     * @param listener
     * @param verificationToken
     * Token used to verify the receiver is still valid, and wants to continue receiving events.
     */
    void addListener(@Nonnull IMEEssentiaMonitorReceiver listener, @Nonnull Object verificationToken);

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
    long extractEssentia(
            @Nonnull Aspect aspect,
            long amount,
            @Nonnull Actionable mode,
            @Nonnull BaseActionSource source,
            boolean powered);

    /**
     * Returns the how much of the specified essentia is in the network.
     *
     * @param aspect
     * @return
     */
    long getEssentiaAmount(@Nonnull Aspect aspect);

    /**
     * Gets the list of essentia in the network.
     *
     * @return
     */
    @Nonnull
    Collection<IAspectStack> getEssentiaList();

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
    long injectEssentia(
            @Nonnull Aspect aspect,
            long amount,
            @Nonnull Actionable mode,
            @Nonnull BaseActionSource source,
            boolean powered);

    /**
     * Removes a listener from the essentia grid.
     *
     * @param listener
     */
    public void removeListener(@Nonnull IMEEssentiaMonitorReceiver listener);
}
