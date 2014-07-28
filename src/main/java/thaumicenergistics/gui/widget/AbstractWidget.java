package thaumicenergistics.gui.widget;

import java.util.List;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.util.GuiHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;

public abstract class AbstractWidget
	extends Gui
{
	private static int GL_RESCALE_NORMAL = 0x803A;

	/**
	 * The width and height of the aspect
	 */
	public static int WIDGET_SIZE = 18;

	protected int xPosition;

	protected int yPosition;

	protected IWidgetHost hostGUI;

	public AbstractWidget( IWidgetHost hostGUI, int xPos, int yPos )
	{
		this.hostGUI = hostGUI;

		this.xPosition = xPos;

		this.yPosition = yPos;
	}

	public void setPosition( int xPos, int yPos )
	{
		this.xPosition = xPos;
		this.yPosition = yPos;
	}

	// This is a mess...
	protected void drawHoveringText( List<String> descriptionLines, int posX, int posY, FontRenderer fontrenderer )
	{
		if( !descriptionLines.isEmpty() )
		{
			GL11.glDisable( GL_RESCALE_NORMAL );

			RenderHelper.disableStandardItemLighting();

			GL11.glDisable( GL11.GL_LIGHTING );

			GL11.glDisable( GL11.GL_DEPTH_TEST );

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

			this.zLevel = 300.0F;

			int drawColor = 0xF0100010;
			this.drawGradientRect( offsetX - 3, offsetY - 4, offsetX + maxStringLength + 3, offsetY - 3, drawColor, drawColor );
			this.drawGradientRect( offsetX - 3, offsetY + tooltipHeight + 3, offsetX + maxStringLength + 3, offsetY + tooltipHeight + 4, drawColor,
				drawColor );
			this.drawGradientRect( offsetX - 3, offsetY - 3, offsetX + maxStringLength + 3, offsetY + tooltipHeight + 3, drawColor, drawColor );
			this.drawGradientRect( offsetX - 4, offsetY - 3, offsetX - 3, offsetY + tooltipHeight + 3, drawColor, drawColor );
			this.drawGradientRect( offsetX + maxStringLength + 3, offsetY - 3, offsetX + maxStringLength + 4, offsetY + tooltipHeight + 3, drawColor,
				drawColor );

			drawColor = 0x505000FF;
			int fadeColor = ( ( drawColor & 0xFEFEFE ) >> 1 ) | ( drawColor & 0xFF000000 );
			this.drawGradientRect( offsetX - 3, ( offsetY - 3 ) + 1, ( offsetX - 3 ) + 1, ( offsetY + tooltipHeight + 3 ) - 1, drawColor, fadeColor );
			this.drawGradientRect( offsetX + maxStringLength + 2, ( offsetY - 3 ) + 1, offsetX + maxStringLength + 3,
				( offsetY + tooltipHeight + 3 ) - 1, drawColor, fadeColor );
			this.drawGradientRect( offsetX - 3, offsetY - 3, offsetX + maxStringLength + 3, ( offsetY - 3 ) + 1, drawColor, drawColor );
			this.drawGradientRect( offsetX - 3, offsetY + tooltipHeight + 2, offsetX + maxStringLength + 3, offsetY + tooltipHeight + 3, fadeColor,
				fadeColor );

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
			this.zLevel = 0.0F;

			GL11.glEnable( GL11.GL_LIGHTING );

			GL11.glEnable( GL11.GL_DEPTH_TEST );

			RenderHelper.enableStandardItemLighting();

			GL11.glEnable( GL_RESCALE_NORMAL );
		}
	}
	
	public void drawMouseHoverUnderlay()
	{
		GL11.glDisable( GL11.GL_LIGHTING );

		GL11.glDisable( GL11.GL_DEPTH_TEST );
		
		this.drawGradientRect( this.xPosition + 1, this.yPosition + 1, this.xPosition + 17, this.yPosition + 17, 0x80FFFFFF, 0x80FFFFFF );

		GL11.glEnable( GL11.GL_LIGHTING );

		GL11.glEnable( GL11.GL_DEPTH_TEST );
	}

	public boolean isMouseOverWidget( int mouseX, int mouseY )
	{
		return GuiHelper.isPointInGuiRegion( this.xPosition, this.yPosition, AbstractWidget.WIDGET_SIZE, AbstractWidget.WIDGET_SIZE, mouseX, mouseY,
			this.hostGUI.guiLeft(), this.hostGUI.guiTop() );
	}

	public abstract void drawTooltip( int mouseX, int mouseY );

	public abstract void drawWidget();

	public abstract void mouseClicked();

}
