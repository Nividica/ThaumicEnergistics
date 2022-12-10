package thaumicenergistics.common.integration.tc;

import cpw.mods.fml.common.ModContainer;
import java.util.HashMap;
import thaumcraft.api.aspects.Aspect;

/**
 * Manages hooks inserted into Thaumcraft's {@code Aspect} class.
 *
 * @author Nividica
 *
 */
public class AspectHooks {

    /**
     * TODO: Make private and provide wrapper for name or unknown
     * Maps an aspect -> mod.
     */
    public static final HashMap<Aspect, ModContainer> aspectToMod = new HashMap<Aspect, ModContainer>();

    /**
     * Called from the constructor.
     *
     * @param aspect
     */
    public static void hook_AspectInit(final Aspect aspect) {
        // Get the mod
        ModContainer mod = cpw.mods.fml.common.Loader.instance().activeModContainer();

        // Add to the map
        aspectToMod.put(aspect, mod);
    }
}
