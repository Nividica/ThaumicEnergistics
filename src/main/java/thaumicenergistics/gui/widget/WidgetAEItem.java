package thaumicenergistics.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import thaumicenergistics.gui.GuiArcaneCraftingTerminal;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.AppEngRenderItem;

public class WidgetAEItem
	extends AbstractWidget
{
	/**
	 * Cache of the minecraft font renderer
	 */
	private static final FontRenderer FONT_RENDERER = Minecraft.getMinecraft().fontRenderer;

	/**
	 * Cache of the minecraft texture manager
	 */
	private static final TextureManager TEXTURE_MANAGER = Minecraft.getMinecraft().getTextureManager();

	/**
	 * Cache of the AE item renderer
	 */
	private final AppEngRenderItem aeItemRenderer;

	/**
	 * The itemstack this widget represents
	 */
	private IAEItemStack aeItemStack;

	/**
	 * Creates the widget
	 * 
	 * @param hostGUI
	 * @param xPos
	 * @param yPos
	 * @param aeItemRenderer
	 */
	public WidgetAEItem( IWidgetHost hostGUI, int xPos, int yPos, AppEngRenderItem aeItemRenderer )
	{
		super( hostGUI, xPos, yPos );

		this.aeItemRenderer = aeItemRenderer;
	}

	@Override
	public void drawTooltip( int mouseX, int mouseY )
	{
		if( this.aeItemStack != null )
		{
			( (GuiArcaneCraftingTerminal)this.hostGUI ).renderToolTip( this.aeItemStack.getItemStack(), mouseX, mouseY );
		}
	}

	/**
	 * Draws the itemstack if there is one.
	 */
	@Override
	public void drawWidget()
	{
		if( this.aeItemStack != null )
		{
			// Set the z level
			this.zLevel = 2.0F;
			this.aeItemRenderer.zLevel = 2.0F;

			// Set the item
			this.aeItemRenderer.aestack = this.aeItemStack;

			// Draw the item
			this.aeItemRenderer.renderItemAndEffectIntoGUI( WidgetAEItem.FONT_RENDERER, WidgetAEItem.TEXTURE_MANAGER,
				this.aeItemStack.getItemStack(), this.xPosition + 1, this.yPosition + 1 );

			// Draw the amount
			this.aeItemRenderer.renderItemOverlayIntoGUI( WidgetAEItem.FONT_RENDERER, WidgetAEItem.TEXTURE_MANAGER, this.aeItemStack.getItemStack(),
				this.xPosition + 1, this.yPosition + 1 );

			// Reset the z level
			this.zLevel = 0.0F;
			this.aeItemRenderer.zLevel = 0.0F;
		}

	}

	/**
	 * Returns the itemstack this widget represents.
	 * 
	 * @return IAEItemstack if the widget has one, null otherwise.
	 */
	public IAEItemStack getItemStack()
	{
		return this.aeItemStack;
	}

	@Override
	public void mouseClicked()
	{
		// Unused
	}

	/**
	 * Set the itemstack this widget represents
	 * 
	 * @param itemStack
	 */
	public void setItemStack( IAEItemStack itemStack )
	{
		this.aeItemStack = itemStack;
	}

}
