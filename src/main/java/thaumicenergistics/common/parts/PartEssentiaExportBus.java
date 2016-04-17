package thaumicenergistics.common.parts;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileJarFillableVoid;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.integration.tc.EssentiaTileContainerHelper;

/**
 * Exports essentia into {@link IAspectContainer}
 *
 * @author Nividica
 *
 */
public class PartEssentiaExportBus
	extends ThEPartEssentiaIOBus_Base
{

	private static final String NBT_KEY_VOID = "IsVoidAllowed";

	/**
	 * If true, excess essentia will be voided when facing a void jar.
	 */
	private boolean isVoidAllowed = false;

	public PartEssentiaExportBus()
	{
		super( AEPartsEnum.EssentiaExportBus, SecurityPermissions.EXTRACT );
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
			if( EssentiaTileContainerHelper.INSTANCE.injectEssentiaIntoContainer( this.facingContainer, 1, filterAspect, Actionable.SIMULATE ) <= 0 )
			{
				if( !( ( this.isVoidAllowed ) &&
								( EssentiaTileContainerHelper.INSTANCE.getAspectInContainer( this.facingContainer ) == filterAspect ) ) )
				{
					// Container will not accept any of this, and cannot void(essentia type wrong)
					continue;
				}
			}

			// Get the monitor
			IMEEssentiaMonitor essMonitor = this.getGridBlock().getEssentiaMonitor();
			if( essMonitor == null )
			{
				return false;
			}

			// Simulate a network extraction
			long extractedAmount = essMonitor.extractEssentia( filterAspect, amountToFillContainer, Actionable.SIMULATE, this.asMachineSource, true );

			// Was any extracted?
			if( extractedAmount <= 0 )
			{
				// Unable to extract from network
				continue;
			}

			long filledAmount = 0;
			if( this.isVoidAllowed && ( this.facingContainer instanceof TileJarFillableVoid ) )
			{
				// In void mode, we don't care if the jar can hold it or not.
				filledAmount = extractedAmount;
			}
			else
			{
				// Simulate filling the container
				filledAmount = EssentiaTileContainerHelper.INSTANCE.injectEssentiaIntoContainer( this.facingContainer, (int)extractedAmount,
					filterAspect, Actionable.SIMULATE );
			}

			// Was the container filled?
			if( filledAmount <= 0 )
			{
				// Unable to inject into container
				continue;
			}

			// Fill the container
			long actualFilledAmount = EssentiaTileContainerHelper.INSTANCE.injectEssentiaIntoContainer( this.facingContainer, (int)filledAmount,
				filterAspect, Actionable.MODULATE );

			// Is voiding not allowed?
			if( !this.isVoidAllowed )
			{
				filledAmount = actualFilledAmount;
			}

			// Take essentia from the network
			essMonitor.extractEssentia( filterAspect, filledAmount, Actionable.MODULATE, this.asMachineSource, true );

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
	 * Returns if voiding is allowed.
	 *
	 * @return
	 */
	@Override
	public boolean isVoidAllowed()
	{
		return this.isVoidAllowed;
	}

	@Override
	public void onClientRequestFilterList( final EntityPlayer player )
	{
		// Call super
		super.onClientRequestFilterList( player );
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read void
		if( data.hasKey( PartEssentiaExportBus.NBT_KEY_VOID ) )
		{
			this.isVoidAllowed = data.getBoolean( PartEssentiaExportBus.NBT_KEY_VOID );
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
		helper.setInvColor( ThEPartBase.INVENTORY_OVERLAY_COLOR );
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
		ts.setColorOpaque_I( this.getHost().getColor().blackVariant );

		if( this.isActive() )
		{
			Tessellator.instance.setBrightness( ThEPartBase.ACTIVE_FACE_BRIGHTNESS );
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

	/**
	 * Called when a player has requested to change the void mode
	 */
	public void toggleVoidMode( final EntityPlayer player )
	{
		// Swap void modes
		this.isVoidAllowed = !this.isVoidAllowed;
	}

	@Override
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		// Call super
		super.writeToNBT( data, saveType );

		boolean doSave = ( saveType == PartItemStack.World );
		if( !doSave )
		{
			// Are there any filters?
			for( Aspect aspect : this.filteredAspects )
			{
				if( aspect != null )
				{
					// Only save the void state if filters are set.
					doSave = true;
					break;
				}
			}
		}

		// Write void
		if( doSave && this.isVoidAllowed )
		{
			data.setBoolean( PartEssentiaExportBus.NBT_KEY_VOID, this.isVoidAllowed );
		}
	}

}
