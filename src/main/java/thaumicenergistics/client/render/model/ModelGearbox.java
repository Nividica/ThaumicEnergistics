package thaumicenergistics.client.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.common.tiles.TileGearBox;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelGearbox
	extends ModelBase
{
	private class SideTransformation
	{
		private static final float SPOKE_OFFSET = 1.2F;

		/**
		 * Common
		 */
		public final float rotateAngleY, rotateAngleZ;

		/**
		 * Shaft
		 */
		public final float shaftOffsetX, shaftOffsetY, shaftOffsetZ;

		/**
		 * Spokes
		 */
		public final float spokeOffsetX, spokeOffsetY, spokeOffsetZ;

		/**
		 * Does this side have a shaft?
		 */
		public boolean hasShaft = true;

		/*
		 * Does this side have a gear?
		 */
		public boolean hasGear = false;

		/**
		 * Creates the transfomation
		 * 
		 * @param rotateAngleY
		 * @param rotateAngleZ
		 * @param offsetX
		 * @param offsetY
		 * @param offsetZ
		 */
		public SideTransformation( final float rotateAngleY, final float rotateAngleZ, final float offsetX, final float offsetY, final float offsetZ )
		{
			// Set common
			this.rotateAngleY = rotateAngleY;
			this.rotateAngleZ = rotateAngleZ;

			// Set shaft
			this.shaftOffsetX = offsetX;
			this.shaftOffsetY = offsetY;
			this.shaftOffsetZ = offsetZ;

			// Set spoke
			this.spokeOffsetX = offsetX * SPOKE_OFFSET;
			this.spokeOffsetY = offsetY * SPOKE_OFFSET;
			this.spokeOffsetZ = offsetZ * SPOKE_OFFSET;

		}

		/**
		 * Set what is rendered.
		 * 
		 * @param shaft
		 * @param gear
		 */
		public void setIsRendered( final boolean shaft, final boolean gear )
		{
			this.hasShaft = shaft;
			this.hasGear = gear;
		}
	}

	/**
	 * 90 degrees
	 */
	private static final float HALF_TURN = 1.570796325F;

	/**
	 * 45 degrees
	 */
	private static final float QUARTER_TURN = 0.7853981625F;

	/**
	 * Distance to the edge from the center.
	 */
	private static final float EDGE_DISTANCE = 3.12F;

	/**
	 * Number of valid sides.
	 */
	private static final int SIDE_COUNT = ForgeDirection.VALID_DIRECTIONS.length;

	/**
	 * Models
	 */
	private final ModelRenderer Transmission, Shaft, Spoke;

	/**
	 * Rotation angle of the shafts and gears.
	 */
	public float rotationAngle = 0.0F;

	/**
	 * Describes how to render each sides shaft and gear.
	 */
	private final SideTransformation[] transformations;

	/**
	 * Creates the model.
	 */
	public ModelGearbox()
	{
		this.textureWidth = 32;
		this.textureHeight = 32;

		// Create the transmission box
		this.Transmission = new ModelRenderer( this, 1, 0 );
		this.Transmission.addBox( 0F, 0F, 0F, 7, 7, 7 );
		this.Transmission.setRotationPoint( -3.5F, -3.5F, -3.5F );
		this.Transmission.setTextureSize( 64, 64 );
		this.Transmission.mirror = true;

		// Create the shaft
		this.Shaft = new ModelRenderer( this, 1, 15 );
		this.Shaft.addBox( -1.5F, -0.5F, -0.5F, 3, 1, 1 );
		this.Shaft.setRotationPoint( 0F, 0F, 0F );
		this.Shaft.setTextureSize( 64, 64 );
		this.Shaft.mirror = true;

		// Create the gear spoke
		this.Spoke = new ModelRenderer( this, 1, 18 );
		this.Spoke.addBox( -0.5F, -3.5F, -0.5F, 1, 7, 1 );
		this.Spoke.setRotationPoint( 0F, 0F, 0F );
		this.Spoke.setTextureSize( 64, 64 );
		this.Spoke.mirror = true;

		// Set the shaft and spoke transformations
		this.transformations = new SideTransformation[ForgeDirection.VALID_DIRECTIONS.length];
		this.transformations[ForgeDirection.EAST.ordinal()] = new SideTransformation( 0F, 0F, EDGE_DISTANCE, 0F, 0F );
		this.transformations[ForgeDirection.WEST.ordinal()] = new SideTransformation( 0F, 0F, -EDGE_DISTANCE, 0F, 0F );
		this.transformations[ForgeDirection.UP.ordinal()] = new SideTransformation( 0F, HALF_TURN, 0F, EDGE_DISTANCE, 0F );
		this.transformations[ForgeDirection.DOWN.ordinal()] = new SideTransformation( 0F, HALF_TURN, 0F, -EDGE_DISTANCE, 0F );
		this.transformations[ForgeDirection.SOUTH.ordinal()] = new SideTransformation( HALF_TURN, 0F, 0F, 0F, EDGE_DISTANCE );
		this.transformations[ForgeDirection.NORTH.ordinal()] = new SideTransformation( HALF_TURN, 0F, 0F, 0F, -EDGE_DISTANCE );
	}

	/**
	 * Renders a shaft on the specified side.
	 * 
	 * @param side
	 * @param withGear
	 * @param withExtension
	 * @param f5
	 */
	private void renderShaft( final int side, final float f5 )
	{
		// Get the transform
		SideTransformation sideTransform = this.transformations[side];

		if( sideTransform.hasShaft )
		{
			// Set shaft orientation
			this.Shaft.rotateAngleY = sideTransform.rotateAngleY;
			this.Shaft.rotateAngleZ = sideTransform.rotateAngleZ;

			// Set shaft position
			this.Shaft.offsetX = sideTransform.shaftOffsetX;
			this.Shaft.offsetY = sideTransform.shaftOffsetY;
			this.Shaft.offsetZ = sideTransform.shaftOffsetZ;

			// Render shaft
			this.Shaft.render( f5 );

			if( sideTransform.hasGear )
			{
				// Set spoke orientation
				this.Spoke.rotateAngleY = sideTransform.rotateAngleY;
				this.Spoke.rotateAngleZ = sideTransform.rotateAngleZ;

				// Set spoke position
				this.Spoke.offsetX = sideTransform.spokeOffsetX;
				this.Spoke.offsetY = sideTransform.spokeOffsetY;
				this.Spoke.offsetZ = sideTransform.spokeOffsetZ;

				// Render gear
				this.Spoke.rotateAngleX += QUARTER_TURN;
				this.Spoke.render( f5 );
				this.Spoke.rotateAngleX += QUARTER_TURN;
				this.Spoke.render( f5 );
				this.Spoke.rotateAngleX += QUARTER_TURN;
				this.Spoke.render( f5 );
				this.Spoke.rotateAngleX += QUARTER_TURN;
				this.Spoke.render( f5 );
			}
		}
	}

	/**
	 * Renders the gearbox.
	 */
	@Override
	public void render( final Entity entity, final float f, final float f1, final float f2, final float f3, final float f4, final float f5 )
	{
		// Call super
		super.render( entity, f, f1, f2, f3, f4, f5 );

		// Set the angles
		this.setRotationAngles( f, f1, f2, f3, f4, f5, entity );

		// Render the transmission
		this.Transmission.render( f5 );

		// Set all the shafts and gears to the same rotation angle.
		this.Shaft.rotateAngleX = this.rotationAngle;
		this.Spoke.rotateAngleX = this.rotationAngle;

		// Render shafts
		for( int side = 0; side < ModelGearbox.SIDE_COUNT; side++ )
		{
			this.renderShaft( side, f5 );
		}

	}

	@Override
	public void setRotationAngles( final float f, final float f1, final float f2, final float f3, final float f4, final float f5, final Entity entity )
	{
		super.setRotationAngles( f, f1, f2, f3, f4, f5, entity );
	}

	/**
	 * Updates the model to match the tile entity data.
	 * 
	 * @param gearboxTile
	 */
	public void updateToTileEntity( final TileGearBox gearboxTile )
	{
		// Set if each shaft has a gear.
		for( int side = 0; side < ModelGearbox.SIDE_COUNT; side++ )
		{
			boolean withGear = !( gearboxTile.sideIsFacingCrankable[side] );
			this.transformations[side].setIsRendered( true, withGear );
		}

		// Set the rotation angle.
		this.rotationAngle = gearboxTile.shaftRotation;
	}
}
