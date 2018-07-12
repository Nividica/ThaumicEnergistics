package thaumicenergistics.api.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import appeng.api.storage.data.IAEStack;

/**
 * Based completely on MeaningfulFluidIterator and MeaningfulItemIterator from Applied Energistics 2
 * TODO: Make a generic MeaningfulIterator in AE2
 *
 * @author BrockWS
 */
public class MeaningfulEssentiaIterator<T extends IAEStack> implements Iterator<T> {

    private Iterator<T> parent;
    private T next;

    public MeaningfulEssentiaIterator(Iterator<T> iterator) {
        this.parent = iterator;
    }

    @Override
    public boolean hasNext() {
        while (this.parent.hasNext()) {
            this.next = this.parent.next();
            if (this.next.isMeaningful()) {
                return true;
            } else {
                this.parent.remove();
            }
        }

        this.next = null;
        return false;
    }

    @Override
    public T next() {
        if (this.next == null) {
            throw new NoSuchElementException();
        }

        return this.next;
    }

    @Override
    public void remove() {
        this.parent.remove();
    }
}
