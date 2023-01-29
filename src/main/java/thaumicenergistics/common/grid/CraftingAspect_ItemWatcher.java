package thaumicenergistics.common.grid;

import thaumicenergistics.common.items.ItemCraftingAspect;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;

class CraftingAspect_ItemWatcher implements IMEMonitorHandlerReceiver<IAEItemStack> {

    private final GridEssentiaCache gridCache;

    public CraftingAspect_ItemWatcher(final GridEssentiaCache gridCache) {
        this.gridCache = gridCache;
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return this.gridCache.internalGrid == verificationToken;
    }

    @Override
    public void onListUpdate() {
        // Ignored
    }

    @Override
    public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change,
            final BaseActionSource actionSource) {
        for (IAEItemStack stack : change) {
            // Is the stack craftable, has NBT tag, and is a crafting aspect?
            if (stack.isCraftable() && stack.hasTagCompound() && (stack.getItem() instanceof ItemCraftingAspect)) {
                this.gridCache.markForUpdate();
                break;
            }
        }
    }
}
