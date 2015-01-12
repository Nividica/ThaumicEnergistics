package thaumicenergistics.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.implementations.items.IAEWrench;

public abstract class AbstractBlockAEWrenchable
	extends BlockContainer
{

	protected AbstractBlockAEWrenchable( final Material material )
	{
		super( material );
	}

	/**
	 * Called when the block is being removed via AE wrench.
	 */
	protected void onDismantled( final World world, final int x, final int y, final int z )
	{
	}

	/**
	 * Called when the block has been hit with an AE wrench.
	 * 
	 * @return True if handled, false otherwise.
	 */
	protected boolean onWrenched( final World world, final int x, final int y, final int z, final int side )
	{
		return false;
	}

	/**
	 * Returning false will cancel onBlockActivated.
	 * 
	 * @return
	 */
	public boolean canPlayerInteract( final EntityPlayer player )
	{
		return true;
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
	public boolean onBlockActivated( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		return false;
	}

	/**
	 * The block was right-clicked
	 */
	@Override
	public final boolean onBlockActivated( final World world, final int x, final int y, final int z, final EntityPlayer player, final int side,
											final float hitX, final float hitY, final float hitZ )
	{
		// Can the player interact with the block?
		if( !this.canPlayerInteract( player ) )
		{
			return false;
		}

		// Is the player holding a anything?
		if( player.getHeldItem() != null )
		{
			// Is the player holding an AE wrench item?
			if( player.getHeldItem().getItem() instanceof IAEWrench )
			{
				// Is the player sneaking?
				if( player.isSneaking() )
				{
					// Call on wrench
					this.onDismantled( world, x, y, z );

					// Call break
					this.breakBlock( world, x, y, z, this, world.getBlockMetadata( x, y, z ) );

					// Drop the block
					this.dropBlockAsItem( world, x, y, z, new ItemStack( this ) );

					// Set the block to air
					world.setBlockToAir( x, y, z );

					return true;
				}

				// Let the subclass handle the event
				return this.onWrenched( world, x, y, z, side );
			}
		}

		return this.onBlockActivated( world, x, y, z, player );

	}

}
