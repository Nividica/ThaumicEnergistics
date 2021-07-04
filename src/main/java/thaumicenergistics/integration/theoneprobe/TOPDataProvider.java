package thaumicenergistics.integration.theoneprobe;

import mcjty.theoneprobe.api.IProbeInfoProvider;
import thaumicenergistics.init.ModGlobals;

/**
 * @author Alex811
 */
public abstract class TOPDataProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return ModGlobals.MOD_ID + ":" + this.getClass().getSimpleName();
    }
}
