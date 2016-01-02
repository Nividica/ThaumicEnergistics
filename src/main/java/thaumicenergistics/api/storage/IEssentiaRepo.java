package thaumicenergistics.api.storage;

import java.util.Collection;
import java.util.Set;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;

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
	public void copyFrom( Collection<AspectStack> stacks );

	/**
	 * Gets the aspect stack associated with the aspect or null.
	 */
	public AspectStack get( Aspect aspect );

	/**
	 * Returns all aspect information stored in the repo.
	 * 
	 * @return
	 */
	public Collection<AspectStack> getAll();

	/**
	 * Gets the aspect stack associated with the aspect or specified the default value.
	 */
	public AspectStack getOrDefault( Aspect aspect, AspectStack defaultValue );

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
	public AspectStack postChange( Aspect aspect, long change, Boolean isCraftable );

	/**
	 * Changes the aspect in the repo by the specified aspect stack.
	 * 
	 * @param change
	 * @return The previous stack, if there was one.
	 */
	public AspectStack postChange( AspectStack change );

	/**
	 * Removes an aspect from the repo.
	 * Returns the removed stack.
	 * 
	 * @param aspect
	 * @return
	 */
	public AspectStack remove( Aspect aspect );

	/**
	 * Sets the aspect in the repo by the specified values.
	 * 
	 * @param aspect
	 * @param amount
	 * @param isCraftable
	 * @return The previous stack, if there was one.
	 */
	public AspectStack setAspect( Aspect aspect, long amount, boolean isCraftable );

	/**
	 * Sets the aspect in the repo to the specified aspect stack.
	 * 
	 * @param stack
	 * @return The previous stack, if there was one.
	 */
	public AspectStack setAspect( AspectStack stack );

	/**
	 * Returns the number of unique aspects stored.
	 * 
	 * @return
	 */
	public int size();
}
