package thaumicenergistics.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public abstract class BaseGui
	extends GuiContainer
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
	 * Cache the font renderer
	 * 
	 * @param container
	 */
	private static final FontRenderer FONT_RENDERER = Minecraft.getMinecraft().fontRenderer;

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

	public BaseGui( final Container container )
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
		this.drawGradientRect( bounds.L, bounds.T, bounds.R, bounds.B, BaseGui.TOOLTIP_COLOR_BACKGROUND, BaseGui.TOOLTIP_COLOR_BACKGROUND );

		// Draw outer borders
		this.drawTooltipBorders( bounds, BaseGui.TOOLTIP_COLOR_OUTER, BaseGui.TOOLTIP_COLOR_OUTER, 0 );

		// Adjust bounds for inner borders
		bounds.T++ ;
		bounds.L++ ;
		bounds.B-- ;
		bounds.R-- ;

		// Draw inner borders
		this.drawTooltipBorders( bounds, BaseGui.TOOLTIP_COLOR_INNER_BEGIN, BaseGui.TOOLTIP_COLOR_INNER_END, 1 );
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

	@Override
	protected abstract void drawGuiContainerBackgroundLayer( float alpha, int mouseX, int mouseY );

	/**
	 * Draws an on-screen tooltip.
	 * 
	 * @param tooltip
	 * Lines shown in the tooltip. Can be empty, can not be null.
	 * @param posX
	 * X anchor position to draw the tooltip. Generally the mouse's X position.
	 * @param posY
	 * Y anchor position to draw the tooltip. Generally the mouse's Y position.
	 */
	protected final void drawTooltip( final List<String> tooltip, int posX, int posY )
	{
		if( !tooltip.isEmpty() )
		{
			// Disable rescaling
			GL11.glDisable( GL12.GL_RESCALE_NORMAL );

			// Disable lighting
			GL11.glDisable( GL11.GL_LIGHTING );

			// Disable depth testing
			GL11.glDisable( GL11.GL_DEPTH_TEST );

			// Assume string length is zero
			int maxStringLength_px = 0;

			// Get max string length from lines in the list
			for( String string : tooltip )
			{
				// Get the length of the string
				int stringLen = BaseGui.FONT_RENDERER.getStringWidth( string );

				// Is it larger than the previous length?
				if( stringLen > maxStringLength_px )
				{
					// Set it to maximum
					maxStringLength_px = stringLen;
				}
			}

			// Offset the tooltip slightly
			posX = posX + BaseGui.TOOLTIP_OFFSET;
			posY = posY - BaseGui.TOOLTIP_OFFSET;

			// Base height of 8
			int tooltipHeight = BaseGui.TOOLTIP_EMPTY_HEIGHT;

			// Adjust height based on the number of lines
			if( tooltip.size() > 1 )
			{
				// Calculate the line height
				int lineHeight = ( tooltip.size() - 1 ) * BaseGui.TOOLTIP_LINE_HEIGHT;

				// Adjust the height
				tooltipHeight += ( BaseGui.TOOLTIP_HEIGHT_MARGIN + lineHeight );
			}

			// Get the current z level
			float prevZlevel = this.zLevel;

			// Set the new level to some high number
			this.zLevel = 300;

			// Tooltip boundary
			Bounds bounds = new Bounds( posY - BaseGui.TOOLTIP_BORDER_SIZE, posX - BaseGui.TOOLTIP_BORDER_SIZE, posY + tooltipHeight +
							BaseGui.TOOLTIP_BORDER_SIZE, posX + maxStringLength_px + BaseGui.TOOLTIP_BORDER_SIZE );

			// Draw the background and borders
			this.drawTooltipBackground( bounds );

			// Draw each line
			for( int index = 0; index < tooltip.size(); index++ )
			{
				// Get the line
				String line = tooltip.get( index );

				// Draw the line
				FONT_RENDERER.drawStringWithShadow( line, posX, posY, -1 );

				// Is this the first line?
				if( index == 0 )
				{
					// Add the margin
					posY += BaseGui.TOOLTIP_HEIGHT_MARGIN;
				}

				// Add the line height
				posY += BaseGui.TOOLTIP_LINE_HEIGHT;
			}

			// Return the z level to what it was before
			this.zLevel = prevZlevel;

			// Reenable lighting
			GL11.glEnable( GL11.GL_LIGHTING );

			// Reenable depth testing
			GL11.glEnable( GL11.GL_DEPTH_TEST );

			// Reenable scaling
			GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		}
	}

}
