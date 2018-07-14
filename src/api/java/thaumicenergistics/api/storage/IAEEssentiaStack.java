package thaumicenergistics.api.storage;

import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.EssentiaStack;

import appeng.api.storage.data.IAEStack;

/**
 * @author BrockWS
 */
public interface IAEEssentiaStack extends IAEStack<IAEEssentiaStack> {

    Aspect getAspect();

    EssentiaStack getStack();
}
