package thaumicenergistics.common.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.util.Platform;

/**
 * Base class for wrenchable blocks.
 *
 * @author Nividica
 *
 */
public abstract class AbstractBlockAEWrenchable extends BlockContainer {

    protected AbstractBlockAEWrenchable(final Material material) {
        super(material);
    }

    /**
     * Called when the block is right-clicked
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param player
     * @return
     */
    protected boolean onBlockActivated(final World world, final int x, final int y, final int z,
            final EntityPlayer player) {
        return false;
    }

    /**
     * Called when the block is being removed via AE wrench. Return an itemstack that represents the block. Can be null.
     */
    protected ItemStack onDismantled(final World world, final int x, final int y, final int z) {
        return null;
    }

    /**
     * Returning false will cancel onBlockActivated.
     *
     * @return
     */
    public boolean canPlayerInteract(final EntityPlayer player) {
        return true;
    }

    /**
     * The block was right-clicked
     */
    @Override
    public final boolean onBlockActivated(final World world, final int x, final int y, final int z,
            final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ) {
        // Can the player interact with the block?
        if (!this.canPlayerInteract(player)) {
            return false;
        }

        // Is the player holding an AE wrench item?
        if (Platform.isWrench(player, player.getHeldItem(), x, y, z)) {
            // Is the player sneaking?
            if (player.isSneaking()) {
                // Call on dismantled
                ItemStack representitive = this.onDismantled(world, x, y, z);

                // Call break
                this.breakBlock(world, x, y, z, this, world.getBlockMetadata(x, y, z));

                // Is there an representative itemstack?
                if (representitive == null) {
                    // Create a basic stack.
                    representitive = new ItemStack(this);
                }

                // Drop the itemstack
                this.dropBlockAsItem(world, x, y, z, representitive);

                // Set the block to air
                world.setBlockToAir(x, y, z);
            }

            return true;
        }

        return this.onBlockActivated(world, x, y, z, player);
    }
}
