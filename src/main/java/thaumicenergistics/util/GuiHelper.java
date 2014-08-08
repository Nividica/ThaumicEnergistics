package thaumicenergistics.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

public class GuiHelper
{
	/**
	 * Maps int -> mouse button
	 */
	public static final int MOUSE_BUTTON_LEFT = 0;
	public static final int MOUSE_BUTTON_RIGHT = 1;
	public static final int MOUSE_BUTTON_WHEEL = 2;

	/**
	 * Character MC uses to denote the next character is a formating character
	 */
	public static final String CHAT_COLOR_HEADER = "§";

	/**
	 * GL capability code for rescaling.
	 */
	private static int GL_RESCALE_NORMAL = 0x803A;
	
	private static final int COLOR_ARRAY_SIZE = 4;

	/**
	 * Checks if the specified point is within or on the bounds of a rectangle.
	 * This version localizes the rectangle to the confounds of the current gui.
	 * 
	 * @param top
	 * @param left
	 * @param height
	 * @param width
	 * @param pointX
	 * @param pointY
	 * @param guiLeft
	 * @param guiTop
	 * @return
	 */
	public static boolean isPointInGuiRegion( int top, int left, int height, int width, int pointX, int pointY, int guiLeft, int guiTop )
	{
		return isPointInRegion( top, left, height, width, pointX - guiLeft, pointY - guiTop );
	}

	/**
	 * Checks if the specified point is within or on the bounds of a rectangle
	 * 
	 * @param top
	 * @param left
	 * @param height
	 * @param width
	 * @param pointX
	 * @param pointY
	 * @return
	 */
	public static boolean isPointInRegion( int top, int left, int height, int width, int pointX, int pointY )
	{
		return ( pointX >= top ) && ( pointX <= ( top + width ) ) && ( pointY >= left ) && ( pointY <= ( left + height ) );
	}

	// This is a huge mess...
	public static void drawTooltip( Gui guiObject, List<String> descriptionLines, int posX, int posY, FontRenderer fontrenderer )
	{
		if( !descriptionLines.isEmpty() )
		{
			// Disable rescaling
			GL11.glDisable( GL_RESCALE_NORMAL );

			// Disable lighting
			GL11.glDisable( GL11.GL_LIGHTING );

			// Disable depth testing
			GL11.glDisable( GL11.GL_DEPTH_TEST );

			try
			{
				// Use reflection to access the methods and feilds we need
				Field zLevel = Gui.class.getDeclaredField( "zLevel" );
				zLevel.setAccessible( true );

				Method drawGradientRect = Gui.class.getDeclaredMethod( "drawGradientRect", int.class, int.class, int.class, int.class, int.class,
					int.class );
				drawGradientRect.setAccessible( true );

				int maxStringLength = 0;

				for( String string : descriptionLines )
				{

					int stringLen = fontrenderer.getStringWidth( string );

					if( stringLen > maxStringLength )
					{
						maxStringLength = stringLen;
					}
				}

				int offsetX = posX + 12;

				int offsetY = posY - 12;

				int tooltipHeight = 8;

				if( descriptionLines.size() > 1 )
				{
					tooltipHeight += 2 + ( ( descriptionLines.size() - 1 ) * 10 );
				}

				// Get the current z level
				float prevZlevel = (Float)zLevel.get( guiObject );

				// Set the new level to some high number
				zLevel.set( guiObject, 300.0F );

				int drawColor = 0xF0100010;

				drawGradientRect.invoke( guiObject, offsetX - 3, offsetY - 4, offsetX + maxStringLength + 3, offsetY - 3, drawColor, drawColor );
				drawGradientRect.invoke( guiObject, offsetX - 3, offsetY + tooltipHeight + 3, offsetX + maxStringLength + 3, offsetY + tooltipHeight +
								4, drawColor, drawColor );
				drawGradientRect.invoke( guiObject, offsetX - 3, offsetY - 3, offsetX + maxStringLength + 3, offsetY + tooltipHeight + 3, drawColor,
					drawColor );
				drawGradientRect.invoke( guiObject, offsetX - 4, offsetY - 3, offsetX - 3, offsetY + tooltipHeight + 3, drawColor, drawColor );
				drawGradientRect.invoke( guiObject, offsetX + maxStringLength + 3, offsetY - 3, offsetX + maxStringLength + 4, offsetY +
								tooltipHeight + 3, drawColor, drawColor );

				drawColor = 0x505000FF;
				int fadeColor = ( ( drawColor & 0xFEFEFE ) >> 1 ) | ( drawColor & 0xFF000000 );
				drawGradientRect.invoke( guiObject, offsetX - 3, ( offsetY - 3 ) + 1, ( offsetX - 3 ) + 1, ( offsetY + tooltipHeight + 3 ) - 1,
					drawColor, fadeColor );
				drawGradientRect.invoke( guiObject, offsetX + maxStringLength + 2, ( offsetY - 3 ) + 1, offsetX + maxStringLength + 3, ( offsetY +
								tooltipHeight + 3 ) - 1, drawColor, fadeColor );
				drawGradientRect.invoke( guiObject, offsetX - 3, offsetY - 3, offsetX + maxStringLength + 3, ( offsetY - 3 ) + 1, drawColor,
					drawColor );
				drawGradientRect.invoke( guiObject, offsetX - 3, offsetY + tooltipHeight + 2, offsetX + maxStringLength + 3, offsetY + tooltipHeight +
								3, fadeColor, fadeColor );

				for( int descriptionIndex = 0; descriptionIndex < descriptionLines.size(); descriptionIndex++ )
				{
					String s1 = descriptionLines.get( descriptionIndex );
					fontrenderer.drawStringWithShadow( s1, offsetX, offsetY, -1 );
					if( descriptionIndex == 0 )
					{
						offsetY += 2;
					}
					offsetY += 10;
				}

				// Return the z level to what it was before
				zLevel.set( guiObject, prevZlevel );
			}
			catch( Throwable e )
			{
				// Something went wrong, ignore this for now
			}

			// Reenable lighting
			GL11.glEnable( GL11.GL_LIGHTING );

			// Reenable depth testing
			GL11.glEnable( GL11.GL_DEPTH_TEST );

			// Reenable scaling
			GL11.glEnable( GL_RESCALE_NORMAL );
		}
	}

