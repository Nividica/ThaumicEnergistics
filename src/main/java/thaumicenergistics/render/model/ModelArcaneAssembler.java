package thaumicenergistics.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelArcaneAssembler
	extends ModelBase
{
	/**
	 * 90 degrees
	 */
	private static final float HALF_TURN = 1.570796325F, FULL_TURN = HALF_TURN + HALF_TURN;

	//fields
	private ModelRenderer OuterFrameTop;
	private ModelRenderer OuterFrameBottom;
	private ModelRenderer OuterFrameLeft;
	private ModelRenderer OuterFrameRight;
	private ModelRenderer Glass;
	private ModelRenderer InnerFrameTop;
	private ModelRenderer InnerFrameBottom;
	private ModelRenderer InnerFrameLeft;
	private ModelRenderer InnerFrameRight;

	public ModelArcaneAssembler()
	{
		this.textureWidth = 64;
		this.textureHeight = 64;

		this.OuterFrameTop = new ModelRenderer( this, 0, 0 );
		this.OuterFrameTop.addBox( -17F, -17F, -15F, 34, 2, 2 );
		this.OuterFrameTop.setRotationPoint( 0F, 0F, 0F );
		this.OuterFrameTop.setTextureSize( 64, 64 );
		this.OuterFrameTop.mirror = true;

		this.setRotation( this.OuterFrameTop, 0F, 0F, 0F );
		this.OuterFrameBottom = new ModelRenderer( this, 0, 0 );
		this.OuterFrameBottom.addBox( -17F, 15F, -15F, 34, 2, 2 );
		this.OuterFrameBottom.setRotationPoint( 0F, 0F, 0F );
		this.OuterFrameBottom.setTextureSize( 64, 64 );
		this.OuterFrameBottom.mirror = true;

		this.setRotation( this.OuterFrameBottom, 0F, 0F, 0F );
		this.OuterFrameLeft = new ModelRenderer( this, 0, 0 );
		this.OuterFrameLeft.addBox( -17F, -17F, -15F, 34, 2, 2 );
		this.OuterFrameLeft.setRotationPoint( 0F, 0F, 0F );
		this.OuterFrameLeft.setTextureSize( 64, 64 );
		this.OuterFrameLeft.mirror = true;
		this.setRotation( this.OuterFrameLeft, 0F, 0F, -1.570796F );

		this.OuterFrameRight = new ModelRenderer( this, 0, 0 );
		this.OuterFrameRight.addBox( -17F, -17F, -15F, 34, 2, 2 );
		this.OuterFrameRight.setRotationPoint( 0F, 0F, 0F );
		this.OuterFrameRight.setTextureSize( 64, 64 );
		this.OuterFrameRight.mirror = true;
		this.setRotation( this.OuterFrameRight, 0F, 0F, 1.570796F );

		this.Glass = new ModelRenderer( this, 0, 37 );
		this.Glass.addBox( -13F, -13F, -14F, 26, 26, 1 );
		this.Glass.setRotationPoint( 0F, 0F, 0F );
		this.Glass.setTextureSize( 64, 64 );
		this.Glass.mirror = true;
		this.setRotation( this.Glass, 0F, 0F, 0F );

		this.InnerFrameTop = new ModelRenderer( this, 0, 4 );
		this.InnerFrameTop.addBox( -15F, -15F, -15F, 30, 2, 2 );
		this.InnerFrameTop.setRotationPoint( 0F, 0F, 0F );
		this.InnerFrameTop.setTextureSize( 64, 64 );
		this.InnerFrameTop.mirror = true;
		this.setRotation( this.InnerFrameTop, 0F, 0F, 0F );

		this.InnerFrameBottom = new ModelRenderer( this, 0, 4 );
		this.InnerFrameBottom.addBox( -15F, 13F, -15F, 30, 2, 2 );
		this.InnerFrameBottom.setRotationPoint( 0F, 0F, 0F );
		this.InnerFrameBottom.setTextureSize( 64, 64 );
		this.InnerFrameBottom.mirror = true;
		this.setRotation( this.InnerFrameBottom, 0F, 0F, 0F );

		this.InnerFrameLeft = new ModelRenderer( this, 0, 4 );
		this.InnerFrameLeft.addBox( -15F, -15F, -15F, 30, 2, 2 );
		this.InnerFrameLeft.setRotationPoint( 0F, 0F, 0F );
		this.InnerFrameLeft.setTextureSize( 64, 64 );
		this.InnerFrameLeft.mirror = true;
		this.setRotation( this.InnerFrameLeft, 0F, 0F, -1.570796F );

		this.InnerFrameRight = new ModelRenderer( this, 0, 4 );
		this.InnerFrameRight.addBox( -15F, -15F, -15F, 30, 2, 2 );
		this.InnerFrameRight.setRotationPoint( 0F, 0F, 0F );
		this.InnerFrameRight.setTextureSize( 64, 64 );
		this.InnerFrameRight.mirror = true;
		this.setRotation( this.InnerFrameRight, 0F, 0F, 1.570796F );
	}

	/**
	 * Renders the glass on one side of the block.
	 * 
	 * @param YRotation
	 * @param XRotation
	 * @param f5
	 */
	private void renderGlassSide( final float YRotation, final float XRotation, final float f5 )
	{
		this.Glass.rotateAngleX = XRotation;
		this.Glass.rotateAngleY = YRotation;
		this.Glass.render( f5 );

	}

	/**
	 * Renders the inner frame on one side of the block.
	 * 
	 * @param YRotation
	 * @param XRotation
	 * @param f5
	 */
	private void renderInnerFrameSide( final float YRotation, final float XRotation, final float f5 )
	{
		// Set the Y rotation
		this.InnerFrameTop.rotateAngleY = YRotation;
		this.InnerFrameBottom.rotateAngleY = YRotation;
		this.InnerFrameLeft.rotateAngleY = YRotation;
		this.InnerFrameRight.rotateAngleY = YRotation;

		// Set the X rotation
		this.InnerFrameTop.rotateAngleX = XRotation;
		this.InnerFrameBottom.rotateAngleX = XRotation;
		this.InnerFrameLeft.rotateAngleX = XRotation;
		this.InnerFrameRight.rotateAngleX = XRotation;

		// Render the frame
		this.InnerFrameTop.render( f5 );
		this.InnerFrameBottom.render( f5 );
		this.InnerFrameLeft.render( f5 );
		this.InnerFrameRight.render( f5 );
	}

	/**
	 * Renders the outer frame on one side of the block.
	 * 
	 * @param rotation
	 * @param f5
	 */
	private void renderOuterFrameSide( final float rotation, final float f5 )
	{
		// Set the Y rotation
		this.OuterFrameTop.rotateAngleY = rotation;
		this.OuterFrameBottom.rotateAngleY = rotation;
		this.OuterFrameLeft.rotateAngleX = -rotation;
		this.OuterFrameRight.rotateAngleX = rotation;

		// Render the frame
		this.OuterFrameTop.render( f5 );
		this.OuterFrameBottom.render( f5 );
		this.OuterFrameLeft.render( f5 );
		this.OuterFrameRight.render( f5 );
	}

	private void setRotation( final ModelRenderer model, final float x, final float y, final float z )
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void render( final Entity entity, final float f, final float f1, final float f2, final float f3, final float f4, final float f5 )
	{

		// Render the outer frame

		this.renderOuterFrameSide( 0, f5 );
		this.renderOuterFrameSide( ModelArcaneAssembler.HALF_TURN, f5 );
		this.renderOuterFrameSide( -ModelArcaneAssembler.HALF_TURN, f5 );
		this.renderOuterFrameSide( ModelArcaneAssembler.FULL_TURN, f5 );

		// Render the inner frame
		this.renderInnerFrameSide( 0, 0, f5 );
		this.renderInnerFrameSide( ModelArcaneAssembler.HALF_TURN, 0, f5 );
		this.renderInnerFrameSide( -ModelArcaneAssembler.HALF_TURN, 0, f5 );
		this.renderInnerFrameSide( ModelArcaneAssembler.FULL_TURN, 0, f5 );
		this.renderInnerFrameSide( 0, ModelArcaneAssembler.HALF_TURN, f5 );
		this.renderInnerFrameSide( 0, -ModelArcaneAssembler.HALF_TURN, f5 );

		// Enable transparency
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );

		// Render the glass
		this.renderGlassSide( 0, 0, f5 );
		this.renderGlassSide( ModelArcaneAssembler.HALF_TURN, 0, f5 );
		this.renderGlassSide( -ModelArcaneAssembler.HALF_TURN, 0, f5 );
		this.renderGlassSide( ModelArcaneAssembler.FULL_TURN, 0, f5 );
		this.renderGlassSide( 0, ModelArcaneAssembler.HALF_TURN, f5 );
		this.renderGlassSide( 0, -ModelArcaneAssembler.HALF_TURN, f5 );

		// Disable transparency
		GL11.glDisable( GL11.GL_BLEND );

	}

	@Override
	public void setRotationAngles( final float f, final float f1, final float f2, final float f3, final float f4, final float f5, final Entity entity )
	{
		super.setRotationAngles( f, f1, f2, f3, f4, f5, entity );
	}

}
