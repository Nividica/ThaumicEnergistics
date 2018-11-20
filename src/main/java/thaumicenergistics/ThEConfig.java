package thaumicenergistics;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.config.Config;

import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.init.ModGlobals;

import static net.minecraftforge.common.config.Config.Comment;
import static net.minecraftforge.common.config.Config.Name;

/**
 * @author BrockWS
 */
@SuppressWarnings("ALL")
@Config(modid = ModGlobals.MOD_ID)
public class ThEConfig implements IThEConfig {

    @Name("Essentia Container Capacity")
    @Comment("Specifies how much a item that holds essentia can hold\nFor filling with Essentia Terminal\nBest to set it to how much the item can actually store")
    public static Map<String, Integer> essentiaContainerCapacity = new HashMap<>();


    @Name("Tick Rates")
    public static TickRates tickRates = new TickRates();

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
}
