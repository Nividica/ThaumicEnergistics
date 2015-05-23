package thaumicenergistics.integration.tc;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import appeng.api.config.Actionable;

public class EssentiaTransportHelper
{
	/**
	 * Instance.
	 */
	public static final EssentiaTransportHelper INSTANCE = new EssentiaTransportHelper();

	/**
	 * Private constructor.
	 */
	private EssentiaTransportHelper()
	{
		// Intentionally Empty
	}

	public void takeEssentiaFromTransportNeighbors( final IEssentiaTransportWithSimulate destination, final World world, final int x, final int y,
													final int z )
	{
		// Null checks
		if( ( destination == null ) || ( world == null ) )
		{
			return;
		}

		// For each side
		for( ForgeDirection destinationSide : ForgeDirection.VALID_DIRECTIONS )
		{
			// Can the destination accept essentia from this side?
			if( !destination.canInputFrom( destinationSide ) )
			{
				// Destination can not accept essentia from this side
				continue;
			}

			// Get the source
			TileEntity sourceTile = world.getTileEntity( destinationSide.offsetX + x, destinationSide.offsetY + y, destinationSide.offsetZ + z );

			// Ensure the source is a transport
			if( !( sourceTile instanceof IEssentiaTransport ) )
			{
				// Invalid source
				continue;
			}

			// Cast
			IEssentiaTransport source = (IEssentiaTransport)sourceTile;

			// Get the opposite direction
			ForgeDirection sourceSide = destinationSide.getOpposite();

			// Can the source output to this side?
			if( !source.canOutputTo( sourceSide ) )
			{
				// Source can not output to this side
				continue;
			}

			// Does the source have any essentia to give?
			if( source.getEssentiaAmount( sourceSide ) <= 0 )
			{
				// No essentia from this side
				continue;
			}

			// Does the destination have enough suction?
			int dSuck = destination.getSuctionAmount( destinationSide );
			if( ( dSuck < source.getMinimumSuction() ) || ( dSuck < source.getSuctionAmount( sourceSide ) ) )
			{
				// Destination does not have enough suction.
				continue;
			}

			// Get the source aspect
			Aspect sourceAspect = source.getEssentiaType( sourceSide );
			if( sourceAspect == null )
			{
				// Invalid aspect
				continue;
			}

			// Simulate an injection
			if( destination.addEssentia( sourceAspect, 1, destinationSide, Actionable.SIMULATE ) == 1 )
			{
				// Inject
				destination.addEssentia( sourceAspect, source.takeEssentia( sourceAspect, 1, sourceSide ), destinationSide, Actionable.MODULATE );
			}
		}
	}

}
