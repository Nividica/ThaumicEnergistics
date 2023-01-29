package thaumicenergistics.api.storage;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import thaumcraft.api.aspects.Aspect;

/**
 * Repository, or collection, of essentia.
 *
 * @author Nividica
 *
 */
public interface IEssentiaRepo {

    /**
     * Returns a set of the stored aspects.
     *
     * @return
     */
    @Nonnull
    Set<Aspect> aspectSet();

    /**
     * Removes all entries in the repo.
     */
    void clear();

    /**
     * Returns true if the repo has the aspect stored.
     *
     * @param aspect
     * @return
     */
    boolean containsAspect(@Nonnull Aspect aspect);

    /**
     * Sets the repo to match the specified collection. Any existing data is removed.
     *
     * @param stacks
     */
    void copyFrom(@Nonnull Collection<IAspectStack> stacks);

    /**
     * Gets the aspect stack associated with the aspect or null.
     */
    IAspectStack get(@Nonnull Aspect aspect);

    /**
     * Returns all aspect information stored in the repo.
     *
     * @return
     */
    @Nonnull
    Collection<IAspectStack> getAll();

    /**
     * Gets the aspect stack associated with the aspect or specified the default value.
     */
    IAspectStack getOrDefault(@Nonnull Aspect aspect, @Nullable IAspectStack defaultValue);

    /**
     * Returns true if the repo is empty.
     *
     * @return
     */
    boolean isEmpty();

    /**
     * Changes the aspect in the repo by the specified values.
     *
     * @param aspect
     * @param change
     * @param isCraftable If this value is null and there is a stored value, its crafting status will remain the same.
     *                    Otherwise it is set to false.
     * @return The previous stack, if there was one.
     */
    @Nullable
    IAspectStack postChange(@Nonnull Aspect aspect, long change, Boolean isCraftable);

    /**
     * Changes the aspect in the repo by the specified aspect stack.
     *
     * @param change
     * @return The previous stack, if there was one.
     */
    @Nullable
    IAspectStack postChange(@Nonnull IAspectStack change);

    /**
     * Removes an aspect from the repo.<br>
     * Returns the removed stack, if there was one removed.
     *
     * @param aspect
     * @return
     */
    @Nullable
    IAspectStack remove(Aspect aspect);

    /**
     * Sets the aspect in the repo by the specified values.
     *
     * @param aspect
     * @param amount
     * @param isCraftable
     * @return The previous stack, if there was one.
     */
    @Nullable
    IAspectStack setAspect(@Nonnull Aspect aspect, long amount, boolean isCraftable);

    /**
     * Sets the aspect in the repo to the specified aspect stack.
     *
     * @param stack
     * @return The previous stack, if there was one.
     */
    @Nullable
    IAspectStack setAspect(@Nonnull IAspectStack stack);

    /**
     * Returns the number of unique aspects stored.
     *
     * @return
     */
    int size();
}
