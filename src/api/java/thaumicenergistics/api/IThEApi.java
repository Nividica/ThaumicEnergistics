package thaumicenergistics.api;

/**
 * The Thaumic Energistics API
 *
 * @author BrockWS
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IThEApi {

    /**
     * Contains all items in Thaumic Energistics
     *
     * @return Class which contains item definitions
     */
    IThEItems items();

    /**
     * Contains all blocks/tiles in Thaumic Energistics
     *
     * @return Class which contains block/tile definitions
     */
    IThEBlocks blocks();

    /**
     * Contains all config options in Thaumic Energistics
     *
     * @return Class which contains config options
     */
    IThEConfig config();

    /**
     * Contains all language keys in Thaumic Energistics
     *
     * @return Class which contains language keys
     */
    IThELang lang();
}
