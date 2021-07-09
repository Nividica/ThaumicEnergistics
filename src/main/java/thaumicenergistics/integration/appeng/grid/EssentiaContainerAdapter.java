package thaumicenergistics.integration.appeng.grid;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IItemList;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.util.AEUtil;
import thaumicenergistics.util.EssentiaFilter;

/**
 * Wraps a IAspectContainer for use by a ME system
 * <p>
 * Used by Essentia Storage Bus
 *
 * @author BrockWS
 * @author Alex811
 */
public class EssentiaContainerAdapter implements IMEInventoryHandler<IAEEssentiaStack> {

    private IAspectContainer container;
    private EssentiaFilter config;
    private IncludeExclude whitelistMode;
    private AccessRestriction cachedAccessRestriction;
    private boolean hasReadAccess;
    private boolean hasWriteAccess;
    private boolean reportInaccessible;
    private int priority;

    public EssentiaContainerAdapter(IAspectContainer container, EssentiaFilter config) {
        this.container = container;
        this.config = config;
        this.setWhitelist(true);
        this.setBaseAccess(AccessRestriction.READ_WRITE);
        this.setReportInaccessible(StorageFilter.EXTRACTABLE_ONLY);
        this.priority = 0;
    }

    public boolean isWhitelist() {
        return this.whitelistMode == IncludeExclude.WHITELIST;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelistMode = (whitelist ? IncludeExclude.WHITELIST : IncludeExclude.BLACKLIST);
    }

    public void setReportInaccessible(StorageFilter reportInaccessible) {
        this.reportInaccessible = reportInaccessible != StorageFilter.EXTRACTABLE_ONLY;
    }

    @Override
    public IAEEssentiaStack injectItems(IAEEssentiaStack input, Actionable type, IActionSource src) {
        if (input == null || !input.isMeaningful() || !this.canAccept(input))
            return input;

        // Add to container to see how much it can store
        int notAdded = this.container.addToContainer(input.getAspect(), (int) input.getStackSize());
        if (type == Actionable.SIMULATE) // Annoying hack, maybe talk with Azanor about getting some type of simulation instead
            this.container.takeFromContainer(input.getAspect(), (int) input.getStackSize() - notAdded);
        if (notAdded > 0) // Didn't add it all
            return input.setStackSize(notAdded);
        return null;
    }

    @Override
    public IAEEssentiaStack extractItems(IAEEssentiaStack request, Actionable mode, IActionSource src) {
        if (request == null || !request.isMeaningful())
            return null;
        if (!this.hasReadAccess)
            return null;
        if (this.container.containerContains(request.getAspect()) <= 0) // Make sure the container actually contains it
            return null;

        Aspect aspect = request.getAspect();
        int max = (int) Math.min(this.container.containerContains(aspect), request.getStackSize());

        if (mode == Actionable.SIMULATE)
            return AEUtil.getAEStackFromAspect(aspect, max);

        boolean worked = this.container.takeFromContainer(aspect, max);
        if (!worked)
            return null;

        return request.setStackSize(max);
    }

    @Override
    public IItemList<IAEEssentiaStack> getAvailableItems(IItemList<IAEEssentiaStack> out) {
        if (this.container == null || (!this.hasReadAccess && !this.reportInaccessible))
            return out;
        for (Aspect aspect : this.container.getAspects().getAspects())
            out.add(AEUtil.getAEStackFromAspect(aspect, this.container.containerContains(aspect)));
        return out;
    }

    public void setBaseAccess(AccessRestriction access) {
        this.cachedAccessRestriction = access;
        this.hasReadAccess = access.hasPermission(AccessRestriction.READ);
        this.hasWriteAccess = access.hasPermission(AccessRestriction.WRITE);
    }

    @Override
    public AccessRestriction getAccess() {
        return this.cachedAccessRestriction;
    }

    @Override
    public boolean isPrioritized(IAEEssentiaStack input) {
        return false; // Maybe check if container instanceof TileJar and check if jar has a label with same aspect
    }

    @Override
    public boolean canAccept(IAEEssentiaStack input) {
        if (this.container == null || !this.hasWriteAccess)
            return false;
        boolean inFilter = this.config.isInFilter(input.getAspect());
        boolean containerCanAccept = this.container.doesContainerAccept(input.getAspect());
        if (this.whitelistMode == IncludeExclude.BLACKLIST){
            if (inFilter)
                return false;
            return containerCanAccept;
        }
        if (!this.config.hasAspects())  // on empty whitelist, allow any
            return containerCanAccept;
        return inFilter && containerCanAccept;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true; // TODO: Verify requirement
    }

    @Override
    public IStorageChannel<IAEEssentiaStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class);
    }
}
