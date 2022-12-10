package thaumicenergistics.api.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import thaumcraft.api.aspects.Aspect;

/**
 * GUI that has a selectable aspect.
 *
 * @author Nividica
 *
 */
public interface IAspectSelectorGui extends IWidgetHost {
    /**
     * Return the selector container.
     *
     * @return
     */
    @Nonnull
    IAspectSelectorContainer getContainer();

    /**
     * Return the selected aspect, or null if no aspect is selected.
     *
     * @return
     */
    @Nullable
    Aspect getSelectedAspect();
}
