package thaumicenergistics.common.integration.tc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.*;
import thaumicenergistics.api.IThETransportPermissions;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.storage.AspectStack;
import thaumicenergistics.common.tiles.TileEssentiaVibrationChamber;
import thaumicenergistics.common.tiles.abstraction.TileEVCBase;
import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEFluidStack;

/**
 * Helper class for working with Thaumcraft TileEntity essentia containers.
 *
 * @author Nividica
 *
 */
public final class EssentiaTileContainerHelper {

    /**
     * Singleton
     */
    public static final EssentiaTileContainerHelper INSTANCE = new EssentiaTileContainerHelper();

    /**
     * Cache the permission class
     */
    public final IThETransportPermissions perms = ThEApi.instance().transportPermissions();

    /**
     * Extracts essentia from a container based on the specified fluid stack type and amount.
     *
     * @param container
     * @param request
     * @param mode
     * @return
     */
    public FluidStack extractFromContainer(final IAspectContainer container, final FluidStack request,
            final Actionable mode) {
        // Ensure there is a request
        if ((request == null) || (request.getFluid() == null) || (request.amount == 0)) {
            // No request
            return null;
        }

        // Get the fluid
        Fluid fluid = request.getFluid();

        // Ensure it is a gas
        if (!(fluid instanceof GaseousEssentia)) {
            // Not a gas
            return null;
        }

        // Get the gas's aspect
        Aspect gasAspect = ((GaseousEssentia) fluid).getAspect();

        // Get the amount to extract
        long amountToDrain_EU = EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount(request.amount);

        // Extract
        long extractedAmount_EU = this.extractFromContainer(container, (int) amountToDrain_EU, gasAspect, mode);

        // Was any extracted?
        if (extractedAmount_EU <= 0) {
            // None extracted
            return null;
        }

        // Return the extracted amount
        return new FluidStack(
                fluid,
                (int) EssentiaConversionHelper.INSTANCE.convertEssentiaAmountToFluidAmount(extractedAmount_EU));
    }

    /**
     * Extracts the specified aspect and amount from the container.
     *
     * @param container
     * @param amountToDrain
     * @param aspectToDrain
     * @param mode
     * @return The amount extracted.
     */
    public long extractFromContainer(final IAspectContainer container, int amountToDrain, final Aspect aspectToDrain,
            final Actionable mode) {

        // Is the request empty?
        if (amountToDrain == 0) {
            // Empty request
            return 0;
        }

        // Is the container whitelisted?
        if (!this.perms.canExtractFromAspectContainerTile(container)) {
            // Not whitelisted
            return 0;
        }

        // Get how much is in the container
        int containerAmount = container.getAspects().getAmount(aspectToDrain);

        // Is the container empty?
        if (containerAmount == 0) {
            // Empty container, or does not contain the wanted aspect
            return 0;
        }
        // Is the drain for more than is in the container?
        if (amountToDrain > containerAmount) {
            amountToDrain = containerAmount;
        }

        // Are we really draining, or just simulating?
        if (mode == Actionable.MODULATE) {
            if (!container.takeFromContainer(aspectToDrain, amountToDrain)) return 0;
        }

        // Return how much was drained
        return amountToDrain;
    }

    /**
     * Returns the aspect of the essentia in the container.
     *
     * @param container
     * @return
     */
    public Aspect getAspectInContainer(final IAspectContainer container) {
        // Get the aspect list from the container
        IAspectStack containerStack = this.getAspectStackFromContainer(container);

        // Did we get a stack?
        if (containerStack == null) {
            return null;
        }

        return containerStack.getAspect();
    }

    public IAspectStack getAspectStackFromContainer(final IAspectContainer container) {
        // Ensure we have a container
        if (container == null) {
            return null;
        }

        // Get the list of aspects in the container
        AspectList aspectList = container.getAspects();

        if (aspectList == null) {
            return null;
        }

        // Create the stack
        IAspectStack aspectStack = new AspectStack();

        // Set the aspect
        aspectStack.setAspect(aspectList.getAspectsSortedAmount()[0]);

        if (!aspectStack.hasAspect()) {
            return null;
        }

        // Set the amount
        aspectStack.setStackSize(aspectList.getAmount(aspectStack.getAspect()));

        return aspectStack;
    }

    /**
     * Gets the list of aspects in the container.
     *
     * @param container
     * @return
     */
    public List<IAspectStack> getAspectStacksFromContainer(final IAspectContainer container) {
        List<IAspectStack> stacks = new ArrayList<IAspectStack>();

        // Ensure we have a container
        if (container == null) {
            return stacks;
        }

        // Get the list of aspects in the container
        AspectList aspectList = container.getAspects();

        if (aspectList == null) {
            return stacks;
        }

        // Populate the list
        for (Entry<Aspect, Integer> essentia : aspectList.aspects.entrySet()) {
            if ((essentia != null) && (essentia.getValue() != 0)) {
                stacks.add(new AspectStack(essentia.getKey(), essentia.getValue()));
            }
        }

        return stacks;
    }

    /**
     * Returns the capacity of the specified container.
     *
     * @param container
     * @return Capacity or -1 if unknown capacity.
     */
    public int getContainerCapacity(final IAspectContainer container) {
        return this.perms.getAspectContainerTileCapacity(container);
    }

    public int getContainerStoredAmount(final IAspectContainer container) {
        int stored = 0;

        // Get the essentia list
        for (IAspectStack essentia : this.getAspectStacksFromContainer(container)) {
            if (essentia != null) {
                stored += (int) essentia.getStackSize();
            }
        }

        return stored;
    }

