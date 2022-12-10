package thaumicenergistics.api.gui;

import javax.annotation.Nullable;
import thaumcraft.api.aspects.Aspect;

/**
 * Container that can have a selected aspect.
 *
 * @author Nividica
 *
 */
public interface IAspectSelectorContainer {
    /**
     * Set the selected aspect.
     *
     * @param selectedAspect
     * Null if no selection.
     */
    void setSelectedAspect(@Nullable Aspect selectedAspect);
}
