package thaumicenergistics.api;

import java.util.Map;

import thaumicenergistics.api.config.PrefixSetting;

/**
 * @author BrockWS
 */
public interface IThEConfig {
    Map<String, Integer> essentiaContainerCapacity();

    int tickTimeEssentiaImportBusMin();

    int tickTimeEssentiaImportBusMax();

    int tickTimeEssentiaExportBusMin();

    int tickTimeEssentiaExportBusMax();

    int tickTimeEssentiaStorageBusMin();

    int tickTimeEssentiaStorageBusMax();

    String modSearchPrefix();

    PrefixSetting modSearchSetting();

    String aspectSearchPrefix();

    PrefixSetting aspectSearchSetting();
}
