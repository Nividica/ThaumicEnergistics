package thaumicenergistics.container.slot;

import net.minecraftforge.items.IItemHandler;
import thaumicenergistics.ThaumicEnergisticsApi;

import javax.annotation.Nullable;

/**
 * @author Alex811
 */
public class SlotKnowledgeCore extends ThESlot{
    public SlotKnowledgeCore(IItemHandler handler, int index, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
    }

    @Nullable
    @Override
    public String getSlotTexture() {
        return ThaumicEnergisticsApi.instance().textures().knowledgeCoreSlot().toString();
    }
}
