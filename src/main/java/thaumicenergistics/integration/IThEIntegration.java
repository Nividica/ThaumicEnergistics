package thaumicenergistics.integration;

/**
 * Implemented by Thaumic Energistics Integrations
 *
 * @author BrockWS
 */
public interface IThEIntegration {

    default void preInit(){}

    default void init(){}

    default void postInit(){}

    default String getModID(){
        return ThEIntegrationLoader.getModId(this);
    }
}
