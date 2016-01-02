package thaumicenergistics.api.storage;

import java.util.Collection;
import java.util.Set;
import thaumcraft.api.aspects.Aspect;

public interface IEssentiaRepo
{

	/**
	 * Returns a set of the stored aspects.
	 * 
	 * @return
	 */
	public Set<Aspect> aspectSet();

	/**
	 * Removes all entries in the repo.
	 */
	public void clear();

	/**
	 * Returns true if the repo has the aspect stored.
	 * 
	 * @param aspect
	 * @return
	 */
	public boolean containsAspect( Aspect aspect );

	/**
	 * Sets the repo to match the specified collection.
	 * Any existing data is removed.
	 * 
	 * @param stacks
	 */
	public void copyFrom( Collection<IAspectStack> stacks );

	/**
	 * Gets the aspect stack associated with the aspect or null.
	 */
	public IAspectStack get( Aspect aspect );

	/**
	 * Returns all aspect information stored in the repo.
	 * 
	 * @return
	 */
	public Collection<IAspectStack> getAll();

	/**
	 * Gets the aspect stack associated with the aspect or specified the default value.
	 */
	public IAspectStack getOrDefault( Aspect aspect, IAspectStack defaultValue );

	/**
	 * Returns true if the repo is empty.
	 * 
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * Changes the aspect in the repo by the specified values.
	 * 
	 * @param aspect
	 * @param change
	 * @param isCraftable
	 * If this value is null and there is a stored value, its crafting status will remain the same.
	 * Otherwise it is set to false.
	 * @return The previous stack, if there was one.
	 */
	public IAspectStack postChange( Aspect aspect, long change, Boolean isCraftable );

	/**
	 * Changes the aspect in the repo by the specified aspect stack.
	 * 
	 * @param change
	 * @return The previous stack, if there was one.
	 */
	public IAspectStack postChange( IAspectStack change );

	/**
	 * Removes an aspect from the repo.
	 * Returns the removed stack.
	 * 
	 * @param aspect
	 * @return
	 */
	public IAspectStack remove( Aspect aspect );

	/**
	 * Sets the aspect in the repo by the specified values.
	 * 
	 * @param aspect
	 * @param amount
	 * @param isCraftable
	 * @return The previous stack, if there was one.
	 */
	public IAspectStack setAspect( Aspect aspect, long amount, boolean isCraftable );

	/**
	 * Sets the aspect in the repo to the specified aspect stack.
	 * 
	 * @param stack
	 * @return The previous stack, if there was one.
	 */
	public IAspectStack setAspect( IAspectStack stack );

	/**
	 * Returns the number of unique aspects stored.
	 * 
	 * @return
	 */
	public int size();
}
