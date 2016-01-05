package thaumicenergistics.client.gui;

import java.util.HashMap;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ThEGuiHelper
{

	/**
	 * Singleton
	 */
	public static final ThEGuiHelper INSTANCE = new ThEGuiHelper();

	/**
	 * Maps int -> mouse button
	 */
	public static final int MOUSE_BUTTON_LEFT = 0, MOUSE_BUTTON_RIGHT = 1, MOUSE_BUTTON_WHEEL = 2;

	/**
	 * Special case representing not a click, but a motion of the mouse wheel
	 */
	public static final int MOUSE_WHEEL_MOTION = -2;

	/**
	 * Length of color arrays.
	 */
	private static final int COLOR_ARRAY_SIZE = 4;

	/**
	 * Bitshift amounts based on byte position
	 */
	private final int[] COLOR_SHIFT_AMOUNT = new int[] { 0, 8, 16, 24 };

	/**
	 * Maps aspects to chat colors.
	 */
	private HashMap<Aspect, String> aspectChatColors = new HashMap<Aspect, String>();

	/**
	 * Private constructor.
	 */
	private ThEGuiHelper()
	{
		// Add the chat colors
		this.aspectChatColors.put( Aspect.AIR, EnumChatFormatting.YELLOW.toString() );
		this.aspectChatColors.put( Aspect.WATER, EnumChatFormatting.AQUA.toString() );
		this.aspectChatColors.put( Aspect.FIRE, EnumChatFormatting.RED.toString() );
		this.aspectChatColors.put( Aspect.ORDER, EnumChatFormatting.GRAY.toString() );
		this.aspectChatColors.put( Aspect.ENTROPY, EnumChatFormatting.DARK_GRAY.toString() );
		this.aspectChatColors.put( Aspect.EARTH, EnumChatFormatting.GREEN.toString() );

	}

	public static final void drawScaledText( final FontRenderer fontRenderer, final String text, final float scale, final float posX, final float posY )
	{
		// Disable lighting
		GL11.glDisable( GL11.GL_LIGHTING );

		// Disable depth testing
		GL11.glDisable( GL11.GL_DEPTH_TEST );

		// Push the current matrix
		GL11.glPushMatrix();

		// Scale the GUI
		GL11.glScaled( scale, scale, scale );

		// Calculate inverse scale
		float inverseScale = 1.0f / scale;

		// Calculate final X position
		final int X = (int)( ( posX - ( fontRenderer.getStringWidth( text ) * scale ) ) * inverseScale );

		// Calculate final Y position
		final int Y = (int)( ( posY - ( 7.0f * scale ) ) * inverseScale );

		// Render
		fontRenderer.drawStringWithShadow( text, X, Y, 16777215 );

		// Pop the matrix
		GL11.glPopMatrix();

		// Enable lighting
		GL11.glEnable( GL11.GL_LIGHTING );

		// Enable depth testing
		GL11.glEnable( GL11.GL_DEPTH_TEST );
	}

	public final byte[] convertPackedColorToARGBb( final int color )
	{
		byte[] colorBytes = new byte[COLOR_ARRAY_SIZE];

		// Extract bytes
		for( int i = 0; i < COLOR_ARRAY_SIZE; i++ )
		{
			// Get byte
			colorBytes[COLOR_ARRAY_SIZE - 1 - i] = (byte)( ( color >> this.COLOR_SHIFT_AMOUNT[i] ) & 0xFF );
		}

		return colorBytes;
	}

	public final float[] convertPackedColorToARGBf( final int color )
	{
		float[] colorFloats = new float[COLOR_ARRAY_SIZE];

		// Extract bytes
		for( int i = 0; i < COLOR_ARRAY_SIZE; i++ )
		{
			// Get floats
			colorFloats[COLOR_ARRAY_SIZE - 1 - i] = ( ( color >> this.COLOR_SHIFT_AMOUNT[i] ) & 0xFF ) / 255.0f;
		}

		return colorFloats;
	}

	public final int[] createColorGradient( final int fromColor, final int toColor, final int iterations )
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

		// Extract bytes
		for( int i = 0; i < COLOR_ARRAY_SIZE; i++ )
		{
			// Get fromColor byte
			fromColorBytes[i] = ( fromColor >> this.COLOR_SHIFT_AMOUNT[i] ) & 0xFF;

			// Get toColor byte
			toColorBytes[i] = ( ( toColor >> this.COLOR_SHIFT_AMOUNT[i] ) & 0xFF );

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
				result += ( ( Math.round( currentColor[i] ) & 0xFF ) << this.COLOR_SHIFT_AMOUNT[i] );

			}

			// Set gradient
			gradient[iteration] = result;

		}

		// Set the last color
		gradient[iterations - 1] = toColor;

		return gradient;
	}

	/**
	 * Gets the chat color associated with the specified aspect.
	 * 
	 * @param aspect
	 * @return
	 */
	public final String getAspectChatColor( final Aspect aspect )
	{
		if( this.aspectChatColors.containsKey( aspect ) )
		{
			return this.aspectChatColors.get( aspect );
		}
		return EnumChatFormatting.WHITE.toString();
	}

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
	public final boolean isPointInGuiRegion( final int top, final int left, final int height, final int width, final int pointX, final int pointY,
												final int guiLeft, final int guiTop )
	{
		return this.isPointInRegion( top, left, height, width, pointX - guiLeft, pointY - guiTop );
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
	public final boolean isPointInRegion( final int top, final int left, final int height, final int width, final int pointX, final int pointY )
	{
		return ( pointX >= left ) && ( pointX <= ( left + width ) ) && ( pointY >= top ) && ( pointY <= ( top + height ) );
	}
}
