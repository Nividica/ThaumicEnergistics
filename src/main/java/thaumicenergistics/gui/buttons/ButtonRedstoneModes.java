package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import thaumicenergistics.texture.EnumAEStateIcons;
import appeng.api.config.RedstoneMode;
import com.google.common.base.Splitter;

public class ButtonRedstoneModes
	extends AbstractAEButton
{
	/**
	 * Localization header string for tooltips
	 */
	private static final String TOOLTIP_LOC_HEADER = "gui.tooltips.appliedenergistics2.";

	/**
	 * Mode to represent
	 */
	private RedstoneMode redstoneMode;

	/**
	 * True when this button is attached to a level emitter
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
		// Call super
		super( ID, xPos, yPos, width, height, null );

		// Set the if we are attached to an emitter
		this.emitter = emitter;

		// Set the redstone mode
		this.setRedstoneMode( mode );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
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
	}

	/**
	 * Sets the redstone mode this button represents.
	 * 
	 * @param mode
	 */
	public void setRedstoneMode( RedstoneMode mode )
	{
		// Set the mode
		this.redstoneMode = mode;

		// Set the icon
		switch ( this.redstoneMode )
		{
			case HIGH_SIGNAL:
				this.icon = EnumAEStateIcons.REDSTONE_HIGH;
				break;

			case IGNORE:
				this.icon = EnumAEStateIcons.REDSTONE_IGNORE;
				break;

			case LOW_SIGNAL:
				this.icon = EnumAEStateIcons.REDSTONE_LOW;
				break;

			case SIGNAL_PULSE:
				this.icon = EnumAEStateIcons.REDSTONE_PULSE;
				break;
		}
	}
}
