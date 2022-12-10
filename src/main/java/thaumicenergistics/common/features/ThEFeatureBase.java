package thaumicenergistics.common.features;

/**
 * Defines what a basic feature of ThE is.
 *
 * @author Nividica
 *
 */
public abstract class ThEFeatureBase {

    /**
     * Has the feature been called to register itself?
     */
    private boolean hasRegistered = false;

    /**
     * Is the feature available.
     */
    protected boolean available = false;

    /**
     * Creates the feature.
     *
     * @param initiallyAvailable
     */
    public ThEFeatureBase(final boolean initiallyAvailable) {
        this.available = initiallyAvailable;
    }

    /**
     * Registers any additional features of this feature.
     */
    protected abstract void registerAdditional();

    /**
     * Registers the features crafting recipes, if it has any.
     *
     * @param cdi
     */
    protected abstract void registerCrafting(final CommonDependantItems cdi);

    /**
     * Is the feature available?
     *
     * @return
     */
    public boolean isAvailable() {
        return this.available;
    }

    /**
     * Asks the feature to register itself.
     *
     * @param cdi
     */
    public final void registerFeature(final CommonDependantItems cdi) {
        // Is the feature enabled and has not yet been registered?
        if (this.isAvailable() && !this.hasRegistered) {
            // Register crafting recipe(s)
            this.registerCrafting(cdi);

            // Register anything else
            this.registerAdditional();

            // Mark that this feature has been registered
            this.hasRegistered = true;
        }
    }
}
