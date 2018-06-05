package com.blue.thaumicenergistics.api;

import java.lang.reflect.Method;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;


public abstract class ThEApi
{
    protected static ThEApi api = null;


    @Nullable
    public static ThEApi instance()
    {
        if( ThEApi.api == null )
        {
            try
            {
                Class clazz = Class.forName( "thaumicenergistics.implementation.ThEAPIImplementation" );

                Method instanceAccessor = clazz.getMethod( "instance" );

                ThEApi.api = (ThEApi)instanceAccessor.invoke(null);
            }
            catch( Throwable e )
            {
                return null;
            }
        }

        return ThEApi.api;
    }

    @Nonnull
    public abstract IThEBlocks blocks();


    @Nonnull
    public abstract IThEConfig config();


    @Nonnull
    public abstract ImmutableList<List<IThEEssentiaGas>> essentiaGases();


    @Nonnull
    public abstract IThEInteractionHelper interact();


    @Nonnull
    public abstract IThEItems items();


    @Nonnull
    public abstract IThEParts parts();


    @Nonnull
    public abstract IThETransportPermissions transportPermissions();

}
