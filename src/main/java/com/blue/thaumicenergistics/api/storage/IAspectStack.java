package com.blue.thaumicenergistics.api.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.aspects.Aspect;

/**
 * A stack, or collection, of a single aspect.
 */

public interface IAspectStack
{
    /**
     * Changes the stack size by the delta amount and returns the new stack size.
     *
     * @param delta
     * @return Adjusted stack size.
     */
    long adjustStackSize( long delta );

    /**
     * Creates a copy of this stack and returns it.
     *
     * @return Copy of the stack.
     */
    @Nonnull
    IAspectStack copy();

    /**
     * Returns the aspect that is stored.
     *
     * @return
     */
    @Nullable
    Aspect getAspect();

    /**
     * Gets the Thaumcraft description for the aspect.
     *
     * @return Localized description, or empty string if no aspect.
     */
    @Nonnull
    String getAspectDesc();

    /**
     * Gets the name of the aspect.<br>
     * This method does not take into consideration if the player has yet discovered the aspect.
     *
     * @return
     */
    @Nonnull
    String getAspectName();

    /**
     * Gets the display name of the aspect for the player.
     *
     * @param player
     * @return
     */
    @Nonnull
    String getAspectName( @Nullable EntityPlayer player );

    /**
     * gets the Thaumcraft tag for the aspect
     *
     * @return Thaumcraft tag, or empty string if no aspect.
     */
    @Nonnull
    String getAspectTag();

    /**
     * The chat color associated with this aspect.
     *
     * @return Chat prefix string, or empty string if no aspect.
     */
    @Nonnull
    String getChatColor();

    /**
     * Returns true if the aspect is craftable.
     *
     * @return
     */
    boolean getCraftable();

    /**
     * Returns the stack size.
     *
     * @return
     */
    long getStackSize();

    /**
     * Returns true if the stack has a non-null aspect set.
     *
     * @return
     */
    boolean hasAspect();

    /**
     * Checks if the player has discovered this aspect.
     *
     * @param player
     * @return
     */
    boolean hasPlayerDiscovered( @Nullable EntityPlayer player );

    /**
     * Returns true if the size is not positive
     *
     * @return
     */
    boolean isEmpty();

    /**
     *Sets this stack to the data in the stream.
     *
     * @param stream
     */
    void readFromStream( @Nonnull ByteBuf stream );

    /**
     * Sets everything.
     *
     * @param aspect
     * @param size
     * @param craftable
     */
    void setAll( @Nullable Aspect aspect, long size, boolean craftable );

    /**
     * Sets the values of this stack to match the passed stack.<br>
     * If the stack is null, all values are reset.
     *
     * @param stack
     */
    void setAll( @Nullable IAspectStack stack );

    /**
     * Sets the aspect for the stack.
     *
     * @param aspect
     */
    void setAspect( @Nullable Aspect aspect );

    /**
     * Sets if the aspect is craftable.
     *
     * @param craftable
     */
    void setCraftable( boolean craftable );

    /**
     * Sets the size of the stack.
     *
     * @param size
     */
    void setStackSize( long size );

    /**
     * Writes this aspect stack to the specified NBT tag
     *
     * @param data
     * The tag to write to
     * @return The nbt tag passed in.
     */
    @Nonnull
    NBTTagCompound writeToNBT( @Nonnull NBTTagCompound data );

    /**
     * Writes the stack to a bytebuf stream.
     *
     * @param stream
     */
    void writeToStream( @Nonnull ByteBuf stream );
}
