package thaumicenergistics.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.container.*;
import thaumicenergistics.gui.abstraction.AbstractGuiWithScrollbar;
import thaumicenergistics.gui.buttons.GuiButtonSortingMode;
import thaumicenergistics.gui.widget.AbstractWidget;
import thaumicenergistics.gui.widget.IAspectSelectorGui;
import thaumicenergistics.gui.widget.WidgetAspectSelector;
import thaumicenergistics.gui.widget.WidgetAspectSelectorComparator;
import thaumicenergistics.network.packet.server.Packet_S_EssentiaCellTerminal;
import thaumicenergistics.parts.AEPartEssentiaTerminal;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.GuiHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Essentia terminal, wireless terminal, and cell(ME Chest) Gui.
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiEssentiaCellTerminal
	extends AbstractGuiWithScrollbar
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
	private static final int GUI_SIZE_X = 195;

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
	private static final int TITLE_POS_X = 7;

	/**
	 * Y position of the title string
	 */
	private static final int TITLE_POS_Y = -12;

	/**
	 * X position of the selected aspect info.
	 */
	private static final int SELECTED_INFO_POS_X = 45;

	/**
	 * Y position of the name selected aspect info.
	 */
	private static final int SELECTED_INFO_NAME_POS_Y = 73;

	/**
	 * Y position of the amount selected aspect info.
	 */
	private static final int SELECTED_INFO_AMOUNT_POS_Y = 83;

	/**
	 * X offset to start drawing widgets
	 */
	private static final int WIDGET_OFFSET_X = 7;

	/**
	 * Y offset to start drawing widgets
	 */
	private static final int WIDGET_OFFSET_Y = -1;

	/**
	 * X offset to draw the search field
	 */
	private static final int SEARCH_X_OFFSET = 98;

	/**
	 * Y offset to draw the search field
	 */
	private static final int SEARCH_Y_OFFSET = -12;

	/**
	 * Width of the search field
	 */
	private static final int SEARCH_WIDTH = 69;

	/**
	 * Height of the search field
	 */
	private static final int SEARCH_HEIGHT = 10;

	/**
	 * The maximum number of display-able characters.
	 */
	private static final int SEARCH_MAX_CHARS = 14;

	/**
	 * X position of the sorting mode button.
	 */
	private static final int SORT_MODE_BUTTON_POS_X = -18;

	/**
	 * Y position of the sorting mode button.
	 */
	private static final int SORT_MODE_BUTTON_POS_Y = -15;

	/**
	 * Width and height of the sorting mode button.
	 */
	private static final int SORT_MODE_BUTTON_SIZE = 16;

	/**
	 * ID of the sort mode button.
	 */
	private static final int SORT_MODE_BUTTON_ID = 0;

	/**
	 * X position of the scroll bar
	 */
	private static final int SCROLLBAR_POS_X = 175;

	/**
	 * Y position of the scroll bar
	 */
	private static final int SCROLLBAR_POS_Y = 0;

	/**
	 * Height of the scroll bar
	 */
	private static final int SCROLLBAR_HEIGHT = 70;

	/**
	 * Local translation of the title.
	 */
	private final String guiTitle;

	/**
	 * Local translation of name prefix for selected aspect info.
	 */
	private final String selectedInfoNamePrefix;

	/**
	 * Local translation of amount prefix for selected aspect info.
	 */
	private final String selectedInfoAmountPrefix;

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
	 * The container associated with this gui
	 */
	protected AbstractContainerCellTerminalBase baseContainer;

	/**
	 * Mode used to sort the aspects
	 */
	protected ComparatorMode sortMode = ComparatorMode.MODE_ALPHABETIC;

	/**
	 * The cached amount of the selected stack.
	 */
	private long cacheAmountSelected = -1;

	/**
	 * The cached display string of the selected stack.
	 */
	private String cacheAmountDisplay = "0";

	/**
	 * Tracks how many rows of the grid can not be displayed without scrolling.
	 */
	private int previousOverflowRows = 0;

	/**
	 * The currently selected aspect
	 */
	public AspectStack selectedAspectStack;

	/**
	 * Creates the gui.
	 * 
	 * @param player
	 * Player viewing this gui.
	 * @param container
	 * Container associated with the gui.
	 */
	private GuiEssentiaCellTerminal( final EntityPlayer player, final AbstractContainerCellTerminalBase container, final String title )
	{
		// Call super
		super( container );

		// Set the container.
		this.baseContainer = container;

		// Set the player
		this.player = player;

		// Set the X size
		this.xSize = GuiEssentiaCellTerminal.GUI_SIZE_X;

		// Set the Y size
		this.ySize = GuiEssentiaCellTerminal.GUI_SIZE_Y;

		// Set the title
		this.guiTitle = title;

		// Set the name prefix
		this.selectedInfoNamePrefix = ThEStrings.Gui_SelectedAspect.getLocalized() + ": ";

		// Set the amount prefix
		this.selectedInfoAmountPrefix = ThEStrings.Gui_SelectedAmount.getLocalized() + ": ";

	}

	/**
	 * Creates the GUI for an essentia cell inside an ME chest.
	 * 
	 * @param player
	 * Player viewing the gui.
	 * @param world
	 * World the chest is in.
	 * @param x
	 * X position of the chest.
	 * @param y
	 * Y position of the chest.
	 * @param z
	 * Z position of the chest.
	 * @return
	 */
	public static GuiEssentiaCellTerminal NewEssentiaCellGui( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		return new GuiEssentiaCellTerminal( player, new ContainerEssentiaCell( player, world, x, y, z ),
						ThEStrings.Gui_TitleEssentiaCell.getLocalized() );
	}

	/**
	 * Creates the GUI for an essentia terminal.
	 * 
	 * @param terminal
	 * @param player
	 * @return
	 */
	public static GuiEssentiaCellTerminal NewEssentiaTerminalGui( final AEPartEssentiaTerminal terminal, final EntityPlayer player )
	{
		return new GuiEssentiaCellTerminal( player, new ContainerEssentiaTerminal( terminal, player ),
						ThEStrings.Part_EssentiaTerminal.getLocalized() );
	}

	/**
	 * Creates the GUI for a wireless essentia terminal.
	 * 
	 * @param player
	 * @return
	 */
	public static GuiEssentiaCellTerminal NewWirelessEssentiaTerminalGui( final EntityPlayer player )
	{
		return new GuiEssentiaCellTerminal( player, new ContainerWirelessEssentiaTerminal( player, null ),
						ThEStrings.Part_EssentiaTerminal.getLocalized() );
	}

	/**
	 * Sorts the list of matching widgets based on the current
	 * sorting mode.
	 */
	private void sortMatchingList()
	{
		// Sort the results
		Collections.sort( this.matchingSearchWidgets, new WidgetAspectSelectorComparator( this.sortMode ) );
	}

	/**
	 * Updates the scroll bar's range.
	 */
	private void updateScrollMaximum()
	{
		// Calculate the number of widgets the will overflow
		double overflowWidgets = Math.max( 0, this.matchingSearchWidgets.size() - GuiEssentiaCellTerminal.WIDGETS_PER_PAGE );

		// Calculate how many rows will overflow
		int overflowRows = (int)Math.ceil( overflowWidgets / GuiEssentiaCellTerminal.WIDGETS_PER_ROW );

		// Update if the range has changed
		if( overflowRows != this.previousOverflowRows )
		{
			// Update the scroll bar
			this.scrollBar.setRange( 0, overflowRows, 1 );
			this.onScrollbarMoved();
			this.previousOverflowRows = overflowRows;
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
			if( ( this.searchTerm == "" ) || ( currentWidget.getAspect().getTag().contains( this.searchTerm ) ) )
			{
				this.matchingSearchWidgets.add( currentWidget );
			}
		}

		// Sort
		this.sortMatchingList();

		// Update scrollbar
		this.updateScrollMaximum();

	}

	/**
	 * Draws the GUI background image.
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int sizeX, final int sizeY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Set the texture to the gui's texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_TERMINAL.getTexture() );

		// Draw the gui
		this.drawTexturedModalRect( this.guiLeft, this.guiTop - GuiEssentiaCellTerminal.GUI_OFFSET_Y, 0, 0, this.xSize, this.ySize );

		// Draw the search field.
		this.searchBar.drawTextBox();
	}

	/**
	 * Draw the foreground layer.
	 */
	@Override
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Call super
		super.drawGuiContainerForegroundLayer( mouseX, mouseY );

		// Draw the title
		this.fontRendererObj.drawString( this.guiTitle, GuiEssentiaCellTerminal.TITLE_POS_X, GuiEssentiaCellTerminal.TITLE_POS_Y, 0 );

		// Draw the widgets
		this.drawWidgets( mouseX, mouseY );

		// Do we have a selected aspect?
		if( ( this.selectedAspectStack != null ) && ( this.selectedAspectStack.stackSize > 0 ) )
		{
			// Update the display amount?
			if( this.selectedAspectStack.stackSize != this.cacheAmountSelected )
			{
				// Convert the selected amount into a string
				this.cacheAmountDisplay = GuiHelper.shortenCount( this.selectedAspectStack.stackSize );

				// Cache the amount
				this.cacheAmountSelected = this.selectedAspectStack.stackSize;
			}

			// Get the name of the aspect
			String aspectName = this.selectedAspectStack.getAspectName( this.player );

			// Draw the name
			this.fontRendererObj.drawString( this.selectedInfoNamePrefix + aspectName, GuiEssentiaCellTerminal.SELECTED_INFO_POS_X,
				GuiEssentiaCellTerminal.SELECTED_INFO_NAME_POS_Y, 0 );

			// Draw the amount
			this.fontRendererObj.drawString( this.selectedInfoAmountPrefix + this.cacheAmountDisplay, GuiEssentiaCellTerminal.SELECTED_INFO_POS_X,
				GuiEssentiaCellTerminal.SELECTED_INFO_AMOUNT_POS_Y, 0 );
		}
	}

	@Override
	protected ScrollbarParams getScrollbarParameters()
	{
		return new ScrollbarParams( GuiEssentiaCellTerminal.SCROLLBAR_POS_X, GuiEssentiaCellTerminal.SCROLLBAR_POS_Y,
						GuiEssentiaCellTerminal.SCROLLBAR_HEIGHT );
	}

	/**
	 * Called when the player types a key.
	 */
	@Override
	protected void keyTyped( final char key, final int keyID )
	{
		// Pass the key to the search field.
		this.searchBar.textboxKeyTyped( key, keyID );

		// Did they press the escape key?
		if( keyID == Keyboard.KEY_ESCAPE )
		{
			// Slot the screen.
			this.mc.thePlayer.closeScreen();
		}
		else if( this.searchBar.isFocused() )
		{
			// Get the search term
			this.searchTerm = this.searchBar.getText().trim().toLowerCase();

			// Re-search the widgets
			this.updateSearch();
		}
		// Disable numeric key hotbar swaping.
		else if( !Character.isDigit( key ) )
		{
			super.keyTyped( key, keyID );
		}

	}

	/**
	 * Called when the player clicks the mouse.
	 */
	@Override
	protected void mouseClicked( final int mouseX, final int mouseY, final int mouseBtn )
	{
		// Get the number of widgets that match the current search.
		int listSize = this.matchingSearchWidgets.size();

		int index = 0;

		// Is the mouse in the widget area?
		if( GuiHelper.INSTANCE.isPointInGuiRegion(
			GuiEssentiaCellTerminal.WIDGET_OFFSET_Y,
			GuiEssentiaCellTerminal.WIDGET_OFFSET_X,
			GuiEssentiaCellTerminal.ROWS_PER_PAGE * AbstractWidget.WIDGET_SIZE,
			GuiEssentiaCellTerminal.WIDGETS_PER_ROW * AbstractWidget.WIDGET_SIZE,
			mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Rows
			for( int y = 0; y < GuiEssentiaCellTerminal.ROWS_PER_PAGE; y++ )
			{
				// Columns
				for( int x = 0; x < GuiEssentiaCellTerminal.WIDGETS_PER_ROW; x++ )
				{
					// Calculate the index
					index = ( ( y + this.currentScroll ) * GuiEssentiaCellTerminal.WIDGETS_PER_ROW ) + x;

					// Is the index in bounds?
					if( index < listSize )
					{
						// Get the widget at this index
						WidgetAspectSelector currentWidget = this.matchingSearchWidgets.get( index );

						// Is the mouse over this widget?
						if( currentWidget.isMouseOverWidget( mouseX, mouseY ) )
						{
							// Play clicky sound
							Minecraft.getMinecraft().getSoundHandler().playSound(
								PositionedSoundRecord.func_147674_a( new ResourceLocation( "gui.button.press" ), 1.0F ) );

							// Send the click to the widget
							currentWidget.mouseClicked();

							// Stop searching and do not pass to super
							return;
						}
					}
					else
					{
						// Unselect
						this.baseContainer.setSelectedAspect( null );

						// Stop searching
						y = GuiEssentiaCellTerminal.ROWS_PER_PAGE;
						break;
					}
				}
			}
		}

		// Was the mouse right-clicked over the search field?
		if( ( mouseBtn == GuiHelper.MOUSE_BUTTON_RIGHT ) &&
						GuiHelper.INSTANCE.isPointInGuiRegion( GuiEssentiaCellTerminal.SEARCH_Y_OFFSET, GuiEssentiaCellTerminal.SEARCH_X_OFFSET,
							GuiEssentiaCellTerminal.SEARCH_HEIGHT, GuiEssentiaCellTerminal.SEARCH_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Clear the search text
			this.searchTerm = "";
			this.searchBar.setText( this.searchTerm );

			// Update the widgets
			this.updateSearch();

			// Do not pass to super
			return;
		}

		// Pass to search field.
		this.searchBar.mouseClicked( mouseX, mouseY, mouseBtn );

		// Call super
		super.mouseClicked( mouseX, mouseY, mouseBtn );

	}

	/**
	 * Called when a button is clicked.
	 */
	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
		// Is the button the sort mode button?
		if( button.id == GuiEssentiaCellTerminal.SORT_MODE_BUTTON_ID )
		{
			// Request update from server
			Packet_S_EssentiaCellTerminal.sendSortMode( this.player,
				( this.sortMode == ComparatorMode.MODE_ALPHABETIC ? ComparatorMode.MODE_AMOUNT : ComparatorMode.MODE_ALPHABETIC ) );
		}
	}

	@Override
	protected void onScrollbarMoved()
	{
		this.currentScroll = this.scrollBar.getCurrentScroll();
	}

	/**
	 * Draw the widgets
	 * 
	 * @param mouseX
	 * @param mouseY
	 */
	public void drawWidgets( final int mouseX, final int mouseY )
	{
		// Anything to draw?
		if( !this.matchingSearchWidgets.isEmpty() )
		{
			// Calculate the starting index
			int startingIndex = this.currentScroll * GuiEssentiaCellTerminal.WIDGETS_PER_ROW;

			// Calculate the ending index
			int endingIndex = Math.min( this.matchingSearchWidgets.size(), startingIndex + WIDGETS_PER_PAGE );

			// Set the starting positions
			int widgetPosX = GuiEssentiaCellTerminal.WIDGET_OFFSET_X;
			int widgetPosY = GuiEssentiaCellTerminal.WIDGET_OFFSET_Y;
			int widgetColumnPosition = 1;

			// Holder for the widget under the mouse
			WidgetAspectSelector widgetUnderMouse = null;

			for( int index = startingIndex; index < endingIndex; index++ )
			{
				// Get the widget
				WidgetAspectSelector currentWidget = this.matchingSearchWidgets.get( index );

				// Set the position
				currentWidget.setPosition( widgetPosX, widgetPosY );

				// Draw the widget
				currentWidget.drawWidget();

				// Is the mouse over this widget?
				if( currentWidget.isMouseOverWidget( mouseX, mouseY ) )
				{
					// Set the widget
					widgetUnderMouse = this.matchingSearchWidgets.get( index );
				}

				// Increment the column position
				widgetColumnPosition++ ;

				// Are we done with this row?
				if( widgetColumnPosition > GuiEssentiaCellTerminal.WIDGETS_PER_ROW )
				{
					// Reset X
					widgetPosX = GuiEssentiaCellTerminal.WIDGET_OFFSET_X;

					// Reset column position to 1
					widgetColumnPosition = 1;

					// Increment y
					widgetPosY += AbstractWidget.WIDGET_SIZE;
				}
				else
				{
					// Increment the x position
					widgetPosX += AbstractWidget.WIDGET_SIZE;
				}
			}

			// Was the mouse over a widget?
			if( widgetUnderMouse != null )
			{
				// Get the tooltip from the widget
				widgetUnderMouse.getTooltip( this.tooltip );
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
		return this.baseContainer;
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
	 * If the mouse wheel moves, updates the scrollbar
	 */
	@Override
	public void handleMouseInput()
	{
		// Call super
		super.handleMouseInput();

		// Get the delta z for the scroll wheel
		int deltaZ = Mouse.getEventDWheel();

		// Did the wheel move?
		if( deltaZ != 0 )
		{
			// Inform the scroll bar
			this.scrollBar.wheel( deltaZ );
			this.onScrollbarMoved();
		}
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
		this.searchBar = new GuiTextField( this.fontRendererObj, this.guiLeft + GuiEssentiaCellTerminal.SEARCH_X_OFFSET, this.guiTop +
						GuiEssentiaCellTerminal.SEARCH_Y_OFFSET, GuiEssentiaCellTerminal.SEARCH_WIDTH, GuiEssentiaCellTerminal.SEARCH_HEIGHT );

		// Set the search bar to draw in the foreground
		this.searchBar.setEnableBackgroundDrawing( false );

		// Start focused
		this.searchBar.setFocused( false );

		// Set maximum length
		this.searchBar.setMaxStringLength( GuiEssentiaCellTerminal.SEARCH_MAX_CHARS );

		// Clear any existing buttons
		this.buttonList.clear();

		// Add the sort mode button
		this.buttonList.add( new GuiButtonSortingMode( GuiEssentiaCellTerminal.SORT_MODE_BUTTON_ID, this.guiLeft +
						GuiEssentiaCellTerminal.SORT_MODE_BUTTON_POS_X, this.guiTop + GuiEssentiaCellTerminal.SORT_MODE_BUTTON_POS_Y,
						GuiEssentiaCellTerminal.SORT_MODE_BUTTON_SIZE, GuiEssentiaCellTerminal.SORT_MODE_BUTTON_SIZE ) );
	}

	/**
	 * Called when the server sends a full list of network aspects.
	 * 
	 * @param aspectStackList
	 */
	public void onReceiveAspectList( final List<AspectStack> aspectStackList )
	{
		// Update the container
		this.baseContainer.onReceivedAspectList( aspectStackList );

		// Update the gui
		this.updateAspects();
	}

	/**
	 * Called when an aspect in the list changes amount.
	 * 
	 * @param change
	 */
	public void onReceiveAspectListChange( final AspectStack change )
	{
		// Update the container
		if( this.baseContainer.onReceivedAspectListChange( change ) )
		{
			// Update the gui
			this.updateAspects();
		}
	}

	/**
	 * Called when the server sends a change to the selected aspect.
	 * 
	 * @param selectedAspect
	 */
	public void onReceiveSelectedAspect( final Aspect selectedAspect )
	{
		// Update the container
		this.baseContainer.onReceivedSelectedAspect( selectedAspect );

		// Update the gui
		this.updateSelectedAspect();
	}

	/**
	 * Called when the server sends a change in the sorting mode.
	 * 
	 * @param sortMode
	 */
	public void onSortModeChanged( final ComparatorMode sortMode )
	{
		// Set the sort mode
		this.sortMode = sortMode;

		// Update the sort button
		( (GuiButtonSortingMode)this.buttonList.get( GuiEssentiaCellTerminal.SORT_MODE_BUTTON_ID ) ).setSortMode( sortMode );

		// Resort the list
		this.sortMatchingList();
	}

	/**
	 * Refreshes our aspect list to match that of the container.
	 */
	public void updateAspects()
	{
		// Create a new list
		this.aspectWidgets = new ArrayList<WidgetAspectSelector>();

		// Make a widget for every aspect
		for( AspectStack aspectStack : this.baseContainer.getAspectStackList() )
		{
			// Create the widget
			this.aspectWidgets.add( new WidgetAspectSelector( this, aspectStack, 0, 0, this.player ) );
		}

		// Update the scrollbar
		this.updateScrollMaximum();

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
		for( AspectStack aspectStack : this.baseContainer.getAspectStackList() )
		{
			// Does this match?
			if( aspectStack.aspect == this.baseContainer.getSelectedAspect() )
			{
				// Set our selection
				this.selectedAspectStack = aspectStack;

				// Done
				return;
			}
		}

	}

}
