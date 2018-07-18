package thaumicenergistics.part;

import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.item.part.ItemEssentiaExportBus;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;

/**
 * @author BrockWS
 */
public class PartEssentiaExportBus extends PartSharedEssentiaBus {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_export_bus_base")
    };

    private static IPartModel MODEL_BASE = new ThEPartModel(MODELS[0]);

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
        return false;
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        return TickRateModulation.IDLE;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return MODEL_BASE;
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d vec3d) {
        GuiHandler.openGUI(ModGUIs.ESSENTIA_EXPORT_BUS, player, this.hostTile.getPos(), this.side);
        return true;
    }
}
