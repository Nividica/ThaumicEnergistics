package thaumicenergistics.aspect;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
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
	public long amount;

	/**
	 * Creates an empty stack
	 */
	public AspectStack()
	{
		this.aspect = null;
		this.amount = 0;
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

		this.amount = amount;
	}

	/**
	 * Sets this stacks values to match that of the specified stack.
	 * 
	 * @param source
	 */
	public AspectStack( final AspectStack source )
	{
		this.aspect = source.aspect;

		this.amount = source.amount;
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
	 * Creates a copy of this stack and returns it.
	 * 
	 * @return Copy of the stack.
	 */
	public AspectStack copy()
	{
		return new AspectStack( this );
	}

	/**
	 * Gets the display name for the aspect.
	 * 
	 * @return Aspect name, or empty string if no aspect.
	 */
	public String getAspectName( final EntityPlayer player )
	{
		// Do we have an aspect?
		if( this.aspect == null )
		{
			return "";
		}

		// Have we researched the aspect?
		if( !this.hasPlayerDiscovered( player ) )
		{
			return StatCollector.translateToLocal( "tc.aspect.unknown" );
		}

		return this.aspect.getName();
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
			return GuiHelper.instance.getAspectChatColor( this.aspect );
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

			nbt.setLong( "Amount", this.amount );
		}

		return nbt;
	}
}
