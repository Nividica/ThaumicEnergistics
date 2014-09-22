package thaumicenergistics.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.parts.AEPartVisInterface;
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

	/**
	 * Erases all data
	 */
	private void clearData()
	{
		this.hasData = false;
		this.world = -1;
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.side = ForgeDirection.UNKNOWN.ordinal();
		this.UID = 0;
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
	public AEPartVisInterface getInterface()
	{
		if( this.hasData )
		{

			try
			{
				// Get the world
				World world = DimensionManager.getWorld( this.world );

				// Get the host tile
				IPartHost host = (IPartHost)( world.getTileEntity( this.x, this.y, this.z ) );

				// Get the part
				AEPartVisInterface visInterface = (AEPartVisInterface)host.getPart( ForgeDirection.getOrientation( this.side ) );

				// Check the UID
				if( visInterface.getUID() == this.UID )
				{
					return visInterface;
				}
			}
			catch( Exception e )
			{
			}
		}

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
