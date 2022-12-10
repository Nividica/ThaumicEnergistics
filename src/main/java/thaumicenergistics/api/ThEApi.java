package thaumicenergistics.api;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Method;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Thaumic Energistics API
 *
 * @author Nividica
 *
 */
public abstract class ThEApi {
    protected static ThEApi api = null;

    /**
     * Gets the Thaumic Energistics API.
     * Note: Only available after the PREINIT event.
     */
    @Nullable
    public static ThEApi instance() {
        // Have we already retrieved the api?
        if (ThEApi.api == null) {
            try {
                // Attempt to locate the API implementation
                Class clazz = Class.forName("thaumicenergistics.implementaion.ThEAPIImplementation");

                // Get the instance method
                Method instanceAccessor = clazz.getMethod("instance");

                // Attempt to get the API instance
                ThEApi.api = (ThEApi) instanceAccessor.invoke(null);
            } catch (Exception e) {
                // Unable to locate the API, return null
                return null;
            }
        }

        return ThEApi.api;
    }

    /**
     * Blocks
     */
    @Nonnull
    public abstract IThEBlocks blocks();

    /**
     * Configuration
     */
    @Nonnull
    public abstract IThEConfig config();

    /**
     * Essentia Gasses
     */
    @Nonnull
    public abstract ImmutableList<List<IThEEssentiaGas>> essentiaGases();

    /**
     * Gets the ThE interaction manager
     *
     * @return
     */
    @Nonnull
    public abstract IThEInteractionHelper interact();

    /**
     * Items
     */
    @Nonnull
    public abstract IThEItems items();

    /**
     * Cable Parts
     */
    @Nonnull
    public abstract IThEParts parts();

    /**
     * Transport Permissions.
     */
    @Nonnull
    public abstract IThETransportPermissions transportPermissions();
}
