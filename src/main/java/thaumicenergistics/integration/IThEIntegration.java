package thaumicenergistics.integration;

import net.minecraftforge.fml.common.Loader;

/**
 * Implemented by Thaumic Energistics Integrations
 *
 * @author BrockWS
 */
public interface IThEIntegration {

    default void preInit(){}

    default void init(){}

    default void postInit(){}

    String getModID();

    default boolean isLoaded() {
        return Loader.isModLoaded(this.getModID());
    }

    default boolean isRequired() {
        return false;
    }
}
