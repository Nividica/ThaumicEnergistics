package thaumicenergistics.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumicenergistics.api.TEApi;
import thaumicenergistics.aspect.AspectStack;

public class TileEssentiaProvider
	extends TileProviderBase
	implements IEssentiaTransport
{
	public static final String TILE_ID = "TileEssentiaProvider";

	/**
	 * How much power does this require just to be active?
	 */
	@Override
	protected double getIdlePowerusage()
	{
		return 3.0;
	}

	@Override
	protected ItemStack getItemFromTile( final Object obj )
	{
		// Return the itemstack the visually represents this tile
		return TEApi.instance().blocks().EssentiaProvider.getStack();

	}

	protected Aspect getNeighborWantedAspect( final ForgeDirection face )
	{
		System.out.println( face );

		// Get the tile entity next to this face
		TileEntity neighbor = this.worldObj.getTileEntity( this.xCoord + face.offsetX, this.yCoord + face.offsetY, this.zCoord + face.offsetZ );

		// Do we have essentia transport neighbor?
		if( ( neighbor != null ) && ( neighbor instanceof IEssentiaTransport ) )
		{
			// Get the aspect they want
			Aspect wantedAspect = ( (IEssentiaTransport)neighbor ).getSuctionType( face.getOpposite() );

			// Return the aspect they want
			return wantedAspect;
		}

		return null;
	}

	@Override
	protected void onChannelUpdate()
	{
		// Ignored
	}

	@Override
	public int addEssentia( final Aspect aspect, final int amount, final ForgeDirection face )
	{
		// Doesn't accept essentia
		return 0;
	}

	@Override
	public boolean canInputFrom( final ForgeDirection face )
	{
		// Doesn't accept essentia
		return false;
	}

	@Override
	public boolean canOutputTo( final ForgeDirection face )
	{
		System.out.println( face );
		// Can output to any side
		return true;
	}

	@Override
	public int getEssentiaAmount( final ForgeDirection face )
	{
		// Get the aspect this neighbor wants
		Aspect wantedAspect = this.getNeighborWantedAspect( face );

		// Does the neighbor want anything?
		if( wantedAspect != null )
		{
			// Get the stack from the network
			AspectStack matchingStack = this.getAspectStackFromNetwork( wantedAspect );

			// Does the network have that aspect?
			if( matchingStack != null )
			{
				// Return the amount we have
				return (int)matchingStack.amount;
			}
		}

		// No match or no request
		return 0;
	}

	@Override
	public Aspect getEssentiaType( final ForgeDirection face )
	{
		// Get the aspect this neighbor wants
		Aspect wantedAspect = this.getNeighborWantedAspect( face );

		// Does the neighbor want anything?
		if( wantedAspect != null )
		{
			// Does the network have that aspect?
			if( this.getAspectStackFromNetwork( wantedAspect ) != null )
			{
				// Return the aspect they want
				return wantedAspect;
			}
		}

		// No match or no request
		return null;
	}

	@Override
	public int getMinimumSuction()
	{
		// Any amount of suction is good enough
		return 0;
	}

	@Override
	public int getSuctionAmount( final ForgeDirection face )
	{
		// Doesn't accept essentia
		return 0;
	}

	@Override
	public Aspect getSuctionType( final ForgeDirection face )
	{
		// Doesn't accept essentia
		return null;
	}

	@Override
	public boolean isConnectable( final ForgeDirection face )
	{
		// Can connect on any side
		return true;
	}

	@Override
	public boolean renderExtendedTube()
	{
		// We take up a full block
		return false;
	}

	@Override
	public void setSuction( final Aspect aspect, final int amount )
	{
		// Ignored
	}

	@Override
	public int takeEssentia( final Aspect aspect, final int amount, final ForgeDirection face )
	{
		// Extract essentia from the network, and return the amount extracted
		return this.extractEssentiaFromNetwork( aspect, amount, false );
	}

}
