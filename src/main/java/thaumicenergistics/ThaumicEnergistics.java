package thaumicenergistics;

import org.apache.logging.log4j.Logger;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.IThEIntegration;
import thaumicenergistics.integration.appeng.ThEAppliedEnergistics;
import thaumicenergistics.integration.thaumcraft.ThEThaumcraft;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.util.ThELog;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

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

    /**
     * Thaumic Energistics Logger
     */
    public static Logger LOGGER;

    private static List<IThEIntegration> INTEGRATIONS = new ArrayList<>();

    /**
     * Called before the load event.
     *
     * @param event FMLPreInitializationEvent
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ThaumicEnergistics.LOGGER = event.getModLog();

        PacketHandler.register();

        ThaumicEnergistics.INTEGRATIONS.add(new ThEThaumcraft());
        ThaumicEnergistics.INTEGRATIONS.add(new ThEAppliedEnergistics());

        ThELog.info("Integrations: PreInit");
        ThaumicEnergistics.INTEGRATIONS.forEach(IThEIntegration::preInit);
    }

    /**
     * Called after the preInit event, and before the post init event.
     *
     * @param event FMLInitializationEvent
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ThaumicEnergistics.INSTANCE, new GuiHandler());

        ThELog.info("Integrations: Init");
        ThaumicEnergistics.INTEGRATIONS.forEach(IThEIntegration::init);
    }

    /**
     * Called after the load event.
     *
     * @param event FMLPostInitializationEvent
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ThELog.info("Integrations: PostInit");
        ThaumicEnergistics.INTEGRATIONS.forEach(IThEIntegration::postInit);
    }
}
