package thaumicenergistics.container.part;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IItemList;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.PacketMEEssentiaUpdate;
import thaumicenergistics.network.PacketUIAction;
import thaumicenergistics.network.packets.PacketInvHeldUpdate;
import thaumicenergistics.part.PartEssentiaTerminal;
import thaumicenergistics.util.AEUtil;
import thaumicenergistics.util.ForgeUtil;
import thaumicenergistics.util.TCUtil;

/**
 * @author BrockWS
 */
public class ContainerEssentiaTerminal extends ContainerBase implements IMEMonitorHandlerReceiver<IAEEssentiaStack> {

    private PartEssentiaTerminal part;
    private IMEMonitor<IAEEssentiaStack> monitor;

    public ContainerEssentiaTerminal(EntityPlayer player, PartEssentiaTerminal part) {
        super(player);
        this.part = part;
        if (ForgeUtil.isServer()) {
            this.monitor = this.part.getInventory(AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class));
            if (this.monitor != null) {
                this.monitor.addListener(this, null);
            }
        }

        this.bindPlayerInventory(player.inventory, 0, 138);
    }

    @Override
    public void onAction(EntityPlayerMP player, PacketUIAction packet) {
        InventoryPlayer inv = player.inventory;
        if (packet.action == ActionType.FILL_ESSENTIA_ITEM && packet.requestedStack != null) {
            IAEEssentiaStack requestedStack = packet.requestedStack;
            ItemStack toFill = inv.getItemStack().copy();
            if (toFill.isEmpty() || !(toFill.getItem() instanceof IEssentiaContainerItem))
                return;
            toFill.setCount(1);

            IEssentiaContainerItem containerItem = (IEssentiaContainerItem) toFill.getItem();
            int max = TCUtil.getMaxStorable(containerItem);
            if (max < 1 || (containerItem.getAspects(toFill) != null && containerItem.getAspects(toFill).size() > 0))
                return;

            IAEEssentiaStack stack = this.monitor.extractItems(requestedStack, Actionable.SIMULATE, this.part.source);
            if (stack == null || stack.getStackSize() < max)
                return;
            stack.setStackSize(max);
            containerItem.setAspects(toFill, new AspectList().add(stack.getAspect(), max));
            toFill.setItemDamage(1);
            boolean filledItem = false;
            if (inv.getItemStack().getCount() > 1) { // Player tried to fill multiple at once
                if (inv.addItemStackToInventory(toFill)) {
                    filledItem = true;
                    ItemStack held = inv.getItemStack();
                    held.setCount(held.getCount() - 1);
                    inv.setItemStack(held);
                    PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(held));
                }
            } else {
                player.inventory.setItemStack(toFill);
                filledItem = true;
                PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(toFill));
            }
            if (filledItem)
                this.monitor.extractItems(stack, Actionable.MODULATE, this.part.source);
        } else if (packet.action == ActionType.EMPTY_ESSENTIA_ITEM) {
            ItemStack toEmpty = inv.getItemStack().copy();
            if (toEmpty.isEmpty() || !(toEmpty.getItem() instanceof IEssentiaContainerItem))
                return;
            IEssentiaContainerItem containerItem = (IEssentiaContainerItem) toEmpty.getItem();
            AspectList list = containerItem.getAspects(toEmpty);
            if (list == null || list.size() < 1)
                return;
            AtomicBoolean canInsert = new AtomicBoolean(true);
            list.aspects.forEach((aspect, amount) -> {
                IAEEssentiaStack stack = this.monitor.injectItems(AEUtil.getAEStackFromAspect(aspect, amount), Actionable.SIMULATE, this.part.source);
                if (stack != null && stack.getStackSize() > 0)
                    canInsert.set(false);
            });
            if (!canInsert.get())
                return;

            if (toEmpty.getCount() > 1) {
                toEmpty.setCount(1);
                toEmpty.setTagCompound(null);
                toEmpty.setItemDamage(0);
                if (!inv.addItemStackToInventory(toEmpty))
                    return;
                ItemStack held = inv.getItemStack();
                held.setCount(held.getCount() - 1);
                inv.setItemStack(held);
                PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(held));
            } else {
                toEmpty.setTagCompound(null);
                toEmpty.setItemDamage(0);
                inv.setItemStack(toEmpty);
                PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(toEmpty));
            }
            list.aspects.forEach((aspect, amount) -> this.monitor.injectItems(AEUtil.getAEStackFromAspect(aspect, amount), Actionable.MODULATE, this.part.source));
        }
        super.onAction(player, packet);
    }

    @Override
    public boolean isValid(Object o) {
        return true;
    }

    @Override
    public void postChange(IBaseMonitor<IAEEssentiaStack> iBaseMonitor, Iterable<IAEEssentiaStack> iterable, IActionSource iActionSource) {
        for (final IContainerListener c : this.listeners) {
            this.sendInventory(c);
        }
    }

    @Override
    public void onListUpdate() {
        for (final IContainerListener c : this.listeners) {
            this.sendInventory(c);
        }
    }

    private void sendInventory(IContainerListener listener) {
        if (ForgeUtil.isClient() || !(listener instanceof EntityPlayer) || this.monitor == null)
            return;
        IItemList<IAEEssentiaStack> storage = this.monitor.getStorageList();
        PacketMEEssentiaUpdate packet = new PacketMEEssentiaUpdate();
        for (IAEEssentiaStack stack : storage)
            packet.appendStack(stack);
        PacketHandler.sendToPlayer((EntityPlayerMP) listener, packet);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        this.sendInventory(listener);
    }
}