    /**
     * Attempts to inject essentia into the container. Returns the amount that was injected.
     *
     * @param container
     * @param amountToFill
     * @param aspectToFill
     * @param mode
     * @return
     */
    public long injectEssentiaIntoContainer(final IAspectContainer container, int amountToFill,
            final Aspect aspectToFill, final Actionable mode) {
        // Is the container whitelisted?
        if (!this.perms.canInjectToAspectContainerTile(container)) {
            // Not whitelisted
            return 0;
        }

        // Get the aspect in the container
        IAspectStack storedEssentia = this.getAspectStackFromContainer(container);

        // Match types on jars
        if ((storedEssentia != null) && (container instanceof TileJarFillable)) {
            // Do the aspects match?
            if (aspectToFill != storedEssentia.getAspect()) {
                // Aspects do not match;
                return 0;
            }
        } else if (!(container.doesContainerAccept(aspectToFill))) {
            // Container will not accept this aspect
            return 0;
        }

        // Get how much the container can hold
        int containerCurrentCapacity = this.getContainerCapacity(container) - this.getContainerStoredAmount(container);

        // Is there more to fill than the container will hold?
        if (amountToFill > containerCurrentCapacity) {
            amountToFill = containerCurrentCapacity;
        }

        // Are we really filling, or simulating?
        if (mode == Actionable.MODULATE) {
            // Attempt to inject the full amount
            int remaining = container.addToContainer(aspectToFill, amountToFill);

            // Subtract any that could not be injected
            amountToFill -= remaining;
        }

        return amountToFill;
    }

    /**
     * Attempts to inject the fluid into the container. Returns the amount that was injected in milibuckets.
     *
     * @param container
     * @param fluidStack
     * @param mode
     * @return
     */
    public long injectFluidIntoContainer(final IAspectContainer container, final IAEFluidStack fluidStack,
            final Actionable mode) {
        // Do we have an input?
        if (fluidStack == null) {
            // No input
            return 0;
        }

        // Is the container whitelisted?
        if (!this.perms.canInjectToAspectContainerTile(container)) {
            // Not whitelisted
            return 0;
        }

        // Get the fluid.
        Fluid fluid = fluidStack.getFluid();

        // Ensure it is a gas
        if (!(fluid instanceof GaseousEssentia)) {
            // Not essentia gas
            return 0;
        }

        // Get the aspect of the gas
        Aspect gasAspect = ((GaseousEssentia) fluid).getAspect();

        // Get the amount to fill
        long amountToFill = EssentiaConversionHelper.INSTANCE
                .convertFluidAmountToEssentiaAmount(fluidStack.getStackSize());

        // Inject
        long injectedAmount_EU = this.injectEssentiaIntoContainer(container, (int) amountToFill, gasAspect, mode);

        return EssentiaConversionHelper.INSTANCE.convertEssentiaAmountToFluidAmount(injectedAmount_EU);
    }

    /**
     * Setup the standard white list
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerDefaultContainers() {
        // Alembic
        this.perms.addAspectContainerTileToExtractPermissions(TileAlembic.class, 32);

        // Centrifuge
        this.perms.addAspectContainerTileToExtractPermissions(TileCentrifuge.class, 0);

        // Jars
        this.perms.addAspectContainerTileToBothPermissions(TileJarFillable.class, 64);
        this.perms.addAspectContainerTileToBothPermissions(TileJarFillableVoid.class, 64);

        // Essentia buffer
        this.perms.addAspectContainerTileToExtractPermissions(TileTubeBuffer.class, 8);
        this.perms.addAspectContainerTileToInjectPermissions(TileTubeBuffer.class, 8);

        // Essentia reservoir
        this.perms.addAspectContainerTileToBothPermissions(TileEssentiaReservoir.class, 256);

        // Advanced alchemical furnace
        this.perms.addAspectContainerTileToExtractPermissions(TileAlchemyFurnaceAdvancedNozzle.class, 0);

        // Essentia vibration chamber
        this.perms.addAspectContainerTileToInjectPermissions(
                TileEssentiaVibrationChamber.class,
                TileEVCBase.MAX_ESSENTIA_STORED);

        try {
            Class c = Class.forName("flaxbeard.thaumicexploration.tile.TileEntityTrashJar");
            this.perms.addAspectContainerTileToInjectPermissions(c, 64);
            c = Class.forName("flaxbeard.thaumicexploration.tile.TileEntityBoundJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 64);
        } catch (Exception ignored) {}
        try {
            Class c = Class.forName("makeo.gadomancy.common.blocks.tiles.TileRemoteJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 64);
            c = Class.forName("makeo.gadomancy.common.blocks.tiles.TileStickyJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 64);
        } catch (Exception ignored) {}
        try {
            // Essentia condenser does not directly implement IAspectContainer, so this may fail if used w/o Automagy
            Class c = Class.forName("makeo.gadomancy.common.blocks.tiles.TileEssentiaCompressor");
            this.perms.addAspectContainerTileToBothPermissions(c, 64);
        } catch (Exception ignored) {}
        try {
            // I hope some day Kekztech jars will have a namespace
            Class c = Class.forName("common.tileentities.TE_IchorJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 4096);
            c = Class.forName("common.tileentities.TE_IchorVoidJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 4096);
            c = Class.forName("common.tileentities.TE_ThaumiumReinforcedJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 256);
            c = Class.forName("common.tileentities.TE_ThaumiumReinforcedVoidJar");
            this.perms.addAspectContainerTileToBothPermissions(c, 256);
        } catch (Exception ignored) {}
        try {
            Class c = Class.forName("tuhljin.automagy.tiles.TileEntityJarCreative");
            this.perms.addAspectContainerTileToBothPermissions(c, 1 << 31);
        } catch (Exception ignored) {}
    }
}
