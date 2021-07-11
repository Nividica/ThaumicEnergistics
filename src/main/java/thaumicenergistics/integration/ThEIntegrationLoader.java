package thaumicenergistics.integration;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEAppliedEnergistics;
import thaumicenergistics.integration.hwyla.ThEHwyla;
import thaumicenergistics.integration.invtweaks.ThEInvTweaks;
import thaumicenergistics.integration.thaumcraft.ThEThaumcraft;
import thaumicenergistics.integration.theoneprobe.ThETheOneProbe;
import thaumicenergistics.util.ThELog;

import java.util.HashMap;

/**
 * @author Alex811
 */
public class ThEIntegrationLoader {
    private static final HashMap<IThEIntegration, String> INTEGRATIONS = new HashMap<>();
    private static final ModAPIManager apiManager = ModAPIManager.INSTANCE;

    static {
        registerIntegration("thaumcraft", ThEThaumcraft.class);
        registerIntegration(ModGlobals.MOD_ID_AE2, ThEAppliedEnergistics.class);
        registerIntegration("inventorytweaks", ThEInvTweaks.class);
        registerIntegration("waila", ThEHwyla.class);
        registerIntegration("theoneprobe", ThETheOneProbe.class);
    }

    private static void registerIntegration(String modId, Class<? extends IThEIntegration> integration) {
        if(Loader.isModLoaded(modId) || apiManager.hasAPI(modId)) {
            try {
                INTEGRATIONS.put(integration.newInstance(), modId);
                ThELog.info("Integrations: Registered [" + modId + "]");
            } catch (InstantiationException | IllegalAccessException ex) {
                ThELog.error("Failed to instantiate an integration class", ex);
            }
        }else
            ThELog.debug("Integrations: Not found [" + modId + "]");
    }

    public static String getModId(IThEIntegration integration) {
        return INTEGRATIONS.get(integration);
    }

    public static void preInit() {
        ThELog.info("Integrations: PreInit");
        INTEGRATIONS.keySet().forEach(IThEIntegration::preInit);
    }

    public static void init() {
        ThELog.info("Integrations: Init");
        INTEGRATIONS.keySet().forEach(IThEIntegration::init);
    }

    public static void postInit() {
        ThELog.info("Integrations: PostInit");
        INTEGRATIONS.keySet().forEach(IThEIntegration::postInit);
    }
}
