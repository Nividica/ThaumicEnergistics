package thaumicenergistics.gui.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureMap;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.aspect.AspectStack;

public class WidgetAspectSelector
	extends AbstractAspectWidget
{
	private long amount = 0L;
	private int color;
	private int borderThickness;

	public WidgetAspectSelector( IAspectSelectorGui selectorGui, AspectStack stack, int xPos, int yPos )
	{
		super( selectorGui, stack.aspect, xPos, yPos );

		this.amount = stack.amount;

		this.color = -16711681;

		this.borderThickness = 1;
	}

	private void drawHollowRectWithCorners( int posX, int posY, int heigth, int width, int color, int thickness )
	{
		Gui.drawRect( posX, posY, posX + heigth, posY + thickness, color );
		Gui.drawRect( posX, ( posY + width ) - thickness, posX + heigth, posY + width, color );
		Gui.drawRect( posX, posY, posX + thickness, posY + width, color );
		Gui.drawRect( ( posX + heigth ) - thickness, posY, posX + heigth, posY + width, color );

		Gui.drawRect( posX, posY, posX + thickness + 1, posY + thickness + 1, color );
		Gui.drawRect( posX + heigth, posY + width, ( posX + heigth ) - thickness - 1, ( posY + width ) - thickness - 1, color );
		Gui.drawRect( posX + heigth, posY, ( posX + heigth ) - thickness - 1, posY + thickness + 1, color );
		Gui.drawRect( posX, posY + width, posX + thickness + 1, ( posY + width ) - thickness - 1, color );
	}

	@Override
	public boolean drawTooltip( int posX, int posY, int mouseX, int mouseY )
	{
		if( ( this.aspect == null ) || ( this.amount <= 0L ) )
		{
			return false;
		}

		String amountToText = Long.toString( this.amount );

		List<String> description = new ArrayList<String>();

		description.add( this.aspect.getName() );

		description.add( amountToText );

		this.drawHoveringText( description, mouseX - this.hostGUI.guiLeft(), mouseY - this.hostGUI.guiTop(), Minecraft.getMinecraft().fontRenderer );

		return true;
	}

	@Override
	public void drawWidget()
	{
		Minecraft.getMinecraft().renderEngine.bindTexture( TextureMap.locationBlocksTexture );

		GL11.glDisable( GL11.GL_LIGHTING );

		GL11.glEnable( GL11.GL_BLEND );

		GL11.glBlendFunc( 770, 771 );

		GL11.glColor3f( 1.0F, 1.0F, 1.0F );

		if( this.aspect != null )
		{
			UtilsFX.drawTag( this.xPosition + 1, this.yPosition + 1, this.aspect, 0, 0, this.zLevel );
			
			AspectStack terminalFluid = ( (IAspectSelectorGui)this.hostGUI ).getSelectedAspect();

			Aspect currentAspect = terminalFluid != null ? terminalFluid.aspect : null;

			if( this.aspect == currentAspect )
			{
				this.drawHollowRectWithCorners( this.xPosition, this.yPosition, AbstractWidget.WIDGET_SIZE, AbstractWidget.WIDGET_SIZE, this.color,
					this.borderThickness );
			}
		}

		GL11.glEnable( GL11.GL_LIGHTING );

		GL11.glDisable( GL11.GL_BLEND );
	}

	public long getAmount()
	{
		return this.amount;
	}

	@Override
	public void mouseClicked()
	{
		if( this.aspect != null )
		{
			( (IAspectSelectorGui)this.hostGUI ).getContainer().setSelectedAspect( this.aspect );
		}
	}

	public void setAmount( long amount )
	{
		this.amount = amount;
	}

}
