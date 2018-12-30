package thaumicenergistics.integration;

import net.minecraftforge.fml.common.Loader;

/**
 * Implemented by Thaumic Energistics Integrations
 *
 * @author BrockWS
 */
public interface IThEIntegration {

    void preInit();

    void init();

    void postInit();

    String getModID();

    default boolean isLoaded() {
        return Loader.isModLoaded(this.getModID());
    }

    default boolean isRequired() {
        return false;
    }
}
