package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.texture.AEStateIconsEnum;
import com.google.common.base.Splitter;

public abstract class AbstractAEButton
	extends AbstractButtonBase
{
	/**
	 * Button background
	 */
	private AEStateIconsEnum background = AEStateIconsEnum.REGULAR_BUTTON;

	/**
	 * Icon to draw on the button
	 */
	protected AEStateIconsEnum icon;

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
	public AbstractAEButton( final int ID, final int xPosition, final int yPosition, final int width, final int height, final AEStateIconsEnum icon )
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
	public AbstractAEButton( final int ID, final int xPosition, final int yPosition, final int width, final int height, final AEStateIconsEnum icon,
								final int iconXPosition, final int iconYPosition, final int iconWidth, final int iconHeight, final boolean isTab )
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
			this.background = AEStateIconsEnum.TAB_BUTTON;
		}

	}

	/**
	 * Helper function to draw an AE state icon.
	 * 
	 * @param icon
	 */
	private void drawIcon( final AEStateIconsEnum icon, final int xPosition, final int yPosition, final int width, final int height )
	{
		this.drawScaledTexturedModalRect( xPosition, yPosition, icon.getU(), icon.getV(), width, height, icon.getWidth(), icon.getHeight() );
	}

	/**
	 * Adds info to the tooltip as a white header, and grey body.
	 * The body is broken down into lines of length 30.
	 * 
	 * @param tooltip
	 * @param title
	 * @param text
	 */
	protected void addAboutToTooltip( final List<String> tooltip, final String title, final String text )
	{
		// Title
		tooltip.add( EnumChatFormatting.WHITE + title );

		// Body
		for( String line : Splitter.fixedLength( 30 ).split( text ) )
		{
			tooltip.add( EnumChatFormatting.GRAY + line );
		}
	}

	@Override
	public final void drawButton( final Minecraft minecraftInstance, final int x, final int y )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the AE states texture
		minecraftInstance.getTextureManager().bindTexture( AEStateIconsEnum.AE_STATES_TEXTURE );

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
	public void drawScaledTexturedModalRect( final int xPosition, final int yPosition, final int u, final int v, final int width, final int height,
												final int textureWidth, final int textureHeight )
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
