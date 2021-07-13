package thaumicenergistics.integration.theoneprobe;

import mcjty.theoneprobe.api.IProbeInfoProvider;
import thaumicenergistics.api.IThELangKey;
import thaumicenergistics.init.ModGlobals;

import static mcjty.theoneprobe.api.IProbeInfo.ENDLOC;
import static mcjty.theoneprobe.api.IProbeInfo.STARTLOC;

/**
 * @author Alex811
 */
public abstract class TOPDataProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return ModGlobals.MOD_ID + ":" + this.getClass().getSimpleName();
    }

    protected String getLocalizedKey(IThELangKey key) {
        return STARTLOC + key.getUnlocalizedKey() + ENDLOC;
    }
}
