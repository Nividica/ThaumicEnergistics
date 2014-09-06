package thaumicenergistics.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.container.ContainerEssentiaCell;
import thaumicenergistics.container.ContainerPriority;
import thaumicenergistics.parts.AbstractAEPartBase;
import appeng.api.parts.IPartHost;
import appeng.helpers.IPriorityHost;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler
	implements IGuiHandler
{
	// ID's between 0 and this number indicate that they are AE parts
	private static final int DIRECTION_OFFSET = ForgeDirection.values().length;

	// ID's should increase in values of 10

	/**
	 * Singular ID of the essentia cell gui
	 */
	public static final int ESSENTIA_CELL_ID = 10;

	/**
	 * Base ID of the priority gui.
	 * Add the ForgeDirection's side ordinal to this value.
	 */
	private static final int PRIORITY_ID = 20;

	private static final int END_PLACEHOLDER = 30;

	/**
	 * Gets the AE part at the specified location.
	 * 
	 * @param tileSide
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static AbstractAEPartBase getPart( final ForgeDirection tileSide, final World world, final int x, final int y, final int z )
	{
		// Get the host at the specified position
		IPartHost partHost = (IPartHost)( world.getTileEntity( x, y, z ) );

		// Ensure we got a host
		if( partHost == null )
		{
			return null;
		}

		// Get the part from the host
		return (AbstractAEPartBase)( partHost.getPart( tileSide ) );
	}

	/**
	 * Get the gui element for the AE part at the specified location
	 * 
	 * @param tileSide
	 * @param player
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param isServerSide
	 * @return
	 */
	private static Object getPartGuiElement( final ForgeDirection tileSide, final EntityPlayer player, final World world, final int x, final int y,
												final int z, final boolean isServerSide )
	{
		// Get the part
		AbstractAEPartBase part = GuiHandler.getPart( tileSide, world, x, y, z );

		// Ensure we got the part
		if( part == null )
		{
			return null;
		}

		// Is this server side?
		if( isServerSide )
		{
			// Ask the part for its server element
			return part.getServerGuiElement( player );
		}

		// Ask the part for its client element
		return part.getClientGuiElement( player );
	}

	/**
	 * Helper function to properly generate a priority gui ID
	 * 
	 * @param side
	 * @return
	 */
	public static int generatePriorityID( final ForgeDirection side )
	{
		return GuiHandler.PRIORITY_ID + side.ordinal();
	}

	/**
	 * Launches an AE part gui
	 * 
	 * @param part
	 * @param player
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void launchGui( final AbstractAEPartBase part, final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Ensure the player is allowed to open the gui
		if( part.doesPlayerHavePermissionToOpenGui( player ) )
		{
			player.openGui( ThaumicEnergistics.instance, part.getSide().ordinal(), world, x, y, z );
		}
	}

	/**
	 * Launches a non AE part gui.
	 * 
	 * @param ID
	 * @param player
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void launchGui( final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		player.openGui( ThaumicEnergistics.instance, ID + GuiHandler.DIRECTION_OFFSET, world, x, y, z );
	}

	@Override
	public Object getClientGuiElement( int ID, final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Is the ID a forge direction?
		ForgeDirection side = ForgeDirection.getOrientation( ID );

		// Do we have a world and side?
		if( ( world != null ) && ( side != ForgeDirection.UNKNOWN ) )
		{
			// This is an AE part, get its gui
			return GuiHandler.getPartGuiElement( side, player, world, x, y, z, false );
		}

		// This is not an AE part, adjust the ID
		ID -= GuiHandler.DIRECTION_OFFSET;

		// Is this the essentia cell?
		if( ID == GuiHandler.ESSENTIA_CELL_ID )
		{
			return new GuiEssentiaCell( player, world, x, y, z );
		}

		// Is this the priority window?
		if( ( ID >= GuiHandler.PRIORITY_ID ) && ( ID < GuiHandler.END_PLACEHOLDER ) )
		{
			// Get the side
			side = ForgeDirection.getOrientation( ID - GuiHandler.PRIORITY_ID );

			// Get the part
			AbstractAEPartBase part = GuiHandler.getPart( side, world, x, y, z );

			// Ensure we got the part, and that it implements IPriortyHost
			if( ( part == null ) || !( part instanceof IPriorityHost ) )
			{
				return null;
			}

			// Return the gui
			return new GuiPriority( (IPriorityHost)part, player );

		}

		// No matching GUI element found
		return null;

	}

	@Override
	public Object getServerGuiElement( int ID, final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Is the ID a forge Direction?
		ForgeDirection side = ForgeDirection.getOrientation( ID );

		// Do we have a world and side?
		if( ( world != null ) && ( side != ForgeDirection.UNKNOWN ) )
		{
			// This is an AE part, get its gui
			return GuiHandler.getPartGuiElement( side, player, world, x, y, z, true );
		}

		// This is not an AE part, adjust the ID
		ID -= GuiHandler.DIRECTION_OFFSET;

		// Is this the essentia cell?
		if( ID == GuiHandler.ESSENTIA_CELL_ID )
		{
			return new ContainerEssentiaCell( player, world, x, y, z );
		}

		// Is this the priority window?
		if( ( ID >= GuiHandler.PRIORITY_ID ) && ( ID < GuiHandler.END_PLACEHOLDER ) )
		{
			// Get the side
			side = ForgeDirection.getOrientation( ID - GuiHandler.PRIORITY_ID );

			// Get the part
			AbstractAEPartBase part = GuiHandler.getPart( side, world, x, y, z );

			// Ensure we got the part, and that it implements IPriortyHost
			if( ( part == null ) || !( part instanceof IPriorityHost ) )
			{
				return null;
			}

			// Return the container
			return new ContainerPriority( (IPriorityHost)part, player );

		}

		// No matching GUI element found
		return null;
	}

}
