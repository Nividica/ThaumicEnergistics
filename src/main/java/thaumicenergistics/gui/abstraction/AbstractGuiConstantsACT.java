package thaumicenergistics.gui.abstraction;

import net.minecraft.inventory.Container;

/**
 * Holds all constants for the Arcane Crafting Terminal GUI
 * 
 * @author Nividica
 * 
 */
public abstract class AbstractGuiConstantsACT
	extends AbstractGuiWithScrollbar
{

	/**
	 * Starting X position for craft aspects
	 */
	protected static final int ASPECT_COST_POS_X = 136;

	/**
	 * Starting Y position for craft aspects
	 */
	protected static final int ASPECT_COST_POS_Y = 90;

	/**
	 * Distance between aspect icons
	 */
	protected static final int ASPECT_COST_SPACING = 18;

	/**
	 * Rate at which to blink the aspect if there is not enough in the wand.
	 */
	protected static final double ASPECT_COST_BLINK_SPEED = 0.5D;

	/**
	 * Minimum transparency of the aspect if there is not enough in the wand.
	 */
	protected static final float ASPECT_COST_MIN_ALPHA = 0.25F;

	/**
	 * Minimum transparency of the aspect if there is not enough in the wand.
	 */
	protected static final float ASPECT_COST_MAX_ALPHA = 0.75F;

	/**
	 * ID of the clear grid button
	 */
	protected static final int BUTTON_CLEAR_GRID_ID = 0;

	/**
	 * X offset to draw the clear grid button.
	 */
	protected static final int BUTTON_CLEAR_GRID_POS_X = 98;

	/**
	 * Y offset to draw the clear grid button.
	 */
	protected static final int BUTTON_CLEAR_GRID_POS_Y = 89;

	protected static final int BUTTON_SORT_ORDER_ID = 1;

	protected static final int BUTTON_SORT_ORDER_POS_X = -18;

	protected static final int BUTTON_SORT_ORDER_POS_Y = 8;

	protected static final int BUTTON_SORT_DIR_ID = 2;

	protected static final int BUTTON_SORT_DIR_POS_X = BUTTON_SORT_ORDER_POS_X;

	protected static final int BUTTON_SORT_DIR_POS_Y = BUTTON_SORT_ORDER_POS_Y + 20;

	protected static final int BUTTON_SORT_SIZE = 16;

	/**
	 * The width of the gui
	 */
	protected static final int GUI_WIDTH = 194;

	/**
	 * The height of the gui
	 */
	protected static final int GUI_HEIGHT = 243;

	/**
	 * Number of rows in the ME inventory grid.
	 */
	protected static final int ME_ROWS = 3;

	/**
	 * Number of columns in the ME inventory grid.
	 */
	protected static final int ME_COLUMNS = 9;

	/**
	 * Total number of item widgets.
	 */
	protected static final int ME_WIDGET_COUNT = ME_ROWS * ME_COLUMNS;

	/**
	 * Starting X position of the items.
	 */
	protected static final int ME_ITEM_POS_X = 7;

	/**
	 * Starting Y position of the items.
	 */
	protected static final int ME_ITEM_POS_Y = 17;

	/**
	 * Total width of the ME item grid.
	 */
	protected static final int ME_GRID_WIDTH = 161;

	/**
	 * Total height of the ME item grid.
	 */
	protected static final int ME_GRID_HEIGHT = 53;

	/**
	 * X position of the scroll bar
	 */
	protected static final int SCROLLBAR_POS_X = 175;

	/**
	 * Y position of the scroll bar
	 */
	protected static final int SCROLLBAR_POS_Y = 18;

	/**
	 * Height of the scroll bar
	 */
	protected static final int SCROLLBAR_HEIGHT = 52;

	/**
	 * X offset to draw the search field.
	 */
	protected static final int SEARCH_POS_X = 98;

	/**
	 * Y offset to draw the search field.
	 */
	protected static final int SEARCH_POS_Y = 6;

	/**
	 * Width of the search field.
	 */
	protected static final int SEARCH_WIDTH = 65;

	/**
	 * Height of the search field.
	 */
	protected static final int SEARCH_HEIGHT = 10;

	/**
	 * The maximum number of characters that can be typed in.
	 */
	protected static final int SEARCH_MAX_CHARS = 15;

	/**
	 * X position of the title string
	 */
	protected static final int TITLE_POS_X = 8;

	/**
	 * Y position of the title string
	 */
	protected static final int TITLE_POS_Y = 6;

	/**
	 * Determines how often to update the tooltip when the mouse is not moving.
	 */
	protected static final long WIDGET_TOOLTIP_UPDATE_INTERVAL = 3000L;

	public AbstractGuiConstantsACT( final Container container )
	{
		super( container );
	}

}
