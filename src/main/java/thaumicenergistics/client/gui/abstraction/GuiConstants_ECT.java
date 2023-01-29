package thaumicenergistics.client.gui.abstraction;

import net.minecraft.inventory.Container;

import thaumicenergistics.client.gui.GuiEssentiaCellTerminal;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Holds all the constants for {@link GuiEssentiaCellTerminal}.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public abstract class GuiConstants_ECT extends ThEScrollbarGui {

    /**
     * Sort mode button position.
     */
    protected static final int BUTTON_SORT_MODE_POS_X = -18, BUTTON_SORT_MODE_POS_Y = 9;

    /**
     * View mode button.
     */
    protected static final int BUTTON_VIEW_MODE_POS_X = -18,
            BUTTON_VIEW_MODE_POS_Y = GuiConstants_ECT.BUTTON_SORT_MODE_POS_Y + 20;

    /**
     * Size of the gui.
     */
    protected static final int GUI_WIDTH = 195, GUI_HEIGHT = 204;

    /**
     * Width and height of the mode buttons.
     */
    protected static final int MODE_BUTTON_SIZE = 16;

    /**
     * Search bar.
     */
    protected static final int SEARCH_X_OFFSET = 98, SEARCH_Y_OFFSET = 6, SEARCH_WIDTH = 69, SEARCH_HEIGHT = 10;

    /**
     * The maximum number of displayable characters.
     */
    protected static final int SEARCH_MAX_CHARS = 14;

    /**
     * Scroll bar.
     */
    protected static final int SCROLLBAR_POS_X = 175, SCROLLBAR_POS_Y = 18, SCROLLBAR_HEIGHT = 70;

    /**
     * Selected aspect info positions.
     */
    protected static final int SELECTED_INFO_POS_X = 45, SELECTED_INFO_NAME_POS_Y = 91,
            SELECTED_INFO_AMOUNT_POS_Y = 101;

    /**
     * Position of the title.
     */
    protected static final int TITLE_POS_X = 7, TITLE_POS_Y = 6;

    /**
     * Widget offsets.
     */
    protected static final int WIDGET_OFFSET_X = 7, WIDGET_OFFSET_Y = 17;

    /**
     * Number of widgets per row.
     */
    protected static final int WIDGETS_PER_ROW = 9;

    /**
     * Number of rows per page.
     */
    protected static final int WIDGET_ROWS_PER_PAGE = 4;

    /**
     * Number of widgets per page.
     */
    protected static final int WIDGETS_PER_PAGE = GuiConstants_ECT.WIDGETS_PER_ROW
            * GuiConstants_ECT.WIDGET_ROWS_PER_PAGE;

    public GuiConstants_ECT(final Container container) {
        super(container);
    }
}
