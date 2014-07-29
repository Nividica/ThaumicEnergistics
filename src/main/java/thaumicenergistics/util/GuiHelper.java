package thaumicenergistics.util;


public class GuiHelper
{
	public static final int MOUSE_BUTTON_LEFT = 0;
	public static final int MOUSE_BUTTON_RIGHT = 1;
	public static final int MOUSE_BUTTON_WHEEL = 2;
	
	public static final String CHAT_COLOR_HEADER = "§";
	

	public static boolean isPointInGuiRegion( int top, int left, int height, int width, int pointX, int pointY, int guiLeft, int guiTop )
	{
		return isPointInRegion( top, left, height, width, pointX - guiLeft, pointY - guiTop );
	}

	public static boolean isPointInRegion( int top, int left, int height, int width, int pointX, int pointY )
	{
		return ( pointX >= top ) && ( pointX <= ( top + width ) ) && ( pointY >= left ) && ( pointY <= ( left + height ) );
	}

}
