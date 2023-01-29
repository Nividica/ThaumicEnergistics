package thaumicenergistics.api.tiles;

import javax.annotation.Nonnull;

import net.minecraftforge.common.util.ForgeDirection;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import appeng.api.config.Actionable;

/**
 * An essentia transport that supports {@link Actionable#SIMULATE} and {@link Actionable#MODULATE}.
 *
 * @author Nividica
 *
 */
public interface IEssentiaTransportWithSimulate extends IEssentiaTransport {

    /**
     * Adds essentia to the transport.
     *
     * @param aspect
     * @param amount
     * @param side
     * @param mode
     * @return Amount that <strong>was</strong> injected.
     */
    int addEssentia(@Nonnull Aspect aspect, int amount, @Nonnull ForgeDirection side, @Nonnull Actionable mode);
}
