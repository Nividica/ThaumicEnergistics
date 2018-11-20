package thaumicenergistics.container.slot;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.items.IItemHandler;

/**
 * @author BrockWS
 */
public class SlotUpgrade extends ThESlot {

    public SlotUpgrade(IItemHandler handler, int index, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
    }

    @Override
    public int getBackgroundIconIndex() {
        return 13 * 16 + 15;
    }

    @Override
    public ResourceLocation getBackgroundIcon() {
        return new ResourceLocation("appliedenergistics2", "textures/guis/states.png");
    }
}
