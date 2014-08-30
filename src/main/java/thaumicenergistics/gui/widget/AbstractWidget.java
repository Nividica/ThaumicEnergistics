package thaumicenergistics.gui.widget;

import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.util.GuiHelper;

public abstract class AbstractWidget
	extends Gui
{
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

	protected void drawTooltip( List<String> descriptionLines, int posX, int posY, FontRenderer fontrenderer )
	{
		// Draw the tooltip
		GuiHelper.instance.drawTooltip( this, descriptionLines, posX, posY, fontrenderer );
	}

	public void drawMouseHoverUnderlay()
	{
		GL11.glDisable( GL11.GL_LIGHTING );

		GL11.glDisable( GL11.GL_DEPTH_TEST );

		this.drawGradientRect( this.xPosition + 1, this.yPosition + 1, this.xPosition + 17, this.yPosition + 17, 0x80FFFFFF, 0x80FFFFFF );

		GL11.glEnable( GL11.GL_LIGHTING );

		GL11.glEnable( GL11.GL_DEPTH_TEST );
	}

	public abstract void drawTooltip( int mouseX, int mouseY );

	public abstract void drawWidget();

	public boolean isMouseOverWidget( int mouseX, int mouseY )
	{
		return GuiHelper.instance.isPointInGuiRegion( this.xPosition, this.yPosition, AbstractWidget.WIDGET_SIZE, AbstractWidget.WIDGET_SIZE, mouseX, mouseY,
			this.hostGUI.guiLeft(), this.hostGUI.guiTop() );
	}

	public abstract void mouseClicked();

	public void setPosition( int xPos, int yPos )
	{
		this.xPosition = xPos;
		this.yPosition = yPos;
	}

}
