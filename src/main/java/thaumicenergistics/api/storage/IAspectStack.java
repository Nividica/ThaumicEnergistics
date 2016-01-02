package thaumicenergistics.api.storage;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.aspects.Aspect;

public interface IAspectStack
{
	/**
	 * Changes the stack size by the delta amount and
	 * returns the new stack size.
	 * 
	 * @param delta
	 * @return Adjusted stack size.
	 */
	public long adjustStackSize( long delta );

	/**
	 * Creates a copy of this stack and returns it.
	 * 
	 * @return Copy of the stack.
	 */
	@Nonnull
	public IAspectStack copy();

	/**
	 * Returns the aspect that is stored.
	 * 
	 * @return
	 */
	@Nullable
	public Aspect getAspect();

	/**
	 * Gets the Thaumcraft description for the aspect.
	 * 
	 * @return Localized description, or empty string if no aspect.
	 */
	public String getAspectDescription();

	/**
	 * Gets the name of the aspect.<br>
	 * This method does not take into consideration if the player has yet discovered the aspect.
	 * 
	 * @return
	 */
	@Nonnull
	public String getAspectName();

	/**
	 * Gets the display name of the aspect for the player.
	 * 
	 * @param player
	 * @return
	 */
	@Nonnull
	public String getAspectName( @Nullable EntityPlayer player );

	/**
	 * Gets the Thaumcraft tag for the aspect
	 * 
	 * @return Thaumcraft tag, or empty string if no aspect.
	 */
	@Nonnull
	public String getAspectTag();

	/**
	 * The chat color associated with this aspect.
	 * 
	 * @return Chat prefix string, or empty string if no aspect.
	 */
	@Nonnull
	public String getChatColor();

	/**
	 * Returns true if the aspect is craftable.
	 * 
	 * @return
	 */
	public boolean getCraftable();

	/**
	 * Returns the stack size.
	 * 
	 * @return
	 */
	public long getStackSize();

	/**
	 * Returns true if the stack has a non-null aspect set.
	 * 
	 * @return
	 */
	public boolean hasAspect();

	/**
	 * Checks if the player has discovered this aspect.
	 * 
	 * @param player
	 * @return
	 */
	public boolean hasPlayerDiscovered( @Nullable EntityPlayer player );

	/**
	 * Returns true if the size is not positive.
	 * 
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * Sets this stack to the data in the stream.
	 * 
	 * @param stream
	 */
	public void readFromStream( @Nonnull ByteBuf stream );

	/**
	 * Sets everything.
	 * 
	 * @param aspect
	 * @param size
	 * @param craftable
	 */
	public void setAll( @Nullable Aspect aspect, long size, boolean craftable );

	/**
	 * Sets the values of this stack to match the passed stack.
	 * Unchanged if stack is null.
	 * 
	 * @param stack
	 */
	public void setAll( @Nullable IAspectStack stack );

	/**
	 * Sets the aspect for the stack.
	 * 
	 * @param aspect
	 */
	public void setAspect( @Nullable Aspect aspect );

	/**
	 * Sets if the aspect is craftable.
	 * 
	 * @param craftable
	 */
	public void setCraftable( boolean craftable );

	/**
	 * Sets the size of the stack.
	 * 
	 * @param size
	 */
	public void setStackSize( long size );

	/**
	 * Writes this aspect stack to the specified NBT tag
	 * 
	 * @param data
	 * The tag to write to
	 * @return The nbt tag.
	 */
	public NBTTagCompound writeToNBT( @Nonnull NBTTagCompound data );

	/**
	 * Writes the stack to a bytebuf stream.
	 * 
	 * @param stream
	 */
	public void writeToStream( @Nonnull ByteBuf stream );

}
