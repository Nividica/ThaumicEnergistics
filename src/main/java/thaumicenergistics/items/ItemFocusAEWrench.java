package thaumicenergistics.items;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.network.packet.client.PacketAreaParticleFX;
import thaumicenergistics.network.packet.server.PacketServerWrenchFocus;
import thaumicenergistics.registries.FeatureRegistry;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.util.ThEUtils;
import appeng.api.AEApi;
import appeng.api.parts.IPartHost;
import appeng.core.CommonHelper;
import appeng.parts.PartPlacement;
import appeng.parts.PartPlacement.PlaceType;

public class ItemFocusAEWrench
	extends ItemFocusBasic
{

	/**
	 * Wrench used for dismantling.
	 */
	private static ItemStack psuedoWrench = null;

	/**
	 * How much vis is cost to use the focus.
	 */
	private final static AspectList castCost = new AspectList();

	public ItemFocusAEWrench()
	{
		// Set the casting cost
		ItemFocusAEWrench.castCost.add( Aspect.FIRE, 10 );
		ItemFocusAEWrench.castCost.add( Aspect.AIR, 10 );
	}

	/**
	 * Consumes the casting cost of the focus from he wand and spawns the
	 * activation beam.
	 * 
	 * @param wand
	 * @param wandStack
	 * @param player
	 * @param beamX
	 * @param beamY
	 * @param beamZ
	 */
	private static void consumeVisAndSpawnBeam( final ItemWandCasting wand, final ItemStack wandStack, final EntityPlayer player, final double beamX,
												final double beamY, final double beamZ )
	{
		// Use vis
		wand.consumeAllVis( wandStack, player, ItemFocusAEWrench.castCost, true, false );

		// Spawn beam
		new PacketAreaParticleFX().createWrenchFX( player.worldObj, player.posX, player.posY, player.posZ, beamX, beamY, beamZ, Aspect.ENERGY )
						.sendToAllAround( 20 );
	}

	private static ItemStack getWrench()
	{
		// Has the wrench already be initialized, and can it be?
		if( ( ItemFocusAEWrench.psuedoWrench == null ) && FeatureRegistry.instance().featureWrenchFocus.isAvailable() )
		{
			ItemFocusAEWrench.psuedoWrench = AEApi.instance().definitions().items().certusQuartzWrench().maybeStack( 1 ).orNull();
		}

		return ItemFocusAEWrench.psuedoWrench;
	}

	/**
	 * Returns the wand if it is valid and contains enough charge to perform a
	 * cast.
	 * 
	 * @param stack
	 * @param player
	 * @return
	 */
	private static ItemWandCasting wandIfValid( final ItemStack stack, final EntityPlayer player )
	{
		// Ensure it is a wand or staff
		if( !ThEUtils.isItemValidWand( stack, true ) )
		{
			return null;
		}

		// Get the wand
		ItemWandCasting wand = (ItemWandCasting)stack.getItem();

		// Ensure there is enough vis for the cast
		if( !wand.consumeAllVis( stack, player, ItemFocusAEWrench.castCost, false, false ) )
		{
			// Not enough vis
			return null;
		}

		// Wand is good and is charged
		return wand;
	}

	/**
	 * Called after the client has sent the request to the server.
	 * Because where your eyes are matters a great deal.
	 * 
	 * @param player
	 * @param eyeHeight
	 * @param x
	 * @param y
	 * @param z
	 * @param side
	 */
	public static void performDismantleOnPartHost( final EntityPlayer player, final float eyeHeight, final MovingObjectPosition position )
	{
		// Get the wrench
		ItemStack wrench;

		// Ensure there is a wrench
		if( ( wrench = ItemFocusAEWrench.getWrench() ) == null )
		{
			return;
		}

		// Get the item the player is holding
		ItemStack heldItem = player.getCurrentEquippedItem();

		// Ensure it is a charged wand
		ItemWandCasting wand;
		if( ( wand = ItemFocusAEWrench.wandIfValid( heldItem, player ) ) == null )
		{
			return;
		}

		// Update the AE render mode, in-case they are hiding facades
		CommonHelper.proxy.updateRenderMode( player );

		// Set the eye height
		PartPlacement.eyeHeight = eyeHeight;

		// Wrench the target
		PartPlacement.place( wrench, position.blockX, position.blockY, position.blockZ, position.sideHit, player, player.worldObj,
			PlaceType.INTERACT_FIRST_PASS, 0 );

		// Reset the AE render mode
		CommonHelper.proxy.updateRenderMode( null );

		// Take vis and show beam
		ItemFocusAEWrench.consumeVisAndSpawnBeam( wand, heldItem, player, position.blockX, position.blockY, position.blockZ );
	}

	/**
	 * Attempts to use the focus.
	 * Position must contain a block hit.
	 * 
	 * @param world
	 * @param player
	 * @param position
	 * @param wandStack
	 * @param action
	 * @return
	 */
	private boolean onUse( final World world, final EntityPlayer player, final MovingObjectPosition position, final ItemStack wandStack,
							final Action action )
	{
		// For all current actions the player must be sneaking
		if( !player.isSneaking() )
		{
			return false;
		}

		// Get the wand
		ItemWandCasting wand;
		if( ( wand = ItemFocusAEWrench.wandIfValid( wandStack, player ) ) == null )
		{
			// Wand does not have enough charge.
			return false;
		}

		// Ensure the player is allowed to modify the block
		if( !world.canMineBlock( player, position.blockX, position.blockY, position.blockZ ) )
		{
			// Player can not modify block
			return false;
		}

		// Is the block an part host?
		boolean isPartHost = ( world.getTileEntity( position.blockX, position.blockY, position.blockZ ) instanceof IPartHost );

		// Was a part host right clicked?
		if( isPartHost && ( action == Action.RIGHT_CLICK_BLOCK ) )
		{
			// Send packet if client side
			if( world.isRemote )
			{
				new PacketServerWrenchFocus().createWrenchFocusRequest( player, position ).sendPacketToServer();
			}

			return true;
		}

		// All further actions ignored on client side.
		if( ( world.isRemote ) || !( player instanceof EntityPlayerMP ) )
		{
			return true;
		}

		// Was a non-part host left clicked?
		if( !isPartHost && ( action == Action.LEFT_CLICK_BLOCK ) )
		{
			// Get the block
			Block block = world.getBlock( position.blockX, position.blockY, position.blockZ );

			// Attempt to rotate the block
			if( block.rotateBlock( world, position.blockX, position.blockY, position.blockZ, ForgeDirection.getOrientation( position.sideHit ) ) )
			{
				// Take vis and show beam
				ItemFocusAEWrench.consumeVisAndSpawnBeam( wand, wandStack, player, position.blockX, position.blockY, position.blockZ );

				// Fire an update
				block.onNeighborBlockChange( world, position.blockX, position.blockY, position.blockZ, Blocks.air );
			}

			return true;
		}

		// Save what the player is holding
		ItemStack prevHolding = player.getCurrentEquippedItem();

		// Set to true if the focus did something.
		boolean handled = false;

		try
		{
			// Set the wrench as what the player is holding
			player.setCurrentItemOrArmor( 0, ItemFocusAEWrench.getWrench() );

			// The sneak state of the player depends on what was clicked

			// Was a part host left clicked?
			if( isPartHost && ( action == Action.LEFT_CLICK_BLOCK ) )
			{
				// Set the player as not sneaking
				player.setSneaking( false );
			}

			// Was a non-part host right clicked?
			//if( !isPartHost && ( action == Action.RIGHT_CLICK_BLOCK ) )
			//{
			// Ensure the player is sneaking
			//player.setSneaking( true );
			//}

			// Calculate the offsets
			float xOffset = (float)position.hitVec.xCoord - position.blockX;
			float yOffset = (float)position.hitVec.yCoord - position.blockY;
			float zOffset = (float)position.hitVec.zCoord - position.blockZ;

			// Activate
			handled = ( (EntityPlayerMP)player ).theItemInWorldManager.activateBlockOrUseItem( player, world, ItemFocusAEWrench.getWrench(),
				position.blockX, position.blockY, position.blockZ, position.sideHit, xOffset, yOffset, zOffset );

			// Was it handled or is the block gone?
			if( handled || ( world.getBlock( position.blockX, position.blockY, position.blockZ ) == Blocks.air ) )
			{
				// Take vis and show beam
				ItemFocusAEWrench.consumeVisAndSpawnBeam( wand, wandStack, player, position.blockX, position.blockY, position.blockZ );
			}
		}

		finally
		{
			// Restore what the player was holding and sneak state
			player.setCurrentItemOrArmor( 0, prevHolding );
			player.setSneaking( true );
		}

		return handled;
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
		// Is the focus enabled?
		if( FeatureRegistry.instance().featureWrenchFocus.isAvailable() )
		{
			// Wrench enabled
			return ThEStrings.Item_FocusAEWrench.getUnlocalized();
		}

		// Wrench disabled
		return ThEStrings.Item_FocusAEWrench_Disabled.getUnlocalized();
	}

	@Override
	public String getUnlocalizedName( final ItemStack itemStack )
	{
		return this.getUnlocalizedName();
	}

	@Override
	public AspectList getVisCost( final ItemStack itemStack )
	{

		return ItemFocusAEWrench.castCost;
	}

	/**
	 * Called when the player left-clicks
	 */
	@Override
	public boolean onEntitySwing( final EntityLivingBase entity, final ItemStack wandStack )
	{
		// Is the entity a player?
		if( entity instanceof EntityPlayer )
		{
			// Cast to player
			EntityPlayer player = (EntityPlayer)entity;

			// Ray trace
			MovingObjectPosition position = this.getMovingObjectPositionFromPlayer( player.worldObj, player, true );

			// Was a block hit?
			if( ( position != null ) && ( position.typeOfHit == MovingObjectType.BLOCK ) )
			{
				// Use the focus
				return this.onUse( player.worldObj, player, position, wandStack, Action.LEFT_CLICK_BLOCK );
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

		// Was a block hit?
		if( ( position != null ) && ( position.typeOfHit == MovingObjectType.BLOCK ) )
		{
			// Use the focus
			this.onUse( world, player, position, wandStack, Action.RIGHT_CLICK_BLOCK );
		}

		return wandStack;
	}

	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		this.icon = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":foci.aewrench" );
	}

}
