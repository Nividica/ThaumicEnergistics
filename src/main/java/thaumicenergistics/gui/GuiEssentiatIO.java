package thaumicenergistics.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaIOBus;
import thaumicenergistics.gui.abstraction.AbstractGuiWidgetHost;
import thaumicenergistics.gui.buttons.ButtonRedstoneModes;
import thaumicenergistics.gui.widget.AbstractWidget;
import thaumicenergistics.gui.widget.WidgetAspectSlot;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.network.IAspectSlotGui;
import thaumicenergistics.network.packet.server.PacketServerEssentiaIOBus;
import thaumicenergistics.parts.AEPartEssentiaExportBus;
import thaumicenergistics.parts.AEPartEssentiaIO;
import thaumicenergistics.parts.AEPartEssentiaImportBus;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.GuiTextureManager;
import appeng.api.config.RedstoneMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEssentiatIO
	extends AbstractGuiWidgetHost
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
	 * X position of the redstone control button.
	 */
	private static final int REDSTONE_CONTROL_BUTTON_POS_X = -18;

	/**
	 * Y position of the redstone control button.
	 */
	private static final int REDSTONE_CONTROL_BUTTON_POS_Y = 2;

	/**
	 * Width and height of the redstone control button.
	 */
	private static final int REDSTONE_CONTROL_BUTTON_SIZE = 16;

	/**
	 * ID of the redstone control button.
	 */
	private static final int REDSTONE_CONTROL_BUTTON_ID = 0;

	/**
	 * X position of the title string.
	 */
	private static final int TITLE_POS_X = 6;

	/**
	 * Y position of the title string.
	 */
	private static final int TITLE_POS_Y = 5;

	/**
	 * The part associated with this gui.
	 */
	private AEPartEssentiaIO part;

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
	 * True if this is redstone controlled
	 */
	private boolean redstoneControlled;

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
	 * Creates the gui
	 * 
	 * @param partBus
	 * @param player
	 */
	public GuiEssentiatIO( AEPartEssentiaIO partBus, EntityPlayer player )
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
		this.xSize = ( this.hasNetworkTool ? GuiEssentiatIO.GUI_WIDTH_WITH_TOOL : GuiEssentiatIO.GUI_WIDTH_NO_TOOL );

		// Set the height
		this.ySize = GuiEssentiatIO.GUI_HEIGHT;

		// Set the title
		if( partBus instanceof AEPartEssentiaImportBus )
		{
			// Import
			this.guiTitle = AEPartsEnum.EssentiaImportBus.getStatName();
		}
		else if( partBus instanceof AEPartEssentiaExportBus )
		{
			// Export
			this.guiTitle = AEPartsEnum.EssentiaExportBus.getStatName();
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
	protected void drawGuiContainerBackgroundLayer( float alpha, int mouseX, int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Get the GUI texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_IO_BUS.getTexture() );

		// Does the user have a network tool?
		if( this.hasNetworkTool )
		{
			// Draw the full gui
			this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, GuiEssentiatIO.GUI_WIDTH_WITH_TOOL, GuiEssentiatIO.GUI_HEIGHT );
		}
		else
		{
			// Draw main gui
			this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, GuiEssentiatIO.GUI_MAIN_WIDTH, GuiEssentiatIO.GUI_HEIGHT );

			// Draw upgrade slots
			this.drawTexturedModalRect( this.guiLeft + GuiEssentiatIO.GUI_MAIN_WIDTH, this.guiTop, GuiEssentiatIO.GUI_MAIN_WIDTH, 0,
				GuiEssentiatIO.GUI_UPGRADES_WIDTH, GuiEssentiatIO.GUI_UPGRADES_HEIGHT );
		}

		// Call super
		super.drawGuiContainerBackgroundLayer( alpha, mouseX, mouseY );
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseButton )
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
				Aspect itemAspect = EssentiaItemContainerHelper.instance.getAspectInContainer( this.player.inventory.getItemStack() );

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
	public void actionPerformed( GuiButton button )
	{
		// Call super
		super.actionPerformed( button );

		// Was the clicked button the redstone mode button?
		if( button.id == GuiEssentiatIO.REDSTONE_CONTROL_BUTTON_ID )
		{
			new PacketServerEssentiaIOBus().createRequestChangeRedstoneMode( this.player, this.part ).sendPacketToServer();
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
	{
		// Call super
		super.drawGuiContainerForegroundLayer( mouseX, mouseY );

		// Draw the title
		this.fontRendererObj.drawString( this.guiTitle, GuiEssentiatIO.TITLE_POS_X, GuiEssentiatIO.TITLE_POS_Y, 0x000000 );

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
		else
		{
			// Add the tooltip from the buttons
			this.addTooltipFromButtons( mouseX, mouseY );
		}

		// Draw the tooltip
		this.drawTooltip( mouseX - this.guiLeft, mouseY - this.guiTop );
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
		for( int row = 0; row < GuiEssentiatIO.FILTER_GRID_SIZE; row++ )
		{
			for( int column = 0; column < GuiEssentiatIO.FILTER_GRID_SIZE; column++ )
			{
				// Calculate the index
				int index = ( row * GuiEssentiatIO.FILTER_GRID_SIZE ) + column;

				// Calculate the x position
				int xPos = GuiEssentiatIO.WIDGET_X_POSITION + ( column * AbstractWidget.WIDGET_SIZE );

				// Calculate the y position
				int yPos = GuiEssentiatIO.WIDGET_Y_POSITION + ( row * AbstractWidget.WIDGET_SIZE );

				this.aspectSlotList.add( new WidgetAspectSlot( this, this.player, this.part, index, xPos, yPos, this,
								GuiEssentiatIO.WIDGET_CONFIG_BYTES[index] ) );
			}
		}

		// Request a full update from the server
		new PacketServerEssentiaIOBus().createRequestFullUpdate( this.player, this.part ).sendPacketToServer();

	}

	/**
	 * Called when the server sends a filter size update.
	 * 
	 * @param filterSize
	 */
	public void onReceiveFilterSize( byte filterSize )
	{
		// Inform our part
		this.part.receiveFilterSize( filterSize );

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
	public void onReceiveRedstoneControlled( boolean newRedstoneControled )
	{
		// Do we differ?
		if( this.redstoneControlled != newRedstoneControled )
		{
			// Were we previously controlled?
			if( this.redstoneControlled )
			{
				// Clear the button list
				this.buttonList.clear();
			}
			else
			{
				// Create the redstone button
				this.buttonList.add( new ButtonRedstoneModes( GuiEssentiatIO.REDSTONE_CONTROL_BUTTON_ID, this.guiLeft +
								GuiEssentiatIO.REDSTONE_CONTROL_BUTTON_POS_X, this.guiTop + GuiEssentiatIO.REDSTONE_CONTROL_BUTTON_POS_Y,
								GuiEssentiatIO.REDSTONE_CONTROL_BUTTON_SIZE, GuiEssentiatIO.REDSTONE_CONTROL_BUTTON_SIZE, this.redstoneMode, false ) );
			}

			// Set redstone controlled
			this.redstoneControlled = newRedstoneControled;
		}
	}

	public void onReceiveRedstoneMode( RedstoneMode redstoneMode )
	{
		// Are we redstone controlled, and have the redstone mod button
		if( this.redstoneControlled && ( this.buttonList.size() > 0 ) )
		{
			( (ButtonRedstoneModes)this.buttonList.get( GuiEssentiatIO.REDSTONE_CONTROL_BUTTON_ID ) ).setRedstoneMode( redstoneMode );
		}

		// Mark the mode
		this.redstoneMode = redstoneMode;
	}

	@Override
	public void updateAspects( List<Aspect> aspectList )
	{
		// Inform our part
		this.part.receiveFilterList( aspectList );

		int count = Math.min( this.aspectSlotList.size(), aspectList.size() );

		for( int i = 0; i < count; i++ )
		{
			this.aspectSlotList.get( i ).setAspect( aspectList.get( i ) );
		}

		this.filteredAspects = aspectList;

	}

}
