package thaumicenergistics.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.texture.EnumAEStateIcons;

public class AbstractAEButton
	extends GuiButton
{
	/**
	 * Button background
	 */
	private EnumAEStateIcons background = EnumAEStateIcons.REGULAR_BUTTON;

	/**
	 * Icon to draw on the button
	 */
	protected EnumAEStateIcons icon;

	/**
	 * X position of the icon
	 */
	private int iconXPosition;

	/**
	 * Y position of the icon
	 */
	private int iconYPosition;

	/**
	 * Width of the icon
	 */
	private int iconWidth;

	/**
	 * Height of the icon
	 */
	private int iconHeight;

	/**
	 * Convenience constructor for regular buttons.
	 * 
	 * @param ID
	 * @param xPosition
	 * @param yPosition
	 * @param width
	 * @param height
	 * @param icon
	 */
	public AbstractAEButton( int ID, int xPosition, int yPosition, int width, int height, EnumAEStateIcons icon )
	{
		this( ID, xPosition, yPosition, width, height, icon, 0, 0, width, height, false );
	}

	/**
	 * Creates the button
	 * 
	 * @param ID
	 * @param xPosition
	 * @param yPosition
	 * @param width
	 * @param height
	 * @param icon
	 * @param iconXPosition
	 * Relative to the buttons position.
	 * @param iconYPosition
	 * Relative to the buttons position.
	 * @param iconWidth
	 * @param iconHeight
	 * @param isTab
	 */
	public AbstractAEButton( int ID, int xPosition, int yPosition, int width, int height, EnumAEStateIcons icon, int iconXPosition,
								int iconYPosition, int iconWidth, int iconHeight, boolean isTab )
	{
		// Call super
		super( ID, xPosition, yPosition, width, height, "" );

		// Set the icon
		this.icon = icon;

		// Set icon x position
		this.iconXPosition = xPosition + iconXPosition;

		// Set icon y position
		this.iconYPosition = yPosition + iconYPosition;

		// Set icon width
		this.iconWidth = iconWidth;

		// Set icon height
		this.iconHeight = iconHeight;

		// Set tab background
		if( isTab )
		{
			this.background = EnumAEStateIcons.TAB_BUTTON;
		}

	}

	/**
	 * Helper function to draw an AE state icon.
	 * 
	 * @param icon
	 */
	private void drawIcon( EnumAEStateIcons icon, int xPosition, int yPosition, int width, int height )
	{
		this.drawScaledTexturedModalRect( xPosition, yPosition, icon.getU(), icon.getV(), width, height, icon.getWidth(), icon.getHeight() );
	}

	@Override
	public final void drawButton( Minecraft minecraftInstance, int x, int y )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the AE states texture
		minecraftInstance.getTextureManager().bindTexture( EnumAEStateIcons.AE_STATES_TEXTURE );

		// Draw the background button image
		this.drawIcon( this.background, this.xPosition, this.yPosition, this.width, this.height );

		if( this.icon != null )
		{
			// Draw the overlay icon
			this.drawIcon( this.icon, this.iconXPosition, this.iconYPosition, this.iconWidth, this.iconHeight );
		}

	}

	/**
	 * Draws a textured rectangle at the stored z-value, the texture will
	 * be scaled to fit within the width and height
	 */
	public void drawScaledTexturedModalRect( int xPosition, int yPosition, int u, int v, int width, int height, int textureWidth, int textureHeight )
	{
		// No idea what this is
		float magic_number = 0.00390625F;

		// Calculate the UV's
		float minU = u * magic_number;
		float maxU = ( u + textureWidth ) * magic_number;
		float minV = v * magic_number;
		float maxV = ( v + textureHeight ) * magic_number;

		// Get the tessellator
		Tessellator tessellator = Tessellator.instance;

		// Start drawing
		tessellator.startDrawingQuads();

		// Top left corner
		tessellator.addVertexWithUV( xPosition, yPosition + height, this.zLevel, minU, maxV );

		// Top right corner
		tessellator.addVertexWithUV( xPosition + width, yPosition + height, this.zLevel, maxU, maxV );

		// Bottom right corner
		tessellator.addVertexWithUV( xPosition + width, yPosition, this.zLevel, maxU, minV );

		// Bottom left corner
		tessellator.addVertexWithUV( xPosition, yPosition, this.zLevel, minU, minV );

		// Draw
		tessellator.draw();
	}

}
