package thaumicenergistics.common.entities;

import java.util.HashSet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.Marker;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileJarFillable;
import thaumcraft.common.tiles.TileJarFillableVoid;
import thaumicenergistics.common.entities.WirelessGolemHandler.WirelessServerData;
import appeng.api.config.Actionable;

/**
 * Allows a golem to interact with networked essentia.
 * 
 * @author Nividica
 * 
 */
public class AIGolemWifiEssentia
	extends AIAENetworkGolem
{
	private final HashSet<TileJarFillable> checkedJars = new HashSet<TileJarFillable>();

	/**
	 * The jar the golem plan's to fill up.
	 */
	private TileJarFillable targetJar = null;

	/**
	 * Distance to the target jar.
	 */
	private double targetDistance = 0;

	/**
	 * Is the target jar a void jar?
	 */
	private boolean targetVoid;

	/**
	 * The square range of the golem.
	 */
	private double golemRange;

	/**
	 * True if the golem is filling up jars, false if the golem is emptying something.
	 */
	private boolean modeFill;

	public AIGolemWifiEssentia( final EntityGolemBase golem, final WirelessServerData wsd )
	{
		super( golem, wsd );
	}

	/**
	 * Checks jars connected to the specified jar for potential targets.
	 * 
	 * @param world
	 * @param gHomePos
	 * @param teJar
	 */
	private void checkConnectedJars( final World world, final ChunkCoordinates gHomePos, final TileEntity teJar )
	{
		int x, y, z, setOp;

		// Sanity check
		if( this.checkedJars.size() >= 200 )
		{
			return;
		}

		// Check each direction
		for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
		{
			// Get the position
			x = side.offsetX + teJar.xCoord;
			y = side.offsetY + teJar.yCoord;
			z = side.offsetZ + teJar.zCoord;

			// Get the tile entity
			TileEntity te = world.getTileEntity( x, y, z );

			// Ensure the tile is a jar
			if( !( te instanceof TileJarFillable ) )
			{
				continue;
			}

			// Set the jar if it is closer
			setOp = this.setTargetIfValid( (TileJarFillable)te, gHomePos );
			if( setOp > 2 )
			{
				// Check it's connected jars
				this.checkConnectedJars( world, gHomePos, te );
			}

		}
	}

	/**
	 * Locates the nearest valid jar as a target.
	 */
	private void findClosestValidJar()
	{
		// Is there a target?
		if( this.targetJar != null )
		{
			// Is the jar still there?
			if( this.golem.worldObj.getTileEntity( this.targetJar.xCoord,
				this.targetJar.yCoord, this.targetJar.zCoord ) == this.targetJar )
			{

				// Can it hold more?
				Aspect wantedAspect = ( this.targetJar.aspectFilter != null ? this.targetJar.aspectFilter : this.targetJar.aspect );
				if( ( wantedAspect != null ) && ( this.targetVoid || ( this.targetJar.amount < this.targetJar.maxAmount ) ) )
				{
					// Can the wanted aspect be extracted?
					if( this.network.extractEssentia( wantedAspect, 1, Actionable.SIMULATE ) > 0 )
					{
						return;
					}
				}
			}
		}

		// Reset target jar
		this.targetJar = null;
		this.targetDistance = Double.MAX_VALUE;
		this.targetVoid = false;

		// Get the world the golem is in
		World world = this.golem.worldObj;
		int worldDim = world.provider.dimensionId;

		// Calculate the squared range
		this.golemRange = this.golem.getRange();
		this.golemRange *= this.golemRange;

		// Get the golems position
		ChunkCoordinates gHomePos = this.golem.getHomePosition();

		// Check each of the golems markers
		for( Marker marker : this.golem.getMarkers() )
		{
			// Ensure the marker is in the same dimension
			if( marker.dim != worldDim )
			{
				continue;
			}

			// Get the tile entity
			TileEntity markedTile = world.getTileEntity( marker.x, marker.y, marker.z );

			// Ensure the tile is a jar
			if( !( markedTile instanceof TileJarFillable ) )
			{
				continue;
			}

			// Set the jar if it is closer
			this.setTargetIfValid( (TileJarFillable)markedTile, gHomePos );

			// Check connected jars
			this.checkConnectedJars( world, gHomePos, markedTile );
		}

		// Reset checked
		this.checkedJars.clear();
	}

	/**
	 * Sets the target if the jar is valid, closer, and in range.
	 * 
	 * @param jar
	 * @param gHomePos
	 * @return 1 = Jar out of range, 2 = Jar already visited, 3 = Invalid jar, 4 = Set as target
	 */
	private int setTargetIfValid( final TileJarFillable jar, final ChunkCoordinates gHomePos )
	{
		if( !this.checkedJars.add( jar ) )
		{
			return 2;
		}

		// In range of golem?
		double distance = jar.getDistanceFrom( gHomePos.posX, gHomePos.posY, gHomePos.posZ );
		if( distance > this.golemRange )
		{
			return 1;
		}

		// If the current target is voiding, but the potential target is not,
		// then do not bother with the distance check.
		boolean isVoid = ( jar instanceof TileJarFillableVoid );
		if( !( this.targetVoid && !isVoid ) )
		{
			// Check distance
			if( distance > this.targetDistance )
			{
				return 1;
			}
		}

		// Check for essentia
		if( jar.aspect == null )
		{
			// Check for label
			if( jar.aspectFilter == null )
			{
				return 3;
			}
		}

		// Check amount
		if( !isVoid && ( jar.amount >= jar.maxAmount ) )
		{
			return 3;
		}

		// Valid target
		this.targetJar = jar;
		this.targetDistance = distance;
		this.targetVoid = isVoid;

		return 4;

	}

	@Override
	protected boolean needsNetworkNow()
	{
		// Determine the mode

		// Get the tile entity at the golems home
		ChunkCoordinates gHomePos = this.golem.getHomePosition();
		ForgeDirection side = ForgeDirection.getOrientation( this.golem.homeFacing );
		TileEntity te = this.golem.worldObj.getTileEntity( gHomePos.posX - side.offsetX,
			gHomePos.posY - side.offsetY,
			gHomePos.posZ - side.offsetZ );

		// Is the tile an essentia transport?
		this.modeFill = !( te instanceof IEssentiaTransport );

		// If the tile is not valid, check above it for alembic
		if( this.modeFill )
		{
			for( int i = 1; ( i < 6 ) && this.modeFill; ++i )
			{
				te = this.golem.worldObj.getTileEntity( gHomePos.posX - side.offsetX,
					( gHomePos.posY - side.offsetY ) + i,
					gHomePos.posZ - side.offsetZ );
				this.modeFill = !( te instanceof TileAlembic );
			}
		}

		// Fill mode
		if( this.modeFill )
		{
			// Needs network if the golem is not carrying anything, and there is a target jar
			if( this.golem.essentiaAmount > 0 )
			{
				return false;
			}

			// Golem is not carrying anything, find a jar
			this.findClosestValidJar();
			return( this.targetJar != null );
		}

		// Empty mode
		return( this.golem.getNavigator().noPath() && ( this.golem.essentiaAmount > 0 ) );
	}

	@Override
	public void updateTask()
	{
		// Fill mode
		if( this.modeFill )
		{
			// Get the wanted aspect
			Aspect wantedAspect = ( this.targetJar.aspectFilter != null ? this.targetJar.aspectFilter : this.targetJar.aspect );
			if( wantedAspect == null )
			{
				// Jar is invalid now
				this.targetJar = null;
				return;
			}

			// Get the capacity of the jar
			int amount = this.targetJar.maxAmount;

			// Is the jar not voiding
			if( !this.targetVoid )
			{
				// Calculate the max amount to extract
				amount -= this.targetJar.amount;
				if( amount < 1 )
				{
					// Jar is full
					this.targetJar = null;
					return;
				}
			}

			// Attempt extraction
			int extractedAmount = (int)this.network.extractEssentia( wantedAspect, amount, Actionable.MODULATE );
			if( extractedAmount <= 0 )
			{
				return;
			}

			// Set the golems essentia
			this.golem.essentia = wantedAspect;
			this.golem.essentiaAmount = extractedAmount;
		}
		else
		{
			// Mode empty
			if( ( this.golem.essentia == null ) || ( this.golem.essentiaAmount <= 0 ) || !this.golem.getNavigator().noPath() )
			{
				return;
			}

			// Attempt injection
			int injectedAmount = (int)this.network.insertEssentia( this.golem.essentia, this.golem.essentiaAmount );
			if( injectedAmount == 0 )
			{
				return;
			}

			// Update golem
			this.golem.essentiaAmount -= injectedAmount;
			if( this.golem.essentiaAmount <= 0 )
			{
				this.golem.essentia = null;
			}
		}

		this.golem.updateCarried();
	}

}
