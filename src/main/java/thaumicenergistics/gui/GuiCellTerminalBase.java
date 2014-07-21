package thaumicenergistics.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.ContainerCellTerminalBase;
import thaumicenergistics.gui.widget.AspectWidgetComparator;
import thaumicenergistics.gui.widget.IAspectSelectorGui;
import thaumicenergistics.gui.widget.WidgetAspectSelector;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.GuiHelper;

/**
 * Base class for cell and terminal guis
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiCellTerminalBase
	extends GuiContainer
	implements IAspectSelectorGui
{

	/**
	 * Number of widgets per row
	 */
	protected static final int WIDGETS_PER_ROW = 9;

	/**
	 * Number of rows per page
	 */
	protected static final int ROWS_PER_PAGE = 4;

	/**
	 * Number of widgets per page
	 */
	protected static final int WIDGETS_PER_PAGE = WIDGETS_PER_ROW * ROWS_PER_PAGE;

	/**
	 * Width of the gui
	 */
	private static final int GUI_SIZE_X = 176;

	/**
	 * Height of the gui
	 */
	private static final int GUI_SIZE_Y = 204;

	/**
	 * Offset from the top of the screen to draw the gui at.
	 */
	private static final int GUI_OFFSET_Y = 18;

	/**
	 * X position of the title string
	 */
	private static final int TITLE_POS_X = 9;

	/**
	 * Y position of the title string
	 */
	private static final int TITLE_POS_Y = -12;

	/**
	 * X position of the tooltips.
	 */
	private static final int TOOLTIPS_POS_X = 45;

	/**
	 * Y position of the name tooltip.
	 */
	private static final int TOOLTIP_NAME_POS_Y = 73;

	/**
	 * Y position of the amount tooltip.
	 */
	private static final int TOOLTIP_AMOUNT_POS_Y = 83;

	/**
	 * X offset to start drawing widgets
	 */
	private static final int WIDGET_OFFSET_X = 7;

	/**
	 * Y offset to start drawing widgets
	 */
	private static final int WIDGET_OFFSET_Y = -1;

	/**
	 * X offset to draw the scrollbar
	 */
	private static final int SCROLLBAR_X_OFFSET = 100;

	/**
	 * Y offset to draw the scrollbar
	 */
	private static final int SCROLLBAR_Y_OFFSET = -12;

	/**
	 * Width of the scrollbar
	 */
	private static final int SCROLLBAR_WIDTH = 69;

	/**
	 * Height of the scrollbar
	 */
	private static final int SCROLLBAR_HEIGHT = 10;
	
	/**
	 * The maximum number of displayable characters.
	 */
	private static final int SCROLLBAR_MAX_CHARS = 14;

	/**
	 * Local translation of the title.
	 */
	private final String guiTitle;

	/**
	 * Local translation of name tooltip prefix.
	 */
	private final String tooltipNamePrefix;

	/**
	 * Local translation of amount tooltip prefix.
	 */
	private final String tooltipAmountPrefix;

	/**
	 * The player viewing this gui
	 */
	protected EntityPlayer player;

	/**
	 * Scroll bar position
	 */
	protected int currentScroll = 0;

	/**
	 * Search field
	 */
	protected GuiTextField searchBar;

	/**
	 * All aspects currently in the network
	 */
	protected List<WidgetAspectSelector> aspectWidgets = new ArrayList<WidgetAspectSelector>();

	/**
	 * Aspects matching the current search term
	 */
	protected List<WidgetAspectSelector> matchingSearchWidgets = new ArrayList<WidgetAspectSelector>();

	/**
	 * What the user is searching for
	 */
	protected String searchTerm = "";

	/**
	 * The currently selected aspect
	 */
	public AspectStack selectedAspectStack;

	/**
	 * The container associated with this gui
	 */
	protected ContainerCellTerminalBase containerBase;

	/**
	 * Creates the gui.
	 * 
	 * @param player
	 * Player viewing this gui.
	 * @param container
	 * Container associated with the gui.
	 */
	public GuiCellTerminalBase( EntityPlayer player, ContainerCellTerminalBase container )
	{
		// Call super
		super( container );

		// Set the container.
		this.containerBase = ( (ContainerCellTerminalBase)this.inventorySlots );

		// Inform the container we are the gui.
		this.containerBase.setGui( this );

		// Set the player
		this.player = player;

		// Set the X size
		this.xSize = GuiCellTerminalBase.GUI_SIZE_X;

		// Set the Y size
		this.ySize = GuiCellTerminalBase.GUI_SIZE_Y;

		// Set the title
		this.guiTitle = StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".aeparts.essentia.terminal.name" );

		// Set the name prefix
		this.tooltipNamePrefix = StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".tooltip.aspect" ) + ": ";

		// Set the amount prefix
		this.tooltipAmountPrefix = StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".tooltip.amount" ) + ": ";

	}

	/**
	 * Updates the scroll position based on mouse wheel movement.
	 */
	private void updateScrollPosition()
	{
		// Get the mouse wheel movement
		int deltaMouseWheel = Mouse.getDWheel();

		if ( deltaMouseWheel < 0 )
		{
			this.currentScroll++ ;
		}
		else if ( deltaMouseWheel > 0 )
		{
			this.currentScroll-- ;
		}

		// Lower Bounds check the scrolling
		if ( this.currentScroll < 0 )
		{
			this.currentScroll = 0;
		}
		else
		{
			// Get how many rows is required for the display-able widgets
			int requiredRows = (int)Math.ceil( (double)this.matchingSearchWidgets.size() / (double)GuiCellTerminalBase.WIDGETS_PER_ROW );

			// Subtract from the required rows the starting row
			int rowsToDraw = requiredRows - this.currentScroll;

			// Calculate how many blank rows that would leave
			int blankRows = GuiCellTerminalBase.ROWS_PER_PAGE - rowsToDraw;

			// Would that scroll leave any blank rows?
			if ( blankRows > 0 )
			{
				// Subtract the blank rows from the scroll, bounding to 0
				this.currentScroll = Math.max( 0, this.currentScroll - blankRows );
			}

		}
	}

	/**
	 * Updates the matching widgets based on the current search term.
	 */
	private void updateSearch()
	{
		// Clear the matching widgets
		this.matchingSearchWidgets.clear();

		// Examine each of the possible widgets
		for( WidgetAspectSelector currentWidget : this.aspectWidgets )
		{
			// Is the search term in this aspects tag?
			if ( ( this.searchTerm == "" ) || ( currentWidget.getAspect().getTag().contains( this.searchTerm ) ) )
			{
				this.matchingSearchWidgets.add( currentWidget );
			}
		}

		// Sort the results
		Collections.sort( this.matchingSearchWidgets, new AspectWidgetComparator() );

	}

	/**
	 * Draws the GUI background image.
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( float alpha, int sizeX, int sizeY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Set the texture to the gui's texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_TERMINAL.getTexture() );

		// Draw the gui
		this.drawTexturedModalRect( this.guiLeft, this.guiTop - GuiCellTerminalBase.GUI_OFFSET_Y, 0, 0, this.xSize, this.ySize );

		// Draw the search field.
		this.searchBar.drawTextBox();
	}

	/**
	 * Draw the foreground layer.
	 */
	@Override
	protected void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.guiTitle, GuiCellTerminalBase.TITLE_POS_X, GuiCellTerminalBase.TITLE_POS_Y, 0 );

		// Draw the widgets
		this.drawWidgets( mouseX, mouseY );

		// Do we have a selected aspect?
		if ( this.selectedAspectStack != null )
		{
			// Convert the selected amount into a string
			String amountToText = Long.toString( this.selectedAspectStack.amount );

			// Get the name of the aspect
			String aspectName = this.selectedAspectStack.aspect.getName();

			// Draw the name
			this.fontRendererObj.drawString( this.tooltipNamePrefix + aspectName, GuiCellTerminalBase.TOOLTIPS_POS_X,
				GuiCellTerminalBase.TOOLTIP_NAME_POS_Y, 0 );

			// Draw the amount
			this.fontRendererObj.drawString( this.tooltipAmountPrefix + amountToText, GuiCellTerminalBase.TOOLTIPS_POS_X,
				GuiCellTerminalBase.TOOLTIP_AMOUNT_POS_Y, 0 );
		}
	}

	/**
	 * Called when the player types a key.
	 */
	@Override
	protected void keyTyped( char key, int keyID )
	{
		// Pass the key to the search field.
		this.searchBar.textboxKeyTyped( key, keyID );

		// Did they press the escape key?
		if ( keyID == Keyboard.KEY_ESCAPE )
		{
			// Slot the screen.
			this.mc.thePlayer.closeScreen();
		}
		else
		{
			// Get the search term
			this.searchTerm = this.searchBar.getText().trim().toLowerCase();

			// Re-search the widgets
			this.updateSearch();
		}

	}

	/**
	 * Called when the player clicks the mouse.
	 */
	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseBtn )
	{
		// Pass to super.
		super.mouseClicked( mouseX, mouseY, mouseBtn );

		// Pass to search field.
		this.searchBar.mouseClicked( mouseX, mouseY, mouseBtn );

		// Get the number of widgets that match the current search.
		int listSize = this.matchingSearchWidgets.size();

		int index = 0;

		// Rows
		for( int y = 0; y < GuiCellTerminalBase.ROWS_PER_PAGE; y++ )
		{
			// Columns
			for( int x = 0; x < GuiCellTerminalBase.WIDGETS_PER_ROW; x++ )
			{
				// Calculate the index
				index = ( ( y + this.currentScroll ) * GuiCellTerminalBase.WIDGETS_PER_ROW ) + x;

				// Is the index in bounds?
				if ( index < listSize )
				{
					// Get the widget at this index
					WidgetAspectSelector widget = this.matchingSearchWidgets.get( index );

					// Is the mouse over this widget?
					if ( GuiHelper.isPointInGuiRegion( ( x * WidgetAspectSelector.WIDGET_WIDTH ) + GuiCellTerminalBase.WIDGET_OFFSET_X,
						( y * WidgetAspectSelector.WIDGET_WIDTH ) + GuiCellTerminalBase.WIDGET_OFFSET_Y, WidgetAspectSelector.WIDGET_HEIGHT,
						WidgetAspectSelector.WIDGET_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
					{
						// Play clicky sound
						Minecraft.getMinecraft().getSoundHandler()
										.playSound( PositionedSoundRecord.func_147674_a( new ResourceLocation( "gui.button.press" ), 1.0F ) );

						// Send the click to the widget ( args ignored for WidgetAspectSelector )
						widget.mouseClicked( 0, 0, 0, 0 );

						// Stop searching
						return;
					}
				}
				else
				{
					// Stop searching
					return;
				}
			}
		}

	}

	/**
	 * Draw the widgets
	 * 
	 * @param mouseX
	 * @param mouseY
	 */
	public void drawWidgets( int mouseX, int mouseY )
	{
		// Anything to draw?
		if ( !this.matchingSearchWidgets.isEmpty() )
		{
			// Get the scroll position
			this.updateScrollPosition();

			// Calculate the starting index
			int startingIndex = this.currentScroll * GuiCellTerminalBase.WIDGETS_PER_ROW;

			// Calculate the ending index
			int endingIndex = Math.min( this.matchingSearchWidgets.size(), startingIndex + WIDGETS_PER_PAGE );

			// Set the starting positions
			int widgetPosX = GuiCellTerminalBase.WIDGET_OFFSET_X;
			int widgetPosY = GuiCellTerminalBase.WIDGET_OFFSET_Y;
			int widgetColumnPosition = 1;

			// Holder for the widget under the mouse
			WidgetAspectSelector widgetUnderMouse = null;
			int widgetUnderMousePosX = 0;
			int widgetUnderMousePosY = 0;

			for( int index = startingIndex; index < endingIndex; index++ )
			{
				// Draw the widget
				this.matchingSearchWidgets.get( index ).drawWidget( widgetPosX, widgetPosY );

				// Is the mouse over this widget?
				if ( ( widgetUnderMouse == null ) &&
								GuiHelper.isPointInGuiRegion( widgetPosX, widgetPosY, WidgetAspectSelector.WIDGET_HEIGHT,
									WidgetAspectSelector.WIDGET_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
				{
					// Set the widget and its position
					widgetUnderMouse = this.matchingSearchWidgets.get( index );
					widgetUnderMousePosX = widgetPosX;
					widgetUnderMousePosY = widgetPosY;
				}

				// Increment the column position
				widgetColumnPosition++ ;

				// Are we done with this row?
				if ( widgetColumnPosition > GuiCellTerminalBase.WIDGETS_PER_ROW )
				{
					// Reset X
					widgetPosX = GuiCellTerminalBase.WIDGET_OFFSET_X;

					// Reset column position to 1
					widgetColumnPosition = 1;

					// Increment y
					widgetPosY += WidgetAspectSelector.WIDGET_HEIGHT;
				}
				else
				{
					// Increment the x position
					widgetPosX += WidgetAspectSelector.WIDGET_WIDTH;
				}
			}

			// Was the mouse over a widget?
			if ( widgetUnderMouse != null )
			{
				// Have the widget draw its tooltip
				widgetUnderMouse.drawTooltip( widgetUnderMousePosX, widgetUnderMousePosY, mouseX, mouseY );
			}
		}
		else
		{
			this.currentScroll = 0;
		}
	}

	/**
	 * Gets the container associated with the gui
	 */
	@Override
	public IAspectSelectorContainer getContainer()
	{
		return this.containerBase;
	}

	/**
	 * Gets the currently selected aspect.
	 */
	@Override
	public AspectStack getSelectedAspect()
	{
		return this.selectedAspectStack;
	}

	/**
	 * Gets the starting X position for the Gui.
	 */
	@Override
	public int guiLeft()
	{
		return this.guiLeft;
	}

	/**
	 * Gets the starting Y position for the Gui.
	 */
	@Override
	public int guiTop()
	{
		return this.guiTop;
	}

	/**
	 * Sets the gui up.
	 */
	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		// Reset the mouse wheel state.
		Mouse.getDWheel();

		// Get the aspect list
		this.updateAspects();

		// Set up the search bar
		this.searchBar = new GuiTextField( this.fontRendererObj, this.guiLeft + GuiCellTerminalBase.SCROLLBAR_X_OFFSET, this.guiTop +
						GuiCellTerminalBase.SCROLLBAR_Y_OFFSET, GuiCellTerminalBase.SCROLLBAR_WIDTH, GuiCellTerminalBase.SCROLLBAR_HEIGHT );

		// Set the searchbar to draw in the foreground
		this.searchBar.setEnableBackgroundDrawing( false );

		// Start focused
		this.searchBar.setFocused( true );

		// Set maximum length
		this.searchBar.setMaxStringLength( GuiCellTerminalBase.SCROLLBAR_MAX_CHARS );

	}

	/**
	 * Refreshes our aspect list to match that of the container.
	 */
	public void updateAspects()
	{
		// Create a new list
		this.aspectWidgets = new ArrayList<WidgetAspectSelector>();

		// Make a widget for every aspect
		for( AspectStack aspectStack : this.containerBase.getAspectStackList() )
		{
			// Create the widget
			this.aspectWidgets.add( new WidgetAspectSelector( this, aspectStack ) );
		}

		// Update the search results
		this.updateSearch();

		// Update the selected aspect
		this.updateSelectedAspect();
	}

	/**
	 * Refreshes our selected aspect to match that of the container.
	 */
	public void updateSelectedAspect()
	{
		// No selection by default
		this.selectedAspectStack = null;

		// Check all aspects
		for( AspectStack aspectStack : this.containerBase.getAspectStackList() )
		{
			// Does this match?
			if ( aspectStack.aspect == this.containerBase.getSelectedAspect() )
			{
				// Set our selection
				this.selectedAspectStack = aspectStack;

				// Done
				return;
			}
		}

	}

}
