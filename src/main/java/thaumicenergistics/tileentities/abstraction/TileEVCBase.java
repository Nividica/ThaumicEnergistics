package thaumicenergistics.tileentities.abstraction;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.integration.tc.IEssentiaTransportWithSimulate;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.config.Actionable;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Essentia Vibration Chamber Base
 * Handles most of the mod-interface functionality.
 * 
 * @author Nividica
 * 
 */
public abstract class TileEVCBase
	extends AENetworkTile
	implements IEssentiaTransportWithSimulate, IAspectSource
{
	/**
	 * NBT Key for the stored aspect stack.
	 */
	public static final String NBTKEY_STORED = "StoredEssentia";

	/**
	 * The maximum amount of stored essentia.
	 */
	public static final int MAX_ESSENTIA_STORED = 64;

	/**
	 * Stored Essentia
	 */
	protected AspectStack storedEssentia = null;

	/**
	 * Returns true if the EVC accepts the specified aspect.
	 * 
	 * @param aspect
	 * @return
	 */
	public static boolean acceptsAspect( final Aspect aspect )
	{
		return( ( aspect == Aspect.FIRE ) || ( aspect == Aspect.ENERGY ) );
	}

	/**
	 * 1-100: Ignis, 100-200: Potentia
	 */
	//protected int suctionRotationTimer = 1;

	protected abstract int addEssentia( final Aspect aspect, final int amount, final Actionable mode );

	@Override
	protected ItemStack getItemFromTile( final Object obj )
	{
		// Return the itemstack that visually represents this tile
		return ThEApi.instance().blocks().EssentiaVibrationChamber.getStack();

	}

	/**
	 * Returns true if there is any stored essentia.
	 * 
	 * @return
	 */
	protected boolean hasStoredEssentia()
	{
		return ( this.storedEssentia != null ) && ( !this.storedEssentia.isEmpty() );
	}

	protected abstract void NBTRead( NBTTagCompound data );

	protected abstract void NBTWrite( NBTTagCompound data );

	@SideOnly(Side.CLIENT)
	protected abstract void networkRead( ByteBuf stream );

	protected abstract void networkWrite( ByteBuf stream );

	@Override
	public int addEssentia( final Aspect aspect, final int amount, final ForgeDirection side )
	{
		return this.addEssentia( aspect, amount, Actionable.MODULATE );
	}

	@Override
	public int addEssentia( final Aspect aspect, final int amount, final ForgeDirection side, final Actionable mode )
	{
		return this.addEssentia( aspect, amount, mode );
	}

	@Override
	public int addToContainer( final Aspect aspect, final int amount )
	{
		return this.addEssentia( aspect, amount, Actionable.MODULATE );
	}

	@Override
	public boolean canInputFrom( final ForgeDirection side )
	{
		return( side != this.getForward() );
	}

	/**
	 * Can not output.
	 */
	@Override
	public boolean canOutputTo( final ForgeDirection side )
	{
		return false;
	}

	@Override
	public int containerContains( final Aspect aspect )
	{
		int storedAmount = 0;

		// Is the aspect stored?
		if( ( this.hasStoredEssentia() ) && ( this.storedEssentia.aspect == aspect ) )
		{
			storedAmount = (int)this.storedEssentia.stackSize;
		}

		return storedAmount;
	}

	@Override
	public boolean doesContainerAccept( final Aspect aspect )
	{
		// Is there stored essentia?
		if( this.hasStoredEssentia() )
		{
			// Match to stored essentia
			return aspect == this.storedEssentia.aspect;
		}

		// Nothing is stored, accepts ignis or potentia
		return TileEVCBase.acceptsAspect( aspect );
	}

	@Deprecated
	@Override
	public boolean doesContainerContain( final AspectList aspectList )
	{
		// Is there not stored essentia?
		if( !this.hasStoredEssentia() )
		{
			return false;
		}

		return aspectList.aspects.containsKey( this.storedEssentia.aspect );
	}

	@Override
	public boolean doesContainerContainAmount( final Aspect aspect, final int amount )
	{
		// Does the stored essentia match the aspect?
		if( ( this.storedEssentia == null ) || ( this.storedEssentia.aspect != aspect ) )
		{
			// Does not match
			return false;
		}

		return( this.storedEssentia.stackSize >= amount );
	}

	@Override
	public AspectList getAspects()
	{
		// Create a new list
		AspectList aspectList = new AspectList();

		// Is there stored essentia?
		if( this.hasStoredEssentia() )
		{
			// Add the essentia aspect and amount
			aspectList.add( this.storedEssentia.aspect, (int)this.storedEssentia.stackSize );
		}

		return aspectList;
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.COVERED;
	}

	@Override
	public int getEssentiaAmount( final ForgeDirection side )
	{
		return( this.hasStoredEssentia() ? (int)this.storedEssentia.stackSize : 0 );
	}

	@Override
	public Aspect getEssentiaType( final ForgeDirection side )
	{
		return( this.hasStoredEssentia() ? this.storedEssentia.aspect : null );
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	/**
	 * Can not output.
	 */
	@Override
	public int getMinimumSuction()
	{
		return 0;
	}

	@Override
	public int getSuctionAmount( final ForgeDirection side )
	{
		int suction = 0;

		// Is there anything stored?
		if( this.storedEssentia != null )
		{
			// Not Full?
			if( this.storedEssentia.stackSize < TileEVCBase.MAX_ESSENTIA_STORED )
			{
				// Full suction when stored but not full.
				suction = 128;
			}
		}
		else
		{
			// Less suction when nothing stored.
			suction = 100;
		}

		return suction;
	}

	@Override
	public Aspect getSuctionType( final ForgeDirection side )
	{
		// Default to Ignis
		Aspect suction = Aspect.FIRE;

		// Is there anything stored?
		if( this.hasStoredEssentia() )
		{
			// Suction type must match what is stored
			suction = this.storedEssentia.aspect;
		}
		else
		{
			// Rotate into Potentia?
			if( ( MinecraftServer.getServer().getTickCounter() % 200 ) > 100 )
			{
				// Set to Potentia
				suction = Aspect.ENERGY;
			}
		}

		return suction;
	}

	@Override
	public boolean isConnectable( final ForgeDirection side )
	{
		return( side != this.getForward() );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void onNBTLoad( final NBTTagCompound data )
	{
		// Is there essentia stored?
		if( data.hasKey( TileEVCBase.NBTKEY_STORED ) )
		{
			// Load the stack
			this.storedEssentia = AspectStack.loadAspectStackFromNBT( data.getCompoundTag( TileEVCBase.NBTKEY_STORED ) );
		}

		// Call sub
		this.NBTRead( data );
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void onNBTSave( final NBTTagCompound data )
	{
		// Save storage
		if( this.hasStoredEssentia() )
		{
			// Save stack
			NBTTagCompound stack = new NBTTagCompound();
			this.storedEssentia.writeToNBT( stack );

			// Write into data
			data.setTag( TileEVCBase.NBTKEY_STORED, stack );
		}

		// Call sub
		this.NBTWrite( data );
	}

	@TileEvent(TileEventType.NETWORK_READ)
	@SideOnly(Side.CLIENT)
	public boolean onNetworkRead( final ByteBuf stream )
	{
		// Anything stored?
		if( stream.readBoolean() )
		{
			// Is the local copy null?
			if( this.storedEssentia == null )
			{
				// Create the stack from the stream
				this.storedEssentia = AspectStack.loadAspectStackFromStream( stream );
			}
			else
			{
				// Update the stack from the stream
				this.storedEssentia.readFromStream( stream );
			}
		}
		else
		{
			// Null out the stack
			this.storedEssentia = null;
		}

		// Call sub
		this.networkRead( stream );

		return true;
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void onNetworkWrite( final ByteBuf stream ) throws IOException
	{
		// Is there anything stored?
		boolean hasStored = this.storedEssentia != null;

		// Write stored
		stream.writeBoolean( hasStored );
		if( hasStored )
		{
			// Write the stack
			this.storedEssentia.writeToStream( stream );
		}

		// Call sub
		this.networkWrite( stream );
	}

	/**
	 * Sets up the chamber
	 * 
	 * @return
	 */
	@Override
	public void onReady()
	{
		super.onReady();

		// Ignored on client side
		if( EffectiveSide.isServerSide() )
		{
			// Set idle power usage to zero
			this.gridProxy.setIdlePowerUsage( 0.0D );
		}
	}

	/**
	 * Full block, not extension needed.
	 */
	@Override
	public boolean renderExtendedTube()
	{
		return false;
	}

	@Override
	public void setAspects( final AspectList aspectList )
	{
		// Ignored
	}

	/**
	 * Sets the owner of this tile.
	 * 
	 * @param player
	 */
	public void setOwner( final EntityPlayer player )
	{
		this.gridProxy.setOwner( player );
	}

	@Override
	public void setSuction( final Aspect aspect, final int amount )
	{
		// Ignored
	}

	/**
	 * Can not output.
	 */
	@Override
	public int takeEssentia( final Aspect aspect, final int amount, final ForgeDirection side )
	{
		return 0;
	}

	/**
	 * Can not output.
	 */
	@Override
	public boolean takeFromContainer( final Aspect aspect, final int amount )
	{
		return false;
	}

	/**
	 * Can not output.
	 */
	@Deprecated
	@Override
	public boolean takeFromContainer( final AspectList arg0 )
	{
		return false;
	}
}
