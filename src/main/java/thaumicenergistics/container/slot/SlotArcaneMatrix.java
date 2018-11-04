package thaumicenergistics.container.slot;

import thaumicenergistics.container.ICraftingContainer;

/**
 * @author BrockWS
 */
public class SlotArcaneMatrix extends ThESlot {

    private ICraftingContainer container;

    public SlotArcaneMatrix(ICraftingContainer container, int index, int xPosition, int yPosition) {
        super(container.getInventory("crafting"), index, xPosition, yPosition);
        this.container = container;
    }

    @Override
    public void onSlotChanged() {
        this.container.onMatrixChanged();
        super.onSlotChanged();
    }
}
