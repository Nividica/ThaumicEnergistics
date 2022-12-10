package thaumicenergistics.api;

import javax.annotation.Nullable;
import thaumcraft.api.aspects.Aspect;

/**
 * Gaseous essentia accessor methods.
 *
 * @author Nividica
 *
 */
public interface IThEEssentiaGas {
    /**
     * Get the aspect of the gas.<br>
     * Can be null if the gas itself is invalid.
     *
     * @return
     */
    @Nullable
    Aspect getAspect();
}
