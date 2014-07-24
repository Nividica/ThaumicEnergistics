package thaumicenergistics.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.container.ContainerEssentiaCell;
import thaumicenergistics.parts.AEPartBase;
import appeng.api.parts.IPartHost;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler
	implements IGuiHandler
{
	// ID's between 0 and this number indicate that they are AE parts
	private static final int DIRECTION_OFFSET = ForgeDirection.values().length;

	public static final int ESSENTIA_CELL_ID = 0;

	// Get the gui element for the AE part at the specified location
	private Object getPartGuiElement( ForgeDirection tileSide, EntityPlayer player, World world, int x, int y, int z, boolean isServerSide )
	{
		// Get the host at the specified position
		IPartHost partHost = (IPartHost) ( world.getTileEntity( x, y, z ) );

		// Do we have a host?
		if ( partHost != null )
		{
			// Get the part from the host
			AEPartBase part = (AEPartBase) ( partHost.getPart( tileSide ) );

			// Do we have a part?
			if ( part != null )
			{
				// Is this server side?
				if ( isServerSide )
				{
					// Ask the part for its server element
					return part.getServerGuiElement( player );
				}

				// Ask the part for its client element
				return part.getClientGuiElement( player );
			}
		}

		// Could not retrieve part
		return null;
	}
	
	// Launch AE part gui
	public static void launchGui( AEPartBase part, EntityPlayer player, World world, int x, int y, int z )
	{
		player.openGui( ThaumicEnergistics.instance, part.getSide().ordinal(), world, x, y, z );
	}

	// Launch regular gui
	public static void launchGui( int ID, EntityPlayer player, World world, int x, int y, int z )
	{
		player.openGui( ThaumicEnergistics.instance, ID + GuiHandler.DIRECTION_OFFSET, world, x, y, z );
	}

	@Override
	public Object getClientGuiElement( int ID, EntityPlayer player, World world, int x, int y, int z )
	{
		// Is the ID a forge direction?
		ForgeDirection side = ForgeDirection.getOrientation( ID );

		// Do we have a world and side?
		if ( ( world != null ) && ( side != ForgeDirection.UNKNOWN ) )
		{
			// This is an AE part, get its gui
			return this.getPartGuiElement( side, player, world, x, y, z, false );
		}

		// This is not an AE part
		switch( ID - GuiHandler.DIRECTION_OFFSET )
		{
			case GuiHandler.ESSENTIA_CELL_ID:
				return new GuiEssentiaCell( player, world, x, y, z );
		}
		
		// No matching GUI element found
		return null;

	}

	@Override
	public Object getServerGuiElement( int ID, EntityPlayer player, World world, int x, int y, int z )
	{
		// Is the ID a forge Direction?
		ForgeDirection side = ForgeDirection.getOrientation( ID );

		// Do we have a world and side?
		if ( ( world != null ) && ( side != ForgeDirection.UNKNOWN ) )
		{
			// This is an AE part, get its gui
			return this.getPartGuiElement( side, player, world, x, y, z, true );
		}
		
		// This is not an AE part
		switch( ID - GuiHandler.DIRECTION_OFFSET )
		{
			case GuiHandler.ESSENTIA_CELL_ID:
				return new ContainerEssentiaCell( player, world, x, y, z );
		}
		
		// No matching GUI element found
		return null;
	}

}
