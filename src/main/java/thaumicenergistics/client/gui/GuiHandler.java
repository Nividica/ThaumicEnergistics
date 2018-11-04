package thaumicenergistics.client.gui;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.container.implementations.ContainerCraftConfirm;

import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.client.gui.part.GuiArcaneTerminal;
import thaumicenergistics.client.gui.part.GuiEssentiaExportBus;
import thaumicenergistics.client.gui.part.GuiEssentiaImportBus;
import thaumicenergistics.client.gui.part.GuiEssentiaTerminal;
import thaumicenergistics.container.part.ContainerArcaneTerminal;
import thaumicenergistics.container.part.ContainerEssentiaExportBus;
import thaumicenergistics.container.part.ContainerEssentiaImportBus;
import thaumicenergistics.container.part.ContainerEssentiaTerminal;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.part.PartArcaneTerminal;
import thaumicenergistics.part.PartEssentiaExportBus;
import thaumicenergistics.part.PartEssentiaImportBus;
import thaumicenergistics.part.PartEssentiaTerminal;

/**
 * @author BrockWS
 */
public class GuiHandler implements IGuiHandler {

    public static void openGUI(ModGUIs gui, EntityPlayer player) {
        GuiHandler.openGUI(gui, player, null);
    }

    public static void openGUI(ModGUIs gui, EntityPlayer player, BlockPos pos) {
        GuiHandler.openGUI(gui, player, pos, null);
    }

    public static void openGUI(ModGUIs gui, EntityPlayer player, BlockPos pos, AEPartLocation side) {
        if (gui == null)
            throw new IllegalArgumentException("gui cannot be null!");
        else if (player == null)
            throw new IllegalArgumentException("player cannot be null!");

        if (pos != null)
            player.openGui(ThaumicEnergistics.INSTANCE, GuiHandler.calculateOrdinal(gui, side), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
        else
            player.openGui(ThaumicEnergistics.INSTANCE, GuiHandler.calculateOrdinal(gui, side), player.getEntityWorld(), 0, 0, 0);
    }

    public static int calculateOrdinal(ModGUIs gui, AEPartLocation side) {
        if (side == null)
            side = AEPartLocation.UP;
        return (gui.ordinal() << 4) | side.ordinal();
    }

    public static ModGUIs getGUIFromOrdinal(int ordinal) {
        return ModGUIs.values()[ordinal >> 4];
    }

    public static AEPartLocation getSideFromOrdinal(int ordinal) {
        return AEPartLocation.fromOrdinal(ordinal & 7);
    }

    public static IPart getPartFromWorld(World world, BlockPos pos, AEPartLocation side) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IPartHost) {
            return ((IPartHost) te).getPart(side);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ordinal, EntityPlayer player, World world, int x, int y, int z) {
        ModGUIs guiID = GuiHandler.getGUIFromOrdinal(ordinal);
        AEPartLocation side = GuiHandler.getSideFromOrdinal(ordinal);
        IPart part = GuiHandler.getPartFromWorld(world, new BlockPos(x, y, z), side);

        switch (guiID) {
            case ESSENTIA_IMPORT_BUS:
                return new ContainerEssentiaImportBus(player, (PartEssentiaImportBus) part);
            case ESSENTIA_EXPORT_BUS:
                return new ContainerEssentiaExportBus(player, (PartEssentiaExportBus) part);
            case ESSENTIA_TERMINAL:
                return new ContainerEssentiaTerminal(player, (PartEssentiaTerminal) part);
            case ARCANE_TERMINAL:
                return new ContainerArcaneTerminal(player, (PartArcaneTerminal) part);
            case AE2_CRAFT_CONFIRM:
                return new ContainerCraftConfirm(player.inventory, (PartArcaneTerminal) part);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ordinal, EntityPlayer player, World world, int x, int y, int z) {
        ModGUIs guiID = GuiHandler.getGUIFromOrdinal(ordinal);
        AEPartLocation side = GuiHandler.getSideFromOrdinal(ordinal);
        IPart part = GuiHandler.getPartFromWorld(world, new BlockPos(x, y, z), side);

        switch (guiID) {
            case ESSENTIA_IMPORT_BUS:
                return new GuiEssentiaImportBus(new ContainerEssentiaImportBus(player, (PartEssentiaImportBus) part));
            case ESSENTIA_EXPORT_BUS:
                return new GuiEssentiaExportBus(new ContainerEssentiaExportBus(player, (PartEssentiaExportBus) part));
            case ESSENTIA_TERMINAL:
                return new GuiEssentiaTerminal(new ContainerEssentiaTerminal(player, (PartEssentiaTerminal) part));
            case ARCANE_TERMINAL:
                return new GuiArcaneTerminal(new ContainerArcaneTerminal(player, (PartArcaneTerminal) part));
            default:
                return null;
        }
    }
}
