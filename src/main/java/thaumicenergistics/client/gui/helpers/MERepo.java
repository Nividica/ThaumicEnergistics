package thaumicenergistics.client.gui.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;

import thaumicenergistics.util.AEUtil;

/**
 * Based on ItemRepo and FluidRepo
 *
 * @author BrockWS
 */
public class MERepo<T extends IAEStack<T>> {

    private IItemList<T> list;
    /**
     * Contains all stacks currently in the view
     */
    private ArrayList<T> view = new ArrayList<>();
    private String searchString = "";
    private ViewItems viewMode;
    private SortDir sortDir;
    private SortOrder sortOrder;
    private GuiScrollBar scrollBar;
    private int rowSize = 9;

    public MERepo(Class<? extends IStorageChannel<T>> clazz) {
        this.list = AEUtil.getList(clazz);
        this.viewMode = ViewItems.ALL;
        this.sortDir = SortDir.ASCENDING;
        this.sortOrder = SortOrder.NAME;
    }

    public void updateView() {
        this.view.clear();
        this.view.ensureCapacity(this.list.size());

        boolean searchByMod = false;
        String search = this.searchString;
        if (search.startsWith("@")) {
            searchByMod = true;
            search = search.substring(1);
        }

        Pattern p;
        try {
            p = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ignored) {
            try {
                p = Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException ignored2) {
                return;
            }
        }

        String s = "";

        for (T stack : this.list) {
            if (this.getViewMode() == ViewItems.CRAFTABLE && !stack.isCraftable())
                continue;

            if (this.getViewMode() == ViewItems.STORED && stack.getStackSize() == 0)
                continue;

            // TODO: Don't use platform methods
            if (stack instanceof IAEItemStack)
                s = searchByMod ? Platform.getModId((IAEItemStack) stack) : Platform.getItemDisplayName(stack);
            else if (stack instanceof IAEFluidStack)
                s = searchByMod ? Platform.getModId((IAEFluidStack) stack) : Platform.getFluidDisplayName(stack);

            boolean match = false;
            if (p.matcher(s).find())
                match = true;

            if (!match && !searchByMod) {
                List<String> tooltip = Platform.getTooltip(stack);
                for (String line : tooltip) {
                    if (p.matcher(line).find()) {
                        this.view.add(stack);
                        break;
                    }
                }
            }
            if (match) {
                if (this.getViewMode().equals(ViewItems.CRAFTABLE)) {
                    stack = stack.copy();
                    stack.setStackSize(0);
                }
                this.view.add(stack);
            }
        }

        if (sortOrder == SortOrder.MOD)
            this.sortByMod();
        if (sortOrder == SortOrder.NAME)
            this.sortByName();
        if (sortOrder == SortOrder.AMOUNT)
            this.sortByCount();

        // TODO: Check if this is even needed anymore
        if (this.getScrollBar() != null) {
            if (this.view.size() <= this.getRowSize() * 6) { // We don't need to have scrolling
                this.getScrollBar().setRows(6);
                this.getScrollBar().click(this.scrollBar.getY());
            } else {
                this.getScrollBar().setRows((int) Math.ceil(this.view.size() * this.getRowSize()));
            }
        }
    }

    public void postUpdate(T stack) {
        T existing = this.list.findPrecise(stack);
        if (existing != null) { // Already exists in the list
            existing.reset();
            existing.add(stack);
        } else { // Doesn't exist in the list yet
            this.list.add(stack);
        }
    }

    public T getReferenceStack(int i) {
        int scroll = (int) Math.max(Math.min(this.scrollBar.getCurrentPosition(), Math.ceil((double) this.view.size() / this.rowSize)), 0);
        i += scroll * this.rowSize;
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

    public void setScrollBar(GuiScrollBar scrollBar) {
        this.scrollBar = scrollBar;
    }

    public GuiScrollBar getScrollBar() {
        return this.scrollBar;
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

    public void setViewMode(ViewItems view) {
        this.viewMode = view;
    }

    public ViewItems getViewMode() {
        return this.viewMode;
    }

    public SortDir getSortDir() {
        return this.sortDir;
    }

    public void setSortDir(SortDir sortDir) {
        this.sortDir = sortDir;
    }

    public SortOrder getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    private int checkSortDir(int i) {
        return this.getSortDir() == SortDir.ASCENDING ? i : -i;
    }

    private void sortByName() {
        this.view.sort((o1, o2) -> this.checkSortDir(AEUtil.getDisplayName(o1).compareToIgnoreCase(AEUtil.getDisplayName(o2))));
    }

    private void sortByMod() {
        this.view.sort((o1, o2) -> {
            int i = AEUtil.getModID(o1).compareToIgnoreCase(AEUtil.getModID(o2));
            if (i == 0)
                i = AEUtil.getDisplayName(o1).compareToIgnoreCase(AEUtil.getDisplayName(o2));
            return this.checkSortDir(i);
        });
    }

    private void sortByCount() {
        this.view.sort((o1, o2) -> this.checkSortDir(Long.compare(AEUtil.getStackSize(o2), AEUtil.getStackSize(o1))));
    }
}
