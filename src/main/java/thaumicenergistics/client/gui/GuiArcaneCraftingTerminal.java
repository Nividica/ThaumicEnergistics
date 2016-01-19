package thaumicenergistics.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.Config;
import thaumicenergistics.client.gui.abstraction.GuiConstants_ACT;
import thaumicenergistics.client.gui.buttons.*;
import thaumicenergistics.client.gui.widget.ThEWidget;
import thaumicenergistics.client.gui.widget.WidgetAEItem;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal.ArcaneCrafingCost;
import thaumicenergistics.common.integration.tc.MEItemAspectBridgeContainer;
import thaumicenergistics.common.network.packet.server.Packet_S_ArcaneCraftingTerminal;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.utils.ThEUtils;
import appeng.api.config.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;
import appeng.client.render.AppEngRenderItem;
import appeng.core.AEConfig;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gui for the Arcane Crafting Terminal
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiArcaneCraftingTerminal
	extends GuiConstants_ACT
	implements ISortSource
{
	/**
	 * Renders an AE itemstack into the gui.
	 */
	private AppEngRenderItem aeItemRenderer = new AppEngRenderItem();

	/**
	 * Translated title of the gui.
	 */
	private String guiTitle;

	/**
	 * Number of ME widgets
	 */
	private int widgetCount = GuiConstants_ACT.ME_DEFAULT_ROWS * GuiConstants_ACT.ME_COLUMNS;

	/**
	 * Widget 'slots'.
	 */
	private List<WidgetAEItem> itemWidgets = new ArrayList<WidgetAEItem>();

	/**
	 * Player viewing the gui.
	 */
	private EntityPlayer player;

	/**
	 * Search field.
	 */
	private GuiTextField searchField;

	/**
	 * Item repository
	 */
	private final ItemRepo repo;

	/**
	 * How the items are sorted.
	 */
	private SortOrder sortingOrder = SortOrder.NAME;

	/**
	 * What direction are the items sorted.
	 */
	private SortDir sortingDirection = SortDir.ASCENDING;

	/**
	 * Which items can be viewed in the terminal.
	 */
	private ViewItems viewMode = ViewItems.ALL;

	/**
	 * Is the terminal small or tall?
	 */
	private TerminalStyle terminalStyle = TerminalStyle.SMALL;

	/**
	 * The Y offeset of the lower portion of the terminal.
	 */
	private int lowerTerminalYOffset = 0;

	/**
	 * Tracks mouse movement.
	 */
	private int previousMouseX = 0;
	private int previousMouseY = 0;

	/**
	 * Tracks what widget was under the mouse last draw.
	 */
	private WidgetAEItem previousWidgetUnderMouse = null;

	/**
	 * Tracks the last time the tooltip was updated.
	 */
	private long lastTooltipUpdateTime = 0;

	/**
	 * TODO: Remove this and rework the widgets/slots to be MESlots. Such fun
	 * that will be.
	 */
	private MEItemAspectBridgeContainer meAspectBridge;

	/**
	 * The number of rows to use.
	 */
	private int numberOfWidgetRows = GuiConstants_ACT.ME_DEFAULT_ROWS;

	/**
	 * Button to set the sorting mode.
	 */
	private GuiButtonSortingMode btnSortingMode;

	/**
	 * Button to set accending or decending sorting.
	 */
	private GuiButtonSortingDirection btnSortingDirection;

	/**
	 * Button to change what items (regular | crafting) are shown.
	 */
	private GuiButtonViewType btnViewType;

	/**
	 * Button to change the search mode.
	 */
	private GuiButtonSearchMode btnSearchMode;

	/**
	 * Tracks if the repo/widgets needs to be updated.
	 */
	private boolean viewNeedsUpdate = true;

	/**
	 * Holds the cached tooltip of an item.
	 */
	private ArrayList<String> cachedItemTooltip = new ArrayList<String>();

	public GuiArcaneCraftingTerminal( final PartArcaneCraftingTerminal part, final EntityPlayer player )
	{
		// Call super
		super( new ContainerPartArcaneCraftingTerminal( part, player ) );

		// Set the player
		this.player = player;

		// Set the width and height
		this.xSize = GuiConstants_ACT.GUI_WIDTH;
		this.ySize = GuiConstants_ACT.GUI_HEIGHT;

		// Set the title
		this.guiTitle = ThEStrings.Gui_TitleArcaneCraftingTerminal.getLocalized();

		// Create the repo
		this.repo = new ItemRepo( this.scrollBar, this );

		// Get the terminal style
		this.terminalStyle = (TerminalStyle)AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE );

	}

	/**
	 * Checks if the click was a region deposit.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @return True if click was handled.
	 */
	private boolean clickHandler_RegionDeposit( final int mouseX, final int mouseY )
	{
		// Is the player holding the space key?
		if( Keyboard.isKeyDown( Keyboard.KEY_SPACE ) )
		{
			// Get the slot the mouse is over
			Slot slotClicked = this.getSlotAtPosition( mouseX, mouseY );

			// Was there a slot under the mouse?
			if( slotClicked != null )
			{
				// Ask the server to move the inventory
				Packet_S_ArcaneCraftingTerminal.sendDepositRegion( this.player, slotClicked.slotNumber );

				// Do not pass to super
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the click was inside the search box.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 * @return True if click was handled.
	 */
	private boolean clickHandler_SearchBox( final int mouseX, final int mouseY, final int mouseButton )
	{
		// Was the mouse right-clicked over the search field?
		if( ( mouseButton == ThEGuiHelper.MOUSE_BUTTON_RIGHT ) &&
						ThEGuiHelper.INSTANCE.isPointInGuiRegion( GuiConstants_ACT.SEARCH_POS_Y, GuiConstants_ACT.SEARCH_POS_X,
							GuiConstants_ACT.SEARCH_HEIGHT, GuiConstants_ACT.SEARCH_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Clear the search field
			this.searchField.setText( "" );

			// Update the repo
			this.repo.setSearchString( "" );

			// Repo needs update
			this.viewNeedsUpdate = true;

			// Inform search field.
			this.searchField.mouseClicked( mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton );

			// Do not pass to super
			return true;
		}

		return false;
	}

	/**
	 * Checks if the click was inside the widgets.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 * @return True if click was handled.
	 */
	private boolean clickHandler_Widgets( final int mouseX, final int mouseY, final int mouseButton )
	{
		// Was the click inside the ME grid?
		if( ThEGuiHelper.INSTANCE.isPointInGuiRegion( GuiConstants_ACT.ME_ITEM_POS_Y, GuiConstants_ACT.ME_ITEM_POS_X,
			this.numberOfWidgetRows * GuiConstants_ACT.ME_ROW_HEIGHT, GuiConstants_ACT.ME_GRID_WIDTH, mouseX, mouseY, this.guiLeft,
			this.guiTop ) )
		{
			// Click + empty hand is extract
			boolean doExtract = ( this.player.inventory.getItemStack() == null );

			// Shift+Right click is extract
			doExtract |= ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) && ( mouseButton == ThEGuiHelper.MOUSE_BUTTON_RIGHT ) );

			// Extracting?
			if( doExtract )
			{
				// Search for the widget the mouse is over, and send extract request.
				this.sendItemWidgetClicked( mouseX, mouseY, mouseButton );
			}
			else
			{
				// Inform the server the user would like to deposit the currently held item into the ME network.
				Packet_S_ArcaneCraftingTerminal.sendDeposit( this.player, mouseButton );
			}

			// Do not pass to super
			return true;
		}

		return false;
	}

	/**
	 * Extracts or inserts an item to/from the player held stack based on the
	 * direction the mouse wheel was scrolled.
	 * 
	 * @param deltaZ
	 */
	private void doMEWheelAction( final int deltaZ, final int mouseX, final int mouseY )
	{
		// Is the mouse inside the ME area?
		if( ThEGuiHelper.INSTANCE.isPointInGuiRegion( ME_ITEM_POS_Y, ME_ITEM_POS_X,
			this.numberOfWidgetRows * ME_ROW_HEIGHT, ME_GRID_WIDTH,
			mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Which direction was the scroll?
			if( deltaZ > 0 )
			{
				// Is the player holding anything?
				if( this.player.inventory.getItemStack() != null )
				{
					// Inform the server the user would like to deposit 1 of the currently held items into the ME network.
					Packet_S_ArcaneCraftingTerminal.sendDeposit( this.player, ThEGuiHelper.MOUSE_WHEEL_MOTION );
				}
			}
			else
			{
				// Extract an item based on the widget we are over
				this.sendItemWidgetClicked( mouseX, mouseY, ThEGuiHelper.MOUSE_WHEEL_MOTION );

			}
		}
	}

	/**
	 * Draws the crafting aspect's and their costs.
	 * 
	 * @param craftingCost
	 */
	private void drawCraftingAspects( final List<ArcaneCrafingCost> craftingCost )
	{
		int posY = GuiConstants_ACT.ASPECT_COST_POS_Y + this.lowerTerminalYOffset;
		int column = 0;

		// Draw each primal
		for( ArcaneCrafingCost cost : craftingCost )
		{
			// Set the alpha to full
			float alpha = 1.0F;

			// Do we have enough vis for this aspect?
			if( !cost.hasEnoughVis )
			{
				// Ping-pong the alpha
				alpha = ThEUtils.pingPongFromTime( GuiConstants_ACT.ASPECT_COST_BLINK_SPEED,
					GuiConstants_ACT.ASPECT_COST_MIN_ALPHA, GuiConstants_ACT.ASPECT_COST_MAX_ALPHA );
			}

			// Calculate X position
			int posX = GuiConstants_ACT.ASPECT_COST_POS_X + ( column * GuiConstants_ACT.ASPECT_COST_SPACING );

			// Draw the aspect icon
			UtilsFX.drawTag( posX, posY, cost.primal, cost.visCost, 0, this.zLevel, GL11.GL_ONE_MINUS_SRC_ALPHA, alpha, false );

			// Should we move to the next row?
			if( ++column == 2 )
			{
				// Reset column
				column = 0;

				// Increment Y
				posY += GuiConstants_ACT.ASPECT_COST_SPACING;
			}
		}
	}

	/**
	 * Draws the AE item widgets.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	private WidgetAEItem drawItemWidgets( final int mouseX, final int mouseY )
	{
		boolean hasNoOverlay = true;

		WidgetAEItem widgetUnderMouse = null;

		// Draw the item widgets
		for( int index = 0; index < this.widgetCount; ++index )
		{
			// Get the widget
			WidgetAEItem currentWidget = this.itemWidgets.get( index );

			// Draw the widget
			currentWidget.drawWidget();

			// Is the mouse over this widget?
			if( hasNoOverlay && currentWidget.isMouseOverWidget( mouseX, mouseY ) )
			{
				// Draw the overlay
				currentWidget.drawMouseHoverUnderlay();

				// Set that we have an overlay
				hasNoOverlay = false;

				// Set this widget as the widget under the mouse
				widgetUnderMouse = currentWidget;
			}
		}

		return widgetUnderMouse;
	}

	/**
	 * If the user has clicked on an item widget this will inform the server
	 * so that the item can be extracted from the AE network.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 */
	private void sendItemWidgetClicked( final int mouseX, final int mouseY, final int mouseButton )
	{
		for( int index = 0; index < this.widgetCount; ++index )
		{
			// Get the widget
			WidgetAEItem currentWidget = this.itemWidgets.get( index );

			// Is the mouse over this widget
			if( currentWidget.isMouseOverWidget( mouseX, mouseY ) )
			{
				// Get the AE itemstack this widget represents
				IAEItemStack widgetStack = currentWidget.getItemStack();

				// Did we get an item?
				if( widgetStack != null )
				{
					// Should the item be crafted?
					if( ( widgetStack.getStackSize() == 0 ) || ( mouseButton == ThEGuiHelper.MOUSE_BUTTON_WHEEL ) )
					{
						if( ( widgetStack.isCraftable() ) && ( mouseButton != ThEGuiHelper.MOUSE_BUTTON_RIGHT ) )
						{
							Packet_S_ArcaneCraftingTerminal.sendAutoCraft( this.player, widgetStack );
							return;
						}
					}

					// Let the server know the user is requesting an itemstack.
					Packet_S_ArcaneCraftingTerminal.sendExtract( this.player, widgetStack, mouseButton, GuiScreen.isShiftKeyDown() );
				}

				// Stop searching
				return;
			}
		}
	}

	/**
	 * Calculates the number of rows needed based on terminal style, and updates
	 * the gui parameters.
	 */
	private void setupTerminalStyle()
	{
		int extraRows = 0;

		// Tall?
		if( this.terminalStyle == TerminalStyle.TALL )
		{
			extraRows = Math.max( 0, ( ( this.height - GuiConstants_ACT.GUI_HEIGHT ) / 18 ) - 3 );
		}

		// Update the size and top of the GUI
		this.ySize = GuiConstants_ACT.GUI_HEIGHT + ( extraRows * GuiConstants_ACT.ME_ROW_HEIGHT );
		this.guiTop = ( this.height - this.ySize ) / 2;

		// Update the number of rows
		this.numberOfWidgetRows = GuiConstants_ACT.ME_DEFAULT_ROWS + extraRows;

		// Update number of widgets
		this.widgetCount = this.numberOfWidgetRows * GuiConstants_ACT.ME_COLUMNS;

		// Create the aspect bridge
		try
		{
			this.meAspectBridge = new MEItemAspectBridgeContainer( this.widgetCount );
		}
		catch( Exception e )
		{
			this.meAspectBridge = null;
		}

		// Clear old widgets
		this.itemWidgets.clear();

		// Create the widgets and bridge slots
		for( int row = 0; row < this.numberOfWidgetRows; ++row )
		{
			for( int column = 0; column < GuiConstants_ACT.ME_COLUMNS; ++column )
			{
				// Calculate the index and position
				int index = ( row * GuiConstants_ACT.ME_COLUMNS ) + column;
				int posX = GuiConstants_ACT.ME_ITEM_POS_X + ( column * ThEWidget.WIDGET_SIZE );
				int posY = GuiConstants_ACT.ME_ITEM_POS_Y + ( row * ThEWidget.WIDGET_SIZE );

				// Create the ME slot
				this.itemWidgets.add( new WidgetAEItem( this, posX, posY, this.aeItemRenderer ) );

				// Add the bridge slot
				if( this.meAspectBridge != null )
				{
					this.meAspectBridge.addSlot( index, posX, posY );
				}
			}
		}

		// Update the scroll bar range
		this.updateScrollbarRange();

		// Update the scroll bar height
		this.setScrollBarHeight( GuiConstants_ACT.SCROLLBAR_HEIGHT + ( extraRows * GuiConstants_ACT.ME_ROW_HEIGHT ) );

		// Update the lower terminal portion Y offset
		int prevYOffset = this.lowerTerminalYOffset;
		this.lowerTerminalYOffset = ( extraRows * GuiConstants_ACT.ME_ROW_HEIGHT );

		// Update the container
		if( prevYOffset != this.lowerTerminalYOffset )
		{
			( (ContainerPartArcaneCraftingTerminal)this.inventorySlots ).changeSlotsYOffset( this.lowerTerminalYOffset - prevYOffset );
		}

		// Clear any tooltip
		this.cachedItemTooltip.clear();

	}

	/**
	 * Assigns the network items to the widgets
	 */
	private void updateMEWidgets()
	{
		int repoIndex = 0;
		// List all items
		for( int index = 0; index < this.widgetCount; ++index )
		{
			IAEItemStack stack = this.repo.getReferenceItem( repoIndex++ );

			// Did we get a stack? 
			if( stack != null )
			{
				// Set the item
				this.itemWidgets.get( index ).setItemStack( stack );
				if( this.meAspectBridge != null )
				{
					this.meAspectBridge.setSlot( index, stack.getItemStack().copy() );
				}
			}
			else
			{
				// Set to null
				this.itemWidgets.get( index ).setItemStack( null );
				if( this.meAspectBridge != null )
				{
					this.meAspectBridge.setSlot( index, null );
				}
			}
		}
	}

	/**
	 * Updates the scroll bar's range.
	 */
	private void updateScrollbarRange()
	{
		// Calculate the total number of rows needed to display ALL items
		int totalNumberOfRows = (int)Math.ceil( this.repo.size() / (double)GuiConstants_ACT.ME_COLUMNS );

		// Calculate the scroll based on how many rows can be shown
		int max = Math.max( 0, totalNumberOfRows - this.numberOfWidgetRows );

		// Update the scroll bar
		this.scrollBar.setRange( 0, max, 2 );
	}

	/**
	 * Updates the sorting modes and refreshes the gui.
	 */
	private void updateSorting()
	{
		// Set the direction icon
		this.btnSortingDirection.setSortingDirection( this.sortingDirection );

		// Set the order icon
		this.btnSortingMode.setSortMode( this.sortingOrder );

		// Set the view mode
		this.btnViewType.setViewMode( this.viewMode );

		// Repo needs update
		this.viewNeedsUpdate = true;

	}

	/**
	 * Updates the repo's view, the scroll bar range, and the widgets.
	 */
	private void updateView()
	{
		// Mark clean
		this.viewNeedsUpdate = false;

		// Update repo view
		this.repo.updateView();

		// Update the scroll bar
		this.updateScrollbarRange();

		// Update the widgets
		this.updateMEWidgets();
	}

	/**
	 * Draws the gui texture
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// Does the view need updating?
		if( this.viewNeedsUpdate )
		{
			this.updateView();
		}

		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Set the texture
		this.mc.renderEngine.bindTexture( GuiTextureManager.ARCANE_CRAFTING_TERMINAL.getTexture() );

		// Draw the upper portion: Label, Search, First row
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, GuiConstants_ACT.GUI_MAIN_BODY_WIDTH,
			GuiConstants_ACT.GUI_UPPER_TEXTURE_HEIGHT );

		// Draw the extra rows
		for( int i = 0; i < ( this.numberOfWidgetRows - GuiConstants_ACT.ME_DEFAULT_ROWS ); ++i )
		{
			int yPos = this.guiTop + GuiConstants_ACT.GUI_UPPER_TEXTURE_HEIGHT + ( i * GuiConstants_ACT.ME_ROW_HEIGHT );

			// Draw the texture
			this.drawTexturedModalRect( this.guiLeft, yPos, 0, GuiConstants_ACT.GUI_TEXTURE_ROW_V,
				GuiConstants_ACT.GUI_MAIN_BODY_WIDTH, GuiConstants_ACT.ME_ROW_HEIGHT );
		}

		// Draw the lower portion, bottom two rows, crafting grid, player inventory
		this.drawTexturedModalRect( this.guiLeft, this.guiTop + GuiConstants_ACT.GUI_UPPER_TEXTURE_HEIGHT + this.lowerTerminalYOffset, 0,
			GuiConstants_ACT.ME_ROW_HEIGHT + 17, GuiConstants_ACT.GUI_MAIN_BODY_WIDTH,
			GuiConstants_ACT.GUI_TEXTURE_LOWER_HEIGHT + 18 );

		// Draw view cells
		this.drawTexturedModalRect( this.guiLeft + GuiConstants_ACT.GUI_MAIN_BODY_WIDTH, this.guiTop,
			GuiConstants_ACT.GUI_MAIN_BODY_WIDTH, 0, GuiConstants_ACT.GUI_VIEW_CELL_TEXTURE_WIDTH,
			GuiConstants_ACT.GUI_VIEW_CELL_TEXTURE_HEIGHT );

		// Bind the AE states texture
		Minecraft.getMinecraft().renderEngine.bindTexture( AEStateIconsEnum.AE_STATES_TEXTURE );

		// Draw the view cell backgrounds
		int u = AEStateIconsEnum.VIEW_CELL_BACKGROUND.getU(), v = AEStateIconsEnum.VIEW_CELL_BACKGROUND.getV();
		int h = AEStateIconsEnum.VIEW_CELL_BACKGROUND.getHeight(), w = AEStateIconsEnum.VIEW_CELL_BACKGROUND.getWidth();
		int x = this.guiLeft + ContainerPartArcaneCraftingTerminal.VIEW_SLOT_XPOS, y = this.guiTop +
						ContainerPartArcaneCraftingTerminal.VIEW_SLOT_YPOS;
		for( int row = 0; row < 5; row++ )
		{
			this.drawTexturedModalRect( x, y + ( row * 18 ), u, v, w, h );
		}
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
		this.fontRendererObj.drawString( this.guiTitle, GuiConstants_ACT.TITLE_POS_X, GuiConstants_ACT.TITLE_POS_Y, 0x000000 );

		// Draw the search field.
		this.searchField.drawTextBox();

		// Enable lighting
		GL11.glEnable( GL11.GL_LIGHTING );

		// Draw the widgets and get which one the mouse is over
		WidgetAEItem widgetUnderMouse = this.drawItemWidgets( mouseX, mouseY );

		// Get the cost
		List<ArcaneCrafingCost> craftingCost = ( (ContainerPartArcaneCraftingTerminal)this.inventorySlots ).getCraftingCost( false );

		// Does the current recipe have costs?
		if( craftingCost != null )
		{
			// Draw the costs
			this.drawCraftingAspects( craftingCost );
		}

		// Should we force a tooltip update?
		boolean forceTooltipUpdate = ( ( System.currentTimeMillis() - this.lastTooltipUpdateTime ) >= GuiConstants_ACT.WIDGET_TOOLTIP_UPDATE_INTERVAL );

		// Has the mouse moved, or timeout reached?
		if( forceTooltipUpdate || ( this.previousMouseX != mouseX ) || ( this.previousMouseY != mouseY ) )
		{
			// Do we have a widget under the mouse?
			if( widgetUnderMouse != null )
			{
				// Has the widget changed?
				if( forceTooltipUpdate || ( widgetUnderMouse != this.previousWidgetUnderMouse ) )
				{
					// Clear the tooltip
					this.cachedItemTooltip.clear();

					// Get the tooltip from the widget
					widgetUnderMouse.getTooltip( this.cachedItemTooltip );

					// Set the time
					this.lastTooltipUpdateTime = System.currentTimeMillis();
				}
			}
			else
			{
				// Nothing to display
				this.cachedItemTooltip.clear();
			}

			// Set the previous position
			this.previousMouseX = mouseX;
			this.previousMouseY = mouseY;

			// Set the previous widget
			this.previousWidgetUnderMouse = widgetUnderMouse;
		}

		// Add cached tooltip
		if( !this.cachedItemTooltip.isEmpty() )
		{
			this.tooltip.addAll( this.cachedItemTooltip );
		}
	}

	/**
	 * Returns the scroll bar parameters.
	 * 
	 * @return
	 */
	@Override
	protected ScrollbarParams getScrollbarParameters()
	{
		return new ScrollbarParams(
						GuiConstants_ACT.SCROLLBAR_POS_X,
						GuiConstants_ACT.SCROLLBAR_POS_Y,
						GuiConstants_ACT.SCROLLBAR_HEIGHT +
										( ( GuiConstants_ACT.ME_DEFAULT_ROWS - this.numberOfWidgetRows ) * GuiConstants_ACT.ME_ROW_HEIGHT ) );
	}

	/**
	 * Called when the player types a key.
	 */
	@Override
	protected void keyTyped( final char key, final int keyID )
	{
		// Did they press the escape key?
		if( keyID == Keyboard.KEY_ESCAPE )
		{
			// Close the screen.
			this.mc.thePlayer.closeScreen();
			return;
		}

		// Prevent only spaces
		if( ( key == ' ' ) && ( this.searchField.getText().length() == 0 ) )
		{
			return;
		}

		if( this.searchField.textboxKeyTyped( key, keyID ) )
		{
			// Get the search query
			String newSearch = this.searchField.getText().trim().toLowerCase();

			// Has the query changed?
			if( !newSearch.equals( this.repo.getSearchString() ) )
			{
				// Set the search string
				this.repo.setSearchString( newSearch );

				// Repo needs update
				this.viewNeedsUpdate = true;
			}
		}
		else
		{
			super.keyTyped( key, keyID );
		}

	}

	/**
	 * Called when the mouse is clicked while the gui is open
	 */
	@Override
	protected void mouseClicked( final int mouseX, final int mouseY, final int mouseButton )
	{
		// Handled by the widget area?
		if( this.clickHandler_Widgets( mouseX, mouseY, mouseButton ) )
		{
			return;
		}

		// Handled by region deposit?
		if( this.clickHandler_RegionDeposit( mouseX, mouseY ) )
		{
			return;
		}

		// Handled by search box?
		if( this.clickHandler_SearchBox( mouseX, mouseY, mouseButton ) )
		{
			return;
		}

		// Get search mode
		SearchBoxMode searchBoxMode = (SearchBoxMode)AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );

		// Inform search field of click if auto mode is not on
		if( !( ( searchBoxMode == SearchBoxMode.AUTOSEARCH ) || ( searchBoxMode == SearchBoxMode.NEI_AUTOSEARCH ) ) )
		{
			this.searchField.mouseClicked( mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton );
		}

		// Pass to super
		super.mouseClicked( mouseX, mouseY, mouseButton );
	}

	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
		boolean sortingChanged = false;

		boolean wasLeftClick = true;

		// Which button was clicked?
		switch ( mouseButton )
		{
		case ThEGuiHelper.MOUSE_BUTTON_LEFT:
			// Already true
			break;

		case ThEGuiHelper.MOUSE_BUTTON_RIGHT:
			// Set to false
			wasLeftClick = false;
			break;

		default:
			// Don't handle any other buttons
			return;
		}

		switch ( button.id )
		{
		// Clear grid
		case GuiConstants_ACT.BUTTON_CLEAR_GRID_ID:
			// Attempt to clear the grid
			Packet_S_ArcaneCraftingTerminal.sendClearGrid( this.player );
			break;

		// Sort order
		case GuiConstants_ACT.BUTTON_SORT_MODE_ID:
			switch ( this.sortingOrder )
			{
			case AMOUNT:
				this.sortingOrder = ( wasLeftClick ? SortOrder.MOD : SortOrder.NAME );
				break;

			case INVTWEAKS:
				break;

			case MOD:
				this.sortingOrder = ( wasLeftClick ? SortOrder.NAME : SortOrder.AMOUNT );
				break;

			case NAME:
				this.sortingOrder = ( wasLeftClick ? SortOrder.AMOUNT : SortOrder.MOD );
				break;
			}
			sortingChanged = true;
			break;

		// Sorting direction
		case GuiConstants_ACT.BUTTON_SORT_DIR_ID:
			switch ( this.sortingDirection )
			{
			case ASCENDING:
				this.sortingDirection = SortDir.DESCENDING;
				break;

			case DESCENDING:
				this.sortingDirection = SortDir.ASCENDING;
				break;

			}
			sortingChanged = true;
			break;

		// View type
		case GuiConstants_ACT.BUTTON_VIEW_TYPE_ID:

			// Rotate view mode
			this.viewMode = Platform.rotateEnum( this.viewMode, !wasLeftClick, Settings.VIEW_MODE.getPossibleValues() );

			sortingChanged = true;
			break;

		// Swap armor
		case GuiConstants_ACT.BUTTON_SWAP_ARMOR_ID:
			// Ask the server to swap the armor
			Packet_S_ArcaneCraftingTerminal.sendSwapArmor( this.player );
			break;

		// Terminal style
		case GuiConstants_ACT.BUTTON_TERM_STYLE_ID:
			switch ( this.terminalStyle )
			{
			case SMALL:
				this.terminalStyle = TerminalStyle.TALL;
				break;

			case TALL:
				this.terminalStyle = TerminalStyle.SMALL;
				break;

			default:
				this.terminalStyle = TerminalStyle.SMALL;
				break;

			}

			// Update the AE settings
			AEConfig.instance.getConfigManager().putSetting( Settings.TERMINAL_STYLE, this.terminalStyle );

			// Reinit
			this.initGui();

			break;

		// Search mode
		case GuiConstants_ACT.BUTTON_SEARCH_MODE_ID:
			// Rotate search mode
			SearchBoxMode searchBoxMode = (SearchBoxMode)AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );
			searchBoxMode = Platform.rotateEnum( searchBoxMode, !wasLeftClick, Settings.SEARCH_MODE.getPossibleValues() );

			// Set focus
			this.searchField.setFocused( ( searchBoxMode == SearchBoxMode.AUTOSEARCH ) || ( searchBoxMode == SearchBoxMode.NEI_AUTOSEARCH ) );

			// Update the settings
			AEConfig.instance.settings.putSetting( Settings.SEARCH_MODE, searchBoxMode );

			// Update the button
			this.btnSearchMode.setSearchMode( searchBoxMode );

			// Clear the tooltip
			this.cachedItemTooltip.clear();
			this.lastTooltipUpdateTime = 0;

			break;
		}

		// Was the sorting mode changed?
		if( sortingChanged )
		{
			// Update the sorting
			this.updateSorting();

			// Reset the tooltip
			this.lastTooltipUpdateTime = 0;

			// Send to server
			Packet_S_ArcaneCraftingTerminal.sendMode( this.player, this.sortingOrder, this.sortingDirection, this.viewMode );
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

		// Is shift being held?
		if( GuiScreen.isShiftKeyDown() )
		{
			// Extract or insert based on the motion of the wheel
			this.doMEWheelAction( deltaZ, mouseX, mouseY );
		}
		else
		{
			// Inform the scroll bar
			this.scrollBar.wheel( deltaZ );

			// Update the item widgets
			this.onScrollbarMoved();
		}
	}

	@Override
	protected void onScrollbarMoved()
	{
		// Update the widgets
		this.updateMEWidgets();

		// Clear the tooltip
		this.cachedItemTooltip.clear();
		this.lastTooltipUpdateTime = 0;
	}

	/**
	 * Draws all screen elements, specifically calling on TC to draw the aspects
	 * of whatever the mouse is over.
	 */
	@Override
	public void drawScreen( final int mouseX, final int mouseY, final float mouseBtn )
	{
		// Call super
		super.drawScreen( mouseX, mouseY, mouseBtn );

		// Show TC aspects for items in the ME network.
		if( ( this.meAspectBridge != null ) && ( !Config.showTags == Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) ) )
		{
			this.meAspectBridge.renderAspects( this, this.player );
		}
	}

	/**
	 * Gets the sorting order.
	 */
	@Override
	public Enum getSortBy()
	{
		return this.sortingOrder;
	}

	/**
	 * Gets the sorting direction.
	 */
	@Override
	public Enum getSortDir()
	{
		return this.sortingDirection;
	}

	/**
	 * Gets what items (stored vs crafting) to show.
	 */
	@Override
	public Enum getSortDisplay()
	{
		return this.viewMode;
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

		// Calculate row count
		this.setupTerminalStyle();

		// Get the current search box mode
		SearchBoxMode searchBoxMode = (SearchBoxMode)AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );

		if( this.searchField == null )
		{
			// Set up the search bar
			this.searchField = new GuiTextField( this.fontRendererObj, GuiConstants_ACT.SEARCH_POS_X, GuiConstants_ACT.SEARCH_POS_Y,
							GuiConstants_ACT.SEARCH_WIDTH, GuiConstants_ACT.SEARCH_HEIGHT );

			// Set the search field to draw in the foreground
			this.searchField.setEnableBackgroundDrawing( false );

			// Set maximum length
			this.searchField.setMaxStringLength( GuiConstants_ACT.SEARCH_MAX_CHARS );
		}

		// Start focused?
		this.searchField.setFocused( ( searchBoxMode == SearchBoxMode.AUTOSEARCH ) || ( searchBoxMode == SearchBoxMode.NEI_AUTOSEARCH ) );

		// Set the search string
		this.searchField.setText( this.repo.getSearchString() );

		// Clear any existing buttons
		this.buttonList.clear();

		// Create the clear grid button
		this.buttonList.add( new GuiButtonClearCraftingGrid( GuiConstants_ACT.BUTTON_CLEAR_GRID_ID, this.guiLeft +
						GuiConstants_ACT.BUTTON_CLEAR_GRID_POS_X, this.guiTop + GuiConstants_ACT.BUTTON_CLEAR_GRID_POS_Y +
						this.lowerTerminalYOffset, 8, 8, true ) );

		// Add sort order button
		this.buttonList.add( this.btnSortingMode = new GuiButtonSortingMode( GuiConstants_ACT.BUTTON_SORT_MODE_ID, this.guiLeft +
						GuiConstants_ACT.BUTTON_SORT_MODE_POS_X, this.guiTop + GuiConstants_ACT.BUTTON_SORT_MODE_POS_Y,
						GuiConstants_ACT.BUTTON_AE_SIZE, GuiConstants_ACT.BUTTON_AE_SIZE ) );

		// Add sort direction button
		this.buttonList.add( this.btnSortingDirection = new GuiButtonSortingDirection( GuiConstants_ACT.BUTTON_SORT_DIR_ID, this.guiLeft +
						GuiConstants_ACT.BUTTON_SORT_DIR_POS_X, this.guiTop + GuiConstants_ACT.BUTTON_SORT_DIR_POS_Y,
						GuiConstants_ACT.BUTTON_AE_SIZE, GuiConstants_ACT.BUTTON_AE_SIZE ) );

		// Add view type button
		this.buttonList.add( this.btnViewType = new GuiButtonViewType( GuiConstants_ACT.BUTTON_VIEW_TYPE_ID, this.guiLeft +
						GuiConstants_ACT.BUTTON_VIEW_TYPE_POS_X, this.guiTop + GuiConstants_ACT.BUTTON_VIEW_TYPE_POS_Y,
						GuiConstants_ACT.BUTTON_AE_SIZE, GuiConstants_ACT.BUTTON_AE_SIZE ) );

		// Add swap armor button
		this.buttonList.add( new GuiButtonSwapArmor( GuiConstants_ACT.BUTTON_SWAP_ARMOR_ID, this.guiLeft +
						GuiConstants_ACT.BUTTON_SWAP_ARMOR_POS_X, this.guiTop + GuiConstants_ACT.BUTTON_SWAP_ARMOR_POS_Y +
						this.lowerTerminalYOffset, 8, 8 ) );

		// Add search mode button
		this.buttonList.add( this.btnSearchMode = new GuiButtonSearchMode( GuiConstants_ACT.BUTTON_SEARCH_MODE_ID, this.guiLeft +
						GuiConstants_ACT.BUTTON_SEARCH_MODE_POS_X, this.guiTop + GuiConstants_ACT.BUTTON_SEARCH_MODE_POS_Y,
						GuiConstants_ACT.BUTTON_AE_SIZE, GuiConstants_ACT.BUTTON_AE_SIZE, searchBoxMode ) );

		// Add terminal style button
		this.buttonList.add( new GuiButtonTerminalStyle( GuiConstants_ACT.BUTTON_TERM_STYLE_ID, this.guiLeft +
						GuiConstants_ACT.BUTTON_TERM_STYLE_POS_X, this.guiTop + GuiConstants_ACT.BUTTON_TERM_STYLE_POS_Y,
						GuiConstants_ACT.BUTTON_AE_SIZE, GuiConstants_ACT.BUTTON_AE_SIZE, this.terminalStyle ) );

		// Add the container as a listener
		( (ContainerPartArcaneCraftingTerminal)this.inventorySlots ).registerForUpdates();

		// Request a full update from the server
		Packet_S_ArcaneCraftingTerminal.sendFullListRequest( this.player );

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
	 * Called to update the amount of an item in the ME network.
	 * 
	 * @param change
	 */
	public void onReceiveChange( final IAEItemStack change )
	{
		// Update the repository
		this.repo.postUpdate( change );

		// Repo needs update
		this.viewNeedsUpdate = true;
	}

	/**
	 * Called when the server sends a full list of all
	 * items in the AE network in response to our request.
	 * 
	 * @param itemList
	 */
	public void onReceiveFullList( final IItemList<IAEItemStack> itemList )
	{
		// Clear the repo
		this.repo.clear();

		// Update the repository
		for( IAEItemStack stack : itemList )
		{
			this.repo.postUpdate( stack );
		}

		// Repo needs update
		this.viewNeedsUpdate = true;
	}

	/**
	 * Called when the server sends the sorting order and direction.
	 * 
	 * @param order
	 * @param direction
	 */
	public void onReceiveSorting( final SortOrder order, final SortDir direction, final ViewItems viewMode )
	{
		// Set the direction
		this.sortingDirection = direction;

		// Set the order
		this.sortingOrder = order;

		// Set view mode
		this.viewMode = viewMode;

		// Update
		this.updateSorting();
	}

	/**
	 * Called when the server wants the client to force an update to the aspect
	 * costs.
	 */
	public void onServerSendForceUpdateCost()
	{
		( (ContainerPartArcaneCraftingTerminal)this.inventorySlots ).getCraftingCost( true );
	}

	/**
	 * Called when the view cells change.
	 */
	public void onViewCellsChanged( final ItemStack[] viewCells )
	{
		// Update the repo
		this.repo.setViewCell( viewCells );

		// Repo needs update
		this.viewNeedsUpdate = true;
	}

}
