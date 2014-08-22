package thaumicenergistics.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.gui.buttons.ButtonClearCraftingGrid;
import thaumicenergistics.gui.widget.AbstractWidget;
import thaumicenergistics.gui.widget.WidgetAEItem;
import thaumicenergistics.network.packet.server.PacketServerArcaneCraftingTerminal;
import thaumicenergistics.parts.AEPartArcaneCraftingTerminal;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.GuiHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.render.AppEngRenderItem;

public class GuiArcaneCraftingTerminal
	extends GuiWidgetHost
{
	/**
	 * The width of the gui
	 */
	private final int GUI_WIDTH = 175;

	/**
	 * The height of the gui
	 */
	private final int GUI_HEIGHT = 243;

	/**
	 * X position of the title string
	 */
	private static final int TITLE_POS_X = 8;

	/**
	 * Y position of the title string
	 */
	private static final int TITLE_POS_Y = 6;

	/**
	 * Number of rows in the ME inventory grid.
	 */
	private static final int ME_ROWS = 3;

	/**
	 * Number of columns in the ME inventory grid.
	 */
	private static final int ME_COLUMNS = 9;

	/**
	 * Total number of item widgets.
	 */
	private static final int ME_WIDGET_COUNT = ME_ROWS * ME_COLUMNS;

	/**
	 * Starting X position of the items.
	 */
	private static final int ME_ITEM_POS_X = 7;

	/**
	 * Starting Y position of the items.
	 */
	private static final int ME_ITEM_POS_Y = 17;

	/**
	 * Total width of the ME item grid.
	 */
	private static final int ME_GRID_WIDTH = 161;

	/**
	 * Total height of the ME item grid.
	 */
	private static final int ME_GRID_HEIGHT = 53;

	/**
	 * X offset to draw the search field.
	 */
	private static final int SEARCH_X_OFFSET = 98;

	/**
	 * Y offset to draw the search field.
	 */
	private static final int SEARCH_Y_OFFSET = 6;

	/**
	 * Width of the search field.
	 */
	private static final int SEARCH_WIDTH = 65;

	/**
	 * Height of the search field.
	 */
	private static final int SEARCH_HEIGHT = 10;

	/**
	 * The maximum number of characters that can be typed in.
	 */
	private static final int SEARCH_MAX_CHARS = 15;

	/**
	 * X offset to draw the clear grid button.
	 */
	private static final int CLEAR_GRID_POS_X = 98;

	/**
	 * Y offset to draw the clear grid button.
	 */
	private static final int CLEAR_GRID_POS_Y = 89;

	/**
	 * ID of the clear grid button
	 */
	private static final int CLEAR_GRID_ID = 0;

	/**
	 * Holds the list of all items in the ME network
	 */
	private IItemList<IAEItemStack> networkItems = null;

	/**
	 * Holds a list of changes sent to the gui before the
	 * full list is sent.
	 */
	private List<IAEItemStack> pendingChanges = new ArrayList<IAEItemStack>();

	/**
	 * Renders an AE itemstack into the gui.
	 */
	private AppEngRenderItem aeItemRenderer = new AppEngRenderItem();

	/**
	 * Set to true once a full list request is sent to the server.
	 */
	private boolean hasRequested = false;

	/**
	 * Translated title of the gui.
	 */
	private String guiTitle;

	/**
	 * Widget 'slots'.
	 */
	private WidgetAEItem[] itemWidgets = new WidgetAEItem[GuiArcaneCraftingTerminal.ME_WIDGET_COUNT];

	/**
	 * Player viewing the gui.
	 */
	private EntityPlayer player;

	/**
	 * Search field.
	 */
	private GuiTextField searchBar;

	/**
	 * The current search query as typed in by the player.
	 */
	private String searchQuery = "";

	public GuiArcaneCraftingTerminal( AEPartArcaneCraftingTerminal part, EntityPlayer player )
	{
		// Call super
		super( new ContainerPartArcaneCraftingTerminal( part, player ) );

		// Set the player
		this.player = player;

		// Set the width and height
		this.xSize = this.GUI_WIDTH;
		this.ySize = this.GUI_HEIGHT;

		// Set the title
		this.guiTitle = StatCollector.translateToLocal( "thaumicenergistics.gui.arcane.crafting.terminal.title" );

		// Create the widgets
		for( int row = 0; row < GuiArcaneCraftingTerminal.ME_ROWS; row++ )
		{
			for( int column = 0; column < GuiArcaneCraftingTerminal.ME_COLUMNS; column++ )
			{
				// Calculate the index
				int index = ( row * GuiArcaneCraftingTerminal.ME_COLUMNS ) + column;

				this.itemWidgets[index] = new WidgetAEItem( this, GuiArcaneCraftingTerminal.ME_ITEM_POS_X + ( column * AbstractWidget.WIDGET_SIZE ),
								GuiArcaneCraftingTerminal.ME_ITEM_POS_Y + ( row * AbstractWidget.WIDGET_SIZE ), this.aeItemRenderer );
			}
		}

	}

	/**
	 * If the user has clicked on an item widget this will inform the server
	 * so that the item can be extracted from the AE network.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 */
	private void sendItemWidgetClicked( int mouseX, int mouseY, int mouseButton )
	{
		for( int index = 0; index < GuiArcaneCraftingTerminal.ME_WIDGET_COUNT; index++ )
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
	private void updateWidgetItems()
	{
		// Do we have any network items?
		if( this.networkItems == null )
		{
			return;
		}

		// Create an iterator
		Iterator<IAEItemStack> networkItemIterator = this.networkItems.iterator();

		// Is there a search term?
		if( !this.searchQuery.equals( "" ) )
		{
			// Pass to update with search
			this.updateWithSearch( networkItemIterator );

			return;
		}

		// List all items
		for( int index = 0; index < GuiArcaneCraftingTerminal.ME_WIDGET_COUNT; index++ )
		{
			// Does the iterator have a next?
			if( networkItemIterator.hasNext() )
			{
				// Set the item
				this.itemWidgets[index].setItemStack( networkItemIterator.next() );
			}
			else
			{
				// Set to null
				this.itemWidgets[index].setItemStack( null );
			}
		}
	}

	/**
	 * Assigns the network items to the widgets, skipping any item
	 * that does not match the search query.
	 * 
	 * @param networkItemIterator
	 */
	private void updateWithSearch( Iterator<IAEItemStack> networkItemIterator )
	{
		int widgetIndex = 0;

		String localName;

		while( networkItemIterator.hasNext() )
		{
			// Get the next item
			IAEItemStack nextItemStack = networkItemIterator.next();

			// Get the itemstack display name
			localName = nextItemStack.getItemStack().getDisplayName();

			// Does the name match?
			if( localName.toLowerCase().contains( this.searchQuery ) )
			{
				// Found a match assign it
				this.itemWidgets[widgetIndex].setItemStack( nextItemStack );

				// Increment the widget index
				widgetIndex++ ;

				// Have we run out of widgets?
				if( widgetIndex == GuiArcaneCraftingTerminal.ME_WIDGET_COUNT )
				{
					// Stop searching
					break;
				}
			}
		}

		// Fill any remaining slots with nul
		for( ; widgetIndex < GuiArcaneCraftingTerminal.ME_WIDGET_COUNT; widgetIndex++ )
		{
			this.itemWidgets[widgetIndex].setItemStack( null );
		}
	}

	/**
	 * Draws the gui texture
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( float alpha, int mouseX, int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Set the texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ARCANE_CRAFTING_TERMINAL.getTexture() );

		// Draw the gui image
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT );

		// Draw the search field.
		this.searchBar.drawTextBox();

	}

	/**
	 * Draw the foreground layer.
	 */
	@Override
	protected void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
	{
		boolean noOverlay = true;

		WidgetAEItem widgetUnderMouse = null;

		// Draw the title
		this.fontRendererObj.drawString( this.guiTitle, GuiArcaneCraftingTerminal.TITLE_POS_X, GuiArcaneCraftingTerminal.TITLE_POS_Y, 0x000000 );

		GL11.glEnable( GL11.GL_LIGHTING );
		GL11.glEnable( GL11.GL_DEPTH_TEST );

		// Draw the item widgets
		for( int index = 0; index < GuiArcaneCraftingTerminal.ME_WIDGET_COUNT; index++ )
		{
			// Get the widget
			WidgetAEItem currentWidget = this.itemWidgets[index];

			// Draw the widget
			currentWidget.drawWidget();

			// Is the mouse over this widget?
			if( noOverlay && currentWidget.isMouseOverWidget( mouseX, mouseY ) )
			{
				// Draw the overlay
				currentWidget.drawMouseHoverUnderlay();

				// Set that we have an overlay
				noOverlay = false;

				// Set this widget as the widget under the mouse
				widgetUnderMouse = currentWidget;
			}
		}

		// Do we have a widget under the mouse?
		if( widgetUnderMouse != null )
		{
			// Ask it to draw it's tooltip
			widgetUnderMouse.drawTooltip( mouseX - this.guiLeft, mouseY - this.guiTop );
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
		if( keyID == Keyboard.KEY_ESCAPE )
		{
			// Slot the screen.
			this.mc.thePlayer.closeScreen();
		}
		else
		{
			String oldQuery = this.searchQuery;

			// Get the search query
			this.searchQuery = this.searchBar.getText().trim().toLowerCase();

			// Has the query changed?
			if( !this.searchQuery.equals( oldQuery ) )
			{
				// Re-search the widgets
				this.updateWidgetItems();
			}
		}

	}

	/**
	 * Called when the mouse is clicked while the gui is open
	 */
	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseButton )
	{
		// Was the click inside the ME grid?
		if( GuiHelper.isPointInGuiRegion( GuiArcaneCraftingTerminal.ME_ITEM_POS_X, GuiArcaneCraftingTerminal.ME_ITEM_POS_Y,
			GuiArcaneCraftingTerminal.ME_GRID_HEIGHT, GuiArcaneCraftingTerminal.ME_GRID_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
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
		}
		else
		{
			// Pass to search field.
			this.searchBar.mouseClicked( mouseX, mouseY, mouseButton );

			// Pass to super
			super.mouseClicked( mouseX, mouseY, mouseButton );
		}
	}

	/**
	 * Called when a button is clicked.
	 */
	@Override
	public void actionPerformed( GuiButton button )
	{
		// Was it the clear grid button?
		if( button.id == GuiArcaneCraftingTerminal.CLEAR_GRID_ID )
		{
			// Attempt to clear the grid
			new PacketServerArcaneCraftingTerminal().createRequestClearGrid( this.player ).sendPacketToServer();
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
		this.hasRequested = true;

		// Set up the search bar
		this.searchBar = new GuiTextField( this.fontRendererObj, this.guiLeft + GuiArcaneCraftingTerminal.SEARCH_X_OFFSET, this.guiTop +
						GuiArcaneCraftingTerminal.SEARCH_Y_OFFSET, GuiArcaneCraftingTerminal.SEARCH_WIDTH, GuiArcaneCraftingTerminal.SEARCH_HEIGHT );

		// Set the search field to draw in the foreground
		this.searchBar.setEnableBackgroundDrawing( false );

		// Start focused
		this.searchBar.setFocused( true );

		// Set maximum length
		this.searchBar.setMaxStringLength( GuiArcaneCraftingTerminal.SEARCH_MAX_CHARS );

		// Clear any existing buttons
		this.buttonList.clear();

		// Create the clear grid button
		this.buttonList.add( new ButtonClearCraftingGrid( GuiArcaneCraftingTerminal.CLEAR_GRID_ID, this.guiLeft +
						GuiArcaneCraftingTerminal.CLEAR_GRID_POS_X, this.guiTop + GuiArcaneCraftingTerminal.CLEAR_GRID_POS_Y, 8, 8 ) );

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
	public void onPlayerHeldReceived( IAEItemStack heldItemstack )
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
	public void onReceiveChange( IAEItemStack change )
	{
		// Is the change not null?
		if( change != null )
		{
			// Do we yet have a requested list?
			if( this.hasRequested && ( this.networkItems == null ) )
			{
				// Add to the pending list
				this.pendingChanges.add( change );
				return;
			}

			// Get the matching item
			IAEItemStack networkItem = this.networkItems.findPrecise( change );

			// Is there a match?
			if( networkItem != null )
			{
				// Adjust its amount
				networkItem.incStackSize( change.getStackSize() );
			}
			else if( change.getStackSize() > 0 )
			{
				// Add to the list
				this.networkItems.add( change );
			}
		}

		// Update widgets only if there are not pending changes
		if( this.pendingChanges.isEmpty() )
		{
			this.updateWidgetItems();
		}
	}

	/**
	 * Called when the server sends a full list of all
	 * items in the AE network in response to our request.
	 * 
	 * @param itemList
	 */
	public void onReceiveFullList( IItemList<IAEItemStack> itemList )
	{
		// Set the list
		this.networkItems = itemList;

		// Check pending changes
		if( ( this.networkItems != null ) && ( !this.pendingChanges.isEmpty() ) )
		{
			// Update list with pending changes
			for( int index = 0; index < this.pendingChanges.size(); index++ )
			{
				this.onReceiveChange( this.pendingChanges.get( index ) );
			}

			// Clear pending
			this.pendingChanges.clear();
		}

		// Update the widgets
		this.updateWidgetItems();
	}

	/**
	 * Expose the render tooltip function as public
	 */
	@Override
	public void renderToolTip( ItemStack itemStack, int xPosition, int yPosition )
	{
		super.renderToolTip( itemStack, xPosition, yPosition );
	}

}
