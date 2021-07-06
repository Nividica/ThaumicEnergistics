package thaumicenergistics.client.gui.part;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

import net.minecraft.inventory.Slot;
import thaumicenergistics.client.gui.GuiConfigurable;
import thaumicenergistics.client.gui.helpers.MERepo;
import thaumicenergistics.container.ContainerBaseTerminal;
import thaumicenergistics.container.slot.SlotME;

/**
 * @author BrockWS
 * @author Alex811
 */
public abstract class GuiAbstractTerminal<T extends IAEStack<T>, C extends IStorageChannel<T>> extends GuiConfigurable implements IPowerChannelState {

    protected ContainerBaseTerminal container;
    protected MERepo<T> repo;

    public GuiAbstractTerminal(ContainerBaseTerminal container) {
        super(container);
        this.container = container;
    }

    public MERepo<T> getRepo() {
        return this.repo;
    }

    @Override
    public boolean isPowered() {
        return this.container.getPart().isPowered();
    }

    @Override
    public boolean isActive() {
        return this.container.getPart().isActive();
    }

    @Override
    public void drawSlot(Slot slot) {
        super.drawSlot(slot);
        if(slot instanceof SlotME && !this.isActive())
            drawRect(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, 0x66111111);
    }
}
