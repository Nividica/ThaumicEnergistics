package thaumicenergistics.common.storage;

import java.util.Comparator;
import thaumicenergistics.api.storage.IAspectStack;

/**
 * Compares one aspect stack against another.
 *
 * @author Nividica
 *
 */
public class AspectStackComparator implements Comparator<IAspectStack> {

    /**
     * Modes of comparison
     *
     * @author Nividica
     *
     */
    public enum AspectStackComparatorMode {
        /**
         * Compare based on name
         */
        MODE_ALPHABETIC,

        /**
         * Compare based on amount
         */
        MODE_AMOUNT;

        /**
         * Cache of the enum values
         */
        public static final AspectStackComparatorMode[] VALUES = AspectStackComparatorMode.values();

        /**
         * Returns the next mode of comparison.
         *
         * @return
         */
        public AspectStackComparatorMode nextMode() {
            return (VALUES[(this.ordinal() + 1) % VALUES.length]);
        }

        /**
         * Returns the previous mode of comparison.
         *
         * @return
         */
        public AspectStackComparatorMode previousMode() {
            return (VALUES[(this.ordinal() + (VALUES.length - 1)) % VALUES.length]);
        }
    }

    /**
     * The set mode of comparison
     */
    private AspectStackComparatorMode mode;

    /**
     * Creates the comparator with sorting mode alphabetic.
     */
    public AspectStackComparator() {
        this.setMode(AspectStackComparatorMode.MODE_ALPHABETIC);
    }

    /**
     * Creates the comparator with specified sorting mode.
     * If the mode is unrecognized, the list will not be sorted.
     *
     * @param mode
     * Mode to sort by.
     */
    public AspectStackComparator(final AspectStackComparatorMode mode) {
        this.setMode(mode);
    }

    /**
     * Compares the two stacks by amount.
     *
     * @param left
     * @param right
     * @return
     */
    private int compareByAmount(final IAspectStack left, final IAspectStack right) {
        return (int) (right.getStackSize() - left.getStackSize());
    }

    /**
     * Compares the two stacks by name
     *
     * @param left
     * @param right
     * @return
     */
    private int compareByName(final IAspectStack left, final IAspectStack right) {
        return left.getAspectName().compareTo(right.getAspectName());
    }

    /**
     * Compares two aspect stacks by the selected mode.
     */
    @Override
    public int compare(final IAspectStack left, final IAspectStack right) {
        switch (this.mode) {
            case MODE_ALPHABETIC:
                // Compare tags
                return this.compareByName(left, right);

            case MODE_AMOUNT:
                // Compare amounts
                int comparedAmounts = this.compareByAmount(left, right);

                // Are the amounts equal?
                if (comparedAmounts == 0) {
                    // Compare tags
                    comparedAmounts = this.compareByName(left, right);
                }

                return comparedAmounts;
        }

        return 0;
    }

    /**
     * Sets the mode of comparison.
     *
     * @param mode
     */
    public void setMode(final AspectStackComparatorMode mode) {
        this.mode = mode;
    }
}
