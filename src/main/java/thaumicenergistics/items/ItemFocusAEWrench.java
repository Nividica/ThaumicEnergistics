package thaumicenergistics.items;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.network.packet.client.PacketAreaParticleFX;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.AEApi;
import appeng.api.parts.IPartHost;
import appeng.parts.PartPlacement;
import appeng.parts.PartPlacement.PlaceType;

public class ItemFocusAEWrench
	extends ItemFocusBasic
{

	/**
	 * Wrench used for dismantling.
	 */
	private ItemStack psuedoWrench = null;

	/**
	 * How much vis is cost to use the focus.
	 */
	private final AspectList castCost = new AspectList();

	public ItemFocusAEWrench()
	{
		this.castCost.add( Aspect.FIRE, 10 );
		this.castCost.add( Aspect.AIR, 10 );
	}

	private void activateWrenchLeftClick( final World world, final int x, final int y, final int z, final EntityPlayer player, final int side,
											final ItemStack wandStack )
	{
		// Is this server side?
		if( world.isRemote )
		{
			// Ignored client side
			return;
		}
		// Get the block
		Block block = world.getBlock( x, y, z );

		// Ensure the block is valid
		if( ( block == null ) || ( block == Blocks.air ) )
		{
			// Invalid block
			return;
		}

		// Ensure the player is allowed to modify the block
		if( !world.canMineBlock( player, x, y, z ) )
		{
			// Player can not modify block
			return;
		}

		// Validate wand
		if( ( wandStack == null ) || !( wandStack.getItem() instanceof ItemWandCasting ) )
		{
			// Invalid wand
			return;
		}

		// Get the wand
		ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();

		// Ensure there is enough vis for the cast
		if( !wand.consumeAllVis( wandStack, player, this.castCost, false, false ) )
		{
			// Not enough vis
			return;
		}

		// Attempt to rotate the block
		if( block.rotateBlock( world, x, y, z, ForgeDirection.getOrientation( side ) ) )
		{
			// Spawn beam
			new PacketAreaParticleFX().createWrenchFX( world, player.posX, player.posY, player.posZ, x, y, z, Aspect.ENERGY ).sendToAllAround( 20 );

			// Consume vis
			wand.consumeAllVis( wandStack, player, this.castCost, true, false );

			// Fire an update
			block.onNeighborBlockChange( world, x, y, z, Blocks.air );
		}

	}

	private void activateWrenchRightClick( final World world, final EntityPlayer player, final MovingObjectPosition position,
											final ItemStack wandStack )
	{
		// Is this server side?
		if( world.isRemote )
		{
			// Ignored client side
			return;
		}

		// Get the block that was clicked
		Block block = world.getBlock( position.blockX, position.blockY, position.blockZ );

		// Ensure the block is valid
		if( ( block == null ) || ( block == Blocks.air ) )
		{
			// Invalid block
			return;
		}

		// Validate wand
		if( ( wandStack == null ) || !( wandStack.getItem() instanceof ItemWandCasting ) )
		{
			// Invalid wand
			return;
		}

		// Get the wand
		ItemWandCasting wand = (ItemWandCasting)wandStack.getItem();

		// Ensure there is enough vis for the cast
		if( !wand.consumeAllVis( wandStack, player, this.castCost, false, false ) )
		{
			// Not enough vis
			return;
		}

		// Get the index of the players selected hotbar slot
		int heldIndex = player.inventory.currentItem;

		// Save what the player is holding
		ItemStack prevHolding = player.inventory.mainInventory[heldIndex];

		// Set the wrench as what the player is holding
		player.inventory.mainInventory[heldIndex] = this.getWrench();

		try
		{
			boolean didWrench = false;

			// Is there a part host?
			TileEntity tile = world.getTileEntity( position.blockX, position.blockY, position.blockZ );
			if( tile instanceof IPartHost )
			{
				didWrench = PartPlacement.place( this.getWrench(), position.blockX, position.blockY, position.blockZ, position.sideHit, player,
					world, PlaceType.INTERACT_FIRST_PASS, 0 );
			}

			if( !didWrench )
			{
				// Call onActivate
				block.onBlockActivated( world, position.blockX, position.blockY, position.blockZ, player, position.sideHit,
					(float)position.hitVec.xCoord, (float)position.hitVec.yCoord, (float)position.hitVec.zCoord );

				// Is the block gone?
				didWrench = ( world.getBlock( position.blockX, position.blockY, position.blockZ ) == Blocks.air );
			}

			// Was something removed?
			if( didWrench )
			{
				// Spawn beam
				new PacketAreaParticleFX().createWrenchFX( world, player.posX, player.posY, player.posZ, position.blockX, position.blockY,
					position.blockZ, Aspect.ENERGY ).sendToAllAround( 20 );

				// Use vis
				wand.consumeAllVis( wandStack, player, this.castCost, true, false );
			}

		}
		finally
		{
			// Restore what the player was holding
			player.inventory.mainInventory[heldIndex] = prevHolding;
		}

	}

	private ItemStack getWrench()
	{
		if( this.psuedoWrench == null )
		{
			this.psuedoWrench = AEApi.instance().items().itemCertusQuartzWrench.stack( 1 );
		}

		return this.psuedoWrench;
	}

	@Override
	public int getActivationCooldown( final ItemStack focusstack )
	{
		// 250ms between uses
		return 250;
	}

	@Override
	public int getFocusColor( final ItemStack itemStack )
	{
		return Aspect.ENERGY.getColor();
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank( final ItemStack itemStack, final int rank )
	{
		return new FocusUpgradeType[0];
	}

	@Override
	public String getUnlocalizedName()
	{
		return ThEStrings.Item_FocusAEWrench.getUnlocalized();
	}

	@Override
	public String getUnlocalizedName( final ItemStack itemStack )
	{
		return ThEStrings.Item_FocusAEWrench.getUnlocalized();
	}

	@Override
	public AspectList getVisCost( final ItemStack itemStack )
	{

		return this.castCost;
	}

	/**
	 * Called when the player left-clicks
	 */
	@Override
	public boolean onEntitySwing( final EntityLivingBase entity, final ItemStack wandStack )
	{
		// Is the entity a player, and is the player sneaking?
		if( ( entity instanceof EntityPlayer ) && ( entity.isSneaking() ) )
		{
			// Ray trace
			MovingObjectPosition position = this.getMovingObjectPositionFromPlayer( entity.worldObj, (EntityPlayer)entity, false );

			// Was a block hit?
			if( ( position != null ) && ( position.typeOfHit == MovingObjectType.BLOCK ) )
			{
				// Activation ignored on client side
				if( EffectiveSide.isServerSide() )
				{
					// Use the wrench
					this.activateWrenchLeftClick( entity.worldObj, position.blockX, position.blockY, position.blockZ, (EntityPlayer)entity,
						position.sideHit, wandStack );
				}

				return true;
			}
		}

		return false;

	}

	/**
	 * Called when the player right-clicks
	 */
	@Override
	public ItemStack onFocusRightClick( final ItemStack wandStack, final World world, final EntityPlayer player, final MovingObjectPosition position )
	{
		// Is the player sneaking, and was a block clicked?
		if( ( player.isSneaking() ) && ( position != null ) && ( position.typeOfHit == MovingObjectType.BLOCK ) )
		{
			// Use the wrench
			this.activateWrenchRightClick( world, player, position, wandStack );
		}

		return wandStack;
	}

	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		this.icon = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":foci.aewrench" );
	}

}
