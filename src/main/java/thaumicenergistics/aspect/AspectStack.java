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
	/**
	 * The aspect this stack contains.
	 */
	public Aspect aspect;

	/**
	 * The amount this stack contains
	 */
	public long stackSize;

	/**
	 * Creates an empty stack
	 */
	public AspectStack()
	{
		this.aspect = null;
		this.stackSize = 0;
	}

	/**
	 * Creates a stack using the specified aspect and amount.
	 * 
	 * @param aspect
	 * What aspect this stack will have.
	 * @param amount
	 * How much this stack will have.
	 */
	public AspectStack( final Aspect aspect, final long amount )
	{
		this.aspect = aspect;

		this.stackSize = amount;
	}

	/**
	 * Sets this stacks values to match that of the specified stack.
	 * 
	 * @param source
	 */
	public AspectStack( final AspectStack source )
	{
		this.aspect = source.aspect;

		this.stackSize = source.stackSize;
	}

	/**
	 * Creates an aspect stack from a NBT compound tag.
	 * 
	 * @param nbt
	 * Tag to load from
	 * @return Created stack, or null.
	 */
	public static AspectStack loadAspectStackFromNBT( final NBTTagCompound nbt )
	{
		// Attempt to get the aspect
		Aspect aspect = Aspect.aspects.get( nbt.getString( "AspectTag" ) );

		// Did we get an aspect?
		if( aspect == null )
		{
			return null;
		}

		// Load the amount
		long amount = nbt.getLong( "Amount" );

		// Return a newly created stack.
		return new AspectStack( aspect, amount );
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
		boolean hasDiscovered = false;

		// Ensure we have a player
		if( player != null )
		{
			// Ensure we have an aspect
			if( this.aspect != null )
			{
				// Ask Thaumcraft if the player has discovered the aspect
				hasDiscovered = Thaumcraft.proxy.getPlayerKnowledge().hasDiscoveredAspect( player.getCommandSenderName(), this.aspect );
			}
		}

		return hasDiscovered;
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
	}

	/**
	 * Writes this aspect stack to the specified NBT tag
	 * 
	 * @param nbt
	 * The tag to write to
	 * @return The nbt tag.
	 */
	public NBTTagCompound writeToNBT( final NBTTagCompound nbt )
	{
		// Do we have an aspect?
		if( this.aspect != null )
		{
			nbt.setString( "AspectTag", this.aspect.getTag() );

			nbt.setLong( "Amount", this.stackSize );
		}

		return nbt;
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
	}
}
