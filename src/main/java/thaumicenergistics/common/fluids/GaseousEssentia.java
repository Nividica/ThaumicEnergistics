package thaumicenergistics.common.fluids;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.IThEEssentiaGas;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.utils.ThELog;

/**
 * A fluid which represents the gas form of an essentia.
 *
 * @author Nividica
 *
 */
public class GaseousEssentia extends Fluid implements IThEEssentiaGas {
    /**
     * List of all created gasses.
     */
    public static final LinkedHashMap<Aspect, GaseousEssentia> gasList = new LinkedHashMap<Aspect, GaseousEssentia>();

    /**
     * The aspect the gas is based off of.
     */
    private Aspect associatedAspect;

    /**
     * Creates the gas.
     *
     * @param gasName
     * Name of the gas displayed to the user
     * @param aspect
     * The aspect the gas is based off of.
     */
    private GaseousEssentia(final String gasName, final Aspect aspect) {
        // Pass to super
        super(gasName);

        // Set the aspect
        this.associatedAspect = aspect;

        // Gas slightly glows
        this.setLuminosity(7);

        // Negative density, it floats away!
        this.setDensity(-4);

        // Flow speed, 3x slower than water
        this.setViscosity(3000);

        // This is a gas, adjusts the render pass.
        this.setGaseous(true);
    }

    /**
     * Creates a gas based on the specified aspect
     *
     * @param aspect
     */
    private static void create(final Aspect aspect) {
        // Ensure this has not already been register
        if (gasList.containsKey(aspect)) {
            // Return the existing fluid
            return;
        }

        // Create the name
        String gasName = "gaseous" + aspect.getTag() + "essentia";

        // Create the fluid
        GaseousEssentia newGas = new GaseousEssentia(gasName, aspect);

        // Register the fluid
        if (FluidRegistry.registerFluid(newGas)) {
            // Add to the list
            gasList.put(aspect, newGas);

            // Log info
            // ThELog.info( "Created fluid for aspect %s.", aspect.getTag() );
        } else {
            // Log a warning
            ThELog.warning("Unable to register '%s' as fluid.", aspect.getTag());
        }
    }

    /**
     * Gets the gas form of the specified aspect
     *
     * @param aspect
     * @return
     */
    public static GaseousEssentia getGasFromAspect(final Aspect aspect) {
        return GaseousEssentia.gasList.get(aspect);
    }

    /**
     * Called from load to register all gas types with the game.
     */
    public static void registerGases() {
        // Create a gas for each essentia type
        for (Entry<String, Aspect> aspectType : Aspect.aspects.entrySet()) {
            // Get the aspect
            Aspect aspect = aspectType.getValue();

            // Create and register
            GaseousEssentia.create(aspect);
        }
    }

    /**
     * Get the aspect this gas is based off of.
     *
     * @return
     */
    @Override
    public Aspect getAspect() {
        return this.associatedAspect;
    }

    /**
     * Gets the color of the gas.
     */
    @Override
    public int getColor() {
        if (this.associatedAspect != null) {
            return this.associatedAspect.getColor();
        }

        return super.getColor();
    }

    @Override
    public IIcon getFlowingIcon() {
        return BlockTextureManager.GASEOUS_ESSENTIA.getTexture();
    }

    @Deprecated
    @Override
    public String getLocalizedName() {
        return this.getLocalizedName(null);
    }

    /**
     * Gets the localized version of the gasses name.
     */
    @Override
    public String getLocalizedName(final FluidStack stack) {
        return this.associatedAspect.getName() + " " + ThEStrings.Fluid_GaseousEssentia.getLocalized();
    }

    @Override
    public IIcon getStillIcon() {
        return BlockTextureManager.GASEOUS_ESSENTIA.getTexture();
    }
}
