package thaumicenergistics.integration;

/**
 * Used to pass specific events to a watcher.
 * 
 * @author Nividica
 * 
 */
public interface IEssentiaProviderWatcher
{
	/**
	 * Called when the providers power state changes.
	 */
	public void onProviderPowerChange( boolean isOnline );

	/**
	 * Called when the provider is broken.
	 */
	public void onProviderBroken();
}
