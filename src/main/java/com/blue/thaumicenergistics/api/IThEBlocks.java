package com.blue.thaumicenergistics.api;

import javax.annotation.Nonnull;



public abstract class IThEBlocks
{
    /**
     * Arcane Assembler
     */
    @Nonnull
    public IThEItemDescription ArcaneAssembler;

    /**
     * Distilation Pattern Encoder.
     */
    @Nonnull
    public IThEItemDescription DistillationPatternEncoder;

    /**
     * Essentia Cell Workbench.
     */
    @Nonnull
    public IThEItemDescription EssentiaCellWorkbench;

    /**
     * Essentia Provider.
     */
    @Nonnull
    public IThEItemDescription EssentiaProvider;

    /**
     * Essentia Vibration Chamber.
     */
    @Nonnull
    public IThEItemDescription EssentiaVibrationChamber;

    /**
     * Infusion Provider.
     */
    @Nonnull
    public IThEItemDescription InfusionProvider;

    /**
     * Iron Gearbox.
     */
    @Nonnull
    public IThEItemDescription IronGearbox;

    /**
     * Knowledge Inscriber.
     */
    @Nonnull
    public IThEItemDescription KnowledgeInscriber;

    /**
     * Thaumium Gearbox
     */
    @Nonnull
    public IThEItemDescription ThaumiumGearbox;
}
