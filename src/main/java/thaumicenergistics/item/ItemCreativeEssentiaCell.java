package thaumicenergistics.item;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author BrockWS
 */
public class ItemCreativeEssentiaCell extends ItemEssentiaCell {

    public ItemCreativeEssentiaCell() {
        super("creative", 0, 0);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Contains all types of essentia");
    }
}
