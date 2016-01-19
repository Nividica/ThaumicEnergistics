package thaumicenergistics.client.render.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.common.ThaumicEnergistics;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelGolemWifiAddon
	extends ModelBase
{
	public static ResourceLocation TEXTURE = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/models/golem.wifi.addon.png" );

	/**
	 * Submodels
	 */
	private final ModelRenderer Antenna, PackBack, PackFront;

	/**
	 * Pearl UV coords.
	 */
	private final float pearl_MinU, pearl_MaxU, pearl_MinV, pearl_MaxV;

	/**
	 * Scale of the fluix pearls
	 */
	private final float pearlScale = 0.2f;

	/**
	 * Distance from edge to edge of the antenna
	 */
	private final float antennaWidth = 0.32f;

	/**
	 * Distance from the antenna to render the pearl faces
	 */
	private final float pearl_FaceDistance, pearl_2FD;

	/**
	 * Rotation of the pearls
	 */
	private float pearlRotation = 0.0f;

	private boolean isPearlCompiled = false;

	private int pearlDispListID = -1;

	public ModelGolemWifiAddon()
	{
		this.textureWidth = 16;
		this.textureHeight = 16;

		this.Antenna = new ModelRenderer( this, 10, 0 );
		this.Antenna.addBox( -0.5f, -6f, -0.5f, 1, 3, 1 );
		this.Antenna.setRotationPoint( 0, 0, 0f );
		this.Antenna.setTextureSize( this.textureWidth, this.textureHeight );
		this.Antenna.mirror = true;
		this.setSubmodelRotation( this.Antenna, 3.14f, 0, 0 );

		this.PackBack = new ModelRenderer( this, 0, 0 );
		this.PackBack.addBox( -1f, -3f, -3f, 2, 6, 6 );
		this.PackBack.setRotationPoint( 0, 0, 0f );
		this.PackBack.setTextureSize( this.textureWidth, this.textureHeight );
		this.PackBack.mirror = true;
		this.setSubmodelRotation( this.PackBack, 3.14f, 0, 0 );

		this.PackFront = new ModelRenderer( this, 2, 0 );
		this.PackFront.addBox( -1.5f, -1f, -2f, 1, 2, 4 );
		this.PackFront.setRotationPoint( 0, 0, 0f );
		this.PackFront.setTextureSize( this.textureWidth, this.textureHeight );
		this.PackFront.mirror = true;
		this.setSubmodelRotation( this.PackFront, 3.14f, 0, 0 );

		this.pearl_MinU = 8.0f / this.textureWidth;
		this.pearl_MaxU = 13.5f / this.textureWidth;
		this.pearl_MinV = 6.0f / this.textureHeight;
		this.pearl_MaxV = 11.5f / this.textureHeight;

		this.pearl_FaceDistance = -0.013f / this.pearlScale;
		this.pearl_2FD = this.pearl_FaceDistance + this.pearl_FaceDistance;
	}

	private void compilePearl()
	{
		// Create a new list
		this.pearlDispListID = GLAllocation.generateDisplayLists( 1 );
		GL11.glNewList( this.pearlDispListID, GL11.GL_COMPILE );

		// Get the tesselator
		Tessellator tess = Tessellator.instance;

		// Move to the left of the antenna
		GL11.glTranslatef( -0.0955f, 0.275f, -0.045f );

		// Scale down
		GL11.glScalef( this.pearlScale, this.pearlScale, this.pearlScale );

		// Draw left
		this.drawPearlFaces( tess, this.pearl_MinU, this.pearl_MaxU );

		// Move to the right of the antenna
		GL11.glTranslatef( 0.0f, 0.0f, this.antennaWidth - this.pearl_2FD );

		// Draw right
		this.drawPearlFaces( tess, this.pearl_MaxU, this.pearl_MinU );

		// Rotate 90
		GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );

		// Move to the front of the antenna
		GL11.glTranslatef( -this.antennaWidth - this.pearl_FaceDistance, 0.0f, this.antennaWidth + this.pearl_FaceDistance );

		// Draw front
		this.drawPearlFaces( tess, this.pearl_MaxU, this.pearl_MinU );

		// Move to the back of the antenna
		GL11.glTranslatef( 0.0f, 0.0f, this.antennaWidth - this.pearl_2FD );

		// Draw back
		this.drawPearlFaces( tess, this.pearl_MinU, this.pearl_MaxU );

		// End the list
		GL11.glEndList();
		this.isPearlCompiled = true;
	}

	private void drawPearlFaces( final Tessellator tess, final float minU, final float maxU )
	{
		tess.startDrawingQuads();
		// Face
		tess.addVertexWithUV( 0.0D, 0.0D, 0.0D, maxU, this.pearl_MaxV );
		tess.addVertexWithUV( 1.0D, 0.0D, 0.0D, minU, this.pearl_MaxV );
		tess.addVertexWithUV( 1.0D, 1.0D, 0.0D, minU, this.pearl_MinV );
		tess.addVertexWithUV( 0.0D, 1.0D, 0.0D, maxU, this.pearl_MinV );
		// Back-face
		tess.addVertexWithUV( 0.0D, 1.0D, 0.0D, maxU, this.pearl_MinV );
		tess.addVertexWithUV( 1.0D, 1.0D, 0.0D, minU, this.pearl_MinV );
		tess.addVertexWithUV( 1.0D, 0.0D, 0.0D, minU, this.pearl_MaxV );
		tess.addVertexWithUV( 0.0D, 0.0D, 0.0D, maxU, this.pearl_MaxV );
		tess.draw();
	}

	private void renderPearl( final float partialElapsedTick )
	{
		// Apply Y rotation
		if( !Minecraft.getMinecraft().isGamePaused() )
		{
			this.pearlRotation += ( partialElapsedTick * 2.0f );
			if( this.pearlRotation >= 360.0f )
			{
				this.pearlRotation -= 360.0f;
			}

		}
		GL11.glRotatef( this.pearlRotation, 0.0f, 1.0f, 0.0f );

		// Is the pearl compiled?
		if( !this.isPearlCompiled )
		{
			// Compile the pearl
			compilePearl();
		}

		// Draw the pearl
		GL11.glCallList( this.pearlDispListID );

	}

	private void setSubmodelRotation( final ModelRenderer model, final float x, final float y, final float z )
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void render( final Entity entity, final float partialElapsedTick, final float f1, final float f2, final float f3, final float f4,
						final float f5 )
	{
		Minecraft.getMinecraft().renderEngine.bindTexture( ModelGolemWifiAddon.TEXTURE );
		this.PackBack.render( f5 );
		this.PackFront.render( f5 );
		this.Antenna.render( f5 );
		this.renderPearl( partialElapsedTick );
	}

}
