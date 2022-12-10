package thaumicenergistics.common.integration;

import appeng.api.storage.data.IAEItemStack;
import thaumicenergistics.common.items.ItemCraftingAspect;

public class AEHooks {
    /**
     * Hook for AE2s <em>GuiMeMonitorable.postUpdate()</em>.<br>
     * Returning true will prevent the item from being displayed in GUI's.
     * TODO: Expand this into registration list within the API.
     *
     * @param is
     * @return
     */
    public static boolean isItemGUIBlacklisted(final IAEItemStack is) {
        if (is.getItem() instanceof ItemCraftingAspect) {
            return true;
        }
        return false;
    }
}
