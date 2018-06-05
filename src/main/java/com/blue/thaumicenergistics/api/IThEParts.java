package com.blue.thaumicenergistics.api;

import javax.annotation.Nonnull;



public abstract class IThEParts
{
    /**
     * Arcane crafting terminal.
     */
    @Nonnull
    public IThEItemDescription ArcaneCrafting_Terminal;

    /**
     * Essentia export bus.
     */
    @Nonnull
    public IThEItemDescription Essentia_ExportBus;

    /**
     * Essentia import bus.
     */
    @Nonnull
    public IThEItemDescription Essentia_ImportBus;

    /**
     * Essentia level emitter.
     */
    @Nonnull
    public IThEItemDescription Essentia_LevelEmitter;

    /**
     * Essentia storage bus.
     */
    @Nonnull
    public IThEItemDescription Essentia_StorageBus;

    /**
     * Essentia terminal.
     */
    @Nonnull
    public IThEItemDescription Essentia_Terminal;

    /**
     * Vis relay interface.
     */
    @Nonnull
    public IThEItemDescription VisRelay_Interface;

    /**
     * Essentia storage monitor.
     */
    @Nonnull
    public IThEItemDescription Essentia_StorageMonitor;

    /**
     * Essentia conversion monitor.
     */
    @Nonnull
    public IThEItemDescription Essentia_ConversionMonitor;
}
