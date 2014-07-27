package thaumicenergistics.util;


public class GuiHelper
{
	public static final int MOUSE_BUTTON_LEFT = 0;
	public static final int MOUSE_BUTTON_RIGHT = 1;
	public static final int MOUSE_BUTTON_WHEEL = 2;
	

	public static boolean isPointInGuiRegion( int top, int left, int height, int width, int pointX, int pointY, int guiLeft, int guiTop )
	{
		return isPointInRegion( top, left, height, width, pointX - guiLeft, pointY - guiTop );
	}

	public static boolean isPointInRegion( int top, int left, int height, int width, int pointX, int pointY )
	{
		return ( pointX >= top ) && ( pointX <= ( top + width ) ) && ( pointY >= left ) && ( pointY <= ( left + height ) );
	}
	
	public static class ChatColors
	{
		public static final String BLACK = "§0";
		public static final String DARK_BLUE = "§1";
		public static final String DARK_GREEN = "§2";
		public static final String DARK_AQUA = "§3";
		public static final String DARK_RED = "§4";
		public static final String DARK_PURPLE = "§5";
		public static final String GOLD = "§6";
		public static final String GRAY = "§7";
		public static final String DARK_GRAY = "§8";
		public static final String BLUE = "§9";
		public static final String GREEN = "§a";
		public static final String AQUA = "§b";
		public static final String RED = "§c";
		public static final String LIGHT_PURPLE = "§d";
		public static final String YELLOW = "§e";
		public static final String WHITE = "§f";
		public static final String CHAT_COLOR_HEADER = "§";
	}

}
