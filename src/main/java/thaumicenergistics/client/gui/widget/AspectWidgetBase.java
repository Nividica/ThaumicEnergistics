package thaumicenergistics.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.api.gui.IWidgetHost;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.client.gui.ThEGuiHelper;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.storage.AspectStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class AspectWidgetBase
	extends ThEWidget
{
	/**
	 * Icon to display if the aspect is unknown to the player
	 */
	private static final ResourceLocation UNKNOWN_TEXTURE = new ResourceLocation( "thaumcraft", "textures/aspects/_unknown.png" );

	/**
	 * Stack this widget represents.
	 */
	private final IAspectStack aspectStack;

	/**
	 * True if the player has discovered the aspect.
	 */
	protected boolean hasDiscovered = false;

	/**
	 * Cached aspect name.
	 */
	protected String aspectName = "";

	/**
	 * Cached description of the aspect.
	 */
	protected String aspectDescription = "";

	/**
	 * Cached footnote of the aspect.
	 * NOTE: One day I hope to put the mod the aspect is from here,
	 * not just its primallity.
	 */
	protected String aspectFootnote = "";

	/**
	 * The current player.
	 */
	private EntityPlayer player;

	/**
	 * Color of the aspect as ARGB.
	 */
	private byte[] aspectColorBytes;

	public AspectWidgetBase( final IWidgetHost hostGui, final IAspectStack stack, final int xPos, final int yPos, final EntityPlayer player )
	{
		// Call super
		super( hostGui, xPos, yPos );

		// Set the player
		this.player = player;

		// Create the aspect stack
		this.aspectStack = new AspectStack();

		// Set the aspect
		this.setAspect( stack );
	}

	/**
	 * Clears the stack and optionally fires the on stack changed event.
	 * 
	 * @param doUpdate
	 */
	protected void clearStack( final boolean doUpdate )
	{
		this.aspectStack.setAll( null, 0, false );
		this.aspectName = "";
		this.aspectDescription = "";
		this.aspectFootnote = "";
		this.hasDiscovered = false;

		if( doUpdate )
		{
			this.onStackChanged();
		}
	}

	/**
	 * Draws the aspect icon, or a question mark if not discovered.
	 */
	protected void drawAspect()
	{
		// Ensure there is an aspect to draw
		if( !this.aspectStack.hasAspect() )
		{
			return;
		}

		// Have they discovered this aspect?
		if( this.hasDiscovered )
		{
			// Ask Thaumcraft to draw the aspect
			UtilsFX.drawTag( this.xPosition + 1, this.yPosition + 1, this.aspectStack.getAspect(), 0, 0, this.zLevel );
		}
		// Draw the question mark
		else
		{
			// Bind the Thaumcraft question mark texture
			Minecraft.getMinecraft().renderEngine.bindTexture( AspectWidgetBase.UNKNOWN_TEXTURE );

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

	/**
	 * Called when the stack changes.
	 */
	protected void onStackChanged()
	{
		// Is there an aspect?
		if( this.aspectStack.hasAspect() )
		{
			// Get the aspect name
			this.aspectName = this.aspectStack.getAspectName( this.player );

			// Get if the player has discovered this aspect
			this.hasDiscovered = this.aspectStack.hasPlayerDiscovered( this.player );
			if( this.hasDiscovered )
			{
				// Get the description
				this.aspectDescription = this.aspectStack.getAspectDescription();

				// Set footnote
				if( this.aspectStack.getAspect().isPrimal() )
				{
					this.aspectFootnote = StatCollector.translateToLocal( "tc.aspect.primal" );
				}
				else
				{
					this.aspectFootnote = ThEStrings.Gui_SelectedAspect.getLocalized();
				}
			}
			else
			{
				this.aspectDescription = this.aspectName;
				this.aspectFootnote = this.aspectName;
			}

			// Get the color bytes
			this.aspectColorBytes = ThEGuiHelper.INSTANCE.convertPackedColorToARGBb( this.aspectStack.getAspect().getColor() );

			// Set full alpha
			this.aspectColorBytes[0] = (byte)255;
		}
		else
		{
			// Clear the info
			this.clearStack( false );
		}
	}

	/**
	 * Clears the stack.
	 */
	public void clearWidget()
	{
		this.clearStack( true );
	}

	/**
	 * Gets the stack size.
	 * 
	 * @return
	 */
	public long getAmount()
	{
		return this.aspectStack.getStackSize();
	}

	/**
	 * Gets the aspect stack for this widget.
	 * 
	 * @return
	 */
	public Aspect getAspect()
	{
		return this.aspectStack.getAspect();
	}

	/**
	 * Gets whether or not the stack is craftable.
	 * 
	 * @return
	 */
	public boolean getCraftable()
	{
		return this.aspectStack.getCraftable();
	}

	/**
	 * Returns the aspect stack.
	 * 
	 * @return
	 */
	public IAspectStack getStack()
	{
		return this.aspectStack;
	}

	/**
	 * Returns true if the widget has an aspect.
	 * 
	 * @return
	 */
	public boolean hasAspect()
	{
		return this.aspectStack.hasAspect();
	}

	/**
	 * Set's the aspect stack based on the passed values.
	 * 
	 * @param aspect
	 * @param amount
	 * @param isCraftable
	 */
	public void setAspect( final Aspect aspect, final long amount, final boolean isCraftable )
	{
		// Set the aspect
		this.aspectStack.setAll( aspect, amount, isCraftable );

		this.onStackChanged();
	}

	/**
	 * Sets the aspect based on the stack
	 * 
	 * @param stack
	 */
	public void setAspect( final IAspectStack stack )
	{
		// Copy the values
		this.aspectStack.setAll( stack );

		this.onStackChanged();
	}
}
