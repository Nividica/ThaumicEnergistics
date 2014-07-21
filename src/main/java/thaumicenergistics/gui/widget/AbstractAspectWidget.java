package thaumicenergistics.gui.widget;

import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.util.GuiHelper;

public abstract class AbstractAspectWidget extends Gui
{
	protected IAspectWidgetGui selectorGui;
	protected Aspect aspect;
	protected int width = 0;
	protected int height = 0;

	public AbstractAspectWidget(IAspectWidgetGui selectorGui, int height, int width, Aspect aspect)
	{
		this.selectorGui = selectorGui;
		this.aspect = aspect;
		this.width = width;
		this.height = height;
	}

	protected void drawHoveringText( List list, int x, int y, FontRenderer fontrenderer )
	{
		if ( !list.isEmpty() )
		{
			GL11.glDisable( 32826 );
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable( 2896 );
			GL11.glDisable( 2929 );

			int maxStringLength = 0;

			for( Object string : list )
			{
				String s = (String) string;
				int stringLen = fontrenderer.getStringWidth( s );
				if ( stringLen > maxStringLength )
				{
					maxStringLength = stringLen;
				}
			}

			int i1 = x + 12;
			int j1 = y - 12;
			int k1 = 8;

			if ( list.size() > 1 )
			{
				k1 += 2 + ( ( list.size() - 1 ) * 10 );
			}

			this.zLevel = 300.0F;

			int l1 = -267386864;
			this.drawGradientRect( i1 - 3, j1 - 4, i1 + maxStringLength + 3, j1 - 3, l1, l1 );
			this.drawGradientRect( i1 - 3, j1 + k1 + 3, i1 + maxStringLength + 3, j1 + k1 + 4, l1, l1 );
			this.drawGradientRect( i1 - 3, j1 - 3, i1 + maxStringLength + 3, j1 + k1 + 3, l1, l1 );
			this.drawGradientRect( i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1 );
			this.drawGradientRect( i1 + maxStringLength + 3, j1 - 3, i1 + maxStringLength + 4, j1 + k1 + 3, l1, l1 );
			int i2 = 0x505000FF;
			int j2 = ( ( i2 & 0xFEFEFE ) >> 1 ) | ( i2 & 0xFF000000 );
			this.drawGradientRect( i1 - 3, ( j1 - 3 ) + 1, ( i1 - 3 ) + 1, ( j1 + k1 + 3 ) - 1, i2, j2 );
			this.drawGradientRect( i1 + maxStringLength + 2, ( j1 - 3 ) + 1, i1 + maxStringLength + 3, ( j1 + k1 + 3 ) - 1, i2, j2 );
			this.drawGradientRect( i1 - 3, j1 - 3, i1 + maxStringLength + 3, ( j1 - 3 ) + 1, i2, i2 );
			this.drawGradientRect( i1 - 3, j1 + k1 + 2, i1 + maxStringLength + 3, j1 + k1 + 3, j2, j2 );
			for( int k2 = 0; k2 < list.size(); k2++ )
			{
				String s1 = (String) list.get( k2 );
				fontrenderer.drawStringWithShadow( s1, i1, j1, -1 );
				if ( k2 == 0 )
				{
					j1 += 2;
				}
				j1 += 10;
			}
			this.zLevel = 0.0F;
			GL11.glEnable( 2896 );
			GL11.glEnable( 2929 );
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable( 32826 );
		}
	}

	protected boolean isPointInRegion( int top, int left, int height, int width, int pointX, int pointY )
	{
		return GuiHelper.isPointInGuiRegion( top, left, height, width, pointX, pointY, this.selectorGui.guiLeft(),
			this.selectorGui.guiTop() );
	}

	public abstract boolean drawTooltip( int posX, int posY, int mouseX, int mouseY );

	public abstract void drawWidget( int posX, int posY );

	public Aspect getAspect()
	{
		return this.aspect;
	}

	public abstract void mouseClicked( int posX, int posY, int mouseX, int mouseY );

	public void setAspect( String aspectTag )
	{
		this.aspect = Aspect.aspects.get( aspectTag );
	}
}
