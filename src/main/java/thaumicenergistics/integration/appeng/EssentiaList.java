package thaumicenergistics.integration.appeng;

import java.util.*;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IItemList;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.util.MeaningfulEssentiaIterator;

/**
 * @author BrockWS
 */
public class EssentiaList implements IItemList<IAEEssentiaStack> {

    private final Map<IAEEssentiaStack, IAEEssentiaStack> records = new HashMap<>();

    @Override
    public void addStorage(IAEEssentiaStack option) {
        if (option == null)
            return;

        IAEEssentiaStack st = this.getEssentiaRecord(option);
        if (st != null) {
            st.incStackSize(option.getStackSize());
            return;
        }

        IAEEssentiaStack opt = option.copy();
        this.putEssentiaRecord(opt);
    }

    @Override
    public void addCrafting(IAEEssentiaStack option) {
        if (option == null)
            return;

        IAEEssentiaStack st = this.getEssentiaRecord(option);
        if (st != null) {
            st.setCraftable(true);
            return;
        }

        IAEEssentiaStack opt = option.copy();
        opt.setStackSize(0);
        opt.setCraftable(true);
        this.putEssentiaRecord(opt);
    }

    @Override
    public void addRequestable(IAEEssentiaStack option) {
        if (option == null)
            return;

        IAEEssentiaStack st = this.getEssentiaRecord(option);
        if (st != null) {
            st.setCountRequestable(st.getCountRequestable() + option.getCountRequestable());
            return;
        }

        IAEEssentiaStack opt = option.copy();
        opt.setStackSize(0);
        opt.setCraftable(false);
        opt.setCountRequestable(option.getCountRequestable());
        this.putEssentiaRecord(opt);
    }

    @Override
    public IAEEssentiaStack getFirstItem() {
        for (IAEEssentiaStack stack : this)
            return stack;
        return null;
    }

    @Override
    public int size() {
        return this.records.values().size();
    }

    @Override
    public Iterator<IAEEssentiaStack> iterator() {
        return new MeaningfulEssentiaIterator<>(this.records.values().iterator());
    }

    @Override
    public void resetStatus() {
        for (IAEEssentiaStack s : this)
            s.reset();
    }

    @Override
    public void add(IAEEssentiaStack option) {
        if (option == null)
            return;

        IAEEssentiaStack stack = this.getEssentiaRecord(option);
        if (stack != null) {
            stack.add(option);
            return;
        }

        IAEEssentiaStack opt = option.copy();
        this.putEssentiaRecord(opt);
    }

    @Override
    public IAEEssentiaStack findPrecise(IAEEssentiaStack stack) {
        return stack == null ? null : this.getEssentiaRecord(stack);
    }

    @Override
    public Collection<IAEEssentiaStack> findFuzzy(IAEEssentiaStack stack, FuzzyMode mode) {
        return stack == null ? Collections.emptyList() : Collections.singletonList(this.findPrecise(stack));
    }

    @Override
    public boolean isEmpty() {
        return !this.iterator().hasNext();
    }

    private IAEEssentiaStack getEssentiaRecord(IAEEssentiaStack stack) {
        return this.records.get(stack);
    }

    private IAEEssentiaStack putEssentiaRecord(IAEEssentiaStack stack) {
        return this.records.put(stack, stack);
    }
}
