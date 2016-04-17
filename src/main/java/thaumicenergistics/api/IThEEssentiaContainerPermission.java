package thaumicenergistics.api;

/**
 * Essentia container options.
 *
 * @author Nividica
 *
 */
public interface IThEEssentiaContainerPermission
{
	/**
	 * Can the container be partially filled?
	 */
	boolean canHoldPartialAmount();

	/**
	 * The maximum amount this container can hold
	 */
	int maximumCapacity();
}
