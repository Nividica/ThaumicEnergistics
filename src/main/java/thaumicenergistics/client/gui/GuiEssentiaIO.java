package thaumicenergistics.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.gui.IAspectSlotGui;
import thaumicenergistics.client.gui.abstraction.ThEBaseGui;
import thaumicenergistics.client.gui.buttons.GuiButtonAllowVoid;
import thaumicenergistics.client.gui.buttons.GuiButtonRedstoneModes;
import thaumicenergistics.client.gui.widget.ThEWidget;
import thaumicenergistics.client.gui.widget.WidgetAspectSlot;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.container.ContainerPartEssentiaIOBus;
import thaumicenergistics.common.network.packet.server.Packet_S_EssentiaIOBus;
import thaumicenergistics.common.parts.PartEssentiaExportBus;
import thaumicenergistics.common.parts.PartEssentiaImportBus;
import thaumicenergistics.common.parts.ThEPartEssentiaIOBus_Base;
import thaumicenergistics.common.registries.AEPartsEnum;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import appeng.api.config.RedstoneMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gui for the Import and Export buses
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiEssentiaIO
	extends ThEBaseGui
	implements WidgetAspectSlot.IConfigurable, IAspectSlotGui
{

	/**
	 * Number of rows and columns in the filter grid.
	 */
	private static final int FILTER_GRID_SIZE = 3;

	/**
	 * Controls if the widget is drawn.
	 * if filterSize >= this, the widget can be drawn.
	 */
	private static final byte[] WIDGET_CONFIG_BYTES = new byte[] { 2, 1, 2, 1, 0, 1, 2, 1, 2 };

	/**
	 * Starting X position for widgets.
	 */
	private static final int WIDGET_X_POSITION = 61;

	/**
	 * Starting Y position for widgets.
	 */
	private static final int WIDGET_Y_POSITION = 21;

	/**
	 * Height of the gui
	 */
	private static final int GUI_HEIGHT = 184;

	/**
	 * Width of the gui when the player does not have a network tool.
	 */
	private static final int GUI_WIDTH_NO_TOOL = 211;

	/**
	 * Width of the gui when the player has a network tool.
	 */
	private static final int GUI_WIDTH_WITH_TOOL = 246;

	/**
	 * Width of the main body of the gui.
	 * Ignores upgrade and network tool areas.
	 */
	private static final int GUI_MAIN_WIDTH = 176;

	/**
	 * Width of the upgrade area of the gui.
	 */
	private static final int GUI_UPGRADES_WIDTH = 35;

	/**
	 * Height of the upgrade area of the gui.
	 */
	private static final int GUI_UPGRADES_HEIGHT = 86;

	/**
	 * Position of the title string.
	 */
	private static final int TITLE_POS_X = 6, TITLE_POS_Y = 5;

	/**
	 * Redstone Control button placement
	 */
	private static final int REDSTONE_CONTROL_BUTTON_POS_Y = 2, REDSTONE_CONTROL_BUTTON_POS_X = -18, REDSTONE_CONTROL_BUTTON_SIZE = 16,
					REDSTONE_CONTROL_BUTTON_ID = 0;

	/**
	 * Void button placement
	 */
	private static final int ALLOW_VOID_BUTTON_POS_Y = 2, ALLOW_VOID_BUTTON_POS_X = -19, ALLOW_VOID_BUTTON_ID = 1;

	/**
	 * The part associated with this gui.
	 */
	private ThEPartEssentiaIOBus_Base part;

	/**
	 * The player viewing this bus gui.
	 */
	private EntityPlayer player;

	/**
	 * The size of the buses filter (0-2).
	 */
	private byte filterSize;

	/**
	 * Filter widgets.
	 */
	private List<WidgetAspectSlot> aspectSlotList = new ArrayList<WidgetAspectSlot>();

	/**
	 * Filter aspects.
	 */
	private List<Aspect> filteredAspects = new ArrayList<Aspect>();

	/**
	 * True if the player has a network tool in their inventory.
	 */
	private boolean hasNetworkTool;

	/**
	 * Tracks the redstone mode of the bus.
	 */
	private RedstoneMode redstoneMode = RedstoneMode.HIGH_SIGNAL;

	/**
	 * Title of the gui
	 */
	private final String guiTitle;

	/**
	 * Shown when the bus is redstone controlled
	 */
	private GuiButtonRedstoneModes redstoneControlButton = null;

	/**
	 * Button controlling if voiding is allowed.
	 */
	private GuiButtonAllowVoid voidModeButton = null;

	/**
	 * Creates the gui
	 * 
	 * @param partBus
	 * @param player
	 */
	public GuiEssentiaIO( final ThEPartEssentiaIOBus_Base partBus, final EntityPlayer player )
	{
		// Call super
		super( new ContainerPartEssentiaIOBus( partBus, player ) );

		// Set the bus
		this.part = partBus;

		// Set the player
		this.player = player;

		// Check if the player has a network tool
		this.hasNetworkTool = ( (ContainerPartEssentiaIOBus)this.inventorySlots ).hasNetworkTool();

		// Set the width
		this.xSize = ( this.hasNetworkTool ? GuiEssentiaIO.GUI_WIDTH_WITH_TOOL : GuiEssentiaIO.GUI_WIDTH_NO_TOOL );

		// Set the height
		this.ySize = GuiEssentiaIO.GUI_HEIGHT;

		// Set the title
		if( partBus instanceof PartEssentiaImportBus )
		{
			// Import
			this.guiTitle = AEPartsEnum.EssentiaImportBus.getLocalizedName();
		}
		else if( partBus instanceof PartEssentiaExportBus )
		{
			// Export
			this.guiTitle = AEPartsEnum.EssentiaExportBus.getLocalizedName();
		}
		else
		{
			this.guiTitle = "";
		}
	}

	/**
	 * Draws the GUI background layer
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Get the GUI texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_IO_BUS.getTexture() );

		// Does the user have a network tool?
		if( this.hasNetworkTool )
		{
			// Draw the full gui
			this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, GuiEssentiaIO.GUI_WIDTH_WITH_TOOL, GuiEssentiaIO.GUI_HEIGHT );
		}
		else
		{
			// Draw main gui
			this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, GuiEssentiaIO.GUI_MAIN_WIDTH, GuiEssentiaIO.GUI_HEIGHT );

			// Draw upgrade slots
			this.drawTexturedModalRect( this.guiLeft + GuiEssentiaIO.GUI_MAIN_WIDTH, this.guiTop, GuiEssentiaIO.GUI_MAIN_WIDTH, 0,
				GuiEssentiaIO.GUI_UPGRADES_WIDTH, GuiEssentiaIO.GUI_UPGRADES_HEIGHT );
		}

		// Call super
		super.drawAEToolAndUpgradeSlots( alpha, mouseX, mouseY );
	}

	@Override
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Call super
		super.drawGuiContainerForegroundLayer( mouseX, mouseY );

		// Draw the title
		this.fontRendererObj.drawString( this.guiTitle, GuiEssentiaIO.TITLE_POS_X, GuiEssentiaIO.TITLE_POS_Y, 0x000000 );

		boolean hoverUnderlayRendered = false;

		WidgetAspectSlot slotUnderMouse = null;

		for( int i = 0; i < 9; i++ )
		{
			WidgetAspectSlot slotWidget = this.aspectSlotList.get( i );

			if( ( !hoverUnderlayRendered ) && ( slotWidget.canRender() ) && ( slotWidget.isMouseOverWidget( mouseX, mouseY ) ) )
			{
				slotWidget.drawMouseHoverUnderlay();

				slotUnderMouse = slotWidget;

				hoverUnderlayRendered = true;
			}

			slotWidget.drawWidget();
		}

		// Should we get the tooltip from the slot?
		if( slotUnderMouse != null )
		{
			// Add the tooltip from the widget
			slotUnderMouse.getTooltip( this.tooltip );
		}
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked( final int mouseX, final int mouseY, final int mouseButton )
	{
		// Call super
		super.mouseClicked( mouseX, mouseY, mouseButton );

		// Loop over all widgets
		for( WidgetAspectSlot aspectSlot : this.aspectSlotList )
		{
			// Is the mouse over this widget?
			if( aspectSlot.isMouseOverWidget( mouseX, mouseY ) )
			{
				// Get the aspect of the currently held item
				Aspect itemAspect = EssentiaItemContainerHelper.INSTANCE.getFilterAspectFromItem( this.player.inventory.getItemStack() );

				// Is there an aspect?
				if( itemAspect != null )
				{
					// Are we already filtering for this aspect?
					if( this.filteredAspects.contains( itemAspect ) )
					{
						// Ignore
						return;
					}

				}

				// Inform the slot it was clicked
				aspectSlot.mouseClicked( itemAspect );

				// Stop searching
				break;
			}
		}
	}

	/**
	 * Called when a button is pressed
	 */
	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
		// Was the clicked button the redstone mode button?
		if( button.id == GuiEssentiaIO.REDSTONE_CONTROL_BUTTON_ID )
		{
			Packet_S_EssentiaIOBus.sendRedstoneModeChange( this.player, this.part );
		}
		// Void button?
		else if( button.id == GuiEssentiaIO.ALLOW_VOID_BUTTON_ID )
		{
			if( this.part instanceof PartEssentiaExportBus )
			{
				Packet_S_EssentiaIOBus.sendVoidModeChange( this.player, (PartEssentiaExportBus)this.part );
			}
		}
	}

	@Override
	public byte getConfigState()
	{
		return this.filterSize;
	}

	/**
	 * Sets the gui up.
	 */
	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		// Add the slots
		for( int row = 0; row < GuiEssentiaIO.FILTER_GRID_SIZE; row++ )
		{
			for( int column = 0; column < GuiEssentiaIO.FILTER_GRID_SIZE; column++ )
			{
				// Calculate the index
				int index = ( row * GuiEssentiaIO.FILTER_GRID_SIZE ) + column;

				// Calculate the x position
				int xPos = GuiEssentiaIO.WIDGET_X_POSITION + ( column * ThEWidget.WIDGET_SIZE );

				// Calculate the y position
				int yPos = GuiEssentiaIO.WIDGET_Y_POSITION + ( row * ThEWidget.WIDGET_SIZE );

				this.aspectSlotList.add( new WidgetAspectSlot( this, this.player, this.part, index, xPos, yPos, this,
								GuiEssentiaIO.WIDGET_CONFIG_BYTES[index] ) );
			}
		}

		// Create the redstone control button
		this.redstoneControlButton = new GuiButtonRedstoneModes( GuiEssentiaIO.REDSTONE_CONTROL_BUTTON_ID, this.guiLeft +
						GuiEssentiaIO.REDSTONE_CONTROL_BUTTON_POS_X, this.guiTop + GuiEssentiaIO.REDSTONE_CONTROL_BUTTON_POS_Y,
						GuiEssentiaIO.REDSTONE_CONTROL_BUTTON_SIZE, GuiEssentiaIO.REDSTONE_CONTROL_BUTTON_SIZE, this.redstoneMode, false );

		// Create the allow void button if export bus
		if( this.part instanceof PartEssentiaExportBus )
		{
			this.voidModeButton = new GuiButtonAllowVoid( GuiEssentiaIO.ALLOW_VOID_BUTTON_ID, this.guiLeft + GuiEssentiaIO.ALLOW_VOID_BUTTON_POS_X,
							this.guiTop + GuiEssentiaIO.ALLOW_VOID_BUTTON_POS_Y );
			this.buttonList.add( this.voidModeButton );
		}

		// Request a full update from the server
		Packet_S_EssentiaIOBus.sendFullUpdateRequest( this.player, this.part );

	}

	/**
	 * Called when the server sends a filter size update.
	 * 
	 * @param filterSize
	 */
	public void onReceiveFilterSize( final byte filterSize )
	{
		// Inform our part
		this.part.onReceiveFilterSize( filterSize );

		this.filterSize = filterSize;

		for( int i = 0; i < this.aspectSlotList.size(); i++ )
		{
			WidgetAspectSlot slot = this.aspectSlotList.get( i );

			if( !slot.canRender() )
			{
				slot.setAspect( null );
			}

		}
	}

	/**
	 * Called when the server sends if the bus is redstone controlled.
	 * 
	 * @param newRedstoneControled
	 */
	public void onReceiveRedstoneControlled( final boolean newRedstoneControled )
	{
		// Show/hide the redstone control button
		if( newRedstoneControled && !this.buttonList.contains( this.redstoneControlButton ) )
		{
			// Remove from the list
			this.buttonList.add( this.redstoneControlButton );

			// Adjust void mode button Y
			if( this.voidModeButton != null )
			{
				this.voidModeButton.yPosition += 18;
			}
		}
		else if( !newRedstoneControled && this.buttonList.contains( this.redstoneControlButton ) )
		{
			// Add to list
			this.buttonList.remove( this.redstoneControlButton );

			// Adjust void mode button Y
			if( this.voidModeButton != null )
			{
				this.voidModeButton.yPosition -= 18;
			}
		}
	}

	public void onReceiveRedstoneMode( final RedstoneMode redstoneMode )
	{
		// Set the button state
		this.redstoneControlButton.setRedstoneMode( redstoneMode );

		// Mark the mode
		this.redstoneMode = redstoneMode;
	}

	/**
	 * Called when the server sends the void mode status
	 * 
	 * @param isVoidAllowed
	 */
	public void onServerSendVoidMode( final boolean isVoidAllowed )
	{
		// Set the void mode if there is a void button
		if( this.voidModeButton != null )
		{
			this.voidModeButton.isVoidAllowed = isVoidAllowed;
		}
	}

	@Override
	public void updateAspects( final List<Aspect> aspectList )
	{
		// Inform our part
		this.part.onReceiveFilterList( aspectList );

		int count = Math.min( this.aspectSlotList.size(), aspectList.size() );

		for( int i = 0; i < count; i++ )
		{
			this.aspectSlotList.get( i ).setAspect( aspectList.get( i ), 1, false );
		}

		this.filteredAspects = aspectList;

	}

}
