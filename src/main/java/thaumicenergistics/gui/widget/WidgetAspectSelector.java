package thaumicenergistics.gui.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.aspect.AspectStack;

public class WidgetAspectSelector
	extends AbstractAspectWidget
{
	/**
	 * Thickness of the selector outline.
	 */
	private static final int borderThickness = 1;
	
	/**
	 * The essentia amount for our aspect.
	 */
	private long amount = 0;
	
	/**
	 * Color of the outline when we are selected
	 */
	private int selectorOulineColor;

	public WidgetAspectSelector( IAspectSelectorGui selectorGui, AspectStack stack, int xPos, int yPos )
	{
		super( selectorGui, stack.aspect, xPos, yPos );

		this.amount = stack.amount;

		// Set selector color to cyan
		this.selectorOulineColor = 0xFF00FFFF;
	}

	/**
	 * Draws the selector outline.
	 * @param posX
	 * @param posY
	 * @param heigth
	 * @param width
	 * @param color
	 * @param thickness
	 */
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

	/**
	 * Draws the aspect name and amount
	 */
	@Override
	public void drawTooltip( int mouseX, int mouseY )
	{
		if( ( this.aspect == null ) || ( this.amount <= 0L ) )
		{
			return;
		}

		// Create the description lines
		List<String> descriptionLines = new ArrayList<String>( 2 );

		// Add the name
		descriptionLines.add( this.aspect.getName() );

		// Add the amount
		descriptionLines.add( Long.toString( this.amount ) );

		// Draw the tooltip
		this.drawTooltip( descriptionLines, mouseX - this.hostGUI.guiLeft(), mouseY - this.hostGUI.guiTop(), Minecraft.getMinecraft().fontRenderer );
	}

	/**
	 * Draws the aspect icon and selector border if it is selected.
	 */
	@Override
	public void drawWidget()
	{
		// Bind the block master texture
		//Minecraft.getMinecraft().renderEngine.bindTexture( TextureMap.locationBlocksTexture );

		// Ensure we have an aspect
		if( this.aspect == null )
		{
			return;
		}
		// Disable lighting
		GL11.glDisable( GL11.GL_LIGHTING );

		// Enable blending
		GL11.glEnable( GL11.GL_BLEND );

		// Set the blending mode to blend alpha
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );

		// Full white
		GL11.glColor3f( 1.0F, 1.0F, 1.0F );

		// Draw the aspect
		UtilsFX.drawTag( this.xPosition + 1, this.yPosition + 1, this.aspect, 0, 0, this.zLevel );

		// Get the selected aspect
		AspectStack selectedStack = ( (IAspectSelectorGui)this.hostGUI ).getSelectedAspect();

		// Is there a selectedStack, and does it match ours?
		if( ( selectedStack != null ) && ( selectedStack.aspect == this.aspect  ) )
		{
			this.drawHollowRectWithCorners( this.xPosition, this.yPosition, AbstractWidget.WIDGET_SIZE, AbstractWidget.WIDGET_SIZE, this.selectorOulineColor,
				WidgetAspectSelector.borderThickness );
		}

		// Enable lighting
		GL11.glEnable( GL11.GL_LIGHTING );

		// Disable blending
		GL11.glDisable( GL11.GL_BLEND );
	}

	/**
	 * Gets the essentia amount for our aspect.
	 * @return
	 */
	public long getAmount()
	{
		return this.amount;
	}

	/**
	 * Called when we are clicked
	 */
	@Override
	public void mouseClicked()
	{
		if( this.aspect != null )
		{
			( (IAspectSelectorGui)this.hostGUI ).getContainer().setSelectedAspect( this.aspect );
		}
	}

	/**
	 * Sets the essentia amount for our aspect. 
	 * @param amount
	 */
	public void setAmount( long amount )
	{
		this.amount = amount;
	}
	
	/**
	 * Gets an aspect stack matching our aspect and amount
	 */
	public AspectStack getAspectStackRepresentation()
	{
		return new AspectStack( this.aspect, this.amount );
	}

}
