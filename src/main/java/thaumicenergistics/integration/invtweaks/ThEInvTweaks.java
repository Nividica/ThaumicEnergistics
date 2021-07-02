package thaumicenergistics.integration.invtweaks;

import invtweaks.api.InvTweaksAPI;
import net.minecraftforge.fml.common.Loader;
import thaumicenergistics.integration.IThEIntegration;
import thaumicenergistics.util.ThELog;

/**
 * @author Alex811
 */
public class ThEInvTweaks implements IThEIntegration {
    private static InvTweaksAPI api = null;

    @Override
    public void init() {
        if(this.isLoaded()) {
            try {
                api = (InvTweaksAPI) Class.forName("invtweaks.forge.InvTweaksMod", true, Loader.instance().getModClassLoader())
                        .getField("instance")
                        .get(null);
            } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException ex) {
                ThELog.error("Can't grab the Inventory Tweaks API!", ex);
            }
        }
    }

    @Override
    public String getModID() {
        return "inventorytweaks";
    }

    public static InvTweaksAPI getApi() {
        return api;
    }
}
