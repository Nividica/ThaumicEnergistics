package thaumicenergistics.api.gui;

import java.util.List;
import javax.annotation.Nonnull;
import thaumcraft.api.aspects.Aspect;

/**
 * GUI that displays aspects.
 *
 * @author Nividica
 *
 */
public interface IAspectSlotGui extends IWidgetHost {
    /**
     * The list of aspects to display.
     *
     * @param aspectList
     */
    void updateAspects(@Nonnull List<Aspect> aspectList);
}