	public static int[] createColorGradient( int fromColor, int toColor, int iterations )
	{
		// Is there enough iterations to create a gradient?
		if( iterations < 3 )
		{
			return new int[] { fromColor, toColor };
		}

		// Holds the A,R,G,B bytes of each color
		int[] fromColorBytes = new int[COLOR_ARRAY_SIZE];
		int[] toColorBytes = new int[COLOR_ARRAY_SIZE];

		// Holds how much to change the color amount by for each iteration
		float[] stepAmount = new float[COLOR_ARRAY_SIZE];
		
		// Holds the color 'bytes' as they change
		float[] currentColor = new float[COLOR_ARRAY_SIZE];
		
		// Holds the final list of colors
		int[] gradient = new int[iterations];
		
		// Bitshift amounts based on byte position
		int[] shiftAmount = new int[]{0,8,16,24};

		// Extract bytes
		for( int i = 0; i < COLOR_ARRAY_SIZE; i++ )
		{
			// Get fromColor byte
			fromColorBytes[i] = ( fromColor >> shiftAmount[i] ) & 0xFF;
			
			// Get toColor byte
			toColorBytes[i] = ( ( toColor >> shiftAmount[i] ) & 0xFF );
			
			// Calculate step amount
			stepAmount[i] = ( toColorBytes[i] - fromColorBytes[i] ) / (float)iterations;
			
			// Init the current color
			currentColor[i] = fromColorBytes[i];
		}
		
		// Set the first color
		gradient[0] = fromColor;

		for( int iteration = 1; iteration < iterations; iteration++ )
		{
			int result = 0;
			
			// Add the step amounts to the current color and incorporate into the result color
			for( int i = 0; i < COLOR_ARRAY_SIZE; i++ )
			{
				// Add the step amount
				currentColor[i] += stepAmount[i];
				
				// Add to result color
				result += ( ( Math.round( currentColor[i] ) & 0xFF ) <<	shiftAmount[i] );
				
			}
			
			// Set gradient
			gradient[iteration] = result;
			
		}
		
		// Set the last color
		gradient[iterations-1] = toColor;

		return gradient;
	}
}
