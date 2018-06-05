package com.blue.thaumicenergistics.api.storage;

import net.minecraft.inventory.IInventory;



public interface IInventoryUpdateReciever
{
    /**
     * Called when an inventory is changed.
     *
     * @param sourceInventory
     */
    public void onInventoryChanged( IInventory sourceInventory );
}
