package com.blue.thaumicenergistics.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;



public interface IThEItemDescription
{
    /**
     * Gets the block of this item, if it has one.
     *
     * @return
     */
    @Nullable
    Block getBlock();

    /**
     * Gets the damage, or meta, value of the item.
     *
     * @return
     */

    int getDamage();

    /**
     * Gets the item.
     *
     * @return
     */

    @Nonnull
    Item getItem();

    /**
     * Gets a stack of size 1.
     *
     * @return
     */

    @Nonnull
    ItemStack getStack();

    /**
     * Gets a stack of the specified size.
     *
     * @param amount
     * @return
     */
    @Nonnull
    ItemStack getStacks( int amount );
}
