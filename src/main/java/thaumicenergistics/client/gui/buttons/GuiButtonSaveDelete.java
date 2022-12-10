package thaumicenergistics.client.gui.buttons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.common.container.ContainerKnowledgeInscriber.CoreSaveState;
import thaumicenergistics.common.registries.ThEStrings;

/**
 * Displays save state icons.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class GuiButtonSaveDelete extends ThEStateButton {
    private String cachedTooltip;

    public GuiButtonSaveDelete(
            final int ID, final int xPosition, final int yPosition, final CoreSaveState initialState) {
        // Call super
        super(ID, xPosition, yPosition, 16, 16, null, 0, 0, AEStateIconsEnum.REGULAR_BUTTON);

        // Initial state
        this.setSaveState(initialState);
    }

    @Override
    public void getTooltip(final List<String> tooltip) {
        tooltip.add(this.cachedTooltip);
    }

    /**
     * Sets the save state of the button.
     *
     * @param saveState
     */
    public void setSaveState(final CoreSaveState saveState) {
        switch (saveState) {
            case Disabled_InvalidRecipe:
                this.enabled = false;
                this.stateIcon = null;
                this.cachedTooltip = ThEStrings.TooltipButton_InscriberInvalid.getLocalized();
                break;

            case Disabled_CoreFull:
                this.enabled = false;
                this.stateIcon = null;
                this.cachedTooltip = ThEStrings.TooltipButton_InscriberFull.getLocalized();
                break;

            case Enabled_Delete:
                this.enabled = true;
                this.stateIcon = AEStateIconsEnum.DELETE;
                this.cachedTooltip = ThEStrings.TooltipButton_InscriberDelete.getLocalized();
                break;

            case Enabled_Save:
                this.enabled = true;
                this.stateIcon = AEStateIconsEnum.SAVE;
                this.cachedTooltip = ThEStrings.TooltipButton_InscriberSave.getLocalized();
                break;

            case Disabled_MissingCore:
                this.enabled = false;
                this.stateIcon = null;
                this.cachedTooltip = ThEStrings.TooltipButton_InscriberMissing.getLocalized();
                break;
        }
    }
}
