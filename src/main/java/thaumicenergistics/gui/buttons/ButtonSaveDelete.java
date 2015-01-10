package thaumicenergistics.gui.buttons;

import java.util.List;
import thaumicenergistics.container.ContainerKnowledgeInscriber.CoreSaveState;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.texture.AEStateIconsEnum;

public class ButtonSaveDelete
	extends AbstractAEButton
{
	private String cachedTooltip;

	public ButtonSaveDelete( final int ID, final int xPosition, final int yPosition, final CoreSaveState initialState )
	{
		// Call super
		super( ID, xPosition, yPosition, 16, 16, null );

		// Initial state
		this.setSaveState( initialState );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		tooltip.add( this.cachedTooltip );
	}

	/**
	 * Sets the save state of the button.
	 * 
	 * @param saveState
	 */
	public void setSaveState( final CoreSaveState saveState )
	{
		switch ( saveState )
		{
			case Disabled_InvalidRecipe:
				this.enabled = false;
				this.icon = null;
				this.cachedTooltip = ThEStrings.TooltipButton_InscriberInvalid.getLocalized();
				break;

			case Disabled_CoreFull:
				this.enabled = false;
				this.icon = null;
				this.cachedTooltip = ThEStrings.TooltipButton_InscriberFull.getLocalized();
				break;

			case Enabled_Delete:
				this.enabled = true;
				this.icon = AEStateIconsEnum.DELETE;
				this.cachedTooltip = ThEStrings.TooltipButton_InscriberDelete.getLocalized();
				break;

			case Enabled_Save:
				this.enabled = true;
				this.icon = AEStateIconsEnum.SAVE;
				this.cachedTooltip = ThEStrings.TooltipButton_InscriberSave.getLocalized();
				break;

			case Disabled_MissingCore:
				this.enabled = false;
				this.icon = null;
				this.cachedTooltip = ThEStrings.TooltipButton_InscriberMissing.getLocalized();
				break;

		}
	}
}
