package thaumicenergistics.common.integration.tc;

import java.util.HashMap;
import java.util.HashSet;
import javax.annotation.Nonnull;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumicenergistics.api.entities.IGolemHookHandler;
import thaumicenergistics.api.entities.IGolemHookHandler.InteractionLevel;
import thaumicenergistics.api.entities.IGolemHookSyncRegistry;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.common.utils.ThELog;
import thaumicenergistics.fml.ThECore;

/**
 * Golem hook system.<br>
 * Manages golem hooks and handlers.
 *
 * @author Nividica
 *
 */
public class GolemHooks
{
	private static class DummyHookHandler
		implements IGolemHookHandler
	{

		public DummyHookHandler()
		{
		}

		@Override
		public void addDefaultSyncEntries( final IGolemHookSyncRegistry syncRegistry )
		{

		}

		@Override
		public void bellLeftClicked(	final EntityGolemBase golem, final Object handlerData, final ItemStack itemGolemPlacer,
										final EntityPlayer player, final boolean dismantled,
										final Side side )
		{

		}

		@Override
		public InteractionLevel canHandleInteraction(	final EntityGolemBase golem, final Object handlerData, final EntityPlayer player,
														final Side side )
		{
			return InteractionLevel.NoInteraction;
		}

		@Override
		public Object customInteraction(	final EntityGolemBase golem, final Object handlerData, final IGolemHookSyncRegistry syncData,
											final EntityPlayer player, final Side side )
		{
			return null;
		}

		@Override
		public void golemTick( final EntityGolemBase golem, final Object serverHandlerData, final IGolemHookSyncRegistry syncData )
		{
		}

		@Override
		public boolean needsDynamicUpdates()
		{
			return false;
		}

		@Override
		public boolean needsRenderer()
		{
			return false;
		}

		@Override
		public Object readEntityFromNBT( final EntityGolemBase golem, final NBTTagCompound nbtTag )
		{
			return null;
		}

		@Override
		public void renderGolem(	final EntityGolemBase golem, final Object clientHandlerData, final double x, final double y, final double z,
									final float partialElaspsedTick )
		{

		}

		@Override
		public Object setupGolem( final EntityGolemBase golem, final Object handlerData, final IGolemHookSyncRegistry syncData, final Side side )
		{
			return null;
		}

		@Override
		public Object spawnGolemFromItemStack( final EntityGolemBase golem, final ItemStack itemGolemPlacer, final Side side )
		{
			return null;
		}

		@Override
		public Object syncDataChanged( final IGolemHookSyncRegistry syncData, final Object clientHandlerData )
		{
			return null;
		}

		@Override
		public void writeEntityNBT( final EntityGolemBase golem, final Object serverHandlerData, final NBTTagCompound nbtTag )
		{

		}

	}

	/**
	 * ID of the datawatcher field.
	 */
	private static int DATAWATCHER_ID = -1;

	/**
	 * Default sync values.
	 */
	private static final GolemSyncRegistry defaultSyncRegistry = new GolemSyncRegistry();

	/**
	 * Internal handler used to track sync data.
	 */
	private static final DummyHookHandler internalHandler = new DummyHookHandler();

	/**
	 * All hook handlers
	 */
	protected static final HashSet<IGolemHookHandler> registeredHandlers = new HashSet<IGolemHookHandler>();

	/**
	 * Handlers that need to be called during tick event.
	 */
	protected static final HashSet<IGolemHookHandler> dynamicHandlers = new HashSet<IGolemHookHandler>();

	/**
	 * Handlers that need to be called during render.
	 */
	protected static final HashSet<IGolemHookHandler> renderHandlers = new HashSet<IGolemHookHandler>();

	/**
	 * Logs an exception when it occurs.
	 *
	 * @param hook
	 * @param handler
	 * @param e
	 */
	private static void logCaughtException( final String hook, final IGolemHookHandler handler, final Exception e )
	{
		if( handler != null )
		{
			if( e != null )
			{
				ThELog.warning( "Caught Exception During call to '" + hook + "' for handler '%s':", handler.getClass().getCanonicalName() );
				ThELog.warning( e.toString() );
			}
			else
			{
				ThELog.warning( "Caught Unknown Exception During call to '" + hook + "' for handler '%s'", handler.getClass().getCanonicalName() );
			}
		}
		else if( e != null )
		{
			ThELog.warning( "Caught Exception During call to null handler '" + hook + "':" );
			ThELog.warning( e.toString() );
		}
		else
		{
			ThELog.warning( "Caught Unknown Exception During call to null handler '" + hook + "'" );
		}

	}

