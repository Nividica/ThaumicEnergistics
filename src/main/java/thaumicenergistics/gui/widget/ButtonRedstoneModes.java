package thaumicenergistics.gui.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.util.GuiHelper;
import appeng.api.config.RedstoneMode;
import com.google.common.base.Splitter;

public class ButtonRedstoneModes
	extends GuiButton
{
	/**
	 * Location of the redstone modes texture
	 */
	private static final ResourceLocation TEXTURE_REDSTONE_MODES = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/gui/redstonemodes.png" );

	/**
	 * Localization header string for tooltips
	 */
	private static final String TOOLTIP_LOC_HEADER = "gui.tooltips.appliedenergistics2.";

	/**
	 * Mode to represent
	 */
	private RedstoneMode redstoneMode;

	/**
	 * True when this button is attacted to a level emitter
	 */
	private boolean emitter = false;

	/**
	 * Creates the button.
	 * 
	 * @param ID
	 * @param xPos
	 * @param yPos
	 * @param width
	 * @param height
	 * @param mode
	 */
	public ButtonRedstoneModes( int ID, int xPos, int yPos, int width, int height, RedstoneMode mode )
	{
		this( ID, xPos, yPos, width, height, mode, false );
	}

	/**
	 * Creates the button with argument for if this is an emitter.
	 * 
	 * @param ID
	 * @param xPos
	 * @param yPos
	 * @param width
	 * @param height
	 * @param mode
	 * @param emitter
	 */
	public ButtonRedstoneModes( int ID, int xPos, int yPos, int width, int height, RedstoneMode mode, boolean emitter )
	{
		super( ID, xPos, yPos, width, height, "Display String" );

		this.emitter = emitter;

		this.redstoneMode = mode;
	}

	/**
	 * Draws the button
	 */
	@Override
	public void drawButton( Minecraft minecraftInstance, int x, int y )
	{
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		minecraftInstance.getTextureManager().bindTexture( TEXTURE_REDSTONE_MODES );

		this.drawTexturedModalRect( this.xPosition, this.yPosition, 0, 16, 16, 16 );

		switch ( this.redstoneMode )
		{
			case HIGH_SIGNAL:
				this.drawTexturedModalRect( this.xPosition, this.yPosition, 16, 0, 16, 16 );
				break;

			case IGNORE:
				this.drawTexturedModalRect( this.xPosition, this.yPosition, 48, 0, 16, 16 );
				break;

			case LOW_SIGNAL:
				this.drawTexturedModalRect( this.xPosition, this.yPosition, 0, 0, 16, 16 );
				break;

			case SIGNAL_PULSE:
				this.drawTexturedModalRect( this.xPosition, this.yPosition, 32, 0, 16, 16 );
				break;
		}
	}

	/**
	 * Draws this buttons tooltip.
	 * 
	 * @param mouseX
	 * @param mouseY
	 */
	public void drawTooltip( int mouseX, int mouseY )
	{
		// Create the tooltip array
		List<String> tooltip = new ArrayList<String>();

		// Add header
		tooltip.add( StatCollector.translateToLocal( TOOLTIP_LOC_HEADER + "RedstoneMode" ) );

		// Get the explanation based on mode
		String explanation = "";
		switch ( this.redstoneMode )
		{
			case HIGH_SIGNAL:
				explanation = StatCollector.translateToLocal( this.emitter ? TOOLTIP_LOC_HEADER + "EmitLevelAbove" : TOOLTIP_LOC_HEADER +
								"ActiveWithSignal" );
				break;

			case IGNORE:
				explanation = StatCollector.translateToLocal( TOOLTIP_LOC_HEADER + "AlwaysActive" );
				break;

			case LOW_SIGNAL:
				explanation = StatCollector.translateToLocal( this.emitter ? TOOLTIP_LOC_HEADER + "EmitLevelsBelow" : TOOLTIP_LOC_HEADER +
								"ActiveWithoutSignal" );
				break;

			case SIGNAL_PULSE:
				explanation = StatCollector.translateToLocal( TOOLTIP_LOC_HEADER + "ActiveOnPulse" );
				break;

		}

		// Line wrap the explanation
		for( String current : Splitter.fixedLength( 30 ).split( explanation ) )
		{
			tooltip.add( EnumChatFormatting.GRAY + current );
		}

		// Draw the tooltip
		GuiHelper.drawTooltip( this, tooltip, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer );
	}

	/**
	 * Sets the redstone mode this button represents.
	 * 
	 * @param mode
	 */
	public void setRedstoneMode( RedstoneMode mode )
	{
		this.redstoneMode = mode;
	}
}
