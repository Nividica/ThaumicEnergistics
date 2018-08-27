package thaumicenergistics.definitions;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.definitions.IBlockDefinition;

/**
 * @author BrockWS
 */
public class ThEBlockDefinition extends ThEItemDefinition implements IBlockDefinition {

    private Block block;

    public ThEBlockDefinition(Block block, Item item) {
        super(item);
        this.block = block;
    }

    @Override
    public Optional<Block> maybeBlock() {
        return Optional.of(this.block);
    }

    @Override
    public Optional<ItemBlock> maybeItemBlock() {
        return this.maybeBlock().map(ItemBlock::new);
    }

    @Override
    public boolean isSameAs(IBlockAccess world, BlockPos pos) {
        return this.maybeBlock().isPresent() && world.getBlockState(pos).getBlock() == this.maybeBlock().get();
    }
}