	/**
	 * Hook for ItemGolemBell.onLeftClickEntity
	 *
	 * @param golem
	 * @param dropped
	 * @param player
	 * @param golemHandlerData
	 */
	public static void hook_Bell_OnLeftClickGolem(	final EntityGolemBase golem, final ItemStack dropped, final EntityPlayer player,
													final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		// Get the dismantled status
		boolean dismantled = player.isSneaking();
		Side side = EffectiveSide.side();

		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );

				// Call handler

				handler.bellLeftClicked( golem, handlerData, dropped, player, dismantled, side );
			}
			catch( Exception e )
			{
				logCaughtException( "onBellLeftClick", handler, e );
			}

		}

	}

	/**
	 * Hook for EntityGolemBase.customInteraction
	 *
	 * @param golem
	 * @param player
	 * @param golemHandlerData
	 * @return
	 */
	public static boolean hook_CustomInteraction(	final EntityGolemBase golem, final EntityPlayer player,
													final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		GolemSyncRegistry syncRegistry = ( (GolemSyncRegistry)golemHandlerData.get( internalHandler ) );

		boolean needsSync = false;
		boolean needsSetup = false;
		boolean skipGUI = false;
		InteractionLevel handlerLevel;

		Side side = EffectiveSide.side();

		for( IGolemHookHandler handler : registeredHandlers )
		{
			// Get the current handler data
			Object handlerData = golemHandlerData.getOrDefault( handler, null );

			// Call handler
			try
			{
				handlerLevel = handler.canHandleInteraction( golem, handlerData, player, side );
			}
			catch( Exception e )
			{
				logCaughtException( "canHandleInteraction", handler, e );
				continue;
			}

			switch ( handlerLevel )
			{
			case NoInteraction:
				continue;

			case BasicInteraction:
				break;

			case SyncInteraction:
				needsSync = true;
				break;

			case FullInteraction:
				needsSync = true;
				needsSetup = true;
				break;
			}

			// Handled, dont show the GUI
			skipGUI = true;

			boolean hadData = ( handlerData != null );
			try
			{
				handlerData = handler.customInteraction( golem, handlerData, syncRegistry, player, side );
			}
			catch( Exception e )
			{
				logCaughtException( "customInteraction", handler, e );
				continue;
			}

			// Update golem
			if( handlerData == null )
			{
				if( hadData )
				{
					golemHandlerData.remove( handler );
				}
			}
			else
			{
				golemHandlerData.put( handler, handlerData );
			}

		}

		if( side == Side.SERVER )
		{
			if( needsSetup )
			{
				golem.setupGolem();
			}
			if( needsSync )
			{
				syncRegistry.markDirty();
			}
		}

		return skipGUI;
	}

	/**
	 * Hook for EntityGolemBase.entityInit
	 *
	 * @param golem
	 * @param golemHandlerData
	 */
	public static void hook_EntityInit( final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		// Create a new sync registry
		GolemSyncRegistry localRegistry = new GolemSyncRegistry();
		localRegistry.copyDefaults( defaultSyncRegistry );

		// Add to the handlers
		golemHandlerData.put( internalHandler, localRegistry );

		// Get the datawatcher
		DataWatcher watcher = golem.getDataWatcher();

		// Has the ID been set?
		if( DATAWATCHER_ID == -1 )
		{
			// Set the ID to the next available
			for( int i = 4; i < 31; ++i )
			{
				try
				{
					// Add the object
					watcher.addObject( DATAWATCHER_ID, localRegistry.mappingsToString() );

					// Object was added
					DATAWATCHER_ID = i;
					return;
				}
				catch( IllegalArgumentException e )
				{
				}
			}

			// If execution makes it this far, there were no available ID's :(
			DATAWATCHER_ID = -2;
			ThELog.warning( "Golem Hook API is unable to register channel for sync data." );
			return;
		}
		else if( DATAWATCHER_ID > 0 )
		{
			// Add datawatcher field.
			try
			{
				watcher.addObject( DATAWATCHER_ID, localRegistry.mappingsToString() );
			}
			catch( Exception e )
			{
				localRegistry.markDirty();
			}
		}
	}

	/**
	 * Hook for EntityGolemBase.onEntityUpdate.
	 * Keeps client handlers updated.
	 *
	 * @param golem
	 * @param golemHandlerData
	 */
	public static void hook_onEntityUpdate( final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		// Get the sync registry
		GolemSyncRegistry syncRegistry = (GolemSyncRegistry)golemHandlerData.get( internalHandler );

		if( EffectiveSide.isServerSide() )
		{
			// Update handlers
			for( IGolemHookHandler handler : dynamicHandlers )
			{
				try
				{
					handler.golemTick( golem, golemHandlerData.get( handler ), syncRegistry );
				}
				catch( Exception e )
				{
					logCaughtException( "golemTick", handler, e );
				}
			}

			// Update data watcher
			if( syncRegistry.hasChanged() )
			{
				if( DATAWATCHER_ID > 0 )
				{
					try
					{
						golem.getDataWatcher().updateObject( DATAWATCHER_ID, syncRegistry.mappingsToString() );
					}
					catch( NullPointerException e1 )
					{

						try
						{
							golem.getDataWatcher().addObject( DATAWATCHER_ID, syncRegistry.mappingsToString() );
						}
						catch( Exception e2 )
						{
							syncRegistry.markDirty();
						}
					}
				}
			}
			return;
		}

		if( DATAWATCHER_ID < 0 )
		{
			return;
		}

		// Update sync ticks
		++syncRegistry.clientSyncTicks;

		// Have 20 ticks passed? (Roughly a full second if the game is not lagging)
		if( syncRegistry.clientSyncTicks >= 30.0f )
		{
			// Reset the counter
			syncRegistry.clientSyncTicks = 0.0f;

			// Is the data out of sync?
			String watcherString;
			try
			{
				watcherString = golem.getDataWatcher().getWatchableObjectString( DATAWATCHER_ID );
				if( !syncRegistry.hasChanged() && ( watcherString == syncRegistry.lastUpdatedFrom ) )
				{
					// Data is in sync
					return;
				}
			}
			catch( Exception e )
			{
				return;
			}

			// Read the sync data, and get the list of handlers to update
			HashSet<IGolemHookHandler> handlersToUpdate = syncRegistry.readFromString( watcherString );
			if( handlersToUpdate == null )
			{
				// No handlers to update
				return;
			}

			// Inform each handler
			for( IGolemHookHandler handler : handlersToUpdate )
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );
				boolean hadData = ( handlerData != null );
				try
				{
					// Call handler
					handlerData = handler.syncDataChanged( syncRegistry, handlerData );
				}
				catch( Exception e )
				{
					logCaughtException( "onSyncDataChanged", handler, e );
					continue;
				}

				// Update golem
				if( handlerData == null )
				{
					if( hadData )
					{
						golemHandlerData.remove( handler );
					}
				}
				else
				{
					golemHandlerData.put( handler, handlerData );
				}
			}
		}

	}

	/**
	 * Hook for ItemGolemPlacer.spawnCreature
	 *
	 * @param golem
	 * @param itemGolemPlacer
	 * @param golemHandlerData
	 */
	public static void hook_Placer_SpawnGolem(	final EntityGolemBase golem, final ItemStack itemGolemPlacer,
												final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		// Ensure the stack has an NBT tag
		if( !itemGolemPlacer.hasTagCompound() )
		{
			return;
		}
		Side side = EffectiveSide.side();

		// Inform each handler
		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Call handler
				Object handlerData = handler.spawnGolemFromItemStack( golem, itemGolemPlacer, side );

				// Update golem
				if( handlerData != null )
				{
					golemHandlerData.put( handler, handlerData );
				}
			}
			catch( Exception e )
			{
				logCaughtException( "spawnGolemFromItemStack", handler, e );
			}
		}
	}

	/**
	 * Hook for EntityGolemBase.readEntityFromNBT
	 *
	 * @param golem
	 * @param golemHandlerData
	 * @param nbt
	 */
	public static void hook_ReadEntityFromNBT(	final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData,
												final NBTTagCompound nbt )
	{
		// Inform each handler
		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );

				// Call handler
				handlerData = handler.readEntityFromNBT( golem, nbt );

				// Update golem
				if( handlerData == null )
				{
					golemHandlerData.remove( handler );
				}
				else
				{
					golemHandlerData.put( handler, handlerData );
				}
			}
			catch( Exception e )
			{
				logCaughtException( "readEntityFromNBT", handler, e );
			}
		}
	}

	/**
	 * Hook for RenderGolemBase.render
	 *
	 * @param golem
	 * @param x
	 * @param y
	 * @param z
	 * @param partialElaspsedTick
	 */
	@SideOnly(Side.CLIENT)
	public static void hook_RenderGolem(	final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData, final double x,
											final double y, final double z, final float partialElaspsedTick )
	{

		// Call each render handler
		for( IGolemHookHandler handler : renderHandlers )
		{
			try
			{
				handler.renderGolem( golem, golemHandlerData.get( handler ), x, y, z, partialElaspsedTick );
			}
			catch( Exception e )
			{
				logCaughtException( "renderGolem", handler, e );
			}
		}

	}

	/**
	 * Hook for EntityGolemBase.setupGolem
	 *
	 * @param golem
	 * @param golemHandlerData
	 */
	public static void hook_SetupGolem( final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData )
	{
		// Get the sync data
		GolemSyncRegistry localRegistry = (GolemSyncRegistry)golemHandlerData.get( internalHandler );
		Side side = EffectiveSide.side();

		// Inform each handler
		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );
				boolean hadData = ( handlerData != null );

				// Call handler
				handlerData = handler.setupGolem( golem, handlerData, localRegistry, side );

				// Update golem
				if( handlerData == null )
				{
					if( hadData )
					{
						golemHandlerData.remove( handler );
					}
				}
				else
				{
					golemHandlerData.put( handler, handlerData );
				}
			}
			catch( Exception e )
			{
				logCaughtException( "setupGolem", handler, e );
			}
		}

		// Update data watcher
		if( localRegistry.hasChanged() && ( side == Side.SERVER ) && ( DATAWATCHER_ID > 0 ) )
		{
			try
			{
				golem.getDataWatcher().updateObject( DATAWATCHER_ID, localRegistry.mappingsToString() );
			}
			catch( NullPointerException e )
			{
				localRegistry.markDirty();
			}
		}
	}

	/**
	 * Hook for EntityGolemBase.writeEntityToNBT
	 *
	 * @param golem
	 * @param golemHandlerData
	 * @param nbt
	 */
	public static void hook_WriteEntityToNBT(	final EntityGolemBase golem, final HashMap<IGolemHookHandler, Object> golemHandlerData,
												final NBTTagCompound nbt )
	{
		// Inform each handler
		for( IGolemHookHandler handler : registeredHandlers )
		{
			try
			{
				// Get the current handler data
				Object handlerData = golemHandlerData.getOrDefault( handler, null );

				// Call handler
				handler.writeEntityNBT( golem, handlerData, nbt );
			}
			catch( Exception e )
			{
				logCaughtException( "writeEntityNBT", handler, e );
			}
		}
	}

	/**
	 * Registers a handler.
	 *
	 * @param handler
	 */
	public static void registerHandler( final @Nonnull IGolemHookHandler handler )
	{
		// Ensure the required transforms are present.
		if( ThECore.golemHooksTransformFailed )
		{
			return;
		}

		if( handler == null )
		{
			throw new NullPointerException( "Golem hook handler can not be null." );
		}

		try
		{
			// Add the handler
			registeredHandlers.add( handler );

			// Needs render?
			if( handler.needsRenderer() )
			{
				renderHandlers.add( handler );
			}

			// Needs tick?
			if( handler.needsDynamicUpdates() )
			{
				dynamicHandlers.add( handler );
			}

			// Register sync data
			defaultSyncRegistry.canRegister = true;
			handler.addDefaultSyncEntries( defaultSyncRegistry );
			defaultSyncRegistry.canRegister = false;

		}
		catch( Exception e )
		{
			logCaughtException( "registerHandler", handler, e );
		}

	}
}
