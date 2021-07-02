package thaumicenergistics.part;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.parts.IPartModel;

import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.item.part.ItemEssentiaTerminal;
import thaumicenergistics.util.AEUtil;
import thaumicenergistics.util.ForgeUtil;

/**
 * @author BrockWS
 */
public class PartEssentiaTerminal extends PartSharedTerminal {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_terminal/base"), // 0
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_terminal/on"), // 1
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_terminal/off"), // 2
            new ResourceLocation(ModGlobals.MOD_ID_AE2, "part/display_status_has_channel"), // 3
            new ResourceLocation(ModGlobals.MOD_ID_AE2, "part/display_status_on"), // 4
            new ResourceLocation(ModGlobals.MOD_ID_AE2, "part/display_status_off") // 5
    };

    private static IPartModel MODEL_ON = new ThEPartModel(MODELS[0], MODELS[1], MODELS[4]);
    private static IPartModel MODEL_OFF = new ThEPartModel(MODELS[0], MODELS[2], MODELS[5]);
    private static IPartModel MODEL_HAS_CHANNEL = new ThEPartModel(MODELS[0], MODELS[1], MODELS[3]);

    public PartEssentiaTerminal(ItemEssentiaTerminal itemEssentiaTerminal){
        this(itemEssentiaTerminal, ModGUIs.ESSENTIA_TERMINAL);
    }

    public PartEssentiaTerminal(ItemEssentiaTerminal itemEssentiaTerminal, ModGUIs gui) {
        super(itemEssentiaTerminal, gui);
        this.getConfigManager().registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.getConfigManager().registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        if ((player.isSneaking() && AEUtil.isWrench(player.getHeldItem(hand), player, this.getTile().getPos())))
            return false;

        if (ForgeUtil.isServer())
            GuiHandler.openGUI(ModGUIs.ESSENTIA_TERMINAL, player, this.hostTile.getPos(), this.side);

        this.host.markForUpdate();
        return true;
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
    public ModGUIs getGui() {
        return ModGUIs.ESSENTIA_TERMINAL;
    }
}
