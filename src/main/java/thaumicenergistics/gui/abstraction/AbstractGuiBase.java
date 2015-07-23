package thaumicenergistics.gui.abstraction;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import thaumicenergistics.container.ContainerWithNetworkTool;
import thaumicenergistics.container.slot.SlotNetworkTool;
import thaumicenergistics.gui.buttons.AbstractGuiButtonBase;
import thaumicenergistics.gui.widget.IWidgetHost;
import thaumicenergistics.texture.AEStateIconsEnum;
import thaumicenergistics.util.GuiHelper;
import appeng.api.AEApi;
import appeng.parts.automation.UpgradeInventory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base gui of all Thaumic Energistics guis
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public abstract class AbstractGuiBase
	extends GuiContainer
	implements IWidgetHost
{
	/**
	 * Pixel area represented by the top-left and bottom-right corners of a
	 * rectangle.
	 * 
	 * @author Nividica
	 * 
	 */
	private class Bounds
	{
		/**
		 * Top Y position.
		 */
		public int T;

		/**
		 * Left X position.
		 */
		public int L;

		/**
		 * Bottom Y position.
		 */
		public int B;

		/**
		 * Right X position.
		 */
		public int R;

		/**
		 * Creates the boundary
		 * 
		 * @param t
		 * Top Y position.
		 * @param l
		 * Left X position.
		 * @param b
		 * Bottom Y position.
		 * @param r
		 * Right X position.
		 */
		public Bounds( final int t, final int l, final int b, final int r )
		{
			this.T = t;
			this.L = l;
			this.B = b;
			this.R = r;
		}
	}

	/**
	 * Tooltip offset from the mouse.
	 */
	private static final int TOOLTIP_OFFSET = 12;

	/**
	 * Height of a tooltip with no text.
	 */
	private static final int TOOLTIP_EMPTY_HEIGHT = 8;

	/**
	 * (Top) Margin from the borders to start drawing text.
	 */
	private static final int TOOLTIP_HEIGHT_MARGIN = 2;

	/**
	 * Height of each line of text.
	 */
	private static final int TOOLTIP_LINE_HEIGHT = 10;

	/**
	 * Color of the tooltip's outer borders.
	 */
	private static final int TOOLTIP_COLOR_OUTER = 0xFF000000;

	/**
	 * Color of the tooltip background.
	 */
	private static final int TOOLTIP_COLOR_BACKGROUND = 0xF0100010;

	/**
	 * Starting color of the tooltip's inner borders.
	 */
	private static final int TOOLTIP_COLOR_INNER_BEGIN = 0xC05000FF;

	/**
	 * Ending color of the tooltip's inner borders.
	 */
	private static final int TOOLTIP_COLOR_INNER_END = 0xC05000FF;

	/**
	 * Thickness of the tooltip's borders.
	 */
	private static final int TOOLTIP_BORDER_SIZE = 3;

	private static final int upgradeU = AEStateIconsEnum.UPGRADE_CARD_BACKGROUND.getU();
	private static final int upgradeV = AEStateIconsEnum.UPGRADE_CARD_BACKGROUND.getV();
	private static final int upgradeWidth = AEStateIconsEnum.UPGRADE_CARD_BACKGROUND.getWidth();
	private static final int upgradeHeight = AEStateIconsEnum.UPGRADE_CARD_BACKGROUND.getHeight();

	/**
	 * Lines to draw when drawTooltip is called.
	 */
	protected final List<String> tooltip = new ArrayList<String>();

	public AbstractGuiBase( final Container container )
	{
		super( container );
	}

	/**
	 * Draws the background, outer borders, and inner borders for a tooltip.
	 * 
	 * @param guiObject
	 * @param drawGradientRect
	 * @param bounds
	 * @throws Exception
	 */
	private final void drawTooltipBackground( final Bounds bounds )
	{
		// Background
		this.drawGradientRect( bounds.L, bounds.T, bounds.R, bounds.B, AbstractGuiBase.TOOLTIP_COLOR_BACKGROUND,
			AbstractGuiBase.TOOLTIP_COLOR_BACKGROUND );

		// Draw outer borders
		this.drawTooltipBorders( bounds, AbstractGuiBase.TOOLTIP_COLOR_OUTER, AbstractGuiBase.TOOLTIP_COLOR_OUTER, 0 );

		// Adjust bounds for inner borders
		bounds.T++ ;
		bounds.L++ ;
		bounds.B-- ;
		bounds.R-- ;

		// Draw inner borders
		this.drawTooltipBorders( bounds, AbstractGuiBase.TOOLTIP_COLOR_INNER_BEGIN, AbstractGuiBase.TOOLTIP_COLOR_INNER_END, 1 );
	}

	/**
	 * Draws the vertical and horizontal borders for a tooltip
	 * 
	 * @param guiObject
	 * @param drawGradientRect
	 * @param bounds
	 * @param colorStart
	 * @param colorEnd
	 * @param cornerExpansion
	 * 1 to connect corners, 0 to leave notches.
	 * @throws Exception
	 */
	private final void drawTooltipBorders( final Bounds bounds, final int colorStart, final int colorEnd, final int cornerExpansion )
	{
		// Left
		this.drawGradientRect( bounds.L - 1, bounds.T - cornerExpansion, bounds.L, bounds.B + cornerExpansion, colorStart, colorEnd );

		// Top
		this.drawGradientRect( bounds.L, bounds.T - 1, bounds.R, bounds.T, colorStart, colorEnd );

		// Right
		this.drawGradientRect( bounds.R, bounds.T - cornerExpansion, bounds.R + 1, bounds.B + cornerExpansion, colorStart, colorEnd );

		// Bottom
		this.drawGradientRect( bounds.L, bounds.B, bounds.R, bounds.B + 1, colorStart, colorEnd );
	}

	/**
	 * Checks if the specified point is within the bounds of the specified slot.
	 * 
	 * @param slot
	 * @param x
	 * @param y
	 * @return True if the point is within the slot, false otherwise.
	 */
	private final boolean isPointWithinSlot( final Slot slot, final int x, final int y )
	{
		return GuiHelper.INSTANCE.isPointInGuiRegion( slot.yDisplayPosition, slot.xDisplayPosition, 16, 16, x, y, this.guiLeft, this.guiTop );
	}

	/**
	 * Checks if a not-left-click was on a button.<BR>
	 * Left click is handled by GuiScreen.mouseClicked()
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @return True if click was handled.
	 */
	private final boolean nonLeftClickHandler_Buttons( final int mouseX, final int mouseY, final int mouseButton )
	{
		if( mouseButton != GuiHelper.MOUSE_BUTTON_LEFT )
		{
			// Mouse over button?
			for( Object buttonObj : this.buttonList )
			{
				// Cast
				GuiButton button = (GuiButton)buttonObj;

				// Was mouse over the button?
				if( button.mousePressed( this.mc, mouseX, mouseY ) )
				{
					// Play clicky sound
					button.func_146113_a( this.mc.getSoundHandler() );

					// Call button click event
					this.onButtonClicked( button, mouseButton );

					// Handled
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Adds to the tooltip based on which button the mouse is over.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @return True when a tooltip was added, false otherwise.
	 */
	protected final boolean addTooltipFromButtons( final int mouseX, final int mouseY )
	{
		// Is the mouse over any buttons?
		for( Object obj : this.buttonList )
		{
			// Is it a base button?
			if( obj instanceof AbstractGuiButtonBase )
			{
				// Cast
				AbstractGuiButtonBase currentButton = (AbstractGuiButtonBase)obj;

				// Is the mouse over it?
				if( currentButton.isMouseOverButton( mouseX, mouseY ) )
				{
					// Get the tooltip
					currentButton.getTooltip( this.tooltip );

					// And stop searching
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Draws the slot backgrounds for the network tool and upgrade slots.
	 * 
	 * @param alpha
	 * @param mouseX
	 * @param mouseY
	 */
	protected final void drawAEToolAndUpgradeSlots( final float alpha, final int mouseX, final int mouseY )
	{
		Minecraft.getMinecraft().renderEngine.bindTexture( AEStateIconsEnum.AE_STATES_TEXTURE );

		// Locate any upgrade or network slots
		for( int i = 0; i < this.inventorySlots.inventorySlots.size(); i++ )
		{
			// Get the slot
			Slot slot = (Slot)this.inventorySlots.inventorySlots.get( i );

			// Is it network or upgrade?
			if( ( slot instanceof SlotNetworkTool ) || ( slot.inventory instanceof UpgradeInventory ) )
			{
				// Draw background
				this.drawTexturedModalRect( this.guiLeft + slot.xDisplayPosition, this.guiTop + slot.yDisplayPosition, AbstractGuiBase.upgradeU,
					AbstractGuiBase.upgradeV, AbstractGuiBase.upgradeWidth, AbstractGuiBase.upgradeHeight );
			}
		}
	}

	/**
	 * Draws an on-screen tooltip box based on the string in this.tooltip.
	 * The tooltip is cleared after being drawn.
	 * 
	 * @param posX
	 * X anchor position to draw the tooltip. Generally the mouse's X position.
	 * @param posY
	 * Y anchor position to draw the tooltip. Generally the mouse's Y position.
	 */
	protected final void drawTooltip( int posX, int posY, final boolean clearTooltipAfterDraw )
	{
		if( !this.tooltip.isEmpty() )
		{
			// Disable rescaling
			GL11.glDisable( GL12.GL_RESCALE_NORMAL );

			// Disable lighting
			GL11.glDisable( GL11.GL_LIGHTING );

			// Disable depth testing
			GL11.glDisable( GL11.GL_DEPTH_TEST );

			// Bounds check the position
			if( posY < 0 )
			{
				posY = 0;
			}

			// Assume string length is zero
			int maxStringLength_px = 0;

			// Get max string length from lines in the list
			for( String string : this.tooltip )
			{
				// Get the length of the string
				int stringLen = this.mc.fontRenderer.getStringWidth( string );

				// Is it larger than the previous length?
				if( stringLen > maxStringLength_px )
				{
					// Set it to maximum
					maxStringLength_px = stringLen;
				}
			}

			// Offset the tooltip slightly
			posX = posX + AbstractGuiBase.TOOLTIP_OFFSET;
			posY = posY - AbstractGuiBase.TOOLTIP_OFFSET;

			// Base height of 8
			int tooltipHeight = AbstractGuiBase.TOOLTIP_EMPTY_HEIGHT;

			// Adjust height based on the number of lines
			if( this.tooltip.size() > 1 )
			{
				// Calculate the line height
				int lineHeight = ( this.tooltip.size() - 1 ) * AbstractGuiBase.TOOLTIP_LINE_HEIGHT;

				// Adjust the height
				tooltipHeight += ( AbstractGuiBase.TOOLTIP_HEIGHT_MARGIN + lineHeight );
			}

			// Get the current z level
			float prevZlevel = this.zLevel;

			// Set the new level to some high number
			this.zLevel = 300;

			// Tooltip boundary
			Bounds bounds = new Bounds( posY - AbstractGuiBase.TOOLTIP_BORDER_SIZE, posX - AbstractGuiBase.TOOLTIP_BORDER_SIZE, posY + tooltipHeight +
							AbstractGuiBase.TOOLTIP_BORDER_SIZE, posX + maxStringLength_px + AbstractGuiBase.TOOLTIP_BORDER_SIZE );

			// Draw the background and borders
			this.drawTooltipBackground( bounds );

			// Draw each line
			for( int index = 0; index < this.tooltip.size(); index++ )
			{
				// Get the line
				String line = this.tooltip.get( index );

				// Draw the line
				this.mc.fontRenderer.drawStringWithShadow( line, posX, posY, -1 );

				// Is this the first line?
				if( index == 0 )
				{
					// Add the margin
					posY += AbstractGuiBase.TOOLTIP_HEIGHT_MARGIN;
				}

				// Add the line height
				posY += AbstractGuiBase.TOOLTIP_LINE_HEIGHT;
			}

			// Return the z level to what it was before
			this.zLevel = prevZlevel;

			// Reenable lighting
			GL11.glEnable( GL11.GL_LIGHTING );

			// Reenable depth testing
			GL11.glEnable( GL11.GL_DEPTH_TEST );

			// Reenable scaling
			GL11.glEnable( GL12.GL_RESCALE_NORMAL );

			// Clear the tooltip
			if( clearTooltipAfterDraw )
			{
				this.tooltip.clear();
			}
		}
	}

	/**
	 * Gets the slot who contains the specified point.
	 * 
	 * @param x
	 * @param y
	 * @return Slot the point is within, null if point is within no slots.
	 */
	protected final Slot getSlotAtPosition( final int x, final int y )
	{
		// Loop over all slots
		for( int i = 0; i < this.inventorySlots.inventorySlots.size(); i++ )
		{
			// Get the slot
			Slot slot = (Slot)this.inventorySlots.inventorySlots.get( i );

			// Is the point within the slot?
			if( this.isPointWithinSlot( slot, x, y ) )
			{
				// Return the slot
				return slot;
			}
		}

		// Point was not within any slot
		return null;
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked( final int mouseX, final int mouseY, final int mouseButton )
	{

		if( nonLeftClickHandler_Buttons( mouseX, mouseY, mouseButton ) )
		{
			return;
		}

		// Is this container one that could have a network tool?
		if( this.inventorySlots instanceof ContainerWithNetworkTool )
		{
			// Do we have a network tool?
			if( ( (ContainerWithNetworkTool)this.inventorySlots ).hasNetworkTool() )
			{
				// Get the slot the mouse was clicked over
				Slot slot = this.getSlotAtPosition( mouseX, mouseY );

				// Was the slot the network tool?
				if( ( slot != null ) && ( slot.getStack() != null ) &&
								( slot.getStack().isItemEqual( AEApi.instance().definitions().items().networkTool().maybeStack( 1 ).get() ) ) )
				{
					// Do not allow any interaction with the network tool slot.
					return;
				}
			}
		}

		// Pass to super
		super.mouseClicked( mouseX, mouseY, mouseButton );
	}

	/**
	 * Called when a button is clicked. Includes which button was pressed.
	 * 
	 * @param button
	 * @param mouseButton
	 */
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
	}

	/**
	 * Called when a button is left-clicked<BR>
	 * Note: Do not override, use {@link #onButtonClicked(GuiButton, int) onButtonClicked} instead.
	 * 
	 * @see #onButtonClicked(GuiButton, int )
	 */
	@Override
	public final void actionPerformed( final GuiButton button )
	{
		this.onButtonClicked( button, GuiHelper.MOUSE_BUTTON_LEFT );
	}

	/**
	 * Gets the starting X position for the Gui.
	 */
	@Override
	public final int guiLeft()
	{
		return this.guiLeft;
	}

	/**
	 * Gets the starting Y position for the Gui.
	 */
	@Override
	public final int guiTop()
	{
		return this.guiTop;
	}
}
