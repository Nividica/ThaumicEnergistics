package thaumicenergistics.client.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.IThEWirelessEssentiaTerminal;
import thaumicenergistics.api.gui.IAspectSelectorContainer;
import thaumicenergistics.api.gui.IAspectSelectorGui;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.client.gui.abstraction.GuiConstants_ECT;
import thaumicenergistics.client.gui.buttons.GuiButtonSortingMode;
import thaumicenergistics.client.gui.buttons.GuiButtonViewType;
import thaumicenergistics.client.gui.widget.ThEWidget;
import thaumicenergistics.client.gui.widget.WidgetAspectSelector;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.container.ContainerEssentiaCell;
import thaumicenergistics.common.container.ContainerEssentiaCellTerminalBase;
import thaumicenergistics.common.container.ContainerEssentiaTerminal;
import thaumicenergistics.common.container.ContainerWirelessEssentiaTerminal;
import thaumicenergistics.common.inventory.HandlerWirelessEssentiaTerminal;
import thaumicenergistics.common.items.ItemEssentiaCell;
import thaumicenergistics.common.items.ItemWirelessEssentiaTerminal;
import thaumicenergistics.common.network.packet.server.Packet_S_EssentiaCellTerminal;
import thaumicenergistics.common.parts.PartEssentiaTerminal;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.storage.AspectStackComparator;
import thaumicenergistics.common.storage.AspectStackComparator.AspectStackComparatorMode;
import appeng.api.config.ViewItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * {@link PartEssentiaTerminal}, {@link ItemWirelessEssentiaTerminal}, and {@link ItemEssentiaCell} GUI
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiEssentiaCellTerminal
	extends GuiConstants_ECT
	implements IAspectSelectorGui
{

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
	 * Widgets.
	 */
	protected final WidgetAspectSelector[] aspectWidgets;

	/**
	 * Aspect stacks that match the search term.
	 */
	protected ArrayList<IAspectStack> matchingSearchStacks = new ArrayList<IAspectStack>();

	/**
	 * What the user is searching for
	 */
	protected String searchTerm = "";

	/**
	 * The container associated with this gui
	 */
	protected ContainerEssentiaCellTerminalBase baseContainer;

	/**
	 * Mode used to sort the aspects
	 */
	protected AspectStackComparatorMode sortMode = AspectStackComparatorMode.MODE_ALPHABETIC;

	/**
	 * Set to true when the widgets need to be updated.
	 */
	protected boolean flagWidgetsNeedUpdate = false;

	/**
	 * The sorting mode button.
	 */
	protected GuiButtonSortingMode buttonSortingMode = null;

	/**
	 * Button to change what aspects (regular | crafting) are shown.
	 */
	protected GuiButtonViewType buttonViewMode;

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
	 * Compares aspect stacks
	 */
	private final AspectStackComparator stackComparator;

	/**
	 * Which items can be viewed in the terminal.
	 */
	private ViewItems viewMode = ViewItems.ALL;

	/**
	 * The currently selected aspect
	 */
	public IAspectStack selectedAspectStack;

	/**
	 * Creates the gui.
	 * 
	 * @param player
	 * Player viewing this gui.
	 * @param container
	 * Container associated with the gui.
	 */
	protected GuiEssentiaCellTerminal( final EntityPlayer player, final ContainerEssentiaCellTerminalBase container, final String title )
	{
		// Call super
		super( container );

		// Set the container.
		this.baseContainer = container;

		// Set the player
		this.player = player;

		// Set the X size
		this.xSize = GUI_WIDTH;

		// Set the Y size
		this.ySize = GUI_HEIGHT;

		// Set the title
		this.guiTitle = title;

		// Set the name prefix
		this.selectedInfoNamePrefix = ThEStrings.Gui_SelectedAspect.getLocalized() + ": ";

		// Set the amount prefix
		this.selectedInfoAmountPrefix = ThEStrings.Gui_SelectedAmount.getLocalized() + ": ";

		// Create the widgets
		this.aspectWidgets = new WidgetAspectSelector[WIDGETS_PER_PAGE];
		// Rows
		for( int y = 0; y < WIDGET_ROWS_PER_PAGE; y++ )
		{
			// Columns
			for( int x = 0; x < WIDGETS_PER_ROW; x++ )
			{
				WidgetAspectSelector widget = new WidgetAspectSelector( this, null,
								WIDGET_OFFSET_X + ( x * ThEWidget.WIDGET_SIZE ),
								WIDGET_OFFSET_Y + ( y * ThEWidget.WIDGET_SIZE ),
								player );
				this.aspectWidgets[( y * WIDGETS_PER_ROW ) + x] = widget;
			}
		}

		// Create the comparator
		this.stackComparator = new AspectStackComparator();

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
	public static GuiEssentiaCellTerminal NewEssentiaTerminalGui( final PartEssentiaTerminal terminal, final EntityPlayer player )
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
		// Valid player?
		if( ( player == null ) || ( player instanceof FakePlayer ) )
		{
			return null;
		}

		// Get the item the player is holding.
		ItemStack wirelessTerminal = player.getHeldItem();

		// Ensure the stack is valid
		if( ( wirelessTerminal == null ) )
		{
			// Invalid stack
			return null;
		}

		// Ensure the stack's item implements the wireless interface
		if( !( wirelessTerminal.getItem() instanceof IThEWirelessEssentiaTerminal ) )
		{
			// Invalid item.
			return null;
		}

		// Get the interface
		IThEWirelessEssentiaTerminal terminalInterface = (IThEWirelessEssentiaTerminal)wirelessTerminal.getItem();

		// Create the handler
		HandlerWirelessEssentiaTerminal handler = new HandlerWirelessEssentiaTerminal( player, null, terminalInterface, wirelessTerminal );

		// Create the gui
		return new GuiEssentiaCellTerminal( player, new ContainerWirelessEssentiaTerminal( player, handler ),
						ThEStrings.Part_EssentiaTerminal.getLocalized() );
	}

	/**
	 * True if the mouse is over the widget area
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	private boolean isMouseOverWidgetArea( final int mouseX, final int mouseY )
	{
		return ThEGuiHelper.INSTANCE.isPointInGuiRegion(
			WIDGET_OFFSET_Y, WIDGET_OFFSET_X,
			WIDGET_ROWS_PER_PAGE * ThEWidget.WIDGET_SIZE,
			WIDGETS_PER_ROW * ThEWidget.WIDGET_SIZE,
			mouseX, mouseY, this.guiLeft, this.guiTop );
	}

	/**
	 * Refreshes our aspect list to match that of the container.
	 */
	private void onListUpdate()
	{
		// Update the scrollbar
		this.updateScrollMaximum();

		// Update the search results
		this.updateView();

		// Update the selected aspect
		this.updateSelectedAspect();

		// Mark for widget update
		this.flagWidgetsNeedUpdate = true;
	}

	/**
	 * Updates the scroll bar's range.
	 */
	private void updateScrollMaximum()
	{
		// Calculate the number of widgets the will overflow
		double overflowWidgets = Math.max( 0, this.matchingSearchStacks.size() - WIDGETS_PER_PAGE );

		// Calculate how many rows will overflow
		int overflowRows = (int)Math.ceil( overflowWidgets / WIDGETS_PER_ROW );

		// Update if the range has changed
		if( overflowRows != this.previousOverflowRows )
		{
			// Update the scroll bar
			this.scrollBar.setRange( 0, overflowRows, 1 );
			this.onScrollbarMoved();
			this.previousOverflowRows = overflowRows;
		}

		// Mark for widget update
		this.flagWidgetsNeedUpdate = true;
	}

	/**
	 * Refreshes our selected aspect to match that of the container.
	 */
	private void updateSelectedAspect()
	{
		// No selection by default
		this.selectedAspectStack = null;

		// Check all aspects
		for( IAspectStack aspectStack : this.baseContainer.getAspectStackList() )
		{
			// Does this match?
			if( aspectStack.getAspect() == this.baseContainer.getSelectedAspect() )
			{
				// Set our selection
				this.selectedAspectStack = aspectStack;

				// Done
				return;
			}
		}

	}

	/**
	 * Updates the matching widgets based on the viewing conditions.
	 */
	private void updateView()
	{
		// Clear the matching widgets
		this.matchingSearchStacks.clear();

		// Get the full list
		Collection<IAspectStack> stacks = this.baseContainer.getAspectStackList();

		boolean hideCraftable = ( this.viewMode == ViewItems.STORED );
		boolean hideStored = ( this.viewMode == ViewItems.CRAFTABLE );

		// Examine each of the possible aspects
		for( IAspectStack stack : stacks )
		{
			// Is this stack valid for the current view mode?
			if( ( hideStored && !stack.getCraftable() ) || ( hideCraftable && stack.isEmpty() ) )
			{
				continue;
			}

			// Is the search term in this aspects tag or name?
			if( ( this.searchTerm == "" )
							|| ( stack.getAspectName().contains( this.searchTerm ) )
							|| ( stack.getAspectTag().contains( this.searchTerm ) ) )
			{
				this.matchingSearchStacks.add( stack );
			}
		}

		// Set the comparator mode
		this.stackComparator.setMode( this.sortMode );

		// Sort the results
		Collections.sort( this.matchingSearchStacks, this.stackComparator );

		// Update scrollbar
		this.updateScrollMaximum();

		// Mark for widget update
		this.flagWidgetsNeedUpdate = true;

	}

	/**
	 * Called when the update flag is true.
	 */
	private void updateWidgets()
	{
		// Get the number of matches
		int numberOfSearchMatches = this.matchingSearchStacks.size();

		// Calculate the row offset
		int rowOffset = this.currentScroll * WIDGETS_PER_ROW;

		// Update each widget
		int matchIndex = -1;
		for( int widgetIndex = 0; widgetIndex < this.aspectWidgets.length; ++widgetIndex )
		{
			// Calculate the match index
			matchIndex = widgetIndex + rowOffset;

			// Is the index greater than the number of matches?
			if( matchIndex >= numberOfSearchMatches )
			{
				// Set the aspect to null
				this.aspectWidgets[widgetIndex].clearWidget();
			}
			else
			{
				// Set the aspect
				this.aspectWidgets[widgetIndex].setAspect( this.matchingSearchStacks.get( matchIndex ) );

				// Set crafting mode
				this.aspectWidgets[widgetIndex].setHideAmount( ( this.viewMode == ViewItems.CRAFTABLE ) );
			}
		}

		this.flagWidgetsNeedUpdate = false;
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
		this.drawTexturedModalRect( this.guiLeft, this.guiTop - GUI_OFFSET_Y, 0, 0, this.xSize, this.ySize );

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
		this.fontRendererObj.drawString( this.guiTitle, TITLE_POS_X, TITLE_POS_Y, 0 );

		// Draw the widgets
		this.drawWidgets( mouseX, mouseY );

		// Is there a selected aspect?
		if( this.selectedAspectStack != null )
		{
			// Update the display amount?
			if( this.selectedAspectStack.getStackSize() != this.cacheAmountSelected )
			{
				// Convert the selected amount into a string
				this.cacheAmountDisplay = Long.toString( this.selectedAspectStack.getStackSize() );

				// Cache the amount
				this.cacheAmountSelected = this.selectedAspectStack.getStackSize();
			}

			// Get the name of the aspect
			String aspectName = this.selectedAspectStack.getAspectName( this.player );

			// Draw the name
			this.fontRendererObj.drawString( this.selectedInfoNamePrefix + aspectName,
				SELECTED_INFO_POS_X, SELECTED_INFO_NAME_POS_Y, 0 );

			// Draw the amount
			this.fontRendererObj.drawString( this.selectedInfoAmountPrefix + this.cacheAmountDisplay,
				SELECTED_INFO_POS_X, SELECTED_INFO_AMOUNT_POS_Y, 0 );
		}
	}

	@Override
	protected ScrollbarParams getScrollbarParameters()
	{
		return new ScrollbarParams( SCROLLBAR_POS_X, SCROLLBAR_POS_Y, SCROLLBAR_HEIGHT );
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
			this.updateView();
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
		// Boolean trackers for cleaner code
		final boolean isLeftClick = ( mouseBtn == ThEGuiHelper.MOUSE_BUTTON_LEFT );
		final boolean isRightClick = ( mouseBtn == ThEGuiHelper.MOUSE_BUTTON_RIGHT );
		final boolean isMiddleClick = ( mouseBtn == ThEGuiHelper.MOUSE_BUTTON_WHEEL );

		if( this.isMouseOverWidgetArea( mouseX, mouseY ) )
		{
			// Is the view mode crafting?
			boolean viewingCraftable = ( this.viewMode == ViewItems.CRAFTABLE );

			// Is the player holding anything?
			boolean playerHoldingItem = ( this.player.inventory.getItemStack() != null );

			// Check each widget
			for( WidgetAspectSelector widget : this.aspectWidgets )
			{
				// Is the mouse over this widget?
				if( widget.isMouseOverWidget( mouseX, mouseY ) )
				{
					// Is the player holding something?
					if( playerHoldingItem )
					{
						// Inform the server
						Packet_S_EssentiaCellTerminal.sendInteractWithHeldItem( this.player, widget.getAspect() );
					}
					else
					{
						// Check if the aspect is to be crafted
						if( ( !isRightClick && widget.hasAspect() && widget.getCraftable() ) &&
										( viewingCraftable
										|| ( isMiddleClick || ( isLeftClick && ( widget.getAmount() == 0 ) ) ) ) )
						{
							// Send request
							Packet_S_EssentiaCellTerminal.sendAutoCraft( this.player, widget.getAspect() );

						}

						// Send the click
						widget.onMouseClicked();
					}

					// Stop searching
					return;
				}
			}
		}

		// Is the mouse over the search bar?
		boolean mouseOverSearchBar = ( mouseX >= this.searchBar.xPosition )
						&& ( mouseX < ( this.searchBar.xPosition + this.searchBar.width ) )
						&& ( mouseY >= this.searchBar.yPosition )
						&& ( mouseY < ( this.searchBar.yPosition + this.searchBar.height ) );
		if( mouseOverSearchBar )
		{
			// Left click?
			if( isLeftClick )
			{
				// Pass to search field.
				this.searchBar.mouseClicked( mouseX, mouseY, mouseBtn );

				// Done
				return;

			}
			// Right click?
			else if( isRightClick )
			{
				// Clear the search text
				this.searchTerm = "";
				this.searchBar.setText( this.searchTerm );

				// Update the widgets
				this.updateView();

				// Done
				return;
			}
		}

		// Call super
		super.mouseClicked( mouseX, mouseY, mouseBtn );

	}

	/**
	 * Called when a button is clicked.
	 */
	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
		// Ignore all middle clicks.
		if( mouseButton == ThEGuiHelper.MOUSE_BUTTON_WHEEL )
		{
			return;
		}

		// Is the button the sort mode button?
		if( button == this.buttonSortingMode )
		{
			// Request update from server
			Packet_S_EssentiaCellTerminal.sendChangeSorting( this.player, ( mouseButton == ThEGuiHelper.MOUSE_BUTTON_RIGHT ) );
		}
		else if( button == this.buttonViewMode )
		{
			// Request update from server
			Packet_S_EssentiaCellTerminal.sendChangeView( this.player, ( mouseButton == ThEGuiHelper.MOUSE_BUTTON_RIGHT ) );
		}
	}

	@Override
	protected void onMouseWheel( final int deltaZ, final int mouseX, final int mouseY )
	{
		// Is the mouse inside of, or to the left of, the GUI?
		if( mouseX > ( this.guiLeft + GUI_WIDTH ) )
		{
			// Mouse is to the right of the gui
			return;
		}

		// Is shift not being held down?
		if( !GuiScreen.isShiftKeyDown() )
		{
			// Inform the scroll bar
			this.scrollBar.wheel( deltaZ );
			this.onScrollbarMoved();
		}

	}

	@Override
	protected void onScrollbarMoved()
	{
		// Set the scroll
		this.currentScroll = this.scrollBar.getCurrentScroll();

		// Mark for widget update
		this.flagWidgetsNeedUpdate = true;
	}

	/**
	 * Draw the widgets
	 * 
	 * @param mouseX
	 * @param mouseY
	 */
	public void drawWidgets( final int mouseX, final int mouseY )
	{
		// Do the widgets need to be updated?
		if( this.flagWidgetsNeedUpdate )
		{
			// Update first
			this.updateWidgets();
		}

		// Anything to draw?
		if( !this.matchingSearchStacks.isEmpty() )
		{
			for( int widgetIndex = 0; widgetIndex < this.aspectWidgets.length; ++widgetIndex )
			{
				// Get the widget
				WidgetAspectSelector widget = this.aspectWidgets[widgetIndex];

				// Does the widget not have an aspect?
				if( !widget.hasAspect() )
				{
					// Nothing more to draw
					break;
				}

				// Draw the widget
				widget.drawWidget();

				// Is the mouse over this widget?
				if( this.tooltip.isEmpty() && widget.isMouseOverWidget( mouseX, mouseY ) )
				{
					// Get the tooltip from the widget
					widget.getTooltip( this.tooltip );
				}
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
	public Aspect getSelectedAspect()
	{
		return( this.selectedAspectStack != null ? this.selectedAspectStack.getAspect() : null );
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

		// Enable repeat keys
		Keyboard.enableRepeatEvents( true );

		// Get the aspect list
		this.onListUpdate();

		// Set up the search bar
		this.searchBar = new GuiTextField( this.fontRendererObj,
						this.guiLeft + SEARCH_X_OFFSET,
						this.guiTop + SEARCH_Y_OFFSET,
						SEARCH_WIDTH, SEARCH_HEIGHT );

		// Set the search bar to draw in the foreground
		this.searchBar.setEnableBackgroundDrawing( false );

		// Start focused
		this.searchBar.setFocused( false );

		// Set maximum length
		this.searchBar.setMaxStringLength( SEARCH_MAX_CHARS );

		// Clear any existing buttons
		this.buttonList.clear();

		// Add the sort mode button
		this.buttonSortingMode = new GuiButtonSortingMode( 0,
						this.guiLeft + BUTTON_SORT_MODE_POS_X,
						this.guiTop + BUTTON_SORT_MODE_POS_Y,
						MODE_BUTTON_SIZE, MODE_BUTTON_SIZE );
		this.buttonList.add( this.buttonSortingMode );

		// Add view type button
		this.buttonViewMode = new GuiButtonViewType( 1,
						this.guiLeft + BUTTON_VIEW_MODE_POS_X,
						this.guiTop + BUTTON_VIEW_MODE_POS_Y,
						MODE_BUTTON_SIZE, MODE_BUTTON_SIZE );
		this.buttonList.add( this.buttonViewMode );
	}

	/**
	 * Called when the gui is closing.
	 */
	@Override
	public void onGuiClosed()
	{
		// Call super
		super.onGuiClosed();

		// Disable repeat keys
		Keyboard.enableRepeatEvents( false );
	}

	/**
	 * Called when the server sends a full list of network aspects.
	 * 
	 * @param aspectStackList
	 */
	public void onReceiveAspectList( final Collection<IAspectStack> aspectStackList )
	{
		// Update the container
		this.baseContainer.onReceivedAspectList( aspectStackList );

		// Update the gui
		this.onListUpdate();
	}

	/**
	 * Called when an aspect in the list changes amount.
	 * 
	 * @param change
	 */
	public void onReceiveAspectListChange( final IAspectStack change )
	{
		// Update the container
		this.baseContainer.onReceivedAspectListChange( change );

		// Update the gui
		this.onListUpdate();

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
	public void onViewingModesChanged( final AspectStackComparatorMode sortMode, final ViewItems viewMode )
	{
		// Update the sort button
		this.buttonSortingMode.setSortMode( this.sortMode = sortMode );

		// Update view mode
		this.buttonViewMode.setViewMode( this.viewMode = viewMode );

		// Update the view
		this.updateView();
	}

	/**
	 * Called when a new view mode is sent.
	 * 
	 * @param mode
	 */
	public void onViewModeChanged( final ViewItems mode )
	{
		// Set the mode
		this.viewMode = mode;

		// Update the view
		this.updateView();

		// Mark for widget update
		this.flagWidgetsNeedUpdate = true;
	}

}
