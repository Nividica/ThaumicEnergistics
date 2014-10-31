package thaumicenergistics.api.registry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.IAspectContainer;
import cpw.mods.fml.common.FMLCommonHandler;

public class TEPermissionsTile
{

	/**
	 * Holds a list of tiles that we are allowed to extract from.
	 */
	private final List<Class<? extends TileEntity>> extractWhiteList = new ArrayList<Class<? extends TileEntity>>();
	/**
	 * Holds a list of tiles that we are allowed to inject into.
	 */
	private final List<Class<? extends TileEntity>> injectWhiteList = new ArrayList<Class<? extends TileEntity>>();

	/**
	 * Adds a tile entity to the extract whitelist.
	 * The tile must implement the interface {@link IAspectContainer}
	 * 
	 * @param tile
	 * @return True if added to the list or already present, False if otherwise.
	 */
	public boolean addTileToExtractWhitelist( final Class<? extends TileEntity> tile )
	{
		// Ensure we have a tile
		if( tile != null )
		{
			// Ensure it is a container
			if( IAspectContainer.class.isAssignableFrom( tile ) )
			{
				// Is it already registered?
				if( !this.extractWhiteList.contains( tile ) )
				{
					// Add to the list
					this.extractWhiteList.add( tile );

					// Log the addition
					FMLCommonHandler.instance().getFMLLogger().info( "Adding " + tile.toString() + " to extraction whitelist." );
				}

				return true;
			}
		}

		return false;
	}

	/**
	 * Adds a tile entity to the inject whitelist.
	 * The tile must implement the interface {@link IAspectContainer}
	 * 
	 * @param tile
	 * @return True if added to the list, False if not.
	 */
	public boolean addTileToInjectWhitelist( final Class<? extends TileEntity> tile )
	{
		// Ensure we have a tile
		if( tile != null )
		{
			// Ensure it is a container
			if( IAspectContainer.class.isAssignableFrom( tile ) )
			{
				// Is it already registered?
				if( !this.injectWhiteList.contains( tile ) )
				{
					// Add to the list
					this.injectWhiteList.add( tile );

					// Log the addition
					FMLCommonHandler.instance().getFMLLogger().info( "Adding " + tile.toString() + " to injection whitelist." );
				}

				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the container can be extracted from
	 * 
	 * @param container
	 * @return
	 */
	public boolean canExtract( final IAspectContainer container )
	{
		// Loop over blacklist
		for( Class<? extends TileEntity> whiteClass : this.extractWhiteList )
		{
			// Is the container an instance of this whitelisted class?
			if( whiteClass.isInstance( container ) )
			{
				// Return that we can extract
				return true;
			}
		}
	
		// Return that we can not extract
		return false;
	}

	/**
	 * Checks if the container can be injected into
	 * 
	 * @param container
	 * @return
	 */
	public boolean canInject( final IAspectContainer container )
	{
		// Loop over blacklist
		for( Class<? extends TileEntity> whiteClass : this.injectWhiteList )
		{
			// Is the container an instance of this whitelisted class?
			if( whiteClass.isInstance( container ) )
			{
				// Return that we can inject
				return true;
			}
		}
	
		// Return that we can not inject
		return false;
	}

}
