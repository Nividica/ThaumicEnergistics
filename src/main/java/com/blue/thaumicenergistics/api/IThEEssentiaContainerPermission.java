package com.blue.thaumicenergistics.api;

public interface IThEEssentiaContainerPermission
{
    /**
     * Can the container be partially filled?
     */
    boolean canHoldPartialAmount();

    /**
     * The maximum amount this container can hold
     */
    int maximumCapacity();
}
