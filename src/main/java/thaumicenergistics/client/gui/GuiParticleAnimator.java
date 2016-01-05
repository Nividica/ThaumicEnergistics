package thaumicenergistics.client.gui;

import javax.annotation.Nonnull;
import net.minecraft.client.gui.Gui;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiParticleAnimator
{
	/**
	 * Distance from start to destination.
	 */
	private final int distanceX, distanceY;

	/**
	 * Time when the particle was created.
	 */
	private final double epoch;

	/**
	 * The amount of time the particle is alive for.
	 */
	private final long timeToLive;

	/**
	 * How complete the animation is.
	 */
	private double percentComplete;

	/**
	 * Starting position.
	 */
	public final int startingX, startingY;

	/**
	 * Destination position.
	 */
	public final int destinationX, destinationY;

	/**
	 * The particle to draw.
	 */
	public final EnumGuiParticles theParticle;

	/**
	 * Color of the particle.
	 */
	public float red = 1.0f, green = 1.0f, blue = 1.0f;

	/**
	 * Particle frames per second.
	 */
	public int framesPerSecond = 1;

	public GuiParticleAnimator( final int startX, final int startY, final int destX, final int destY, final float time,
								@Nonnull final EnumGuiParticles particle )
	{
		// Set starting position
		this.startingX = startX;
		this.startingY = startY;

		// Set destination
		this.destinationX = destX;
		this.destinationY = destY;

		// Set TTL
		this.timeToLive = (long)( time * 1000.0f );
		if( this.timeToLive <= 0 )
		{
			this.percentComplete = 1.0f;
		}

		// Set epoch
		this.epoch = System.currentTimeMillis();

		// Set particle
		this.theParticle = particle;

		// Calculate distance
		this.distanceX = destX - startX;
		this.distanceY = destY - startY;
	}

	/**
	 * Returns false if the particle has reached the end of its life.
	 * 
	 * @param gui
	 * @param needsPrepare
	 * @return
	 */
	public boolean draw( final Gui gui, final boolean needsPrepare )
	{
		// Check percentage
		if( this.percentComplete == 1.0f )
		{
			return false;
		}

		// Calculate frame number
		int frame = (int)( System.currentTimeMillis() / ( 1000 / this.framesPerSecond ) );

		// Draw
		return this.draw( gui, frame, needsPrepare );
	}

	/**
	 * Returns false if the particle has reached the end of its life.
	 * 
	 * @param gui
	 * @param particleFrame
	 * @param needsPrepare
	 * @return
	 */
	public boolean draw( final Gui gui, final int particleFrame, final boolean needsPrepare )
	{
		// Check percentage
		if( this.percentComplete == 1.0f )
		{
			return false;
		}

		// Calculate percentage
		this.percentComplete = ( ( System.currentTimeMillis() - this.epoch ) / this.timeToLive );
		if( this.percentComplete > 1.0f )
		{
			this.percentComplete = 1.0f;
		}

		// Calculate position
		int posX = this.startingX + (int)( this.distanceX * this.percentComplete );
		int posY = this.startingY + (int)( this.distanceY * this.percentComplete );

		// Draw the particle
		this.theParticle.drawParticle( gui, posX, posY, particleFrame, this.red, this.green, this.blue, needsPrepare );

		return true;
	}

	/**
	 * Sets the color of the particle.
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setColor( final float red, final float green, final float blue )
	{
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
}
