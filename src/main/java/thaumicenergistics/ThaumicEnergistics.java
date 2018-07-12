package thaumicenergistics;

import org.apache.logging.log4j.Logger;
import thaumicenergistics.api.IThEIntegration;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEAppliedEnergistics;
import thaumicenergistics.integration.thaumcraft.ThEThaumcraft;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * <strong>Thaumic Energistics</strong>
 * <hr>
 * A bridge between Thaumcraft and Applied Energistics. Essentia storage management, transportation, and application.
 *
 * @author Nividica
 */
@Mod(modid = ModGlobals.MOD_ID, name = ModGlobals.MOD_NAME, version = ModGlobals.MOD_VERSION, dependencies = ModGlobals.MOD_DEPENDENCIES)
public class ThaumicEnergistics {

    /**
     * Singleton instance
     */
    @Mod.Instance(value = ModGlobals.MOD_ID)
    public static ThaumicEnergistics INSTANCE;

    private IThEIntegration thaumcraftIntegration = new ThEThaumcraft();
    private IThEIntegration appliedEnergisticsIntegration = new ThEAppliedEnergistics();

    public static Logger LOG;

    /**
     * Called before the load event.
     *
     * @param event FMLPreInitializationEvent
     */
    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        LOG = event.getModLog();
        LOG.info("Integrations: PreInit");
        thaumcraftIntegration.preInit();
        appliedEnergisticsIntegration.preInit();
    }

    /**
     * Called after the preInit event, and before the post init event.
     *
     * @param event FMLInitializationEvent
     */
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        LOG.info("Integrations: Init");
        thaumcraftIntegration.init();
        appliedEnergisticsIntegration.init();
    }

    /**
     * Called after the load event.
     *
     * @param event FMLPostInitializationEvent
     */
    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        LOG.info("Integrations: PostInit");
        thaumcraftIntegration.postInit();
        appliedEnergisticsIntegration.postInit();
    }
}
