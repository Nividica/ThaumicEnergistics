package thaumicenergistics.gui.widget;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import thaumicenergistics.util.GuiHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.AppEngRenderItem;

public class WidgetAEItem
	extends AbstractWidget
{
	/**
	 * Cache of the minecraft instance
	 */
	private static final Minecraft MC = Minecraft.getMinecraft();

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

	/**
	 * Draw's the tooltip for the widgets itemstack.
	 */
	@Override
	public void drawTooltip( int mouseX, int mouseY )
	{
		if( this.aeItemStack != null )
		{
			// Get the stack
			ItemStack stack = this.aeItemStack.getItemStack();

			// Get the tooltip list
			List<String> tooltip = stack.getTooltip( WidgetAEItem.MC.thePlayer, WidgetAEItem.MC.gameSettings.advancedItemTooltips );

			// Set colors
			for( int index = 0; index < tooltip.size(); index++ )
			{
				if( index == 0 )
				{
					// Item name based on rarity
					tooltip.set( index, stack.getRarity().rarityColor + tooltip.get( index ) );
				}
				else
				{
					// The rest grey
					tooltip.set( index, EnumChatFormatting.GRAY + tooltip.get( index ) );
				}
			}

			// Draw the tooltip
			GuiHelper.instance.drawTooltip( (Gui)this.hostGUI, tooltip, mouseX, mouseY, WidgetAEItem.MC.fontRenderer );
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
			this.aeItemRenderer.renderItemAndEffectIntoGUI( WidgetAEItem.MC.fontRenderer, WidgetAEItem.TEXTURE_MANAGER,
				this.aeItemStack.getItemStack(), this.xPosition + 1, this.yPosition + 1 );

			// Draw the amount
			this.aeItemRenderer.renderItemOverlayIntoGUI( WidgetAEItem.MC.fontRenderer, WidgetAEItem.TEXTURE_MANAGER,
				this.aeItemStack.getItemStack(), this.xPosition + 1, this.yPosition + 1 );

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
