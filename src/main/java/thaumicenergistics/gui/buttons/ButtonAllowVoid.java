package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import thaumcraft.common.config.ConfigBlocks;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.texture.AEStateIconsEnum;

public class ButtonAllowVoid
	extends AbstractButtonBase
{
	/**
	 * Cache the render engine
	 */
	private static final TextureManager RENDER_ENGINE = Minecraft.getMinecraft().renderEngine;

	/**
	 * Cache the font renderer
	 */
	private static final FontRenderer FONT_RENDERER = Minecraft.getMinecraft().fontRenderer;

	/**
	 * Height and width of the button
	 */
	private static final int BUTTON_SIZE = 18;

	/**
	 * Height and width of the disabled icon.
	 */
	private static final int DISABLED_ICON_SIZE = 16;

	/**
	 * Create an item renderer
	 */
	private static RenderItem itemRenderer = new RenderItem();

	/**
	 * Void jar itemstack.
	 */
	private static ItemStack voidJar = null;

	/**
	 * Disabled icon from the AE sprite sheet.
	 */
	private AEStateIconsEnum disabledIcon = AEStateIconsEnum.DISABLED;

	/**
	 * When false, draws the disabled icon over the jar
	 */
	public boolean isVoidAllowed = false;

	public ButtonAllowVoid( final int ID, final int xPosition, final int yPosition )
	{
		super( ID, xPosition, yPosition, ButtonAllowVoid.BUTTON_SIZE, ButtonAllowVoid.BUTTON_SIZE, "" );

		// Ensure we have made the void jar stack
		if( ButtonAllowVoid.voidJar == null )
		{
			ButtonAllowVoid.voidJar = new ItemStack( ConfigBlocks.blockJar, 1, 3 );
		}
	}

	@Override
	public void drawButton( final Minecraft minecraftInstance, final int x, final int y )
	{
		// Draw the usual button
		super.drawButton( minecraftInstance, x, y );

		// Draw the void jar
		itemRenderer.renderItemIntoGUI( ButtonAllowVoid.FONT_RENDERER, ButtonAllowVoid.RENDER_ENGINE, voidJar, this.xPosition + 1,
			this.yPosition + 1, false );

		// Is void not allowed?
		if( !this.isVoidAllowed )
		{
			// Bind the AE states texture
			minecraftInstance.getTextureManager().bindTexture( AEStateIconsEnum.AE_STATES_TEXTURE );

			// Draw the disabled icon
			this.drawTexturedModalRect( this.xPosition + 1, this.yPosition + 1, this.disabledIcon.getU(), this.disabledIcon.getV(),
				ButtonAllowVoid.DISABLED_ICON_SIZE, ButtonAllowVoid.DISABLED_ICON_SIZE );
		}
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		// Add the title
		tooltip.add( ThEStrings.TooltipButton_VoidHeader.getLocalized() );

		// Add allowed/disabled
		if( this.isVoidAllowed )
		{
			tooltip.add( EnumChatFormatting.GRAY + ThEStrings.TooltipButton_VoidAllow.getLocalized() );
		}
		else
		{
			tooltip.add( EnumChatFormatting.GRAY + ThEStrings.TooltipButton_VoidDisable.getLocalized() );
		}

		// Add the note
		tooltip.add( EnumChatFormatting.ITALIC + ThEStrings.TooltipButton_VoidNote.getLocalized() );

	}
}
