package thaumicenergistics.client.gui.part;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

import thaumicenergistics.client.gui.GuiBase;
import thaumicenergistics.client.gui.helpers.MERepo;
import thaumicenergistics.container.ContainerBase;

/**
 * @author BrockWS
 */
public abstract class GuiAbstractTerminal<T extends IAEStack<T>, C extends IStorageChannel<T>> extends GuiBase {

    protected MERepo<T> repo;
    private C channel;

    public GuiAbstractTerminal(ContainerBase container) {
        super(container);
    }

    public MERepo<T> getRepo() {
        return this.repo;
    }

    public C getChannel() {
        return this.channel;
    }
}
