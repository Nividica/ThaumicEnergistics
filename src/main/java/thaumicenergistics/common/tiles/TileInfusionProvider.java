package thaumicenergistics.common.tiles;

import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.Thaumcraft;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.tiles.abstraction.TileProviderBase;

/**
 * Provides essentia to devices via Infusion mechanics.
 *
 * @author Nividica
 *
 */
public class TileInfusionProvider
	extends TileProviderBase
	implements IAspectSource
{
	/**
	 * Shows runes on the infusion provider.
	 *
	 * @param aspectColor
	 */
	private void doParticalFX( final int aspectColor )
	{
		// Convert each color to percentage
		float red = ( aspectColor & 0xFF0000 ) / (float)0xFF0000;
		float green = ( aspectColor & 0x00FF00 ) / (float)0x00FF00;
		float blue = ( aspectColor & 0x0000FF ) / (float)0x0000FF;

		// Add particles
		for( int i = 0; i < 5; i++ )
		{
			Thaumcraft.proxy.blockRunes( this.worldObj, this.xCoord, this.yCoord, this.zCoord, red, green, blue, 15, -0.1F );
		}
		for( int i = 0; i < 5; i++ )
		{
			Thaumcraft.proxy.blockRunes( this.worldObj, this.xCoord, this.yCoord, this.zCoord, red, green, blue, 15, 0.1F );
		}
	}

	/**
	 * How much power does this require just to be active?
	 */
	@Override
	protected double getIdlePowerusage()
	{
		return 5.0;
	}

	@Override
	protected ItemStack getItemFromTile( final Object obj )
	{
		// Return the itemstack the visually represents this tile
		return ThEApi.instance().blocks().InfusionProvider.getStack();

	}

	@Override
	public int addToContainer( final Aspect tag, final int amount )
	{
		// Ignored
		return 0;
	}

	@Override
	public int containerContains( final Aspect tag )
	{
		// Ignored
		return 0;
	}

	@Override
	public boolean doesContainerAccept( final Aspect tag )
	{
		// Ignored
		return false;
	}

	@Deprecated
	@Override
	public boolean doesContainerContain( final AspectList ot )
	{
		// Ignored
		return false;
	}

	@Override
	public boolean doesContainerContainAmount( final Aspect tag, final int amount )
	{
		// Ignored
		return false;
	}

	@Override
	public AspectList getAspects()
	{
		// Ignored
		return null;
	}

	@Override
	public void setAspects( final AspectList aspects )
	{
		// Ignored
	}

	@Override
	public boolean takeFromContainer( final Aspect tag, final int amount )
	{
		// Can we extract the essentia from the network?
		if( this.extractEssentiaFromNetwork( tag, amount, true ) == amount )
		{
			// Show partical FX
			this.doParticalFX( tag.getColor() );

			return true;
		}

		return false;
	}

	@Deprecated
	@Override
	public boolean takeFromContainer( final AspectList ot )
	{
		// Ignored
		return false;
	}

}
