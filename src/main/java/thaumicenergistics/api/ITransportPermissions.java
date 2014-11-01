package thaumicenergistics.api;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaContainerItem;

/**
 * Defines what items and tile entities ThaumicEnergistics is allowed to
 * interact with.
 * 
 * @author Nividica
 * 
 */
public interface ITransportPermissions
{

	/**
	 * Adds a tile entity to the extract whitelist.
	 * The tile must implement the interface {@link IAspectContainer}
	 * 
	 * @param tileClass
	 * @return True if added to the list or already present, False otherwise.
	 */
	public boolean addAspectContainerTileToExtractPermissions( Class<? extends TileEntity> tileClass );

	/**
	 * Adds a tile entity to the inject whitelist.
	 * The tile must implement the interface {@link IAspectContainer}
	 * 
	 * @param tileClass
	 * @return True if added to the list, False if not.
	 */
	public boolean addAspectContainerTileToInjectPermissions( Class<? extends TileEntity> tileClass );

	/**
	 * Adds an item to the whitelist that must match the specified metadata
	 * value.
	 * 
	 * @param itemClass
	 * @param capacity
	 * @param metadata
	 * @param canHoldPartialAmount
	 */
	public void addEssentiaContainerItemToTransportPermissions( Class<? extends IEssentiaContainerItem> itemClass, int capacity, int metadata,
																boolean canHoldPartialAmount );

	/**
	 * Checks if the container can be extracted from
	 * 
	 * @param container
	 * @return
	 */
	public boolean canExtractFromAspectContainerTile( IAspectContainer container );

	/**
	 * Checks if the container can be injected into
	 * 
	 * @param container
	 * @return
	 */
	public boolean canInjectToAspectContainerTile( IAspectContainer container );

	/**
	 * Gets the information about the container as it was registered to the
	 * whitelist.
	 * 
	 * @param itemClass
	 * @param metadata
	 * @return Info if was registered, null otherwise.
	 */
	public IEssentiaContainerPermission getEssentiaContainerInfo( Class<? extends Item> itemClass, int metadata );
}
