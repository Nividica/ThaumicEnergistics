package thaumicenergistics.gui;

import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.container.ContainerPartArcaneCraftingTerminal.ArcaneCrafingCost;
import thaumicenergistics.gui.abstraction.AbstractGuiConstantsACT;
import thaumicenergistics.gui.buttons.ButtonClearCraftingGrid;
import thaumicenergistics.gui.buttons.ButtonSortingDirection;
import thaumicenergistics.gui.buttons.ButtonSortingMode;
import thaumicenergistics.gui.widget.AbstractWidget;
import thaumicenergistics.gui.widget.WidgetAEItem;
import thaumicenergistics.network.packet.server.PacketServerArcaneCraftingTerminal;
import thaumicenergistics.parts.AEPartArcaneCraftingTerminal;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.GuiHelper;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;
import appeng.client.render.AppEngRenderItem;

public class GuiArcaneCraftingTerminal
	extends AbstractGuiConstantsACT
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
	 * Widget 'slots'.
	 */
	private WidgetAEItem[] itemWidgets = new WidgetAEItem[AbstractGuiConstantsACT.ME_WIDGET_COUNT];

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
	 * Scroll bar
	 */
	private final GuiScrollbar scrollBar;

	/**
	 * Serves as a graphics call bridge for the scroll bar.
	 */
	private AEBaseGui aeGuiBridge;

	/**
	 * True if the scroll bar has mouse focus.
	 */
	private boolean isScrollBarHeld = false;

	/**
	 * The last Y position of the mouse when the scroll bar has mouse focus.
	 */
	private int scrollHeldPrevY = 0;

	/**
	 * How the items are sorted.
	 */
	private SortOrder sortingOrder = SortOrder.NAME;

	/**
	 * What direction are the items sorted.
	 */
	private SortDir sortingDirection = SortDir.ASCENDING;

	public GuiArcaneCraftingTerminal( final AEPartArcaneCraftingTerminal part, final EntityPlayer player )
	{
		// Call super
		super( new ContainerPartArcaneCraftingTerminal( part, player ) );

		// Set the player
		this.player = player;

		// Set the width and height
		this.xSize = AbstractGuiConstantsACT.GUI_WIDTH;
		this.ySize = AbstractGuiConstantsACT.GUI_HEIGHT;

		// Set the title
		this.guiTitle = StatCollector.translateToLocal( "thaumicenergistics.gui.arcane.crafting.terminal.title" );

		// Create the widgets
		for( int row = 0; row < AbstractGuiConstantsACT.ME_ROWS; row++ )
		{
			for( int column = 0; column < AbstractGuiConstantsACT.ME_COLUMNS; column++ )
			{
				// Calculate the index
				int index = ( row * AbstractGuiConstantsACT.ME_COLUMNS ) + column;

				this.itemWidgets[index] = new WidgetAEItem( this, AbstractGuiConstantsACT.ME_ITEM_POS_X + ( column * AbstractWidget.WIDGET_SIZE ),
								AbstractGuiConstantsACT.ME_ITEM_POS_Y + ( row * AbstractWidget.WIDGET_SIZE ), this.aeItemRenderer );
			}
		}

		// Create the scrollbar
		this.scrollBar = new GuiScrollbar();

		// Create the repo
		this.repo = new ItemRepo( this.scrollBar, this );

	}

	/**
	 * Extracts or inserts an item to/from the player held stack based on the
	 * direction the mouse wheel was scrolled.
	 * 
	 * @param deltaZ
	 */
	private void doMEWheelAction( final int deltaZ )
	{
		// Get the mouse position
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		// Is the mouse inside the ME area?
		if( GuiHelper.instance.isPointInGuiRegion( AbstractGuiConstantsACT.ME_ITEM_POS_Y, AbstractGuiConstantsACT.ME_ITEM_POS_X,
			AbstractGuiConstantsACT.ME_GRID_HEIGHT, AbstractGuiConstantsACT.ME_GRID_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Which direction was the scroll?
			if( deltaZ > 0 )
			{
				// Is the player holding anything?
				if( this.player.inventory.getItemStack() != null )
				{
					// Inform the server the user would like to deposit 1 of the currently held items into the ME network.
					new PacketServerArcaneCraftingTerminal().createRequestDeposit( this.player, GuiHelper.MOUSE_WHEEL_MOTION ).sendPacketToServer();
				}
			}
			else
			{
				// Extract an item based on the widget we are over
				this.sendItemWidgetClicked( mouseX, mouseY, GuiHelper.MOUSE_WHEEL_MOTION );

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
		int posY = AbstractGuiConstantsACT.ASPECT_COST_POS_Y;
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
				alpha = GuiHelper.instance.pingPongFromTime( AbstractGuiConstantsACT.ASPECT_COST_BLINK_SPEED,
					AbstractGuiConstantsACT.ASPECT_COST_MIN_ALPHA, AbstractGuiConstantsACT.ASPECT_COST_MAX_ALPHA );
			}

			// Calculate X position
			int posX = AbstractGuiConstantsACT.ASPECT_COST_POS_X + ( column * AbstractGuiConstantsACT.ASPECT_COST_SPACING );

			// Draw the aspect icon
			UtilsFX.drawTag( posX, posY, cost.primal, cost.visCost, 0, this.zLevel, GL11.GL_ONE_MINUS_SRC_ALPHA, alpha, false );

			// Should we move to the next row?
			if( ++column == 2 )
			{
				// Reset column
				column = 0;

				// Increment Y
				posY += AbstractGuiConstantsACT.ASPECT_COST_SPACING;
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
		for( int index = 0; index < AbstractGuiConstantsACT.ME_WIDGET_COUNT; index++ )
		{
			// Get the widget
			WidgetAEItem currentWidget = this.itemWidgets[index];

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
		for( int index = 0; index < AbstractGuiConstantsACT.ME_WIDGET_COUNT; index++ )
		{
			// Get the widget
			WidgetAEItem currentWidget = this.itemWidgets[index];
			// Is the mouse over this widget

			if( currentWidget.isMouseOverWidget( mouseX, mouseY ) )
			{
				// Get the AE itemstack this widget represents
				IAEItemStack widgetStack = currentWidget.getItemStack();

				// Did we get an item?
				if( widgetStack != null )
				{
					// Get the state of the shift keys
					boolean isShiftHeld = Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT );

					// Let the server know the user is requesting an itemstack.
					new PacketServerArcaneCraftingTerminal().createRequestExtract( this.player, widgetStack, mouseButton, isShiftHeld )
									.sendPacketToServer();
				}

				// Stop searching
				return;
			}
		}
	}

	/**
	 * Assigns the network items to the widgets
	 */
	private void updateMEWidgets()
	{
		// List all items
		for( int index = 0; index < AbstractGuiConstantsACT.ME_WIDGET_COUNT; index++ )
		{
			IAEItemStack stack = this.repo.getReferenceItem( index );

			// Did we get a stack?
			if( stack != null )
			{
				// Set the item
				this.itemWidgets[index].setItemStack( stack );
			}
			else
			{
				// Set to null
				this.itemWidgets[index].setItemStack( null );
			}
		}
	}

	/**
	 * Updates the scroll bar's range.
	 */
	private void updateScrollMaximum()
	{
		// Calculate the scroll max
		int max = Math.max( 0, ( this.repo.size() / AbstractGuiConstantsACT.ME_COLUMNS ) - 2 );

		// Update the scroll bar
		this.scrollBar.setRange( 0, max, 2 );
	}

	/**
	 * Updates the sorting modes and refreshes the gui.
	 */
	private void updateSorting()
	{
		// Set the direction icon
		( (ButtonSortingDirection)this.buttonList.get( AbstractGuiConstantsACT.BUTTON_SORT_DIR_ID ) ).setSortingDirection( this.sortingDirection );

		// Set the order icon
		( (ButtonSortingMode)this.buttonList.get( AbstractGuiConstantsACT.BUTTON_SORT_ORDER_ID ) ).setSortMode( this.sortingOrder );

		// Update the repo
		this.repo.updateView();

		// Update the widgets
		this.updateMEWidgets();

	}

	/**
	 * Draws the gui texture
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Set the texture
		this.mc.renderEngine.bindTexture( GuiTextureManager.ARCANE_CRAFTING_TERMINAL.getTexture() );

		// Draw the gui image
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, AbstractGuiConstantsACT.GUI_WIDTH, AbstractGuiConstantsACT.GUI_HEIGHT );
	}

	/**
	 * Draw the foreground layer.
	 */
	@Override
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.guiTitle, AbstractGuiConstantsACT.TITLE_POS_X, AbstractGuiConstantsACT.TITLE_POS_Y, 0x000000 );

		// Draw the search field.
		this.searchField.drawTextBox();

		// Draw the scroll bar
		this.scrollBar.draw( this.aeGuiBridge );

		// Enable lighting
		GL11.glEnable( GL11.GL_LIGHTING );

		// Draw the widgets and get which one the mouse is over
		WidgetAEItem widgetUnderMouse = this.drawItemWidgets( mouseX, mouseY );

		// Get the cost
		List<ArcaneCrafingCost> craftingCost = ( (ContainerPartArcaneCraftingTerminal)this.inventorySlots ).getCraftingCost();

		// Does the current recipe have costs?
		if( craftingCost != null )
		{
			// Draw the costs
			this.drawCraftingAspects( craftingCost );
		}

		// Do we have a widget under the mouse?
		if( widgetUnderMouse != null )
		{
			// Get the tooltip from the widget
			widgetUnderMouse.getTooltip( this.tooltip );
		}
		else
		{
			// Get the tooltip from the buttons
			this.addTooltipFromButtons( mouseX, mouseY );
		}

		// Draw the tooltip
		this.drawTooltip( mouseX - this.guiLeft, mouseY - this.guiTop );

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
			// Slot the screen.
			this.mc.thePlayer.closeScreen();
		}
		else if( this.searchField.isFocused() )
		{
			// Pass the key to the search field.
			this.searchField.textboxKeyTyped( key, keyID );

			// Get the search query
			String newSearch = this.searchField.getText().trim().toLowerCase();

			// Has the query changed?
			if( !newSearch.equals( this.repo.searchString ) )
			{
				// Set the search string
				this.repo.searchString = newSearch;

				// Update the repo
				this.repo.updateView();

				// Update the scroll max
				this.updateScrollMaximum();

				// Update the widgets
				this.updateMEWidgets();
			}
		}
		// Home Key
		else if( keyID == Keyboard.KEY_HOME )
		{
			// Move the scroll all the way to home
			this.scrollBar.click( this.aeGuiBridge, AbstractGuiConstantsACT.SCROLLBAR_POS_X + 1, AbstractGuiConstantsACT.SCROLLBAR_POS_Y + 1 );
			this.scrollBar.wheel( 1 );
			this.updateMEWidgets();
		}
		// End Key
		else if( keyID == Keyboard.KEY_END )
		{
			// Move the scroll all the way to end
			this.scrollBar.click( this.aeGuiBridge, AbstractGuiConstantsACT.SCROLLBAR_POS_X + 1, AbstractGuiConstantsACT.SCROLLBAR_VERTICAL_BOUND );
			this.updateMEWidgets();

		}
		// Up Key
		else if( keyID == Keyboard.KEY_UP )
		{
			this.scrollBar.wheel( 1 );
			this.updateMEWidgets();
		}
		// Down Key
		else if( keyID == Keyboard.KEY_DOWN )
		{
			this.scrollBar.wheel( -1 );
			this.updateMEWidgets();
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
		// Was the click inside the ME grid?
		if( GuiHelper.instance.isPointInGuiRegion( AbstractGuiConstantsACT.ME_ITEM_POS_Y, AbstractGuiConstantsACT.ME_ITEM_POS_X,
			AbstractGuiConstantsACT.ME_GRID_HEIGHT, AbstractGuiConstantsACT.ME_GRID_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Is the player holding anything?
			if( this.player.inventory.getItemStack() != null )
			{
				// Inform the server the user would like to deposit the currently held item into the ME network.
				new PacketServerArcaneCraftingTerminal().createRequestDeposit( this.player, mouseButton ).sendPacketToServer();
			}
			else
			{
				// Search for the widget the mouse is over, and send extract request.
				this.sendItemWidgetClicked( mouseX, mouseY, mouseButton );
			}

			// Do not pass to super
			return;
		}

		// Is the user holding the space key?
		if( Keyboard.isKeyDown( Keyboard.KEY_SPACE ) )
		{
			// Get the slot the mouse is over
			Slot slotClicked = this.getSlotAtPosition( mouseX, mouseY );

			// Was there a slot under the mouse?
			if( slotClicked != null )
			{
				// Ask the server to move the inventory
				new PacketServerArcaneCraftingTerminal().createRequestDepositRegion( this.player, slotClicked.slotNumber ).sendPacketToServer();

				// Do not pass to super
				return;
			}
		}

		// Is the mouse over the scroll bar area?
		if( GuiHelper.instance.isPointInGuiRegion( AbstractGuiConstantsACT.SCROLLBAR_POS_Y, AbstractGuiConstantsACT.SCROLLBAR_POS_X,
			AbstractGuiConstantsACT.SCROLLBAR_HEIGHT, this.scrollBar.getWidth(), mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// The scroll bar now has mouse focus
			this.isScrollBarHeld = true;

			// Mark this Y
			this.scrollHeldPrevY = mouseY;

			// Jump the scroll to the mouse
			this.scrollBar.click( this.aeGuiBridge, mouseX - this.guiLeft, mouseY - this.guiTop );

			// Update the widgets
			this.updateMEWidgets();

			// Do not pass to super
			return;
		}

		// Was the mouse right-clicked over the search field?
		if( ( mouseButton == GuiHelper.MOUSE_BUTTON_RIGHT ) &&
						GuiHelper.instance.isPointInGuiRegion( AbstractGuiConstantsACT.SEARCH_POS_Y, AbstractGuiConstantsACT.SEARCH_POS_X,
							AbstractGuiConstantsACT.SEARCH_HEIGHT, AbstractGuiConstantsACT.SEARCH_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Clear the search field
			this.searchField.setText( "" );

			// Update the repo
			this.repo.searchString = "";
			this.repo.updateView();

			// Update the widgets
			this.updateMEWidgets();

			// Do not pass to super
			return;
		}

		// Inform search field.
		this.searchField.mouseClicked( mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton );

		// Pass to super
		super.mouseClicked( mouseX, mouseY, mouseButton );
	}

	/**
	 * Called when a button is clicked.
	 */
	@Override
	public void actionPerformed( final GuiButton button )
	{
		boolean sortingChanged = false;

		switch ( button.id )
		{
			case AbstractGuiConstantsACT.BUTTON_CLEAR_GRID_ID:
				// Attempt to clear the grid
				new PacketServerArcaneCraftingTerminal().createRequestClearGrid( this.player ).sendPacketToServer();
				break;

			case AbstractGuiConstantsACT.BUTTON_SORT_ORDER_ID:
				switch ( this.sortingOrder )
				{
					case AMOUNT:
						this.sortingOrder = SortOrder.MOD;
						break;

					case INVTWEAKS:
						break;

					case MOD:
						this.sortingOrder = SortOrder.NAME;
						break;

					case NAME:
						this.sortingOrder = SortOrder.AMOUNT;
						break;
				}
				sortingChanged = true;
				break;

			case AbstractGuiConstantsACT.BUTTON_SORT_DIR_ID:
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
		}

		// Should we update?
		if( sortingChanged )
		{
			// Update the sorting
			this.updateSorting();

			// Send to server
			new PacketServerArcaneCraftingTerminal().createRequestSetSort( this.player, this.sortingOrder, this.sortingDirection )
							.sendPacketToServer();
		}
	}

	@Override
	public void drawScreen( final int mouseX, final int mouseY, final float mouseBtn )
	{
		// Call super
		super.drawScreen( mouseX, mouseY, mouseBtn );

		// Is the mouse holding the scroll bar?
		if( this.isScrollBarHeld )
		{
			// Is the mouse button still being held down?
			if( Mouse.isButtonDown( GuiHelper.MOUSE_BUTTON_LEFT ) )
			{
				// Has the Y changed?
				if( mouseY == this.scrollHeldPrevY )
				{
					return;
				}

				boolean correctForZero = false;

				// Mark the Y
				this.scrollHeldPrevY = mouseY;

				// Calculate the Y position for the scroll bar
				int repY = mouseY - this.guiTop;

				// Has the mouse exceeded the 'upper' bound?
				if( repY > AbstractGuiConstantsACT.SCROLLBAR_VERTICAL_BOUND )
				{
					repY = AbstractGuiConstantsACT.SCROLLBAR_VERTICAL_BOUND;
				}
				// Has the mouse exceeded the 'lower' bound?
				else if( repY <= AbstractGuiConstantsACT.SCROLLBAR_POS_Y )
				{
					repY = AbstractGuiConstantsACT.SCROLLBAR_POS_Y;

					// We will have to correct for zero
					correctForZero = true;
				}

				// Update the scroll bar
				this.scrollBar.click( this.aeGuiBridge, AbstractGuiConstantsACT.SCROLLBAR_POS_X + 1, repY );

				// Should we correct for zero?
				if( correctForZero )
				{
					this.scrollBar.wheel( 1 );
				}

				// Update the widgets
				this.updateMEWidgets();
			}
			else
			{
				// The scroll bar no longer has mouse focus
				this.isScrollBarHeld = false;
			}
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
		return ViewItems.STORED;
	}

	/**
	 * If the mouse wheel has moved, passes the data to the scrollbar
	 */
	@Override
	public void handleMouseInput()
	{
		// Call super
		super.handleMouseInput();

		// Get the delta z for the scroll wheel
		int deltaZ = Mouse.getEventDWheel();

		// Did it move?
		if( deltaZ != 0 )
		{
			// Is shift being held?
			if( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
			{
				// Extract or insert based on the motion of the wheel
				this.doMEWheelAction( deltaZ );
			}
			else
			{
				// Inform the scroll bar
				this.scrollBar.wheel( deltaZ );

				// Update the item widgets
				this.updateMEWidgets();
			}
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

		// Enable repeat keys
		Keyboard.enableRepeatEvents( true );

		// Request a full update from the server
		new PacketServerArcaneCraftingTerminal().createRequestFullList( this.player ).sendPacketToServer();

		// Set up the search bar
		this.searchField = new GuiTextField( this.fontRendererObj, AbstractGuiConstantsACT.SEARCH_POS_X, AbstractGuiConstantsACT.SEARCH_POS_Y,
						AbstractGuiConstantsACT.SEARCH_WIDTH, AbstractGuiConstantsACT.SEARCH_HEIGHT );

		// Set the search field to draw in the foreground
		this.searchField.setEnableBackgroundDrawing( false );

		// Start focused
		this.searchField.setFocused( true );

		// Set maximum length
		this.searchField.setMaxStringLength( AbstractGuiConstantsACT.SEARCH_MAX_CHARS );

		// Clear any existing buttons
		this.buttonList.clear();

		// Create the clear grid button
		this.buttonList.add( new ButtonClearCraftingGrid( AbstractGuiConstantsACT.BUTTON_CLEAR_GRID_ID, this.guiLeft +
						AbstractGuiConstantsACT.BUTTON_CLEAR_GRID_POS_X, this.guiTop + AbstractGuiConstantsACT.BUTTON_CLEAR_GRID_POS_Y, 8, 8 ) );

		// Create the AE bridge
		this.aeGuiBridge = new AEBaseGui( this.inventorySlots )
		{

			@Override
			public void bindTexture( final String file )
			{
				this.bindTexture( "appliedenergistics2", file );
			}

			@Override
			public void bindTexture( final String base, final String file )
			{
				GuiArcaneCraftingTerminal.this.mc.getTextureManager().bindTexture( new ResourceLocation( base, "textures/" + file ) );
			}

			@Override
			public void drawBG( final int arg0, final int arg1, final int arg2, final int arg3 )
			{
				// Ignored
			}

			@Override
			public void drawFG( final int arg0, final int arg1, final int arg2, final int arg3 )
			{
				// Ignored
			}

			@Override
			public void drawTexturedModalRect( final int posX, final int posY, final int sourceOffsetX, final int sourceOffsetY, final int width,
												final int height )
			{
				GuiArcaneCraftingTerminal.this.drawTexturedModalRect( posX, posY, sourceOffsetX, sourceOffsetY, width, height );
			}
		};

		// Setup the scroll bar
		this.scrollBar.setLeft( AbstractGuiConstantsACT.SCROLLBAR_POS_X ).setTop( AbstractGuiConstantsACT.SCROLLBAR_POS_Y )
						.setHeight( AbstractGuiConstantsACT.SCROLLBAR_HEIGHT );

		// No scrolling until we get items
		this.scrollBar.setRange( 0, 0, 1 );

		// Add sort order button
		this.buttonList.add( new ButtonSortingMode( AbstractGuiConstantsACT.BUTTON_SORT_ORDER_ID, this.guiLeft +
						AbstractGuiConstantsACT.BUTTON_SORT_ORDER_POS_X, this.guiTop + AbstractGuiConstantsACT.BUTTON_SORT_ORDER_POS_Y,
						AbstractGuiConstantsACT.BUTTON_SORT_SIZE, AbstractGuiConstantsACT.BUTTON_SORT_SIZE ) );

		// Add sort direction button
		this.buttonList.add( new ButtonSortingDirection( AbstractGuiConstantsACT.BUTTON_SORT_DIR_ID, this.guiLeft +
						AbstractGuiConstantsACT.BUTTON_SORT_DIR_POS_X, this.guiTop + AbstractGuiConstantsACT.BUTTON_SORT_DIR_POS_Y,
						AbstractGuiConstantsACT.BUTTON_SORT_SIZE, AbstractGuiConstantsACT.BUTTON_SORT_SIZE ) );

	}

	/**
	 * Called when the gui is closing.
	 */
	@Override
	public void onGuiClosed()
	{
		// Disable repeat keys
		Keyboard.enableRepeatEvents( false );
	}

	/**
	 * Called to update what the player is holding.
	 * 
	 * @param heldItemstack
	 */
	public void onPlayerHeldReceived( final IAEItemStack heldItemstack )
	{
		ItemStack itemStack = null;

		// Get the stack
		if( heldItemstack != null )
		{
			itemStack = heldItemstack.getItemStack();
		}

		// Set what the player is holding
		this.player.inventory.setItemStack( itemStack );
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
		this.repo.updateView();

		// Update the scroll bar
		this.updateScrollMaximum();

		// Update the widgets
		this.updateMEWidgets();
	}

	/**
	 * Called when the server sends a full list of all
	 * items in the AE network in response to our request.
	 * 
	 * @param itemList
	 */
	public void onReceiveFullList( final IItemList<IAEItemStack> itemList )
	{
		// Update the repository
		for( IAEItemStack stack : itemList )
		{
			this.repo.postUpdate( stack );
		}
		this.repo.updateView();

		// Update the scroll bar
		this.updateScrollMaximum();

		// Update the widgets
		this.updateMEWidgets();
	}

	/**
	 * Called when the server sends the sorting order and direction.
	 * 
	 * @param order
	 * @param direction
	 */
	public void onReceiveSorting( final SortOrder order, final SortDir direction )
	{
		// Set the direction
		this.sortingDirection = direction;

		// Set the order
		this.sortingOrder = order;

		// Update
		this.updateSorting();
	}

}
