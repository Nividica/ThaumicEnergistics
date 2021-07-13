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

import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.client.gui.block.GuiArcaneAssembler;
import thaumicenergistics.client.gui.crafting.GuiCraftAmountBridge;
import thaumicenergistics.client.gui.crafting.GuiCraftConfirmBridge;
import thaumicenergistics.client.gui.crafting.GuiCraftingStatusBridge;
import thaumicenergistics.client.gui.item.GuiKnowledgeCore;
import thaumicenergistics.client.gui.part.*;
import thaumicenergistics.container.block.ContainerArcaneAssembler;
import thaumicenergistics.container.crafting.ContainerCraftAmountBridge;
import thaumicenergistics.container.crafting.ContainerCraftConfirmBridge;
import thaumicenergistics.container.crafting.ContainerCraftingStatusBridge;
import thaumicenergistics.container.item.ContainerKnowledgeCore;
import thaumicenergistics.container.part.*;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.part.*;
import thaumicenergistics.tile.TileArcaneAssembler;

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
        TileEntity te = null;
        ModGUIs guiID = GuiHandler.getGUIFromOrdinal(ordinal);
        AEPartLocation side = GuiHandler.getSideFromOrdinal(ordinal);
        BlockPos pos = new BlockPos(x, y, z);
        IPart part = GuiHandler.getPartFromWorld(world, pos, side);
        if(part == null) te = world.getTileEntity(pos);

        switch (guiID) {
            case ARCANE_ASSEMBLER:
                return new ContainerArcaneAssembler(player, (TileArcaneAssembler) te);
            case ESSENTIA_IMPORT_BUS:
                return new ContainerEssentiaImportBus(player, (PartEssentiaImportBus) part);
            case ESSENTIA_EXPORT_BUS:
                return new ContainerEssentiaExportBus(player, (PartEssentiaExportBus) part);
            case ESSENTIA_STORAGE_BUS:
                return new ContainerEssentiaStorageBus(player, (PartEssentiaStorageBus) part);
            case ESSENTIA_TERMINAL:
                return new ContainerEssentiaTerminal(player, (PartEssentiaTerminal) part);
            case ARCANE_TERMINAL:
                return new ContainerArcaneTerminal(player, (PartArcaneTerminal) part);
            case ARCANE_INSCRIBER:
                return new ContainerArcaneInscriber(player, (PartArcaneInscriber) part);
            case AE2_CRAFT_AMOUNT:
                return new ContainerCraftAmountBridge(player.inventory, (PartSharedTerminal) part);
            case AE2_CRAFT_CONFIRM:
                return new ContainerCraftConfirmBridge(player.inventory, (PartSharedTerminal) part);
            case AE2_CRAFT_STATUS:
                return new ContainerCraftingStatusBridge(player.inventory, (PartSharedTerminal) part);
            case AE2_PRIORITY:
                return new ContainerPriorityBridge(player.inventory, (PartEssentiaStorageBus) part);
            case KNOWLEDGE_CORE_ADD:
            case KNOWLEDGE_CORE_DEL:
            case KNOWLEDGE_CORE_VIEW:
                return new ContainerKnowledgeCore(player, guiID, player.openContainer);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ordinal, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = null;
        ModGUIs guiID = GuiHandler.getGUIFromOrdinal(ordinal);
        AEPartLocation side = GuiHandler.getSideFromOrdinal(ordinal);
        BlockPos pos = new BlockPos(x, y, z);
        IPart part = GuiHandler.getPartFromWorld(world, pos, side);
        if(part == null) te = world.getTileEntity(pos);

        switch (guiID) {
            case ARCANE_ASSEMBLER:
                return new GuiArcaneAssembler(new ContainerArcaneAssembler(player, (TileArcaneAssembler) te));
            case ESSENTIA_IMPORT_BUS:
                return new GuiEssentiaImportBus(new ContainerEssentiaImportBus(player, (PartEssentiaImportBus) part));
            case ESSENTIA_EXPORT_BUS:
                return new GuiEssentiaExportBus(new ContainerEssentiaExportBus(player, (PartEssentiaExportBus) part));
            case ESSENTIA_STORAGE_BUS:
                return new GuiEssentiaStorageBus(new ContainerEssentiaStorageBus(player, (PartEssentiaStorageBus) part));
            case ESSENTIA_TERMINAL:
                return new GuiEssentiaTerminal(new ContainerEssentiaTerminal(player, (PartEssentiaTerminal) part));
            case ARCANE_TERMINAL:
                return new GuiArcaneTerminal(new ContainerArcaneTerminal(player, (PartArcaneTerminal) part));
            case ARCANE_INSCRIBER:
                return new GuiArcaneInscriber(new ContainerArcaneInscriber(player, (PartArcaneInscriber) part));
            case AE2_CRAFT_AMOUNT:
                return new GuiCraftAmountBridge(player, (PartSharedTerminal) part);
            case AE2_CRAFT_CONFIRM:
                return new GuiCraftConfirmBridge(player.inventory, (PartSharedTerminal) part);
            case AE2_CRAFT_STATUS:
                return new GuiCraftingStatusBridge(player.inventory, (PartSharedTerminal) part);
            case AE2_PRIORITY:
                return new GuiPriorityBridge(player, (PartEssentiaStorageBus) part);
            case KNOWLEDGE_CORE_ADD:
            case KNOWLEDGE_CORE_DEL:
            case KNOWLEDGE_CORE_VIEW:
                return new GuiKnowledgeCore(new ContainerKnowledgeCore(player, guiID, player.openContainer));
            default:
                return null;
        }
    }
}
