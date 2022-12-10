package thaumicenergistics.implementaion;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.common.LoaderState;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.*;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.utils.ThELog;

/**
 * Implements {@link ThEApi}.
 *
 * @author Nividica
 *
 */
public class ThEAPIImplementation extends ThEApi {
    /**
     * Create the API instance.
     */
    private static ThEAPIImplementation INSTANCE = null;

    private final ThEBlocks blocks;
    private final ThEItems items;
    private final ThEParts parts;
    private final List<IThEEssentiaGas> essentiaGases;
    private final ThETransportPermissions transportPermissions;

    private final ThEInteractionHelper interactionHelper;

    /**
     * Private constructor
     */
    private ThEAPIImplementation() {
        this.blocks = new ThEBlocks();
        this.items = new ThEItems();
        this.parts = new ThEParts();
        this.essentiaGases = new ArrayList<IThEEssentiaGas>();
        this.transportPermissions = new ThETransportPermissions();
        this.interactionHelper = new ThEInteractionHelper();
    }

    /**
     * Checks if ThE has finished the preinit phase.
     *
     * @return
     */
    protected static boolean hasFinishedPreInit() {
        // Ensure that ThE has finished the PreInit phase
        if (ThaumicEnergistics.getLoaderState() == LoaderState.NOINIT) {
            // Invalid state, API can not yet load.
            ThELog.warning("API is not available until ThE finishes the PreInit phase.");
            return false;
        }

        return true;
    }

    /**
     * Gets the Thaumic Energistics API.
     * Note: Only available after the PREINIT event.
     */
    public static ThEAPIImplementation instance() {
        // Has the singleton been created?
        if (ThEAPIImplementation.INSTANCE == null) {
            // Ensure that preinit has finished.
            if (!ThEAPIImplementation.hasFinishedPreInit()) {
                return null;
            }

            // Create the singleton.
            ThEAPIImplementation.INSTANCE = new ThEAPIImplementation();
        }

        return ThEAPIImplementation.INSTANCE;
    }

    @Override
    public IThEBlocks blocks() {
        return this.blocks;
    }

    @Override
    public IThEConfig config() {
        return ThaumicEnergistics.config;
    }

    @Override
    public ImmutableList<List<IThEEssentiaGas>> essentiaGases() {
        // Do we need to update?
        if (this.essentiaGases.size() != GaseousEssentia.gasList.size()) {
            // Clear the list
            this.essentiaGases.clear();

            // Get the iterator
            Iterator<Entry<Aspect, GaseousEssentia>> iterator =
                    GaseousEssentia.gasList.entrySet().iterator();

            // Add all gasses
            while (iterator.hasNext()) {
                this.essentiaGases.add(iterator.next().getValue());
            }
        }

        return ImmutableList.of(this.essentiaGases);
    }

    @Override
    public IThEInteractionHelper interact() {
        return this.interactionHelper;
    }

    @Override
    public IThEItems items() {
        return this.items;
    }

    @Override
    public IThEParts parts() {
        return this.parts;
    }

    @Override
    public IThETransportPermissions transportPermissions() {
        return this.transportPermissions;
    }
}
