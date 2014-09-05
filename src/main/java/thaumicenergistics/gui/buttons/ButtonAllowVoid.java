package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import thaumcraft.common.config.ConfigBlocks;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.texture.EnumAEStateIcons;

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
	 * Header string for all tooltips used.
	 */
	private static final String TOOLTIP_LOC_HEADER = ThaumicEnergistics.MOD_ID + ".tooltip.button.void";

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
	private EnumAEStateIcons disabledIcon = EnumAEStateIcons.DISABLED;

	/**
	 * First line of the tooltip
	 */
	private String tooltipTitle = StatCollector.translateToLocal( ButtonAllowVoid.TOOLTIP_LOC_HEADER );
	
	/**
	 * Second line of the tooltip if void is disabled
	 */
	private String tooltipDisabled = StatCollector.translateToLocal( ButtonAllowVoid.TOOLTIP_LOC_HEADER + ".disabled" );

	/**
	 * Second line of the tooltip if void is allowed
	 */
	private String tooltipAllowed = StatCollector.translateToLocal( ButtonAllowVoid.TOOLTIP_LOC_HEADER + ".allowed" );
	/**
	 * Third line of the tooltip
	 */
	private String tooltipNote = StatCollector.translateToLocal( ButtonAllowVoid.TOOLTIP_LOC_HEADER + ".note" );

	/**
	 * When false, draws the disabled icon over the jar
	 */
	public boolean isVoidAllowed = false;

	public ButtonAllowVoid( int ID, int xPosition, int yPosition )
	{
		super( ID, xPosition, yPosition, ButtonAllowVoid.BUTTON_SIZE, ButtonAllowVoid.BUTTON_SIZE, "" );

		// Ensure we have made the void jar stack
		if( ButtonAllowVoid.voidJar == null )
		{
			ButtonAllowVoid.voidJar = new ItemStack( ConfigBlocks.blockJar, 1, 3 );
		}
	}

	@Override
	public void getTooltip( List<String> tooltip )
	{
		// Add the title
		tooltip.add( this.tooltipTitle );
		
		// Add allowed/disabled
		if( this.isVoidAllowed )
		{
			tooltip.add( EnumChatFormatting.GRAY + this.tooltipAllowed );
		}
		else
		{
			tooltip.add( EnumChatFormatting.GRAY + this.tooltipDisabled );
		}
		
		// Add the note
		tooltip.add( EnumChatFormatting.ITALIC + this.tooltipNote );

	}

	@Override
	public void drawButton( Minecraft minecraftInstance, int x, int y )
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
			minecraftInstance.getTextureManager().bindTexture( EnumAEStateIcons.AE_STATES_TEXTURE );

			// Draw the disabled icon
			this.drawTexturedModalRect( this.xPosition + 1, this.yPosition + 1, this.disabledIcon.getU(), this.disabledIcon.getV(),
				ButtonAllowVoid.DISABLED_ICON_SIZE, ButtonAllowVoid.DISABLED_ICON_SIZE );
		}
	}
}
