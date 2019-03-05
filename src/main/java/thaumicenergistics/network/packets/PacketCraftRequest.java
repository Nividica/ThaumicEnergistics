package thaumicenergistics.network.packets;

import java.util.concurrent.Future;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.IPart;
import appeng.me.GridAccessException;

import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.container.crafting.ContainerCraftAmountBridge;
import thaumicenergistics.container.crafting.ContainerCraftConfirmBridge;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.integration.appeng.grid.GridUtil;
import thaumicenergistics.integration.appeng.grid.IThEGridHost;
import thaumicenergistics.part.PartBase;

import io.netty.buffer.ByteBuf;

/**
 * @author BrockWS
 */
public class PacketCraftRequest implements IMessage {

    private int amount;
    private boolean shift;

    public PacketCraftRequest() {
    }

    public PacketCraftRequest(int amount, boolean shift) {
        this.amount = amount;
        this.shift = shift;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.amount = buf.readInt();
        this.shift = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.amount);
        buf.writeBoolean(this.shift);
    }

    public static class Handler implements IMessageHandler<PacketCraftRequest, IMessage> {
        @Override
        public IMessage onMessage(PacketCraftRequest message, MessageContext ctx) {
            NetHandlerPlayServer handler = ctx.getServerHandler();
            EntityPlayerMP player = handler.player;
            IThreadListener thread = (IThreadListener) player.world;
            thread.addScheduledTask(() -> {
                if (!(player.openContainer instanceof ContainerCraftAmountBridge))
                    return;
                ContainerCraftAmountBridge ca = (ContainerCraftAmountBridge) player.openContainer;
                if (!(ca.getTarget() instanceof PartBase)||!(ca.getTarget() instanceof IThEGridHost) || ca.getItemToCraft() == null)
                    return;
                PartBase part = (PartBase) ca.getTarget();
                IGridNode node = part.getGridNode();
                ca.getItemToCraft().setStackSize(message.amount);
                Future<ICraftingJob> job = null;

                try {
                    ICraftingGrid cg = GridUtil.getCraftingGrid(node);
                    job = cg.beginCraftingJob(ca.getWorld(), ca.getGrid(), ca.getActionSrc(), ca.getItemToCraft(), null);
                    GuiHandler.openGUI(ModGUIs.AE2_CRAFT_CONFIRM, player, part.getLocation().getPos(), part.side);
                    if (player.openContainer instanceof ContainerCraftConfirmBridge) {
                        ContainerCraftConfirmBridge cc = (ContainerCraftConfirmBridge) player.openContainer;
                        cc.setAutoStart(message.shift);
                        cc.setJob(job);
                        cc.detectAndSendChanges();
                    }
                } catch (GridAccessException ignored) {
                    if (job != null)
                        job.cancel(true);
                }
            });
            return null;
        }
    }
}
