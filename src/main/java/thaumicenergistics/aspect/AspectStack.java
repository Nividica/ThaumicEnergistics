package thaumicenergistics.aspect;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumicenergistics.network.packet.ThEBasePacket;
import thaumicenergistics.util.GuiHelper;

/**
 * Stores an aspect and an amount.
 * 
 * @author Nividica
 * 
 */
public class AspectStack
{
	private static final String NBTKEY_ASPECT_TAG = "AspectTag", NBTKEY_ASPECT_AMOUNT = "Amount", NBTKEY_CRAFTABLE = "Craftable";

	/**
	 * The aspect this stack contains.
	 */
	public Aspect aspect;

	/**
	 * The amount this stack contains
	 */
	public long stackSize;

	/**
	 * True if the aspect is craftable.
	 */
	public boolean isCraftable;

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
	 * @param amount
	 * How much this stack will have.
	 * @param isCraftable
	 * Is the stack craftable.
	 */
	public AspectStack( final Aspect aspect, final long amount, final boolean isCraftable )
	{
		// Set the aspect
		this.aspect = aspect;

		// Set the stack size
		this.stackSize = amount;

		// Set craftable
		this.isCraftable = isCraftable;
	}

	/**
	 * Creates a new stack from the passed stack.
	 * If a new stack is not needed you can also use copyFrom().
	 * 
	 * @param source
	 */
	public AspectStack( final AspectStack stack )
	{
		this.copyFrom( stack );
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

	/**
	 * Creates a copy of this stack and returns it.
	 * 
	 * @return Copy of the stack.
	 */
	public AspectStack copy()
	{
		return new AspectStack( this );
	}

	/**
	 * Sets the values of this stack to match the passed stack.
	 * Unchanged if stack is null.
	 * 
	 * @param stack
	 */
	public void copyFrom( final AspectStack stack )
	{
		if( stack != null )
		{
			this.aspect = stack.aspect;
			this.stackSize = stack.stackSize;
			this.isCraftable = stack.isCraftable;
		}
	}

	/**
	 * Gets the name of the aspect.<br>
	 * This method does not take into consideration if the player has yet discovered the aspect.
	 * 
	 * @return
	 */
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

	/**
	 * Gets the display name of the aspect for the player.
	 * 
	 * @param player
	 * @return
	 */
	public String getAspectName( final EntityPlayer player )
	{
		// Has the player discovered the aspect?
		if( !this.hasPlayerDiscovered( player ) )
		{
			return StatCollector.translateToLocal( "tc.aspect.unknown" );
		}

		return this.getAspectName();
	}

	/**
	 * The chat color associated with this aspect.
	 * 
	 * @return Chat prefix string, or empty string if no aspect.
	 */
	public String getChatColor()
	{
		// Do we have an aspect?
		if( this.aspect != null )
		{
			return GuiHelper.INSTANCE.getAspectChatColor( this.aspect );
		}

		return "";
	}

	/**
	 * Gets the Thaumcraft tag for the aspect
	 * 
	 * @return Thaumcraft tag, or empty string if no aspect.
	 */
	public String getTag()
	{
		// Do we have an aspect?
		if( this.aspect != null )
		{
			return this.aspect.getTag();
		}

		return "";
	}

	/**
	 * Checks if the player has discovered this aspect.
	 * 
	 * @param player
	 * @return
	 */
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

	/**
	 * Returns true if the size is not positive.
	 * 
	 * @return
	 */
	public boolean isEmpty()
	{
		return( this.stackSize <= 0 );
	}

	/**
	 * Sets this stack to the data in the stream.
	 * 
	 * @param stream
	 */
	public void readFromStream( final ByteBuf stream )
	{
		// Read the aspect
		this.aspect = ThEBasePacket.readAspect( stream );

		// Read the amount
		this.stackSize = stream.readLong();

		// Read craftable
		this.isCraftable = stream.readBoolean();
	}

	/**
	 * Writes this aspect stack to the specified NBT tag
	 * 
	 * @param data
	 * The tag to write to
	 * @return The nbt tag.
	 */
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

	/**
	 * Writes the stack to a bytebuf stream.
	 * 
	 * @param stream
	 */
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
