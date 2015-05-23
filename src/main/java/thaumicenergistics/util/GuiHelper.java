package thaumicenergistics.util;

import java.util.HashMap;
import net.minecraft.util.EnumChatFormatting;
import thaumcraft.api.aspects.Aspect;

public final class GuiHelper
{

	/**
	 * Singleton
	 */
	public static final GuiHelper INSTANCE = new GuiHelper();

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
	private GuiHelper()
	{
		// Add the chat colors
		this.aspectChatColors.put( Aspect.AIR, EnumChatFormatting.YELLOW.toString() );
		this.aspectChatColors.put( Aspect.WATER, EnumChatFormatting.AQUA.toString() );
		this.aspectChatColors.put( Aspect.FIRE, EnumChatFormatting.RED.toString() );
		this.aspectChatColors.put( Aspect.ORDER, EnumChatFormatting.GRAY.toString() );
		this.aspectChatColors.put( Aspect.ENTROPY, EnumChatFormatting.DARK_GRAY.toString() );
		this.aspectChatColors.put( Aspect.EARTH, EnumChatFormatting.GREEN.toString() );

	}

	/**
	 * Changes a large number into a binary postfixed version.
	 * For example: 10,500 -> 10.5K
	 * 
	 * @param count
	 * @return
	 */
	public static String shortenCount( final long count )
	{
		int unit = 1000;

		// Are there at least 1000?
		if( count < unit )
			return Long.toString( count );

		// Calculate the exponential
		int exponential = (int)( Math.log( count ) / Math.log( unit ) );

		// Get the posfix char
		char postfix = "KMBT".charAt( exponential - 1 );

		return String.format( "%.1f%c", ( count / Math.pow( unit, exponential ) ), postfix );
	}

	public final byte[] convertPackedColorToARGB( final int color )
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

	/**
	 * Ping pongs a value back and forth from min -> max -> min.
	 * The base speed of this effect is 1 second per transition, 2 seconds
	 * total.
	 * 
	 * @param speedReduction
	 * The higher this value, the slower the effect. The smaller this value, the
	 * faster the effect.
	 * PingPong time (1) = 2 Seconds; (0.5) = 1 Second; (2) = 4 Seconds;
	 * @param minValue
	 * @param maxValue
	 * @return
	 */
	public final float pingPongFromTime( double speedReduction, final float minValue, final float maxValue )
	{
		// NOTE: If I make a math helper class, move this there

		// Sanity check for situations like pingPongFromTime( ?, 1.0F, 1.0F )
		if( minValue == maxValue )
		{
			return minValue;
		}

		// Bounds check speed reduction
		if( speedReduction <= 0 )
		{
			speedReduction = Float.MIN_VALUE;
		}

		// Get the time modulated to 2000, then reduced
		float time = (float)( ( System.currentTimeMillis() / speedReduction ) % 2000.F );

		// Offset by -1000 and take the abs
		time = Math.abs( time - 1000.0F );

		// Convert time to a percentage
		float timePercentage = time / 1000.0F;

		// Get the position in the range we are now at
		float rangePercentage = ( maxValue - minValue ) * timePercentage;

		// Add the range position back to min
		return minValue + rangePercentage;
	}
}
