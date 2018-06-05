package com.blue.thaumicenergistics.api;

import javax.annotation.Nonnull;



public abstract class IThEItems
{
    /**
     * Coalescence Core
     */
    @Nonnull
    public IThEItemDescription CoalescenceCore;

    /**
     * Diffusion Core
     */
    @Nonnull
    public IThEItemDescription DiffusionCore;

    /**
     * 16 Kilobyte essentia storage cell
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_16k;

    /**
     * 1 Kilobyte essentia storage cell
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_1k;

    /**
     * 4 Kilobyte essentia storage cell
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_4k;

    /**
     * 64 Kilobyte essentia storage cell
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_64k;

    /**
     * Creative essentia storage cell
     */
    public IThEItemDescription EssentiaCell_Creative;

    /**
     * Essentia storage cell housing
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_Casing;

    /**
     * 16 Kilobyte essentia storage cell component
     */
    @Nonnull
    public IThEItemDescription EssentiaStorageComponent_16k;

    /**
     * 1 Kilobyte essentia storage cell component
     */
    @Nonnull
    public IThEItemDescription EssentiaStorageComponent_1k;

    /**
     * 4 Kilobyte essentia storage cell component
     */
    public IThEItemDescription EssentiaStorageComponent_4k;

    /**
     * 64 Kilobyte essentia storage cell component
     */
    public IThEItemDescription EssentiaStorageComponent_64k;

    /**
     * Iron gear
     */
    @Nonnull
    public IThEItemDescription IronGear;

    /**
     * Wireless essentia terminal
     */
    @Nonnull
    public IThEItemDescription WirelessEssentiaTerminal;

    /**
     * Knowledge core
     */
    @Nonnull
    public IThEItemDescription KnowledgeCore;

    /**
     * AE Wrench wand focus
     */
    @Nonnull
    public IThEItemDescription WandFocusAEWrench;

    /**
     * Golem wireless backpack
     */
    @Nonnull
    public IThEItemDescription GolemWifiBackpack;
}
