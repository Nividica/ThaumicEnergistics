package thaumicenergistics.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

public class GuiHelper
{
	public static final int MOUSE_BUTTON_LEFT = 0;
	public static final int MOUSE_BUTTON_RIGHT = 1;
	public static final int MOUSE_BUTTON_WHEEL = 2;

	public static final String CHAT_COLOR_HEADER = "§";

	private static int GL_RESCALE_NORMAL = 0x803A;

	public static boolean isPointInGuiRegion( int top, int left, int height, int width, int pointX, int pointY, int guiLeft, int guiTop )
	{
		return isPointInRegion( top, left, height, width, pointX - guiLeft, pointY - guiTop );
	}

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

				Method drawGradientRect = Gui.class.getDeclaredMethod( "drawGradientRect", int.class, int.class, int.class,
					int.class, int.class, int.class );
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
}
