package thaumicenergistics.part;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.item.part.ItemEssentiaExportBus;
import thaumicenergistics.util.AEUtil;

/**
 * @author BrockWS
 */
public class PartEssentiaExportBus extends PartSharedEssentiaBus {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_export_bus/base"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_export_bus/on"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_export_bus/off"),
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_export_bus/has_channel")
    };

    private static IPartModel MODEL_ON = new ThEPartModel(MODELS[0], MODELS[1]);
    private static IPartModel MODEL_OFF = new ThEPartModel(MODELS[0], MODELS[2]);
    private static IPartModel MODEL_HAS_CHANNEL = new ThEPartModel(MODELS[0], MODELS[3]);

    public PartEssentiaExportBus(ItemEssentiaExportBus item) {
        super(item);
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(5, 40, false, false);
    }

    @Override
    public boolean canWork() {
        return this.getConnectedTE() != null && this.config.hasAspects(); // We only want to run if there is something in the filter
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        if (!this.canWork())
            return TickRateModulation.IDLE;
        if (this.getConnectedTE() instanceof IAspectContainer) {
            IAspectContainer container = (IAspectContainer) this.getConnectedTE();

            IStorageGrid storageGrid = this.getGridNode().getGrid().getCache(IStorageGrid.class);
            IMEMonitor<IAEEssentiaStack> storage = storageGrid.getInventory(this.getChannel());

            for (Aspect aspect : this.config) { // Gather a list of aspects that can be put into the container
                if (aspect == null)
                    continue;
                if (container.doesContainerAccept(aspect) && AEUtil.doesStorageContain(storage, aspect)) { // Can container hold the aspect + does ae2 hold the aspect
                    // Simulate extract from ae2
                    IAEEssentiaStack extracted = storage.extractItems(AEUtil.getAEStackFromAspect(aspect, this.calculateAmountToSend()), Actionable.SIMULATE, this.source);
                    // Try add to container, since we can't simulate it
                    int notAdded = container.addToContainer(extracted.getAspect(), (int) extracted.getStackSize());
                    // Couldn't contain it all
                    extracted.decStackSize(notAdded);
                    // Only remove from system the amount the container accepted
                    storage.extractItems(extracted, Actionable.MODULATE, this.source);
                    return TickRateModulation.FASTER; // Only do one every tick
                }
            }

            return TickRateModulation.SLOWER;
        }
        return TickRateModulation.IDLE;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isPowered())
            if (this.isActive())
                return MODEL_HAS_CHANNEL;
            else
                return MODEL_ON;
        return MODEL_OFF;
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d vec3d) {
        GuiHandler.openGUI(ModGUIs.ESSENTIA_EXPORT_BUS, player, this.hostTile.getPos(), this.side);
        return true;
    }
}
