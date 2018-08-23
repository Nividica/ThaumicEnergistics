package thaumicenergistics.client.gui.helpers;

import java.util.ArrayList;

import appeng.api.AEApi;
import appeng.api.storage.data.IItemList;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;

/**
 * Based on ItemRepo and FluidRepo
 *
 * @author BrockWS
 */
public class EssentiaRepo {

    private IItemList<IAEEssentiaStack> list = AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class).createList();
    /**
     * Contains all stacks currently in the view
     */
    private ArrayList<IAEEssentiaStack> view = new ArrayList<>();
    private String searchString = "";
    private int rowSize = 9;

    public EssentiaRepo() {
    }

    public void updateView() {
        this.view.clear();
        this.view.ensureCapacity(this.list.size());
        // TODO: Improve
        // Add Sorting
        // Add searching
        // Add scrolling

        for (IAEEssentiaStack stack : this.list) {
            this.view.add(stack);
        }
    }

    public void postUpdate(IAEEssentiaStack stack) {
        IAEEssentiaStack existing = this.list.findPrecise(stack);
        if (existing != null) { // Already exists in the list
            existing.reset();
            existing.add(stack);
        } else { // Doesn't exist in the list yet
            this.list.add(stack);
        }
    }

    public IAEEssentiaStack getReferenceStack(int i) {
        // TODO: Scroll support
        if (i < this.view.size())
            return this.view.get(i);
        return null;
    }

    public int size() {
        return this.view.size();
    }

    public void clear() {
        this.list.resetStatus();
    }

    public void setRowSize(int rowSize) {
        this.rowSize = rowSize;
    }

    public int getRowSize() {
        return this.rowSize;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
        return this.searchString;
    }
}
