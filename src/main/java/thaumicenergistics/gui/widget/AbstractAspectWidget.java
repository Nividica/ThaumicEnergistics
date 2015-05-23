package thaumicenergistics.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.util.GuiHelper;

public abstract class AbstractAspectWidget
	extends AbstractWidget
{
	private static final ResourceLocation UNKNOWN_TEXTURE = new ResourceLocation( "thaumcraft", "textures/aspects/_unknown.png" );

	private Aspect aspect;

	private boolean hasDiscovered = false;

	protected String aspectName = "";

	private EntityPlayer player;

	private byte[] aspectColorBytes;

	public AbstractAspectWidget( final IWidgetHost hostGui, final Aspect aspect, final int xPos, final int yPos, final EntityPlayer player )
	{
		// Call super
		super( hostGui, xPos, yPos );

		// Set the player
		this.player = player;

		// Set the aspect
		this.setAspect( aspect );
	}

	/**
	 * Draws the aspect icon, or a question mark if not discovered.
	 */
	protected void drawAspect()
	{
		// Ensure there is an aspect to draw
		if( this.aspect == null )
		{
			return;
		}

		// Have they discovered this aspect?
		if( this.hasDiscovered )
		{
			// Ask Thaumcraft to draw the aspect
			UtilsFX.drawTag( this.xPosition + 1, this.yPosition + 1, this.aspect, 0, 0, this.zLevel );
		}
		// Draw the question mark
		else
		{
			// Bind the Thaumcraft question mark texture
			Minecraft.getMinecraft().renderEngine.bindTexture( AbstractAspectWidget.UNKNOWN_TEXTURE );

			// Set the color
			GL11.glColor4ub( this.aspectColorBytes[1], this.aspectColorBytes[2], this.aspectColorBytes[3], this.aspectColorBytes[0] );

			// Enable blending
			GL11.glEnable( GL11.GL_BLEND );

			// Set the blending mode
			GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );

			// Ask Thaumcraft to draw the question texture
			UtilsFX.drawTexturedQuadFull( this.xPosition + 1, this.yPosition + 1, this.zLevel );

			// Disable blending
			GL11.glDisable( GL11.GL_BLEND );
		}
	}

	public Aspect getAspect()
	{
		return this.aspect;
	}

	public void setAspect( final Aspect aspect )
	{
		// Set the aspect
		this.aspect = aspect;

		// Ensure there is an aspect
		if( aspect == null )
		{
			return;
		}

		// Convert to stack
		AspectStack stack = new AspectStack( aspect, 1 );

		// Get the aspect name
		this.aspectName = stack.getAspectName( this.player );

		// Get if the player has discovered this aspect
		this.hasDiscovered = stack.hasPlayerDiscovered( this.player );

		// Get the color bytes
		this.aspectColorBytes = GuiHelper.INSTANCE.convertPackedColorToARGB( aspect.getColor() );

		// Set full alpha
		this.aspectColorBytes[0] = (byte)255;
	}
}
