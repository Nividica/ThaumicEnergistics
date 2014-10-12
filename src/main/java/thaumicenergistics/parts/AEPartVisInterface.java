package thaumicenergistics.parts;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.TileVisRelay;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.VisInterfaceData;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;

public class AEPartVisInterface
	extends AbstractAEPartBase
{
	/**
	 * NBT key for the unique ID
	 */
	private static final String NBT_KEY_UID = "uid";

	/**
	 * Unique ID for this interface
	 */
	private long UID = 0;

	/**
	 * Creates the interface.
	 */
	public AEPartVisInterface()
	{
		super( AEPartsEnum.VisInterface );

		this.UID = System.currentTimeMillis() ^ this.hashCode();
	}

	/**
	 * No gui.
	 */
	@Override
	protected boolean canPlayerOpenGui( final int playerID )
	{
		return false;
	}

	/**
	 * How far to extend the cable.
	 */
	@Override
	public int cableConnectionRenderTo()
	{
		return 2;
	}

	/**
	 * Hit boxes.
	 */
	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		// Face
		helper.addBox( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );

		// Mid
		helper.addBox( 4.0D, 4.0D, 14.0D, 12.0D, 12.0D, 15.0D );

		// Back
		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );

	}

	/**
	 * No idle power usage.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return 0;
	}

	/**
	 * Gets the unique ID for this interface
	 * 
	 * @return
	 */
	public long getUID()
	{
		return this.UID;
	}

	/**
	 * Player right-clicked the interface.
	 */
	@Override
	public boolean onActivate( final EntityPlayer player, final Vec3 position )
	{
		return false;
	}

	/**
	 * Player shift-right-clicked the interface.
	 */
	@Override
	public boolean onShiftActivate( final EntityPlayer player, final Vec3 position )
	{
		// Get what the player is holding
		ItemStack playerHolding = player.inventory.getCurrentItem();

		// Are they holding a memory card?
		if( ( playerHolding != null ) && ( playerHolding.getItem() instanceof IMemoryCard ) )
		{
			// Get the memory card
			IMemoryCard memoryCard = (IMemoryCard)playerHolding.getItem();

			// Create the info data
			VisInterfaceData data = new VisInterfaceData( this );

			// Write into the memory card
			memoryCard.setMemoryCardContents( playerHolding, AEPartsEnum.VisInterface.getUnlocalizedName(), data.writeToNBT() );

			// Notify the user
			memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );

			return true;
		}

		return false;
	}

	/**
	 * Reads the interface data from the tag
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Does it contain the UID?
		if( data.hasKey( AEPartVisInterface.NBT_KEY_UID ) )
		{
			// Read the UID
			this.UID = data.getLong( AEPartVisInterface.NBT_KEY_UID );
		}
	}

	/**
	 * Draws the interface in the inventory.
	 */
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.BUS_SIDE.getTexture();
		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[0], side, side );

		// Face
		helper.setBounds( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		// Mid
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderInventoryBox( renderer );

		// Color overlay
		helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
		helper.setInvColor( AbstractAEPartBase.INVENTORY_OVERLAY_COLOR );
		ts.setBrightness( 0xF000F0 );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[1], ForgeDirection.SOUTH, renderer );

		// Back
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderInventoryBusLights( helper, renderer );

	}

	/**
	 * Draws the interface in the world.
	 */
	@Override
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator tessellator = Tessellator.instance;

		IIcon side = BlockTextureManager.BUS_SIDE.getTexture();
		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTexture(), side, side );

		// Front (facing relay)
		helper.setBounds( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		tessellator.setColorOpaque_I( this.host.getColor().blackVariant );

		if( this.isActive() )
		{
			tessellator.setBrightness( AbstractAEPartBase.ACTIVE_BRIGHTNESS );
		}

		// Mid
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[1], ForgeDirection.SOUTH, renderer );
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderBlock( x, y, z, renderer );

		// Back (facing bus)
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );
	}

	/**
	 * Requests that the interface drain vis from the relay
	 * 
	 * @param vis
	 * @param amount
	 * @return
	 */
	public int requestDrain( final Aspect vis, final int amount )
	{
		// Get the tile we are facing
		TileEntity facingTile = this.getFacingTile();

		// Is it a relay?
		if( facingTile instanceof TileVisRelay )
		{
			// Get the relay
			TileVisRelay facingRelay = (TileVisRelay)facingTile;

			// Is it facing the same direction as we are?
			if( facingRelay.orientation == this.getSide().ordinal() )
			{
				// Ask it for vis
				return facingRelay.consumeVis( vis, amount );
			}
		}

		// Nothing to drain
		return 0;
	}

	/**
	 * Write the interface data to the tag
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Call super
		super.writeToNBT( data );

		// Write the UID
		data.setLong( AEPartVisInterface.NBT_KEY_UID, this.UID );
	}

}
