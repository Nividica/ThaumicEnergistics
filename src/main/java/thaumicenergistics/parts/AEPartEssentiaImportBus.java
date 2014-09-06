package thaumicenergistics.parts;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaImportBus
	extends AEPartEssentiaIO
{

	public AEPartEssentiaImportBus()
	{
		super( AEPartsEnum.EssentiaImportBus );
	}

	/**
	 * Checks if the specified player can open the gui.
	 */
	@Override
	protected boolean canPlayerOpenGui( final int playerID )
	{
		// Does the player have import permissions
		return this.doesPlayerHaveSecurityClearance( playerID, SecurityPermissions.INJECT );
	}

	@Override
	public boolean aspectTransferAllowed( final Aspect aspect )
	{
		boolean noFilters = true;

		if( aspect != null )
		{
			for( Aspect filterAspect : this.filteredAspects )
			{
				// Is the aspect not null?
				if( filterAspect != null )
				{
					// Does it match this aspect?
					if( aspect == filterAspect )
					{
						// Found a match, return true
						return true;
					}

					// Mark that there are filtered aspects
					noFilters = false;
				}
			}

			// Return true if no filters set
			return noFilters;
		}

		return false;
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
			return this.injectEssentaToNetwork( transferAmount );
		}

		return false;
	}

	@Override
	public void getBoxes( final IPartCollsionHelper helper )
	{
		// Face + Large chamber
		helper.addBox( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 16.0F );

		// Small chamber
		helper.addBox( 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 14.0F );

		// Lights
		helper.addBox( 6.0D, 6.0D, 11.0D, 10.0D, 10.0D, 12.0D );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		// Get the tessellator
		Tessellator ts = Tessellator.instance;

		// Get the side texture
		IIcon busSideTexture = BlockTextureManager.BUS_SIDE.getTexture();

		helper.setTexture( busSideTexture, busSideTexture, busSideTexture, BlockTextureManager.ESSENTIA_IMPORT_BUS.getTexture(), busSideTexture,
			busSideTexture );

		// Face
		helper.setBounds( 4.0F, 4.0F, 15.0F, 12.0F, 12.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		// Set the texture to the chamber
		helper.setTexture( BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[2] );

		// Large chamber
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderInventoryBox( renderer );

		// Small chamber
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		helper.renderInventoryBox( renderer );

		// Set the texture back to the side texture
		helper.setTexture( busSideTexture );

		// Small chamber back wall
		helper.setBounds( 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 13.0F );
		helper.renderInventoryBox( renderer );

		// Face overlay
		helper.setBounds( 4.0F, 4.0F, 15.0F, 12.0F, 12.0F, 16.0F );
		helper.setInvColor( AbstractAEPartBase.INVENTORY_OVERLAY_COLOR );
		ts.setBrightness( 15728880 );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[1], ForgeDirection.SOUTH, renderer );

		// Lights
		helper.setBounds( 6.0F, 6.0F, 11.0F, 10.0F, 10.0F, 12.0F );
		this.renderInventoryBusLights( helper, renderer );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon busSideTexture = BlockTextureManager.BUS_SIDE.getTexture();
		helper.setTexture( busSideTexture, busSideTexture, busSideTexture, BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[0], busSideTexture,
			busSideTexture );

		// Face
		helper.setBounds( 4.0F, 4.0F, 15.0F, 12.0F, 12.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		if( this.host.getColor() != AEColor.Transparent )
		{
			ts.setColorOpaque_I( this.host.getColor().blackVariant );
		}
		else
		{
			ts.setColorOpaque_I( AbstractAEPartBase.INVENTORY_OVERLAY_COLOR );
		}

		if( this.isActive() )
		{
			Tessellator.instance.setBrightness( AbstractAEPartBase.ACTIVE_BRIGHTNESS );
		}

		// Face overlay
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[1], ForgeDirection.SOUTH, renderer );

		// Set the pass to alpha
		helper.renderForPass( 1 );

		// Set the texture to the chamber
		helper.setTexture( BlockTextureManager.ESSENTIA_IMPORT_BUS.getTextures()[2] );

		// Large chamber
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderBlock( x, y, z, renderer );

		// Small chamber
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		helper.renderBlock( x, y, z, renderer );

		// Set the pass back to opaque
		helper.renderForPass( 0 );

		// Set the texture back to the side texture
		helper.setTexture( busSideTexture );

		// Small chamber back wall
		helper.setBounds( 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 13.0F );
		helper.renderBlock( x, y, z, renderer );

		// Lights
		helper.setBounds( 6.0F, 6.0F, 11.0F, 10.0F, 10.0F, 12.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );
	}

}
