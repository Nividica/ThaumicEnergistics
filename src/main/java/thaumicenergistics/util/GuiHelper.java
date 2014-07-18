package thaumicenergistics.util;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.gui.widget.WidgetAspectSlot;

public class GuiHelper
{
	private static void drawGradientRect( float zLevel, int par1, int par2, int par3, int par4, int par5, int par6 )
	{
		float f = ( ( par5 >> 24 ) & 0xFF ) / 255.0F;
		float f1 = ( ( par5 >> 16 ) & 0xFF ) / 255.0F;
		float f2 = ( ( par5 >> 8 ) & 0xFF ) / 255.0F;
		float f3 = ( par5 & 0xFF ) / 255.0F;
		float f4 = ( ( par6 >> 24 ) & 0xFF ) / 255.0F;
		float f5 = ( ( par6 >> 16 ) & 0xFF ) / 255.0F;
		float f6 = ( ( par6 >> 8 ) & 0xFF ) / 255.0F;
		float f7 = ( par6 & 0xFF ) / 255.0F;

		GL11.glDisable( 3553 );
		GL11.glEnable( 3042 );
		GL11.glDisable( 3008 );

		OpenGlHelper.glBlendFunc( 770, 771, 1, 0 );

		GL11.glShadeModel( 7425 );

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F( f1, f2, f3, f );
		tessellator.addVertex( par3, par2, zLevel );
		tessellator.addVertex( par1, par2, zLevel );
		tessellator.setColorRGBA_F( f5, f6, f7, f4 );
		tessellator.addVertex( par1, par4, zLevel );
		tessellator.addVertex( par3, par4, zLevel );
		tessellator.draw();

		GL11.glShadeModel( 7424 );
		GL11.glDisable( 3042 );
		GL11.glEnable( 3008 );
		GL11.glEnable( 3553 );
	}

	public static boolean isPointInGuiRegion( int top, int left, int height, int width, int pointX, int pointY, int guiLeft, int guiTop )
	{
		return isPointInRegion( top, left, height, width, pointX - guiLeft, pointY - guiTop );
	}

	public static boolean isPointInRegion( int top, int left, int height, int width, int pointX, int pointY )
	{
		return ( pointX >= ( top - 1 ) ) && ( pointX < ( top + width + 1 ) ) && ( pointY >= ( left - 1 ) ) && ( pointY < ( left + height + 1 ) );
	}

	public static boolean renderOverlay( float zLevel, int guiLeft, int guiTop, WidgetAspectSlot aspectSlot, int mouseX, int mouseY )
	{
		if ( GuiHelper.isPointInGuiRegion( aspectSlot.getPosX(), aspectSlot.getPosY(), 18, 18, mouseX, mouseY, guiLeft, guiTop ) )
		{
			GL11.glDisable( 2896 );

			GL11.glDisable( 2929 );

			GuiHelper.drawGradientRect( zLevel, aspectSlot.getPosX() + 1, aspectSlot.getPosY() + 1, aspectSlot.getPosX() + 17,
				aspectSlot.getPosY() + 17, -2130706433, -2130706433 );

			GL11.glEnable( 2896 );

			GL11.glEnable( 2929 );

			return true;
		}
		return false;
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
