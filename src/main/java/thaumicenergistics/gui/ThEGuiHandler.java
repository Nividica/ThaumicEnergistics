package thaumicenergistics.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.api.gui.ICraftingIssuerContainer;
import thaumicenergistics.api.networking.ICraftingIssuerHost;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.container.*;
import thaumicenergistics.inventory.HandlerWirelessEssentiaTerminal;
import thaumicenergistics.parts.AbstractAEPartBase;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.helpers.IPriorityHost;
import cpw.mods.fml.common.network.IGuiHandler;

public class ThEGuiHandler
	implements IGuiHandler
{
	// ID's between 0 and this number indicate that they are AE parts
	private static final int DIRECTION_OFFSET = ForgeDirection.values().length;

	// ID's must increase in values of 10
	private static final int ID_STEP_VALUE = 10;

	/**
	 * Singular ID of the essentia cell gui
	 */
	public static final int ESSENTIA_CELL_ID = ID_STEP_VALUE * 1;

	/**
	 * Base ID of the priority gui.
	 * Add the ForgeDirection's side ordinal to this value.
	 */
	public static final int PRIORITY_ID = ID_STEP_VALUE * 2;

	/**
	 * ID of the essentia cell workbench
	 */
	public static final int CELL_WORKBENCH_ID = ID_STEP_VALUE * 3;

	/**
	 * ID of the wireless terminal gui.
	 */
	public static final int WIRELESS_TERMINAL_ID = ID_STEP_VALUE * 4;

	/**
	 * ID of the arcane assembler gui.
	 */
	public static final int ARCANE_ASSEMBLER_ID = ID_STEP_VALUE * 5;

	/**
	 * ID of the knowledge inscriber gui.
	 */
	public static final int KNOWLEDGE_INSCRIBER = ID_STEP_VALUE * 6;

	/**
	 * ID of the knowledge inscriber gui.
	 */
	public static final int ESSENTIA_VIBRATION_CHAMBER = ID_STEP_VALUE * 7;

	/**
	 * ID of the auto crafting amount bridge.
	 * When calling this make sure the current container implements ICraftingIssuerContainer,
	 * or extends AEBaseContainer with the target set to an ICraftingIssuerHost.
	 */
	public static final int AUTO_CRAFTING_AMOUNT = ID_STEP_VALUE * 8;

	/**
	 * ID of the auto crafting confirm bridge.
	 * When calling this make sure the current container extends
	 * AEBaseContainer with the target set to an ICraftingIssuerHost.
	 */
	public static final int AUTO_CRAFTING_CONFIRM = ID_STEP_VALUE * 9;

	/**
	 * ID of the distillation encoder.
	 */
	public static final int DISTILLATION_ENCODER = ID_STEP_VALUE * 10;

	/**
	 * Extra data used for some GUI calls.
	 */
	private static Object[] extraData = null;

	/**
	 * Gets the crafting issuer host or null.
	 * 
	 * @param player
	 * @return
	 */
	private static ICraftingIssuerHost getCraftingIssuerHost( final EntityPlayer player )
	{
		// Is the currently opened container a crafting issuer container?
		if( player.openContainer instanceof ICraftingIssuerContainer )
		{
			// Return the issuer
			return ( (ICraftingIssuerContainer)player.openContainer ).getCraftingHost();
		}

		// Is the currently opened container an AE base container?
		if( player.openContainer instanceof AEBaseContainer )
		{
			// Get the target
			Object target = ( (AEBaseContainer)player.openContainer ).getTarget();

			// Is the target an ICraftingIssuerHost?
			if( target instanceof ICraftingIssuerHost )
			{
				return (ICraftingIssuerHost)target;
			}
		}

		return null;
	}

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
	private static IPart getPart( final ForgeDirection tileSide, final World world, final int x, final int y, final int z )
	{
		// Get the host at the specified position
		IPartHost partHost = (IPartHost)( world.getTileEntity( x, y, z ) );

		// Ensure we got a host
		if( partHost == null )
		{
			return null;
		}

		// Get the part from the host
		return( partHost.getPart( tileSide ) );
	}

	/**
	 * Gets the sided part for the GUI ID.
	 * 
	 * @param ID
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static IPart getPartFromSidedID( final int ID, final World world, final int x, final int y, final int z )
	{
		ForgeDirection side = ForgeDirection.getOrientation( ID % ThEGuiHandler.ID_STEP_VALUE );
		return ThEGuiHandler.getPart( side, world, x, y, z );
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
		AbstractAEPartBase part = (AbstractAEPartBase)ThEGuiHandler.getPart( tileSide, world, x, y, z );

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
	 * Returns true if the specified ID is within the base range.
	 * 
	 * @param BaseID
	 * @param ID
	 * @return
	 */
	private static boolean isIDInRange( final int ID, final int BaseID )
	{
		return( ( ID >= BaseID ) && ( ID < ( BaseID + ThEGuiHandler.ID_STEP_VALUE ) ) );
	}

	/**
	 * Helper function to properly generate a GUI ID that
	 * includes a forge direction.
	 * 
	 * @param ID
	 * @param side
	 * @return
	 */
	public static int generateSidedID( final int ID, final ForgeDirection side )
	{
		return ID + side.ordinal();
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
			player.openGui( ThaumicEnergistics.INSTANCE, part.getSide().ordinal(), world, x, y, z );
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
		player.openGui( ThaumicEnergistics.INSTANCE, ID + ThEGuiHandler.DIRECTION_OFFSET, world, x, y, z );
	}

	/**
	 * Launches a non AE part gui with the specified extra data.
	 * 
	 * @param ID
	 * @param player
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param extraData
	 */
	public static void launchGui( final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z,
									final Object[] extraData )
	{
		ThEGuiHandler.extraData = extraData;
		player.openGui( ThaumicEnergistics.INSTANCE, ID + ThEGuiHandler.DIRECTION_OFFSET, world, x, y, z );
		ThEGuiHandler.extraData = null;
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
			return ThEGuiHandler.getPartGuiElement( side, player, world, x, y, z, false );
		}

		// This is not an AE part, adjust the ID
		ID -= ThEGuiHandler.DIRECTION_OFFSET;

		// Check basic ID's
		switch ( ID )
		{
		// Is this the essentia cell?
		case ThEGuiHandler.ESSENTIA_CELL_ID:
			return GuiEssentiaCellTerminal.NewEssentiaCellGui( player, world, x, y, z );

			// Is this the cell workbench?
		case ThEGuiHandler.CELL_WORKBENCH_ID:
			return new GuiEssentiaCellWorkbench( player, world, x, y, z );

			// Is this the wireless gui?
		case ThEGuiHandler.WIRELESS_TERMINAL_ID:
			return GuiEssentiaCellTerminal.NewWirelessEssentiaTerminalGui( player );

			// Is this the arcane assembler?
		case ThEGuiHandler.ARCANE_ASSEMBLER_ID:
			return new GuiArcaneAssembler( player, world, x, y, z );

			// Is this the knowledge inscriber?
		case ThEGuiHandler.KNOWLEDGE_INSCRIBER:
			return new GuiKnowledgeInscriber( player, world, x, y, z );

			// Vibration chamber?
		case ThEGuiHandler.ESSENTIA_VIBRATION_CHAMBER:
			return new GuiEssentiaVibrationChamber( player, world, x, y, z );

			// Distillation encoder?
		case ThEGuiHandler.DISTILLATION_ENCODER:
			return new GuiDistillationEncoder( player, world, x, y, z );

			// AE2 Autocrafting Amount?
		case ThEGuiHandler.AUTO_CRAFTING_AMOUNT:
			ICraftingIssuerHost amountHost = ThEGuiHandler.getCraftingIssuerHost( player );
			if( amountHost != null )
			{
				return new GuiCraftAmountBridge( player, amountHost );
			}
			return null;

			// AE2 Autocrafting Confirm?
		case ThEGuiHandler.AUTO_CRAFTING_CONFIRM:
			ICraftingIssuerHost confirmHost = ThEGuiHandler.getCraftingIssuerHost( player );
			if( confirmHost != null )
			{
				return new GuiCraftConfirmBridge( player, confirmHost );
			}
			return null;

		}

		// Is this the priority window?
		if( ThEGuiHandler.isIDInRange( ID, ThEGuiHandler.PRIORITY_ID ) )
		{
			// Get the part
			IPart part = ThEGuiHandler.getPartFromSidedID( ID, world, x, y, z );

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
			return ThEGuiHandler.getPartGuiElement( side, player, world, x, y, z, true );
		}

		// This is not an AE part, adjust the ID
		ID -= ThEGuiHandler.DIRECTION_OFFSET;

		switch ( ID )
		{
		// Is this the essentia cell?
		case ThEGuiHandler.ESSENTIA_CELL_ID:
			return new ContainerEssentiaCell( player, world, x, y, z );

			// Is this the cell workbench?
		case ThEGuiHandler.CELL_WORKBENCH_ID:
			return new ContainerEssentiaCellWorkbench( player, world, x, y, z );

			// Is this the wireless gui?
		case ThEGuiHandler.WIRELESS_TERMINAL_ID:
			HandlerWirelessEssentiaTerminal handler = (HandlerWirelessEssentiaTerminal)ThEGuiHandler.extraData[0];
			return new ContainerWirelessEssentiaTerminal( player, handler );

			// Is this the arcane assembler?
		case ThEGuiHandler.ARCANE_ASSEMBLER_ID:
			return new ContainerArcaneAssembler( player, world, x, y, z );

			// Is this the knowledge inscriber?
		case ThEGuiHandler.KNOWLEDGE_INSCRIBER:
			return new ContainerKnowledgeInscriber( player, world, x, y, z );

			// Vibration chamber?
		case ThEGuiHandler.ESSENTIA_VIBRATION_CHAMBER:
			return new ContainerEssentiaVibrationChamber( player, world, x, y, z );

			// Distillation encoder?
		case ThEGuiHandler.DISTILLATION_ENCODER:
			return new ContainerDistillationEncoder( player, world, x, y, z );

			// AE2 Autocrafting Amount?
		case ThEGuiHandler.AUTO_CRAFTING_AMOUNT:
			ICraftingIssuerHost amountHost = ThEGuiHandler.getCraftingIssuerHost( player );
			if( amountHost != null )
			{
				return new ContainerCraftAmount( player.inventory, amountHost );
			}
			return null;

			// AE2 Autocrafting Confirm?
		case ThEGuiHandler.AUTO_CRAFTING_CONFIRM:
			ICraftingIssuerHost confirmHost = ThEGuiHandler.getCraftingIssuerHost( player );
			if( confirmHost != null )
			{
				return new ContainerCraftConfirm( player.inventory, confirmHost );
			}
			return null;

		}

		// Is this the priority window?
		if( ThEGuiHandler.isIDInRange( ID, ThEGuiHandler.PRIORITY_ID ) )
		{
			// Get the part
			IPart part = ThEGuiHandler.getPartFromSidedID( ID, world, x, y, z );

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
