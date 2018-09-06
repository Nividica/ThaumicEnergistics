package thaumicenergistics.integration.appeng.cell;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IItemList;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.integration.appeng.EssentiaList;
import thaumicenergistics.util.AEUtil;

/**
 * @author BrockWS
 */
public class CreativeEssentiaCellInventory implements ICellInventoryHandler<IAEEssentiaStack> {

    private IItemList<IAEEssentiaStack> storedAspects = new EssentiaList();

    private CreativeEssentiaCellInventory() {
        Aspect.aspects.forEach((s, aspect) -> storedAspects.add(AEUtil.getAEStackFromAspect(aspect, 1000)));
    }

    public static ICellInventoryHandler getCell(ItemStack s, ISaveProvider c) {
        return new CreativeEssentiaCellInventory();
    }

    @Nullable
    @Override
    public ICellInventory<IAEEssentiaStack> getCellInv() {
        return null;
    }

    @Override
    public boolean isPreformatted() {
        return false;
    }

    @Override
    public boolean isFuzzy() {
        return false;
    }

    @Override
    public IncludeExclude getIncludeExcludeMode() {
        return IncludeExclude.WHITELIST;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(IAEEssentiaStack iaeEssentiaStack) {
        return false;
    }

    @Override
    public boolean canAccept(IAEEssentiaStack iaeEssentiaStack) {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true;
    }

    @Override
    public IAEEssentiaStack injectItems(IAEEssentiaStack stack, Actionable actionable, IActionSource src) {
        return null;
    }

    @Override
    public IAEEssentiaStack extractItems(IAEEssentiaStack stack, Actionable actionable, IActionSource src) {
        return stack.copy();
    }

    @Override
    public IItemList<IAEEssentiaStack> getAvailableItems(IItemList<IAEEssentiaStack> list) {
        this.storedAspects.forEach(list::add);
        return list;
    }

    @Override
    public IStorageChannel<IAEEssentiaStack> getChannel() {
        return AEUtil.getStorageChannel(IEssentiaStorageChannel.class);
    }
}
