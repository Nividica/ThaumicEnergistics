package thaumicenergistics.api;

import javax.annotation.Nonnull;

/**
 * Thaumic Energistics items.
 *
 * @author Nividica
 *
 */
public abstract class IThEItems {

    /**
     * Coalescence Core.
     */
    @Nonnull
    public IThEItemDescription CoalescenceCore;

    /**
     * Diffusion Core.
     */
    @Nonnull
    public IThEItemDescription DiffusionCore;

    /**
     * 16 KiloByte essentia storage cell.
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_16k;

    /**
     * 1 KiloByte essentia storage cell.
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_1k;

    /**
     * 4 KiloByte essentia storage cell.
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_4k;

    /**
     * 64 KiloByte essentia storage cell.
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_64k;

    /**
     * Creative essentia storage cell.
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_Creative;

    /**
     * Essentia storage cell housing.
     */
    @Nonnull
    public IThEItemDescription EssentiaCell_Casing;

    /**
     * 16 KiloByte essentia storage cell component.
     */
    @Nonnull
    public IThEItemDescription EssentiaStorageComponent_16k;

    /**
     * 1 KiloByte essentia storage cell component.
     */
    @Nonnull
    public IThEItemDescription EssentiaStorageComponent_1k;

    /**
     * 4 KiloByte essentia storage cell component.
     */
    @Nonnull
    public IThEItemDescription EssentiaStorageComponent_4k;

    /**
     * 64 KiloByte essentia storage cell component.
     */
    @Nonnull
    public IThEItemDescription EssentiaStorageComponent_64k;

    /**
     * Iron gear.
     */
    @Nonnull
    public IThEItemDescription IronGear;

    /**
     * Wireless essentia terminal
     */
    @Nonnull
    public IThEItemDescription WirelessEssentiaTerminal;

    /**
     * Knowledge core.
     */
    @Nonnull
    public IThEItemDescription KnowledgeCore;

    /**
     * AE Wrench wand focus.
     */
    @Nonnull
    public IThEItemDescription WandFocusAEWrench;

    /**
     * Golem wireless backpack.
     */
    @Nonnull
    public IThEItemDescription GolemWifiBackpack;

    /**
     * Cell microscope
     */
    @Nonnull
    public IThEItemDescription CellMicroscope;
}
