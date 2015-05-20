package thaumicenergistics.parts;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.parts.PartItemStack;
import appeng.util.Platform;

public abstract class AbstractAEPartRotateable
	extends AbstractAEPartBase
{
	/**
	 * NBT keys
	 */
	private static final String NBT_KEY_ROT_DIR = "partRotation";

	/**
	 * What direction should be rotated to.
	 * Valid values are 0,1,2,3.
	 */
	private byte renderRotation = 0;

	public AbstractAEPartRotateable( final AEPartsEnum associatedPart )
	{
		super( associatedPart );
	}

	/**
	 * Rotates the renderer.
	 * 
	 * @param renderer
	 * @param reset
	 */
	protected void rotateRenderer( final RenderBlocks renderer, final boolean reset )
	{
		int rot = ( reset ? 0 : this.renderRotation );
		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = rot;
	}

	/**
	 * Called when the part is right-clicked
	 */
	@Override
	public boolean onActivate( final EntityPlayer player, final Vec3 position )
	{
		// Get the host tile entity
		TileEntity hte = this.getHostTile();

		// Is the player not sneaking and using a wrench?
		if( !player.isSneaking() && Platform.isWrench( player, player.inventory.getCurrentItem(), hte.xCoord, hte.yCoord, hte.zCoord ) )
		{
			if( EffectiveSide.isServerSide() )
			{
				// Bounds check the rotation
				if( ( this.renderRotation > 3 ) || ( this.renderRotation < 0 ) )
				{
					this.renderRotation = 0;
				}

				// Rotate
				switch ( this.renderRotation )
				{
					case 0:
						this.renderRotation = 1;
						break;
					case 1:
						this.renderRotation = 3;
						break;
					case 2:
						this.renderRotation = 0;
						break;
					case 3:
						this.renderRotation = 2;
						break;
				}

				// Mark for sync & save
				this.markForUpdate();
				this.markForSave();
			}
			return true;
		}

		return super.onActivate( player, position );
	}

	/**
	 * Reads the rotation
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read rotation
		if( data.hasKey( AbstractAEPartRotateable.NBT_KEY_ROT_DIR ) )
		{
			this.renderRotation = data.getByte( AbstractAEPartRotateable.NBT_KEY_ROT_DIR );
		}
	}

	/**
	 * Reads the rotation from the stream.
	 */
	@Override
	public boolean readFromStream( final ByteBuf stream ) throws IOException
	{
		boolean redraw = false;

		// Call super
		redraw |= super.readFromStream( stream );

		// Read the rotation
		byte streamRot = stream.readByte();

		// Did the rotaion change?
		if( this.renderRotation != streamRot )
		{
			this.renderRotation = streamRot;
			redraw |= true;
		}

		return redraw;
	}

	/**
	 * Saves the rotation
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		// Call super
		super.writeToNBT( data, saveType );

		// Write the rotation
		if( this.renderRotation != 0 )
		{
			data.setByte( AbstractAEPartRotateable.NBT_KEY_ROT_DIR, this.renderRotation );
		}
	}

	/**
	 * Writes the rotation to the stream.
	 */
	@Override
	public void writeToStream( final ByteBuf stream ) throws IOException
	{
		// Call super
		super.writeToStream( stream );

		// Write the rotation
		stream.writeByte( this.renderRotation );
	}
}
