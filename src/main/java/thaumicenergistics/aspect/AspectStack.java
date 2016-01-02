package thaumicenergistics.aspect;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.network.packet.ThEBasePacket;
import thaumicenergistics.util.GuiHelper;

/**
 * Stores an aspect and an amount.
 * 
 * @author Nividica
 * 
 */
public class AspectStack
	implements IAspectStack
{
	private static final String NBTKEY_ASPECT_TAG = "AspectTag", NBTKEY_ASPECT_AMOUNT = "Amount", NBTKEY_CRAFTABLE = "Craftable";

	/**
	 * The aspect this stack contains.
	 */
	@Nullable
	protected Aspect aspect;

	/**
	 * The amount this stack contains
	 */
	protected long stackSize;

	/**
	 * True if the aspect is craftable.
	 */
	protected boolean isCraftable;

	/**
	 * Creates an empty stack
	 */
	public AspectStack()
	{
		this( null, 0, false );
	}

	/**
	 * Creates a stack using the specified aspect and amount.
	 * Defaults to not craftable.
	 * 
	 * @param aspect
	 * What aspect this stack will have.
	 * @param amount
	 * How much this stack will have.
	 */
	public AspectStack( final Aspect aspect, final long amount )
	{
		this( aspect, amount, false );
	}

	/**
	 * Creates a stack using the specified aspect and amount, and
	 * sets if it is craftable.
	 * 
	 * @param aspect
	 * What aspect this stack will have.
	 * @param size
	 * How much this stack will have.
	 * @param craftable
	 * Is the stack craftable.
	 */
	public AspectStack( final Aspect aspect, final long size, final boolean craftable )
	{
		this.setAll( aspect, size, craftable );
	}

	/**
	 * Creates a new stack from the passed stack.
	 * If a new stack is not needed you can also use copyFrom().
	 * 
	 * @param source
	 */
	public AspectStack( final AspectStack stack )
	{
		this.setAll( stack );
	}

	/**
	 * Creates an aspect stack from a NBT compound tag.
	 * 
	 * @param data
	 * Tag to load from
	 * @return Created stack, or null.
	 */
	public static AspectStack loadAspectStackFromNBT( final NBTTagCompound data )
	{
		Aspect aspect = null;

		// Does the tag have the tag?
		if( data.hasKey( NBTKEY_ASPECT_TAG ) )
		{
			// Attempt to get the aspect
			aspect = Aspect.aspects.get( data.getString( NBTKEY_ASPECT_TAG ) );
		}

		// Is there an aspect?
		if( aspect == null )
		{
			return null;
		}

		// Load the amount
		long amount = 0;
		if( data.hasKey( NBTKEY_ASPECT_AMOUNT ) )
		{
			amount = data.getLong( NBTKEY_ASPECT_AMOUNT );
		}

		// Load crafting
		boolean craftable = data.hasKey( NBTKEY_CRAFTABLE );

		// Return a newly created stack.
		return new AspectStack( aspect, amount, craftable );
	}

	/**
	 * Creates an aspect stack from the stream.
	 * 
	 * @param stream
	 * @return
	 */
	public static AspectStack loadAspectStackFromStream( final ByteBuf stream )
	{
		// Create the stack
		AspectStack stack = new AspectStack();

		// Read in the values
		stack.readFromStream( stream );

		// Return the stack
		return stack;
	}

	@Override
	public long adjustStackSize( final long delta )
	{
		this.stackSize += delta;
		return this.stackSize;
	}

	@Override
	public IAspectStack copy()
	{
		return new AspectStack( this );
	}

	@Override
	public Aspect getAspect()
	{
		return this.aspect;
	}

	@Override
	public String getAspectDescription()
	{
		// Is there an aspect?
		if( this.aspect != null )
		{
			// Return it's description
			return this.aspect.getLocalizedDescription();
		}

		// Return empty string
		return "";
	}

	@Override
	public String getAspectName()
	{
		// Ensure there is an aspect
		if( this.aspect == null )
		{
			return "";
		}

		// Return the name of the aspect
		return this.aspect.getName();
	}

	@Override
	public String getAspectName( final EntityPlayer player )
	{
		// Has the player discovered the aspect?
		if( !this.hasPlayerDiscovered( player ) )
		{
			return StatCollector.translateToLocal( "tc.aspect.unknown" );
		}

		return this.getAspectName();
	}

	@Override
	public String getAspectTag()
	{
		// Do we have an aspect?
		if( this.aspect != null )
		{
			return this.aspect.getTag();
		}

		return "";
	}

	@Override
	public String getChatColor()
	{
		// Do we have an aspect?
		if( this.aspect != null )
		{
			return GuiHelper.INSTANCE.getAspectChatColor( this.aspect );
		}

		return "";
	}

	@Override
	public boolean getCraftable()
	{
		return this.isCraftable;
	}

	@Override
	public long getStackSize()
	{
		return this.stackSize;
	}

	@Override
	public boolean hasAspect()
	{
		return( this.aspect != null );
	}

	@Override
	public boolean hasPlayerDiscovered( final EntityPlayer player )
	{
		// Ensure we have a player & aspect
		if( ( player != null ) && ( this.aspect != null ) )
		{
			// Ask Thaumcraft if the player has discovered the aspect
			return Thaumcraft.proxy.getPlayerKnowledge().hasDiscoveredAspect( player.getCommandSenderName(), this.aspect );
		}

		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return( this.stackSize <= 0 );
	}

	@Override
	public void readFromStream( final ByteBuf stream )
	{
		// Read the aspect
		this.aspect = ThEBasePacket.readAspect( stream );

		// Read the amount
		this.stackSize = stream.readLong();

		// Read craftable
		this.isCraftable = stream.readBoolean();
	}

	@Override
	public void setAll( final Aspect aspect, final long size, final boolean craftable )
	{
		this.aspect = aspect;
		this.stackSize = size;
		this.isCraftable = craftable;
	}

	@Override
	public void setAll( final IAspectStack stack )
	{
		if( stack != null )
		{
			this.aspect = stack.getAspect();
			this.stackSize = stack.getStackSize();
			this.isCraftable = stack.getCraftable();
		}
	}

	@Override
	public void setAspect( final Aspect aspect )
	{
		this.aspect = aspect;
	}

	@Override
	public void setCraftable( final boolean craftable )
	{
		this.isCraftable = craftable;
	}

	@Override
	public void setStackSize( final long size )
	{
		this.stackSize = size;
	}

	@Override
	public NBTTagCompound writeToNBT( final NBTTagCompound data )
	{
		// Is there an aspect?
		if( this.aspect != null )
		{
			// Write the tag
			data.setString( NBTKEY_ASPECT_TAG, this.aspect.getTag() );

			// Write the amount
			if( this.stackSize > 0 )
			{
				data.setLong( NBTKEY_ASPECT_AMOUNT, this.stackSize );
			}

			// Craftable?
			if( this.isCraftable )
			{
				// Write craftable
				data.setBoolean( NBTKEY_CRAFTABLE, true );
			}
		}

		return data;
	}

	@Override
	public void writeToStream( final ByteBuf stream )
	{
		// Write the aspect
		ThEBasePacket.writeAspect( this.aspect, stream );

		// Write the stored amount
		stream.writeLong( this.stackSize );

		// Write if craftable
		stream.writeBoolean( this.isCraftable );
	}
}
