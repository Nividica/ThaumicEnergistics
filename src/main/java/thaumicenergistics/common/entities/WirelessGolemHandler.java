package thaumicenergistics.common.entities;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumicenergistics.api.entities.IGolemHookHandler;
import thaumicenergistics.api.entities.IGolemHookSyncRegistry;
import thaumicenergistics.client.render.model.ModelGolemWifiBackpack;
import thaumicenergistics.common.integration.tc.GolemCoreType;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.items.ItemGolemWirelessBackpack;
import thaumicenergistics.common.items.ItemGolemWirelessBackpack.BackpackSkins;
import thaumicenergistics.common.utils.EffectiveSide;
import appeng.api.AEApi;
import appeng.items.parts.ItemFacade;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WirelessGolemHandler
	implements IGolemHookHandler
{
	/**
	 * Data stored on the client side.
	 * 
	 * @author Nividica
	 * 
	 */
	public static class WirelessClientData
	{
		public boolean isInRange = false;
		public float pearlRotation = 0.0f;
		public BackpackSkins skin = BackpackSkins.Thaumium;
	}

	/**
	 * Data stored on the server side.
	 * 
	 * @author Nividica
	 * 
	 */
	public static class WirelessServerData
	{
		public final String encryptionKey;
		public int tickCounter = 0;
		public boolean isInRange = false;
		public ItemStack facade = null;
		public BackpackSkins skin = BackpackSkins.Thaumium;

		public WirelessServerData( final String encKey )
		{
			this.encryptionKey = encKey;
		}
	}

	private static WirelessGolemHandler INSTANCE = null;

	/**
	 * NBT keys
	 */
	private static final String NBTKEY_WIFI_KEY = "wifiBackpackKey", NBTKEY_FACADE = "facade";

	/**
	 * State flags.
	 */
	private static final Character SYNCFLAG_HAS_WIFI_IN_RANGE = Character.valueOf( 'i' ),
					SYNCFLAG_HAS_WIFI_OUT_OF_RANGE = Character.valueOf( 'o' ),
					SYNCFLAG_NO_WIFI = Character.valueOf( 'n' ),
					SYNCFIELD_SKIN = 'a';

	@SideOnly(Side.CLIENT)
	private ModelGolemWifiBackpack addonModel;

	/**
	 * ID of the wifi sync field.
	 */
	private int wifiSyncID = -1;

	/**
	 * ID of the skin sync field.
	 */
	private int skinSyncID = -1;

	/**
	 * Cache of the backpack item.
	 */
	private ItemGolemWirelessBackpack backpackItem;

	/**
	 * Item that represents a facade, or null.
	 */
	private ItemFacade facadeItem;

	/**
	 * Private constructor.
	 */
	private WirelessGolemHandler()
	{
		// Setup the backpack model if client side
		if( EffectiveSide.isClientSide() )
		{
			this.setupModel();
		}

		// Store a reference to the backpack item.
		this.backpackItem = (ItemGolemWirelessBackpack)ItemEnum.GOLEM_WIFI_BACKPACK.getItem();

		// Store a reference to the facade item
		this.facadeItem = (ItemFacade)AEApi.instance().definitions().items().facade().maybeItem().orNull();
	}

	/**
	 * Returns the instance of the handler
	 * 
	 * @return
	 */
	public static WirelessGolemHandler getInstance()
	{
		if( INSTANCE == null )
		{
			INSTANCE = new WirelessGolemHandler();
		}
		return INSTANCE;
	}

	/**
	 * Gets a character based on the skin.
	 * 
	 * @param skin
	 * @return
	 */
	private Character backpackSkinToSyncChar( final BackpackSkins skin )
	{
		int id = 0;
		if( skin != null )
		{
			id = skin.ordinal();
		}
		return Character.valueOf( (char)( SYNCFIELD_SKIN + id ) );
	}

	/**
	 * Han the backpack be handled?
	 * 
	 * @param golem
	 * @param handlerData
	 * @param player
	 * @param side
	 * @return
	 */
	private InteractionLevel canHandleInteration_Backpack( final EntityGolemBase golem, final Object handlerData, final EntityPlayer player,
															final Side side )
	{
		// Does the golem already have a backpack on?
		if( handlerData != null )
		{
			// Already has backpack.
			return InteractionLevel.NoInteraction;
		}

		// Is the core valid?
		GolemCoreType core = GolemCoreType.getCoreByID( golem.getCore() );
		if( core == null )
		{
			return InteractionLevel.NoInteraction;
		}
		switch ( core )
		{
		// Unsupported cores
		case Butcher:
		case Empty:
		case Fish:
		case Guard:
		case Harvest:
		case Lumber:
		case Sorting:
		case Use:
		default:
			return InteractionLevel.NoInteraction;

			// Supported cores
		case Gather:
		case Fill:
		case Liquid:
		case Essentia:
			break;
		}

		// Client side?
		if( side == Side.CLIENT )
		{
			//Swing the item
			player.swingItem();
			return InteractionLevel.NoInteraction;
		}

		return InteractionLevel.FullInteraction;
	}

	/**
	 * Can the facade be handled?
	 * 
	 * @param golem
	 * @param handlerData
	 * @param player
	 * @param side
	 * @param heldItem
	 * @return
	 */
	private InteractionLevel canHandleInteration_Facade( final EntityGolemBase golem, final Object handlerData, final EntityPlayer player,
															final Side side,
															final ItemStack heldItem )
	{
		// Is the golem wearing a backpack?
		if( handlerData == null )
		{
			// No backpack to apply it to
			return InteractionLevel.NoInteraction;
		}

		// Get the skin for the facade
		BackpackSkins skin = this.getSkinFromFacade( heldItem );
		if( skin == null )
		{
			// Unsupported facade
			return InteractionLevel.NoInteraction;
		}

		// Client side?
		if( ( side == Side.CLIENT ) && ( handlerData instanceof WirelessClientData ) )
		{
			//Swing the item
			player.swingItem();

			// Set the skin
			( (WirelessClientData)handlerData ).skin = skin;

			return InteractionLevel.NoInteraction;
		}

		return InteractionLevel.SyncInteraction;
	}

	/**
	 * Returns the skin associated with the facade, or null if there is not one.
	 * 
	 * @param facade
	 * @return
	 */
	private BackpackSkins getSkinFromFacade( final ItemStack facade )
	{
		Block b = this.facadeItem.getBlock( facade );
		int metaData = this.facadeItem.getMeta( facade );

		if( b == ConfigBlocks.blockCosmeticSolid )
		{
			// Thaumium block?
			if( metaData == 4 )
			{
				return BackpackSkins.Thaumium;
			}
			// Tallow block?
			else if( metaData == 5 )
			{
				return BackpackSkins.Tallow;
			}
		}
		// Greatwood log?
		else if( ( b == ConfigBlocks.blockMagicalLog ) && ( metaData == 0 ) )
		{
			return BackpackSkins.GreatWood;
		}
		// Flesh?
		else if( ( b == ConfigBlocks.blockTaint ) && ( metaData == 2 ) )
		{
			return BackpackSkins.Flesh;
		}
		// Stone block?
		else if( b == Blocks.stone )
		{
			return BackpackSkins.Stone;
		}
		// Hay block?
		else if( b == Blocks.hay_block )
		{
			return BackpackSkins.Straw;
		}
		// Bricks?
		else if( b == Blocks.brick_block )
		{
			return BackpackSkins.Clay;
		}
		// Iron?
		else if( b == Blocks.iron_block )
		{
			return BackpackSkins.Iron;
		}
		else if( b == Blocks.gold_block )
		{
			return BackpackSkins.Gold;
		}
		else if( b == Blocks.diamond_block )
		{
			return BackpackSkins.Diamond;
		}

		return null;
	}

	/**
	 * Creates the wifi addon model.
	 */
	@SideOnly(Side.CLIENT)
	private void setupModel()
	{
		this.addonModel = new ModelGolemWifiBackpack();
	}

	/**
	 * Gets the skin based on the character
	 * 
	 * @param skinSyncChar
	 * @return
	 */
	private BackpackSkins syncCharToBackpackSkin( final char skinSyncChar )
	{
		int id = skinSyncChar - SYNCFIELD_SKIN;
		if( ( id < 0 ) || ( id > BackpackSkins.VALUES.length ) )
		{
			id = 0;
		}
		return BackpackSkins.VALUES[id];
	}

	@Override
	public void addDefaultSyncEntries( final IGolemHookSyncRegistry syncRegistry )
	{
		// Register the wifi entry
		this.wifiSyncID = syncRegistry.registerSyncChar( this, SYNCFLAG_NO_WIFI );

		// Register facade item
		this.skinSyncID = syncRegistry.registerSyncChar( this, this.backpackSkinToSyncChar( BackpackSkins.Thaumium ) );
	}

	@Override
	public void bellLeftClicked( final EntityGolemBase golem, final Object handlerData, final ItemStack itemGolemPlacer, final EntityPlayer player,
									final boolean dismantled, final Side side )
	{
		// Goes the golem have a backpack on?
		if( handlerData instanceof WirelessServerData )
		{
			WirelessServerData wsd = ( (WirelessServerData)handlerData );

			// Create a new backpack item
			ItemStack backpack = ItemEnum.GOLEM_WIFI_BACKPACK.getStack();

			// Set the key
			this.backpackItem.setEncryptionKey( backpack, wsd.encryptionKey, null );

			// Drop the backpack
			golem.entityDropItem( backpack, 0.5f );

			// Is there a facade?
			if( wsd.facade != null )
			{
				// Drop the facade
				golem.entityDropItem( wsd.facade, 0.5f );
			}
		}

	}

	@Override
	public InteractionLevel canHandleInteraction( final EntityGolemBase golem, final Object handlerData, final EntityPlayer player, final Side side )
	{
		// Get the held item
		ItemStack heldItem = player.inventory.getCurrentItem();

		// Empty hand?
		if( heldItem == null )
		{
			return InteractionLevel.NoInteraction;
		}

		// Backpack?
		if( heldItem.getItem() == this.backpackItem )
		{
			return this.canHandleInteration_Backpack( golem, handlerData, player, side );
		}

		// Facade?
		else if( ( this.facadeItem != null ) && ( heldItem.getItem() == this.facadeItem ) )
		{
			return this.canHandleInteration_Facade( golem, handlerData, player, side, heldItem );
		}

		return InteractionLevel.NoInteraction;

	}

	@Override
	public Object customInteraction( final EntityGolemBase golem, final Object handlerData, final IGolemHookSyncRegistry syncData,
										final EntityPlayer player, final Side side )
	{
		// Get the held item
		ItemStack heldItem = player.inventory.getCurrentItem();

		// Backpack?
		if( heldItem.getItem() == this.backpackItem )
		{
			// Is the backpack linked?
			String encKey = this.backpackItem.getEncryptionKey( heldItem );
			if( encKey != null )
			{
				// Play sound
				golem.worldObj.playSoundAtEntity( golem, "thaumcraft:upgrade", 0.5F, 1.0F );

				// Take the item
				player.inventory.setInventorySlotContents( player.inventory.currentItem, null );

				// Create the data
				return new WirelessServerData( encKey );
			}
		}

		// Facade?
		else if( ( this.facadeItem != null ) && ( heldItem.getItem() == this.facadeItem ) )
		{
			WirelessServerData wsd = (WirelessServerData)handlerData;

			// Is there a facade set?
			if( ( wsd.facade != null ) && ( !player.capabilities.isCreativeMode ) )
			{
				// Drop the old facade
				golem.entityDropItem( wsd.facade, 0.5f );
			}

			// Set facade
			wsd.facade = heldItem.copy();
			wsd.facade.stackSize = 1;

			// Take a facade from the player
			if( !player.capabilities.isCreativeMode )
			{
				--heldItem.stackSize;
				if( heldItem.stackSize <= 0 )
				{
					player.inventory.setInventorySlotContents( player.inventory.currentItem, null );
				}
			}

			// Set the skin
			wsd.skin = this.getSkinFromFacade( wsd.facade );
			syncData.updateSyncChar( this, this.skinSyncID, this.backpackSkinToSyncChar( wsd.skin ) );

			// Play sound
			golem.worldObj.playSoundAtEntity( golem, "thaumcraft:cameraticks", 0.5F, 1.0F );

			return wsd;
		}

		return null;
	}

	@Override
	public void golemTick( final EntityGolemBase golem, final Object serverHandlerData, final IGolemHookSyncRegistry syncData )
	{
		// Has backpack?
		if( serverHandlerData instanceof WirelessServerData )
		{
			WirelessServerData wsd = (WirelessServerData)serverHandlerData;

			// Set sync data every 18th tick
			if( ++wsd.tickCounter > 18 )
			{
				// Reset counter
				wsd.tickCounter = 0;

				// Set sync data
				syncData.updateSyncChar( this, this.wifiSyncID, ( wsd.isInRange ? SYNCFLAG_HAS_WIFI_IN_RANGE : SYNCFLAG_HAS_WIFI_OUT_OF_RANGE ) );
				syncData.updateSyncChar( this, this.skinSyncID, this.backpackSkinToSyncChar( wsd.skin ) );
			}
		}
	}

	@Override
	public boolean needsDynamicUpdates()
	{
		return true;
	}

	@Override
	public boolean needsRenderer()
	{
		return true;
	}

	@Override
	public Object readEntityFromNBT( final EntityGolemBase golem, final NBTTagCompound nbtTag )
	{
		WirelessServerData wsd = null;

		// Does the golem have a backpack?
		if( nbtTag.hasKey( NBTKEY_WIFI_KEY ) )
		{
			// Create the data
			wsd = new WirelessServerData( nbtTag.getString( NBTKEY_WIFI_KEY ) );

			if( nbtTag.hasKey( NBTKEY_FACADE ) )
			{
				// Read the facade
				try
				{
					wsd.facade = ItemStack.loadItemStackFromNBT( nbtTag.getCompoundTag( NBTKEY_FACADE ) );
					wsd.skin = this.getSkinFromFacade( wsd.facade );
				}
				catch( Exception e )
				{
					// Invalid facade
				}
			}

		}

		return wsd;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderGolem( final EntityGolemBase golem, final Object clientHandlerData, final double x, final double y, final double z,
								final float partialElaspsedTick )
	{
		if( !( clientHandlerData instanceof WirelessClientData ) )
		{
			return;
		}
		WirelessClientData wcd = (WirelessClientData)clientHandlerData;

		if( golem.hurtTime > 0 )
		{
			// Red tint
			GL11.glColor4f( 1.0f, 0.0f, 0.0f, 1.0f );
		}
		else if( golem.healing > 0 )
		{
			// Green tint
			GL11.glColor3f( 0.5f, 1.0f, 0.5f );
		}
		else
		{
			// Full white
			GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
		}

		// Is the golem in range and active?
		boolean golemActive = wcd.isInRange && !golem.inactive;

		// Update the rotation of the pearl
		if( golemActive && !Minecraft.getMinecraft().isGamePaused() )
		{
			wcd.pearlRotation += partialElaspsedTick * 2.0f;
			if( wcd.pearlRotation >= 360.0f )
			{
				wcd.pearlRotation -= 360.0f;
			}

		}

		// Push the matrix
		GL11.glPushMatrix();

		// Translate to the golem
		GL11.glTranslatef( (float)x, (float)y + 0.64f, (float)z );

		// Rotate to face away from the golem
		float golemYaw = 270.0f;

		// Interpolate the golems rotation
		golemYaw -= ( ( golem.renderYawOffset * partialElaspsedTick ) + ( golem.prevRenderYawOffset * ( 1.0f - partialElaspsedTick ) ) );

		// Rotate with the golem
		GL11.glRotatef( golemYaw, 0.0f, 1.0f, 0.0f );

		// Translate just behind the golem
		GL11.glTranslatef( -0.16f, 0.0f, 0.0f );

		// Scale
		GL11.glScalef( 0.8f, 0.8f, 0.8f );

		// Render
		this.addonModel.render( wcd.pearlRotation, 0.0625f, golemActive, wcd.skin );

		// Pop the matrix
		GL11.glPopMatrix();

	}

	@Override
	public Object setupGolem( final EntityGolemBase golem, final Object handlerData, final IGolemHookSyncRegistry syncData, final Side side )
	{
		if( side == Side.CLIENT )
		{
			return handlerData;
		}

		// Does the golem have a backpack?
		if( handlerData instanceof WirelessServerData )
		{
			WirelessServerData wsd = (WirelessServerData)handlerData;

			// Get the golems core
			GolemCoreType core = GolemCoreType.getCoreByID( golem.getCore() );
			if( core != null )
			{
				switch ( core )
				{
				case Gather:
					// Add Gather AI script
					golem.tasks.addTask( 1, new AIGolemWifiGather( golem, wsd ) );
					break;

				case Fill:
					// Add Fill AI script
					golem.tasks.addTask( 3, new AIGolemWifiFill( golem, wsd ) );
					break;

				case Liquid:
					// Add liquid AI script
					golem.tasks.addTask( 2, new AIGolemWifiLiquid( golem, wsd ) );
					break;

				case Essentia:
					// Add essentia AI script
					golem.tasks.addTask( 2, new AIGolemWifiEssentia( golem, wsd ) );
					break;
				default:
					break;
				}
			}

			// Update the sync data
			syncData.updateSyncChar( this, this.wifiSyncID, ( wsd.isInRange ? SYNCFLAG_HAS_WIFI_IN_RANGE : SYNCFLAG_HAS_WIFI_OUT_OF_RANGE ) );
		}
		else
		{
			// Update the sync data
			syncData.updateSyncChar( this, this.wifiSyncID, SYNCFLAG_NO_WIFI );
		}

		return handlerData;
	}

	@Override
	public Object spawnGolemFromItemStack( final EntityGolemBase golem, final ItemStack itemGolemPlacer, final Side side )
	{
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object syncDataChanged( final IGolemHookSyncRegistry syncData, final Object clientHandlerData )
	{
		// Does the golem have on a backpack?
		char syncFlag = syncData.getSyncCharOrDefault( this.wifiSyncID, SYNCFLAG_NO_WIFI );
		boolean inRange;
		if( syncFlag == SYNCFLAG_HAS_WIFI_IN_RANGE )
		{
			inRange = true;
		}
		else if( syncFlag == SYNCFLAG_HAS_WIFI_OUT_OF_RANGE )
		{
			inRange = false;
		}
		else
		{
			// No backpack.
			return null;
		}

		// Has existing data?
		WirelessClientData wcd = null;
		if( clientHandlerData instanceof WirelessClientData )
		{
			wcd = (WirelessClientData)clientHandlerData;
		}
		else
		{
			// Create new data
			wcd = new WirelessClientData();
		}

		// Update the range
		wcd.isInRange = inRange;

		// Get the skin
		char skinChar = syncData.getSyncCharOrDefault( this.skinSyncID, SYNCFIELD_SKIN );
		wcd.skin = this.syncCharToBackpackSkin( skinChar );

		return wcd;
	}

	@Override
	public void writeEntityNBT( final EntityGolemBase golem, final Object serverHandlerData, final NBTTagCompound nbtTag )
	{
		// Does the golem have a backpack?
		if( serverHandlerData instanceof WirelessServerData )
		{
			WirelessServerData wsd = ( (WirelessServerData)serverHandlerData );

			// Write network key
			nbtTag.setString( NBTKEY_WIFI_KEY, wsd.encryptionKey );

			// Has facade?
			if( wsd.facade != null )
			{
				// Write the facade
				nbtTag.setTag( NBTKEY_FACADE, wsd.facade.writeToNBT( new NBTTagCompound() ) );
			}
		}
	}

}
