package thaumicenergistics.implementaion;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.localization.PlayerMessages;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.IThEInteractionHelper;
import thaumicenergistics.api.IThEWirelessEssentiaTerminal;
import thaumicenergistics.api.entities.IGolemHookHandler;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.client.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.grid.WirelessAELink;
import thaumicenergistics.common.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.common.integration.tc.GolemHooks;
import thaumicenergistics.common.inventory.HandlerWirelessEssentiaTerminal;
import thaumicenergistics.common.network.packet.server.Packet_S_ArcaneCraftingTerminal;
import thaumicenergistics.common.storage.AspectStack;
import thaumicenergistics.common.utils.ThELog;

/**
 * Implements {@link IThEInteractionHelper}
 */
public class ThEInteractionHelper implements IThEInteractionHelper {

    @Override
    public long convertEssentiaAmountToFluidAmount(final long essentiaAmount) {
        return EssentiaConversionHelper.INSTANCE.convertEssentiaAmountToFluidAmount(essentiaAmount);
    }

    @Override
    public long convertFluidAmountToEssentiaAmount(final long milibuckets) {
        return EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount(milibuckets);
    }

    @Override
    public IAspectStack createAspectStack(final Aspect aspect, final long stackSize) {
        return new AspectStack(aspect, stackSize);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class getArcaneCraftingTerminalGUIClass() {
        return GuiArcaneCraftingTerminal.class;
    }

    @Override
    public void openWirelessTerminalGui(final EntityPlayer player) {
        // Valid player?
        if ((player == null) || (player instanceof FakePlayer)) {
            return;
        }

        // Ignored client side
        if (player.worldObj.isRemote) {
            return;
        }

        // Get the item the player is holding.
        ItemStack wirelessTerminal = player.getHeldItem();

        // Ensure the stack is valid
        if ((wirelessTerminal == null)) {
            // Invalid terminal
            return;
        }

        // Ensure the stack's item implements the wireless interface
        if (!(wirelessTerminal.getItem() instanceof IThEWirelessEssentiaTerminal)) {
            // Invalid item.
            return;
        }

        // Get the interface
        IThEWirelessEssentiaTerminal terminalInterface = (IThEWirelessEssentiaTerminal) wirelessTerminal.getItem();

        // Ensure the terminal has power
        if (terminalInterface.getAECurrentPower(wirelessTerminal) == 0) {
            // Terminal is dead
            player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
            return;
        }

        // Ensure the terminal is linked
        if (!HandlerWirelessEssentiaTerminal.isTerminalLinked(terminalInterface, wirelessTerminal)) {
            // Unlinked terminal
            player.addChatMessage(PlayerMessages.CommunicationError.get());
            return;
        }

        // Get the encryption key
        String encKey = terminalInterface.getEncryptionKey(wirelessTerminal);

        // Are any AP's in range?
        ArrayList<IWirelessAccessPoint> accessPoints = WirelessAELink.locateAPsInRangeOfPlayer(player, encKey);

        // Error occured
        if (accessPoints == null) {
            player.addChatMessage(PlayerMessages.CommunicationError.get());
        }
        // None in range
        else if (accessPoints.isEmpty()) {
            player.addChatMessage(PlayerMessages.OutOfRange.get());
        }
        // Launch the gui
        else {
            ThEGuiHandler.launchGui(
                    ThEGuiHandler.WIRELESS_TERMINAL_ID,
                    player,
                    player.worldObj,
                    (int) player.posX,
                    (int) player.posY,
                    (int) player.posZ,
                    new Object[] {
                        new HandlerWirelessEssentiaTerminal(player, encKey, terminalInterface, wirelessTerminal)
                    });
        }
    }

    @Deprecated
    @Override
    public void openWirelessTerminalGui(
            final EntityPlayer player, final IThEWirelessEssentiaTerminal terminalInterface) {
        this.openWirelessTerminalGui(player);
    }

    @Override
    public void registerGolemHookHandler(final IGolemHookHandler handler) {
        try {
            GolemHooks.registerHandler(handler);
        } catch (Exception e) {
            ThELog.warning("Caught Exception During API call to registerGolemHookHandler");
            ThELog.warning(e.toString());
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setArcaneCraftingTerminalRecipe(final ItemStack[] itemsVanilla) {
        try {
            // Get the player
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            // Is the player looking at an ACT?
            if (!(player.openContainer instanceof ContainerPartArcaneCraftingTerminal)) {
                return;
            }

            boolean hasItems = false;

            // Ensure the input items array is the correct size
            if ((itemsVanilla == null) || (itemsVanilla.length != 9)) {
                return;
            }

            // Create the AE items array
            IAEItemStack[] items = new IAEItemStack[9];

            // Get the items and convert them to their AE counterparts.
            for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
                if (itemsVanilla[slotIndex] != null) {
                    items[slotIndex] = AEApi.instance().storage().createItemStack(itemsVanilla[slotIndex]);
                    hasItems = true;
                }
            }

            // Send the list to the server
            if (hasItems) {
                Packet_S_ArcaneCraftingTerminal.sendSetCrafting_NEI(player, items);
            }
        } catch (Exception e) {
            ThELog.warning("Caught Exception During API call to setArcaneCraftingTerminalRecipe");
            ThELog.warning(e.toString());
            e.printStackTrace();
        }
    }
}
