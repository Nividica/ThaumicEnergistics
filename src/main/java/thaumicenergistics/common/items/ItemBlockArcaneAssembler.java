package thaumicenergistics.common.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.tiles.TileArcaneAssembler;

/**
 * {@link TileArcaneAssembler} item.
 *
 * @author Nividica
 *
 */
public class ItemBlockArcaneAssembler extends ItemBlock {

    public ItemBlockArcaneAssembler(final Block block) {
        super(block);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, @SuppressWarnings("rawtypes") final List displayList,
            final boolean advancedItemTooltips) {
        // Ensure the stack has a tag
        if (!stack.hasTagCompound()) {
            return;
        }

        // Ensure it has stored vis
        if (stack.getTagCompound().hasKey(TileArcaneAssembler.NBTKEY_STORED_VIS)) {
            // Add the info
            displayList.add(
                    EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString()
                            + ThEStrings.Tooltip_ArcaneAssemblerHasVis.getLocalized());
        }
    }
}
