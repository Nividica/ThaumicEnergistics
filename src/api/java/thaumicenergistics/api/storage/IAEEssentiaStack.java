package thaumicenergistics.api.storage;

import appeng.api.storage.data.IAEStack;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.api.EssentiaStack;

/**
 * @author BrockWS
 */
public interface IAEEssentiaStack extends IAEStack<IAEEssentiaStack> {

    /**
     * Get the Thaumcraft aspect of the stack
     *
     * @return Thaumcraft Aspect
     */
    Aspect getAspect();

    /**
     * Get the general Essentia Stack this AEStack represents
     *
     * @return Essentia Stack of this stack
     */
    EssentiaStack getStack();
}
