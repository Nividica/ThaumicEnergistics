package thaumicenergistics.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.texture.AEStateIcons;

public class AbstractAEButton
	extends GuiButton
{
	private static final AEStateIcons BACKGROUND = AEStateIcons.BLANK_BUTTON;

	protected AEStateIcons icon;

	public AbstractAEButton( int ID, int xPosition, int yPosition, int width, int height, AEStateIcons icon )
	{
		// Call super
		super( ID, xPosition, yPosition, width, height, "" );

		// Set the icon
		this.icon = icon;
	}

	@Override
	public final void drawButton( Minecraft minecraftInstance, int x, int y )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the AE states texture
		minecraftInstance.getTextureManager().bindTexture( AEStateIcons.AE_STATES_TEXTURE );

		// Draw the background button image
		this.drawIcon( AbstractAEButton.BACKGROUND );

		if( this.icon != null )
		{
			// Draw the overlay icon
			this.drawIcon( this.icon );
		}

	}

	/**
	 * Helper function to draw an AE state icon.
	 * 
	 * @param icon
	 */
	private void drawIcon( AEStateIcons icon )
	{
		this.drawScaledTexturedModalRect( this.xPosition, this.yPosition, icon.getU(), icon.getV(), this.width, this.height, AEStateIcons.ICON_SIZE,
			AEStateIcons.ICON_SIZE );
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

		Tessellator tessellator = Tessellator.instance;

		tessellator.startDrawingQuads();

		// Top left corner
		tessellator.addVertexWithUV( xPosition, yPosition + height, this.zLevel, minU, maxV );

		// Top right corner
		tessellator.addVertexWithUV( xPosition + width, yPosition + height, this.zLevel, maxU, maxV );

		// Bottom right corner
		tessellator.addVertexWithUV( xPosition + width, yPosition, this.zLevel, maxU, minV );

		// Bottom left corner
		tessellator.addVertexWithUV( xPosition, yPosition, this.zLevel, minU, minV );

		tessellator.draw();
	}

}
