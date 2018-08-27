package thaumicenergistics.api;

import appeng.api.definitions.IItemDefinition;

/**
 * Contains functions that return the Item Definition for each item in Thaumic Energistics
 *
 * @author BrockWS
 * @since 1.0.0
 * @version 1.0.0
 */
public interface IThEItems {

    IItemDefinition essentiaCell1k();

    IItemDefinition essentiaCell4k();

    IItemDefinition essentiaCell16k();

    IItemDefinition essentiaCell64k();

    IItemDefinition essentiaCellCreative();

    IItemDefinition essentiaImportBus();

    IItemDefinition essentiaExportBus();

    IItemDefinition essentiaStorageBus();

    IItemDefinition essentiaTerminal();

    IItemDefinition dummyAspect();
}
