package thaumicenergistics.util;

import java.lang.ref.WeakReference;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.parts.AEPartVisInterface;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;

public class VisInterfaceData
{
	private static final String NBT_KEY_HAS_DATA = "hasData";
	private static final String NBT_KEY_DATA = "data";
	private static final String NBT_KEY_UID = "uid";

	/**
	 * True if there is data.
	 */
	private boolean hasData = false;

	/**
	 * World the interface is in.
	 */
	private int world;

	/**
	 * X position of the interface.
	 */
	private int x;

	/**
	 * Y position of the interface.
	 */
	private int y;

	/**
	 * Z position of the interface.
	 */
	private int z;

	/**
	 * Side the part is attached to
	 */
	private int side;

	/**
	 * ID of the interface.
	 */
	private long UID;

	/**
	 * Cached reference to interface.
	 */
	private WeakReference<AEPartVisInterface> visInterface;

	/**
	 * Creates the info, setting that there is no data
	 */
	public VisInterfaceData()
	{
		this.clearData();
	}

	/**
	 * Creates the data from a vis interface part.
	 * 
	 * @param visInterface
	 */
	public VisInterfaceData( final AEPartVisInterface visInterface )
	{
		// Get the host tile
		TileEntity host = visInterface.getHostTile();

		if( host != null )
		{
			// Set the data
			this.world = host.getWorldObj().provider.dimensionId;
			this.x = host.xCoord;
			this.y = host.yCoord;
			this.z = host.zCoord;
			this.side = visInterface.getSide().ordinal();
			this.UID = visInterface.getUID();
			this.hasData = true;
		}
	}

	private void refreshCache()
	{
		try
		{
			// Get the world
			World world = DimensionManager.getWorld( this.world );

			// Ensure the world is not null
			if( world == null )
			{
				return;
			}

			// Get the tile
			TileEntity tile = world.getTileEntity( this.x, this.y, this.z );

			// Ensure we got a tile, and that it is a part host
			if( !( tile instanceof IPartHost ) )
			{
				return;
			}

			// Get the part from the host
			IPart part = ( (IPartHost)tile ).getPart( ForgeDirection.getOrientation( this.side ) );

			// Ensure we got the part, and it is a vis interface
			if( !( part instanceof AEPartVisInterface ) )
			{
				return;
			}

			// Cast to the interface
			AEPartVisInterface visInterface = (AEPartVisInterface)part;

			// Check the UID
			if( visInterface.getUID() == this.UID )
			{
				// Set the interface
				this.visInterface = new WeakReference<AEPartVisInterface>( visInterface );
			}
		}
		catch( Exception e )
		{
		}
	}

	/**
	 * Erases all data
	 */
	public void clearData()
	{
		this.hasData = false;
		this.world = -1;
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.side = ForgeDirection.UNKNOWN.ordinal();
		this.UID = 0;
		this.visInterface = new WeakReference<AEPartVisInterface>( null );
	}

	/**
	 * True if there is data.
	 * 
	 * @return
	 */
	public boolean getHasData()
	{
		return this.hasData;
	}

	/**
	 * Gets the vis interface.
	 * 
	 * @return
	 */
	public AEPartVisInterface getInterface( final boolean forceUpdate )
	{
		// Do we have any data to load from?
		if( this.hasData )
		{
			// Do we have an interface cached?
			AEPartVisInterface vInt = this.visInterface.get();
			if( forceUpdate || vInt == null )
			{
				// Attempt to get the interface
				this.refreshCache();

				// Return it if cached
				return this.visInterface.get();
			}

			// Return the cached
			return vInt;

		}

		// Nothing to load
		return null;
	}

	/**
	 * Reads the info directly from the tag.
	 * 
	 * @param tag
	 */
	public void readFromNBT( final NBTTagCompound tag )
	{
		// Read if we have data
		this.hasData = tag.getBoolean( VisInterfaceData.NBT_KEY_HAS_DATA );

		if( this.hasData )
		{
			// Load the info
			int[] info = tag.getIntArray( VisInterfaceData.NBT_KEY_DATA );
			this.world = info[0];
			this.x = info[1];
			this.y = info[2];
			this.z = info[3];
			this.side = info[4];

			// Read the uid
			this.UID = tag.getLong( VisInterfaceData.NBT_KEY_UID );

			// Erase the cache
			this.visInterface.clear();
		}
	}

	/**
	 * Reads the info from the data tag.
	 * 
	 * @param data
	 * @param name
	 */
	public void readFromNBT( final NBTTagCompound data, final String name )
	{
		this.clearData();

		// Does the data tag have our data?
		if( data.hasKey( name ) )
		{
			// Read the tag
			NBTTagCompound tag = data.getCompoundTag( name );

			this.readFromNBT( tag );
		}
	}

	/**
	 * Creates a new NBT tag and writes the data into it
	 * 
	 * @return
	 */
	public NBTTagCompound writeToNBT()
	{
		// Create the tag
		NBTTagCompound tag = new NBTTagCompound();

		// Write if we have data
		tag.setBoolean( VisInterfaceData.NBT_KEY_HAS_DATA, this.hasData );

		if( this.hasData )
		{
			// Write the data
			tag.setIntArray( VisInterfaceData.NBT_KEY_DATA, new int[] { this.world, this.x, this.y, this.z, this.side } );

			// Write the uid
			tag.setLong( VisInterfaceData.NBT_KEY_UID, this.UID );
		}

		return tag;
	}

	/**
	 * Writes the info into the data tag.
	 * 
	 * @param data
	 * @param name
	 */
	public void writeToNBT( final NBTTagCompound data, final String name )
	{
		// Write into the data tag
		data.setTag( name, this.writeToNBT() );
	}
}
