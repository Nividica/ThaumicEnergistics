package thaumicenergistics.api.grid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.common.util.ForgeDirection;

import thaumcraft.api.aspects.Aspect;
import appeng.api.networking.IGrid;
import appeng.api.util.DimensionalCoord;

/**
 * Digital Vis source.
 *
 * @author Nividica
 *
 */
public interface IDigiVisSource {

    /**
     * Drains digivis from this source.
     *
     * @param digiVisAspect
     * @param amount
     * @return The amount drained.
     */
    int consumeVis(@Nonnull Aspect digiVisAspect, int amount);

    /**
     * Get's the AE grid the source is attached to.
     *
     * @return
     */
    @Nullable
    IGrid getGrid();

    /**
     * Returns the location of the source.
     *
     * @return
     */
    @Nonnull
    DimensionalCoord getLocation();

    /**
     * Gets the side of cable the source part is attached to. If this returns UNKNOWN, it is assumed the source is a
     * whole block.
     *
     * @return
     */
    @Nonnull
    ForgeDirection getSide();

    /**
     * Get's the unique identifier for this source.
     *
     * @return
     */
    long getUID();

    /**
     * Is the source active?
     *
     * @return
     */
    boolean isActive();
}
