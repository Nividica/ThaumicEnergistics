package thaumicenergistics.parts;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.TileJarFillableVoid;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.network.packet.client.PacketClientEssentiaIOBus;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaExportBus
	extends AEPartEssentiaIO
{

	private static final String NBT_KEY_VOID = "IsVoidAllowed";

	/**
	 * If true, excess essentia will be voided when facing a void jar.
	 */
	private boolean isVoidAllowed = false;

	public AEPartEssentiaExportBus()
	{
		super( AEPartsEnum.EssentiaExportBus );
	}

	/**
	 * Sends the state of the void mode to the specified player.
	 * 
	 * @param player
	 */
	private void sendVoidModeToClient( final EntityPlayer player )
	{
		new PacketClientEssentiaIOBus().createVoidModeUpdate( player, this.isVoidAllowed ).sendPacketToPlayer();
	}

	/**
	 * Checks if the specified player can open the gui.
	 */
	@Override
	protected boolean canPlayerOpenGui( final int playerID )
	{
		// Does the player have export permissions
		return this.doesPlayerHaveSecurityClearance( playerID, SecurityPermissions.EXTRACT );
	}

	@Override
	public boolean aspectTransferAllowed( final Aspect aspect )
	{
		return true;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	/**
	 * Attempts to transfer essentia out of the network and into the adjacent
	 * container.
	 */
	@Override
	public boolean doWork( final int amountToFillContainer )
	{
		// Ensure we have a container.
		if( this.facingContainer == null )
		{
			// Invalid container
			return false;
		}

		// Loop over all aspect filters
		for( Aspect filterAspect : this.filteredAspects )
		{
			// Can we transfer?
			if( filterAspect == null )
			{
				// Invalid or not allowed
				continue;
			}

			// Can we inject any of this into the container
			if( EssentiaTileContainerHelper.instance.injectIntoContainer( this.facingContainer, 1, filterAspect, Actionable.SIMULATE ) < 1 )
			{
				if( !( ( this.isVoidAllowed ) && ( EssentiaTileContainerHelper.instance.getAspectInContainer( this.facingContainer ) == filterAspect ) ) )
				{
					// Container will not accept any of this, and cannot void(essentia type wrong)
					continue;
				}
			}

			// Get the gas form of the essentia
			GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( filterAspect );

			// Is there a fluid form of the aspect?
			if( essentiaGas == null )
			{
				continue;
			}

			// Create the fluid stack
			IAEFluidStack toExtract = EssentiaConversionHelper.instance.createAEFluidStackInEssentiaUnits( essentiaGas, amountToFillContainer );

			// Simulate a network extraction
			IAEFluidStack extractedStack = this.extractFluid( toExtract, Actionable.SIMULATE );

			// Were we able to extract any?
			if( ( extractedStack == null ) || ( extractedStack.getStackSize() <= 0 ) )
			{
				// Unable to extract from network
				continue;
			}

			int filledAmountFU, filledAmountEU;
			if( this.isVoidAllowed && ( this.facingContainer instanceof TileJarFillableVoid ) )
			{
				// In void mode, we don't care if the jar can hold it or not.
				filledAmountFU = (int)extractedStack.getStackSize();
			}
			else
			{

				// Simulate filling the container
				filledAmountFU = (int)EssentiaTileContainerHelper.instance.injectIntoContainer( this.facingContainer, extractedStack,
					Actionable.SIMULATE );
			}

			// Were we able to fill the container?
			if( filledAmountFU == 0 )
			{
				continue;
			}

			// Do we have the power to transfer this amount?
			filledAmountEU = (int)EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( filledAmountFU );
			if( !this.takePowerFromNetwork( filledAmountEU, Actionable.SIMULATE ) )
			{
				// Not enough power, abort
				return false;
			}

			// Fill the container
			EssentiaTileContainerHelper.instance.injectIntoContainer( this.facingContainer, extractedStack, Actionable.MODULATE );

			// Take the power required for the filled amount
			this.takePowerFromNetwork( filledAmountEU, Actionable.MODULATE );

			// Take essentia from the network
			this.extractFluid( EssentiaConversionHelper.instance.createAEFluidStackInFluidUnits( essentiaGas, filledAmountFU ), Actionable.MODULATE );

			// Done
			return true;
		}

		return false;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		// Large chamber and back wall
		helper.addBox( 4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 13.5F );

		// Small chamber and front wall
		helper.addBox( 5.0F, 5.0F, 13.5F, 11.0F, 11.0F, 15.0F );

		// Face
		helper.addBox( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[2];
	}

	/**
	 * Called when a player has requested to change the void mode
	 */
	public void onClientRequestChangeVoidMode( final EntityPlayer player )
	{
		// Swap void modes
		this.isVoidAllowed = !this.isVoidAllowed;

		// Send reply back
		this.sendVoidModeToClient( player );
	}

	@Override
	public void onClientRequestFullUpdate( final EntityPlayer player )
	{
		// Call super
		super.onClientRequestFullUpdate( player );

		// Send void mode
		this.sendVoidModeToClient( player );
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read void
		if( data.hasKey( AEPartEssentiaExportBus.NBT_KEY_VOID ) )
		{
			this.isVoidAllowed = data.getBoolean( AEPartEssentiaExportBus.NBT_KEY_VOID );
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon busSideTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[3];

		// Set the texture to the side texture
		helper.setTexture( busSideTexture );

		//Large Chamber back wall
		helper.setBounds( 4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.5F );
		helper.renderInventoryBox( renderer );

		// Set the texture to the chamber
		helper.setTexture( BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[2] );

		// Large chamber
		helper.setBounds( 4.0F, 4.0F, 12.5F, 12.0F, 12.0F, 13.5F );
		helper.renderInventoryBox( renderer );

		// Small chamber
		helper.setBounds( 5.0F, 5.0F, 13.5F, 11.0F, 11.0F, 14.5F );
		helper.renderInventoryBox( renderer );

		// Set the texture back to the side texture
		helper.setTexture( busSideTexture );

		// Small chamber front wall
		helper.setBounds( 5.0F, 5.0F, 14.5F, 11.0F, 11.0F, 15.0F );
		helper.renderInventoryBox( renderer );

		// Setup the face texture
		helper.setTexture( busSideTexture, busSideTexture, busSideTexture, BlockTextureManager.ESSENTIA_EXPORT_BUS.getTexture(), busSideTexture,
			busSideTexture );

		// Face
		helper.setBounds( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		// Face overlay
		helper.setInvColor( AbstractAEPartBase.INVENTORY_OVERLAY_COLOR );
		ts.setBrightness( 0xF000F0 );
		IIcon faceOverlayTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[1];
		helper.renderInventoryFace( faceOverlayTexture, ForgeDirection.UP, renderer );
		helper.renderInventoryFace( faceOverlayTexture, ForgeDirection.DOWN, renderer );
		helper.renderInventoryFace( faceOverlayTexture, ForgeDirection.EAST, renderer );
		helper.renderInventoryFace( faceOverlayTexture, ForgeDirection.WEST, renderer );

		// Lights
		helper.setBounds( 6.0F, 6.0F, 11.0F, 10.0F, 10.0F, 12.0F );
		this.renderInventoryBusLights( helper, renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon busSideTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[3];

		// Set the texture to the side texture
		helper.setTexture( busSideTexture );

		//Large Chamber back wall
		helper.setBounds( 4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.5F );
		helper.renderBlock( x, y, z, renderer );

		// Set to alpha pass
		helper.renderForPass( 1 );

		// Set the texture to the chamber
		helper.setTexture( BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[2] );

		// Large chamber
		helper.setBounds( 4.0F, 4.0F, 12.5F, 12.0F, 12.0F, 13.5F );
		helper.renderBlock( x, y, z, renderer );

		// Small chamber
		helper.setBounds( 5.0F, 5.0F, 13.5F, 11.0F, 11.0F, 14.5F );
		helper.renderBlock( x, y, z, renderer );

		// Set back to opaque pass
		helper.renderForPass( 0 );

		// Set the texture back to the side texture
		helper.setTexture( busSideTexture );

		// Small chamber front wall
		helper.setBounds( 5.0F, 5.0F, 14.5F, 11.0F, 11.0F, 15.0F );
		helper.renderBlock( x, y, z, renderer );

		// Setup the face texture	
		helper.setTexture( busSideTexture, busSideTexture, busSideTexture, BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[0], busSideTexture,
			busSideTexture );

		// Face
		helper.setBounds( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		// Face overlay
		ts.setColorOpaque_I( this.host.getColor().blackVariant );

		if( this.isActive() )
		{
			Tessellator.instance.setBrightness( AbstractAEPartBase.ACTIVE_BRIGHTNESS );
		}

		IIcon faceOverlayTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[1];
		helper.renderFace( x, y, z, faceOverlayTexture, ForgeDirection.UP, renderer );
		helper.renderFace( x, y, z, faceOverlayTexture, ForgeDirection.DOWN, renderer );
		helper.renderFace( x, y, z, faceOverlayTexture, ForgeDirection.EAST, renderer );
		helper.renderFace( x, y, z, faceOverlayTexture, ForgeDirection.WEST, renderer );

		// Lights
		helper.setBounds( 6.0F, 6.0F, 11.0F, 10.0F, 10.0F, 12.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Call super
		super.writeToNBT( data );

		// Write void
		data.setBoolean( AEPartEssentiaExportBus.NBT_KEY_VOID, this.isVoidAllowed );
	}

}
