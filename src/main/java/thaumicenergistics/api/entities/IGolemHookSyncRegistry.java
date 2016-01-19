package thaumicenergistics.api.entities;

/**
 * Allows a hook handler to register data to be synchronized between the server and clients.<br/>
 * 
 * @author Nividica
 * 
 */
public interface IGolemHookSyncRegistry
{
	/**
	 * Returns the sync char if it is mapped, returns {@code defaultChar} otherwise.
	 * 
	 * @param id
	 * @param defaultChar
	 * @return
	 */
	char getSyncCharOrDefault( int id, char defaultChar );

	/**
	 * Registers a new sync byte, and returns the ID that maps to it.<br/>
	 * Note: This can only be called during registration, will throw exception otherwise.
	 * 
	 * @param handler
	 * @param c
	 * Initial value of the char.
	 * @return The ID of the char.
	 */
	int registerSyncChar( IGolemHookHandler handler, char c );

	/**
	 * Updates the sync char.
	 * 
	 * @param handler
	 * @param id
	 * @param c
	 */
	void updateSyncChar( IGolemHookHandler handler, int id, char c );
}
