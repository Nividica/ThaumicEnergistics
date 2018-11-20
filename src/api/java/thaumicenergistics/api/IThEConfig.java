package thaumicenergistics.api;

import java.util.Map;

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
}
