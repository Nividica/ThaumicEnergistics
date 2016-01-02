package thaumicenergistics.gui.abstraction;

import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import thaumicenergistics.util.GuiHelper;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base GUI for guis with scrollbars.
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public abstract class AbstractGuiWithScrollbar
	extends AbstractGuiBase
{
	public class ScrollbarParams
	{
		/**
		 * X position of the scroll bar
		 */
		int scrollbarPosX;

		/**
		 * Y position of the scroll bar
		 */
		int scrollbarPosY;

		/**
		 * Height of the scroll bar
		 */
		int scrollbarHeight;

		/**
		 * Position + Height of the scroll bar.
		 */
		int scrollbarVerticalBound;

		/**
		 * Create the parameters
		 * 
		 * @param x
		 * @param y
		 * @param height
		 */
		public ScrollbarParams( final int x, final int y, final int height )
		{
			this.scrollbarPosX = x;
			this.scrollbarPosY = y;
			this.setHeight( height );
		}

		/**
		 * Sets the height of the scrollbar
		 * 
		 * @param height
		 */
		void setHeight( final int height )
		{
			this.scrollbarHeight = height;
			this.scrollbarVerticalBound = this.scrollbarHeight + this.scrollbarPosY;
		}
	}

	/**
	 * Scroll bar
	 */
	protected final GuiScrollbar scrollBar;

	/**
	 * Serves as a graphics call bridge for the scroll bar.
	 */
	private AEBaseGui aeGuiBridge;
	/**
	 * True if the scroll bar has mouse focus.
	 */
	private boolean isScrollBarHeld = false;
	/**
	 * The last Y position of the mouse when the scroll bar has mouse focus.
	 */
	private int scrollHeldPrevY = 0;

	/**
	 * Scrollbar parameters
	 */
	private ScrollbarParams scrollParams;

	public AbstractGuiWithScrollbar( final Container container )
	{
		super( container );

		// Create the scrollbar
		this.scrollBar = new GuiScrollbar();
	}

	/**
	 * Draw the foreground layer.
	 */
	@Override
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Call super
		super.drawGuiContainerForegroundLayer( mouseX, mouseY );

		// Draw the scroll bar
		this.scrollBar.draw( this.aeGuiBridge );
	}

	/**
	 * Gets the scroll bar parameters from the subclass.
	 * 
	 * @return
	 */
	protected abstract ScrollbarParams getScrollbarParameters();

	/**
	 * Called when the player types a key.
	 */
	@Override
	protected void keyTyped( final char key, final int keyID )
	{
		// Home Key
		if( keyID == Keyboard.KEY_HOME )
		{
			// Move the scroll all the way to home
			this.scrollBar.click( this.aeGuiBridge, this.scrollParams.scrollbarPosX + 1, this.scrollParams.scrollbarPosY + 1 );
			this.scrollBar.wheel( 1 );
			this.onScrollbarMoved();
		}
		// End Key
		else if( keyID == Keyboard.KEY_END )
		{
			// Move the scroll all the way to end
			this.scrollBar.click( this.aeGuiBridge, this.scrollParams.scrollbarPosX + 1, this.scrollParams.scrollbarVerticalBound );
			this.onScrollbarMoved();

		}
		// Up Key
		else if( keyID == Keyboard.KEY_UP )
		{
			this.scrollBar.wheel( 1 );
			this.onScrollbarMoved();
		}
		// Down Key
		else if( keyID == Keyboard.KEY_DOWN )
		{
			this.scrollBar.wheel( -1 );
			this.onScrollbarMoved();
		}
		else
		{
			super.keyTyped( key, keyID );
		}

	}

	/**
	 * Called when the mouse is clicked while the gui is open
	 */
	@Override
	protected void mouseClicked( final int mouseX, final int mouseY, final int mouseButton )
	{
		// Is the mouse over the scroll bar area?
		if( GuiHelper.INSTANCE.isPointInGuiRegion( this.scrollParams.scrollbarPosY, this.scrollParams.scrollbarPosX,
			this.scrollParams.scrollbarHeight, this.scrollBar.getWidth(), mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// The scroll bar now has mouse focus
			this.isScrollBarHeld = true;

			// Mark this Y
			this.scrollHeldPrevY = mouseY;

			// Jump the scroll to the mouse
			this.scrollBar.click( this.aeGuiBridge, mouseX - this.guiLeft, mouseY - this.guiTop );

			// Update the subclass
			this.onScrollbarMoved();

			// Do not pass to super
			return;
		}

		// Call super
		super.mouseClicked( mouseX, mouseY, mouseButton );
	}

	/**
	 * Called when the scroll bar has moved.
	 */
	protected abstract void onScrollbarMoved();

	/**
	 * Changes the height of the scroll bar.
	 * 
	 * @param newHeight
	 */
	protected void setScrollBarHeight( final int newHeight )
	{
		this.scrollParams.setHeight( newHeight );
		this.scrollBar.setHeight( newHeight );
	}

	@Override
	public void drawScreen( final int mouseX, final int mouseY, final float mouseBtn )
	{
		// Is the mouse holding the scroll bar?
		if( this.isScrollBarHeld )
		{
			// Is the mouse button still being held down?
			if( Mouse.isButtonDown( GuiHelper.MOUSE_BUTTON_LEFT ) )
			{
				// Has the Y changed?
				if( mouseY == this.scrollHeldPrevY )
				{
					return;
				}

				boolean correctForZero = false;

				// Mark the Y
				this.scrollHeldPrevY = mouseY;

				// Calculate the Y position for the scroll bar
				int repY = mouseY - this.guiTop;

				// Has the mouse exceeded the 'upper' bound?
				if( repY > this.scrollParams.scrollbarVerticalBound )
				{
					repY = this.scrollParams.scrollbarVerticalBound;
				}
				// Has the mouse exceeded the 'lower' bound?
				else if( repY <= this.scrollParams.scrollbarPosY )
				{
					repY = this.scrollParams.scrollbarPosY;

					// We will have to correct for zero
					correctForZero = true;
				}

				// Update the scroll bar
				this.scrollBar.click( this.aeGuiBridge, this.scrollParams.scrollbarPosX + 1, repY );

				// Should we correct for zero?
				if( correctForZero )
				{
					this.scrollBar.wheel( 1 );
				}

				// Inform the subclass the scrollbar has moved
				this.onScrollbarMoved();
			}
			else
			{
				// The scroll bar no longer has mouse focus
				this.isScrollBarHeld = false;
			}
		}

		// Call super
		super.drawScreen( mouseX, mouseY, mouseBtn );

	}

	/**
	 * Sets the gui up.
	 */
	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		// Get the params
		this.scrollParams = this.getScrollbarParameters();

		// Setup the scroll bar
		this.scrollBar.setLeft( this.scrollParams.scrollbarPosX ).setTop( this.scrollParams.scrollbarPosY )
						.setHeight( this.scrollParams.scrollbarHeight );

		// No scrolling yet
		this.scrollBar.setRange( 0, 0, 1 );

		// Create the AE bridge
		this.aeGuiBridge = new AEBaseGui( this.inventorySlots )
		{
			@Override
			public void bindTexture( final String file )
			{
				this.bindTexture( "appliedenergistics2", file );
			}

			@Override
			public void bindTexture( final String base, final String file )
			{
				AbstractGuiWithScrollbar.this.mc.getTextureManager().bindTexture( new ResourceLocation( base, "textures/" + file ) );
			}

			@Override
			public void drawBG( final int arg0, final int arg1, final int arg2, final int arg3 )
			{
				// Ignored
			}

			@Override
			public void drawFG( final int arg0, final int arg1, final int arg2, final int arg3 )
			{
				// Ignored
			}

			@Override
			public void drawTexturedModalRect( final int posX, final int posY, final int sourceOffsetX, final int sourceOffsetY, final int width,
												final int height )
			{
				AbstractGuiWithScrollbar.this.drawTexturedModalRect( posX, posY, sourceOffsetX, sourceOffsetY, width, height );
			}
		};
	}

}
