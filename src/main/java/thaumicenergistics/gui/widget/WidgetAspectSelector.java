package thaumicenergistics.gui.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.util.GuiHelper;

public class WidgetAspectSelector
	extends AbstractAspectWidget
{
	/**
	 * Thickness of the selector outline.
	 */
	private static final int borderThickness = 1;

	/**
	 * The number of iterations in the gradient
	 */
	private static final int GRADIENT_COUNT = 15;

	/**
	 * Color of the border while aspect is selected
	 */
	private static final int selectorBorderColor = 0xFF00FFFF;

	/**
	 * Array of colors that pulse behind the aspect
	 */
	private int[] backgroundPulseGradient;

	/**
	 * The essentia amount for our aspect.
	 */
	private long amount = 0;

	public WidgetAspectSelector( IAspectSelectorGui selectorGui, AspectStack stack, int xPos, int yPos, EntityPlayer player )
	{
		// Call super
		super( selectorGui, stack.aspect, xPos, yPos, player );

		// Get the amount
		this.amount = stack.amount;
		
		// Get the aspect color
		int aspectColor = stack.aspect.getColor();

		// Create the gradient using the aspect color, varying between opacities
		this.backgroundPulseGradient = GuiHelper.createColorGradient( 0x70000000 | aspectColor, 0x20000000 | aspectColor, GRADIENT_COUNT + 1 );
	}

	/**
	 * Draws the selector outline.
	 * 
	 * @param posX
	 * @param posY
	 * @param width
	 * @param height
	 * @param color
	 * @param thickness
	 */
	private void drawHollowRectWithCorners( int posX, int posY, int width, int height, int color, int thickness )
	{
		// Calculate points

		// Ending X point of the right line
		int rightXEnd = posX + width;

		// Beginning X point of the right line 
		int rightXBegin = rightXEnd - thickness;

		// Ending X point of the left line
		int leftXEnd = posX + thickness;

		// Ending Y point of the top line
		int topYEnd = posY + thickness;

		// Ending Y point of the bottom line
		int bottomYEnd = posY + height;

		// Beginning Y point of the bottom line
		int bottomYBegin = bottomYEnd - thickness;

		// Draw background gradient
		Gui.drawRect( posX, posY, rightXEnd, bottomYEnd, color );

		// Draw notches

		// Top-left notch
		Gui.drawRect( posX, posY, leftXEnd + 1, topYEnd + 1, selectorBorderColor );

		// Top-right notch
		Gui.drawRect( rightXEnd, posY, rightXBegin - 1, topYEnd + 1, selectorBorderColor );

		// Bottom-right notch
		Gui.drawRect( rightXEnd, bottomYEnd, rightXBegin - 1, bottomYBegin - 1, selectorBorderColor );

		// Bottom-left notch
		Gui.drawRect( posX, bottomYEnd, leftXEnd + 1, bottomYBegin - 1, selectorBorderColor );

		// Draw lines

		// Top side
		Gui.drawRect( posX, posY, rightXEnd, topYEnd, selectorBorderColor );

		// Bottom side
		Gui.drawRect( posX, bottomYBegin, rightXEnd, bottomYEnd, selectorBorderColor );

		// Left side
		Gui.drawRect( posX, posY, leftXEnd, bottomYEnd, selectorBorderColor );

		// Right side
		Gui.drawRect( rightXBegin, posY, rightXEnd, bottomYEnd, selectorBorderColor );
	}

	/**
	 * Gets the background gradient color based on the current time.
	 * 
	 * @return
	 */
	private int getBackgroundColor()
	{
		// Get the current time, slowed down.
		int time = (int)( System.currentTimeMillis() / 45L );

		// Lerp the index
		int index = Math.abs( Math.abs( time % ( GRADIENT_COUNT * 2 ) ) - GRADIENT_COUNT );

		// Return the index
		return this.backgroundPulseGradient[index];
	}

	/**
	 * Draws the aspect name and amount
	 */
	@Override
	public void drawTooltip( int mouseX, int mouseY )
	{
		if( ( this.getAspect() == null ) || ( this.amount <= 0L ) )
		{
			return;
		}

		// Create the description lines
		List<String> descriptionLines = new ArrayList<String>( 2 );

		// Add the name
		descriptionLines.add( this.aspectName );

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
		if( this.getAspect() == null )
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

		// Get the selected aspect
		AspectStack selectedStack = ( (IAspectSelectorGui)this.hostGUI ).getSelectedAspect();

		// Is there a selectedStack, and does it match ours?
		if( ( selectedStack != null ) && ( selectedStack.aspect == this.getAspect() ) )
		{
			this.drawHollowRectWithCorners( this.xPosition, this.yPosition, AbstractWidget.WIDGET_SIZE, AbstractWidget.WIDGET_SIZE,
				this.getBackgroundColor(), WidgetAspectSelector.borderThickness );
		}

		// Draw the aspect
		this.drawAspect();

		// Enable lighting
		GL11.glEnable( GL11.GL_LIGHTING );

		// Disable blending
		GL11.glDisable( GL11.GL_BLEND );
	}

	/**
	 * Gets the essentia amount for our aspect.
	 * 
	 * @return
	 */
	public long getAmount()
	{
		return this.amount;
	}

	/**
	 * Gets an aspect stack matching our aspect and amount
	 */
	public AspectStack getAspectStackRepresentation()
	{
		return new AspectStack( this.getAspect(), this.amount );
	}

	/**
	 * Called when we are clicked
	 */
	@Override
	public void mouseClicked()
	{
		if( this.getAspect() != null )
		{
			( (IAspectSelectorGui)this.hostGUI ).getContainer().setSelectedAspect( this.getAspect() );
		}
	}

	/**
	 * Sets the essentia amount for our aspect.
	 * 
	 * @param amount
	 */
	public void setAmount( long amount )
	{
		this.amount = amount;
	}

}
