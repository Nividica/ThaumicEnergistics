package thaumicenergistics.api;

import java.util.Map;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.TerminalStyle;

import thaumicenergistics.api.config.PrefixSetting;

/**
 * @author BrockWS
 * @author Alex811
 */
public interface IThEConfig {

    Map<String, Integer> essentiaContainerCapacity();

    int tickTimeEssentiaImportBusMin();

    int tickTimeEssentiaImportBusMax();

    int tickTimeEssentiaExportBusMin();

    int tickTimeEssentiaExportBusMax();

    int tickTimeEssentiaStorageBusMin();

    int tickTimeEssentiaStorageBusMax();

    int tickTimeArcaneAssemblerMin();

    int tickTimeArcaneAssemblerMax();

    TerminalStyle terminalStyle();

    SearchBoxMode searchBoxMode();

    String modSearchPrefix();

    PrefixSetting modSearchSetting();

    String aspectSearchPrefix();

    PrefixSetting aspectSearchSetting();

    double arcaneAssemblerParticleMultiplier();
}
