package thaumicenergistics.config;

import java.util.HashMap;
import java.util.Map;

import appeng.api.config.SearchBoxMode;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

import appeng.api.config.TerminalStyle;

import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.config.PrefixSetting;
import thaumicenergistics.init.ModGlobals;

import static net.minecraftforge.common.config.Config.Comment;
import static net.minecraftforge.common.config.Config.Name;

/**
 * TODO: Allow config being changed from api
 *
 * @author BrockWS
 * @author Alex811
 */
@SuppressWarnings("ALL")
@Config(modid = ModGlobals.MOD_ID)
public class ThEConfig implements IThEConfig {

    @Name("Essentia Container Capacity")
    @Comment("Specifies how much a item that holds essentia can hold\nFor filling with Essentia Terminal\nBest to set it to how much the item can actually store")
    public static Map<String, Integer> essentiaContainerCapacity = new HashMap<>();

    @Name("Tick Rates")
    public static TickRates tickRates = new TickRates();

    @Name("Client Config")
    public static Client client = new Client();

    public static class Client {

        @Name("Terminal Style")
        public TerminalStyle terminalStyle = TerminalStyle.TALL;

        @Name("Search Box Mode")
        public SearchBoxMode searchBoxMode = SearchBoxMode.AUTOSEARCH;

        @Name("Mod Search Prefix")
        public String modSearchPrefix = "@";

        @Name("Mod Search Setting")
        public PrefixSetting modSearchSetting = PrefixSetting.REQUIRE_PREFIX;

        @Name("Aspect Search Prefix")
        public String aspectSearchPrefix = "#";

        @Name("Aspect Search Setting")
        public PrefixSetting aspectSearchSetting = PrefixSetting.REQUIRE_PREFIX;

        @Name("Arcane Assembler Particle Multiplier")
        public double arcaneAssemblerParticleMultiplier = 1.0;

        private Client() {

        }
    }

    public static class TickRates {
        @Name("Essentia Import Bus Min")
        public int tickTimeEssentiaImportBusMin = 5;
        @Name("Essentia Import Bus Max")
        public int tickTimeEssentiaImportBusMax = 40;

        @Name("Essentia Export Bus Min")
        public int tickTimeEssentiaExportBusMin = 5;
        @Name("Essentia Export Bus Max")
        public int tickTimeEssentiaExportBusMax = 60;

        @Name("Essentia Storage Bus Min")
        public int tickTimeEssentiaStorageBusMin = 5;
        @Name("Essentia Storage Bus Max")
        public int tickTimeEssentiaStorageBusMax = 60;

        @Name("Arcane Assembler Min")
        public int tickTimeArcaneAssemblerMin = 2;
        @Name("Arcane Assembler Max")
        public int tickTimeArcaneAssemblerMax = 40;

        private TickRates() {

        }
    }

    static {
        essentiaContainerCapacity.put("thaumcraft:phial", 10);
    }

    public ThEConfig() {

    }

    @Override
    public Map<String, Integer> essentiaContainerCapacity() {
        return new HashMap<>(this.essentiaContainerCapacity);
    }

    @Override
    public int tickTimeEssentiaImportBusMin() {
        return tickRates.tickTimeEssentiaImportBusMin;
    }

    @Override
    public int tickTimeEssentiaImportBusMax() {
        return tickRates.tickTimeEssentiaImportBusMax;
    }

    @Override
    public int tickTimeEssentiaExportBusMin() {
        return tickRates.tickTimeEssentiaExportBusMin;
    }

    @Override
    public int tickTimeEssentiaExportBusMax() {
        return tickRates.tickTimeEssentiaExportBusMax;
    }

    @Override
    public int tickTimeEssentiaStorageBusMin() {
        return tickRates.tickTimeEssentiaStorageBusMin;
    }

    @Override
    public int tickTimeEssentiaStorageBusMax() {
        return tickRates.tickTimeEssentiaStorageBusMax;
    }

    @Override
    public int tickTimeArcaneAssemblerMin(){
        return tickRates.tickTimeArcaneAssemblerMin;
    }

    @Override
    public int tickTimeArcaneAssemblerMax(){
        return tickRates.tickTimeArcaneAssemblerMax;
    }

    @Override
    public TerminalStyle terminalStyle() {
        return client.terminalStyle;
    }

    @Override
    public SearchBoxMode searchBoxMode() {
        return client.searchBoxMode;
    }

    @Override
    public String modSearchPrefix() {
        return client.modSearchPrefix;
    }

    @Override
    public PrefixSetting modSearchSetting() {
        return client.modSearchSetting;
    }

    @Override
    public String aspectSearchPrefix() {
        return client.aspectSearchPrefix;
    }

    @Override
    public PrefixSetting aspectSearchSetting() {
        return client.aspectSearchSetting;
    }

    @Override
    public double arcaneAssemblerParticleMultiplier() {
        return client.arcaneAssemblerParticleMultiplier;
    }

    public static void save() {
        ConfigManager.sync(ModGlobals.MOD_ID, Config.Type.INSTANCE);
    }
}
