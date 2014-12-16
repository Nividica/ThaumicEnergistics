package thaumicenergistics.parts;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaExportBus
	extends AEPartEssentiaIO
{

	public AEPartEssentiaExportBus()
	{
		super( AEPartsEnum.EssentiaExportBus );
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

	@Override
	public boolean doWork( final int transferAmount )
	{
		if( this.facingContainer != null )
		{
			// Attempt to extract essentia from the network
			return this.extractEssentiaFromNetwork( transferAmount );
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

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon busSideTexture = BlockTextureManager.BUS_SIDE.getTexture();

		// Set the texture to the side texture
		helper.setTexture( busSideTexture );

		//Large Chamber back wall
		helper.setBounds( 4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.5F );
		helper.renderInventoryBox( renderer );

		// Set the texture to the chamber
		helper.setTexture( BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[2] );

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

		IIcon busSideTexture = BlockTextureManager.BUS_SIDE.getTexture();

		// Set the texture to the side texture
		helper.setTexture( busSideTexture );

		//Large Chamber back wall
		helper.setBounds( 4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.5F );
		helper.renderBlock( x, y, z, renderer );

		// Set to alpha pass
		helper.renderForPass( 1 );

		// Set the texture to the chamber
		helper.setTexture( BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[2] );

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

}
