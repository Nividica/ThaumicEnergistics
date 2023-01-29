package thaumicenergistics.api.gui;

import javax.annotation.Nonnull;

import thaumicenergistics.api.grid.ICraftingIssuerHost;

/**
 * Container that can issue AE2 crafting jobs.
 *
 * @author Nividica
 *
 */
public interface ICraftingIssuerContainer {

    /**
     * Gets the crafting issuer host terminal.
     *
     * @return
     */
    @Nonnull
    ICraftingIssuerHost getCraftingHost();
}
