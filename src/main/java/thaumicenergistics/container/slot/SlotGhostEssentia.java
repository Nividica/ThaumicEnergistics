package thaumicenergistics.container.slot;

import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.util.EssentiaFilter;

import net.minecraft.inventory.IInventory;

/**
 * @author BrockWS
 */
public class SlotGhostEssentia extends SlotGhost {

    private EssentiaFilter filter;

    public SlotGhostEssentia(EssentiaFilter filter, IInventory inventory, int index, int xPosition, int yPosition, int groupID) {
        super(inventory, index, xPosition, yPosition, groupID);
        this.filter = filter;
    }

    public SlotGhostEssentia(EssentiaFilter filter, IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        this.filter = filter;
    }

    public EssentiaFilter getFilter() {
        return this.filter;
    }

    public Aspect getAspect() {
        return this.getFilter().getAspect(this.getSlotIndex());
    }
}
