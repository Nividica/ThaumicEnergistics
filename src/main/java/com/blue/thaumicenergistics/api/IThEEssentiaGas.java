package com.blue.thaumicenergistics.api;

import javax.annotation.Nullable;
import thaumcraft.api.aspects.Aspect;

/**
 * Gaseous essentia accessor methods.
 */

public interface IThEEssentiaGas
{
    /**
     * Get the aspect of the gas.<br>
     * Can be null if the gas itself is invalid.
     *
     * @return
     */
    @Nullable
    Aspect getAspect();
}
