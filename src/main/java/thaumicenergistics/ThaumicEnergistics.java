package thaumicenergistics;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.ThEItemColors;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.command.CommandAddVis;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.IThEIntegration;
import thaumicenergistics.integration.appeng.ThEAppliedEnergistics;
import thaumicenergistics.integration.thaumcraft.ThEThaumcraft;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.util.ForgeUtil;
import thaumicenergistics.util.ThELog;

/**
 * <strong>Thaumic Energistics</strong>
 * <hr>
 * A bridge between Thaumcraft and Applied Energistics. Essentia storage management, transportation, and application.
 *
 * @author Nividica
 */
@Mod(modid = ModGlobals.MOD_ID, name = ModGlobals.MOD_NAME, version = ModGlobals.MOD_VERSION, dependencies = ModGlobals.MOD_DEPENDENCIES)
@Mod.EventBusSubscriber
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
        ThEApi.instance(); // Make sure to init the api
        MinecraftForge.EVENT_BUS.register(this);

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
        if (ForgeUtil.isClient()) {
            ThEItemColors.registerItemColors();
        }

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

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandAddVis());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        // TODO: Temporary alpha warning
        TextComponentString s1 = new TextComponentString(
                "Thaumic Energistics is currently in alpha, things may be broken, not implemented or cause lost items and world corruption.\n" +
                        "Post issues to GitHub"
        );
        s1.getStyle().setColor(TextFormatting.RED);
        TextComponentString link = new TextComponentString("https://github.com/Nividica/ThaumicEnergistics");
        link.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Nividica/ThaumicEnergistics")).setColor(TextFormatting.GOLD);

        event.player.sendMessage(s1.appendSibling(link));
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(ModGlobals.MOD_ID))
            ConfigManager.sync(ModGlobals.MOD_ID, Config.Type.INSTANCE);
    }
}
