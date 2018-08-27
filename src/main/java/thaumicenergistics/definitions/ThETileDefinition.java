package thaumicenergistics.definitions;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import appeng.api.definitions.ITileDefinition;

/**
 * @author BrockWS
 */
public class ThETileDefinition extends ThEBlockDefinition implements ITileDefinition {

    private Class<? extends TileEntity> tile;

    public ThETileDefinition(Class<? extends TileEntity> tile, Block block, Item item) {
        super(block, item);
        this.tile = tile;
    }

    @Override
    public Optional<? extends Class<? extends TileEntity>> maybeEntity() {
        return Optional.of(this.tile);
    }
}
