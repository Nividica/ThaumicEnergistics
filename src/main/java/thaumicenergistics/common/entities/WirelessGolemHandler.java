package thaumicenergistics.common.entities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumicenergistics.api.entities.IGolemHookHandler;
import thaumicenergistics.api.entities.IGolemHookSyncRegistry;
import thaumicenergistics.client.render.model.ModelGolemWifiAddon;
import thaumicenergistics.common.integration.tc.GolemCoreType;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.items.ItemGolemWirelessBackpack;
import thaumicenergistics.common.utils.EffectiveSide;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WirelessGolemHandler
	implements IGolemHookHandler
{
	public static class WirelessClientData
	{
		public boolean isInRange = false;

		public WirelessClientData( final boolean inRange )
		{
			this.isInRange = inRange;
		}
	}

	public static class WirelessServerData
	{
		public final String encryptionKey;
		public boolean isInRange = false;

		public WirelessServerData( final String encKey )
		{
			this.encryptionKey = encKey;
		}
	}

	private static WirelessGolemHandler INSTANCE = null;

	/**
	 * NBT key used to represent if the golem has the wifi backpack or not.
	 */
	private static final String NBTKEY_WIFI_KEY = "wifiBackpackKey";

	/**
	 * State flags.
	 */
	private static final Character SYNCFLAG_HAS_WIFI_IN_RANGE = Character.valueOf( 'i' ),
					SYNCFLAG_HAS_WIFI_OUT_OF_RANGE = Character.valueOf( 'o' ),
					SYNCFLAG_NO_WIFI = Character.valueOf( 'n' );

	@SideOnly(Side.CLIENT)
	private ModelGolemWifiAddon addonModel;

	/**
	 * ID of the wifi sync field.
	 */
	private int wifiSyncID = -1;

	/**
	 * Cache of the backpack item.
	 */
	private ItemGolemWirelessBackpack backpackItem;

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
	 * Creates the wifi addon model.
	 */
	@SideOnly(Side.CLIENT)
	private void setupModel()
	{
		this.addonModel = new ModelGolemWifiAddon();
	}

	@Override
	public void addDefaultSyncEntries( final IGolemHookSyncRegistry syncRegistry )
	{
		// Register the wifi entry
		this.wifiSyncID = syncRegistry.registerSyncChar( this, SYNCFLAG_NO_WIFI );
	}

	@Override
	public void bellLeftClicked( final EntityGolemBase golem, final Object handlerData, final ItemStack itemGolemPlacer, final EntityPlayer player,
									final boolean dismantled, final Side side )
	{
		// Goes the golem have a backpack on?
		if( handlerData instanceof WirelessServerData )
		{
			// Create a new backpack item
			ItemStack backpack = ItemEnum.GOLEM_WIFI_BACKPACK.getStack();

			// Set the key
			this.backpackItem.setEncryptionKey( backpack, ( (WirelessServerData)handlerData ).encryptionKey, null );

			// Drop the backpack
			golem.entityDropItem( backpack, 0.5f );
		}

	}

	@Override
	public boolean canHandleInteraction( final EntityGolemBase golem, final Object handlerData, final EntityPlayer player, final Side side )
	{
		// Get the held item
		ItemStack heldItem = player.inventory.getCurrentItem();

		// Check for backpack
		boolean isHoldingBackpack = ( ( heldItem != null ) && ( heldItem.getItem() == this.backpackItem ) );

		// Client side?
		if( side == Side.CLIENT )
		{
			if( isHoldingBackpack )
			{
				//Swing the item
				player.swingItem();
			}
			return false;
		}

		// Does the golem already have a backpack on?
		if( handlerData != null )
		{
			// Already has backpack.
			return false;
		}

		// Is the core valid?
		GolemCoreType core = GolemCoreType.getCoreByID( golem.getCore() );
		if( core == null )
		{
			return false;
		}
		switch ( core )
		{
		// Unsupported cores
		case Butcher:
		case Empty:
		case Essentia: // TODO, complex no doubt
		case Fish: // TODO?
		case Guard:
		case Harvest:
		case Lumber:
		case Sorting:
		case Use:
		default:
			return false;

			// Supported cores
		case Gather:
		case Fill:
		case Liquid:
			return isHoldingBackpack;
		}

	}

	@Override
	public Object customInteraction( final EntityGolemBase golem, final Object handlerData, final EntityPlayer player, final Side side )
	{
		// Get the held item
		ItemStack heldItem = player.inventory.getCurrentItem();

		// Is the backpack linked?
		String encKey = this.backpackItem.getEncryptionKey( heldItem );
		if( encKey != null )
		{
			// Take the item
			player.inventory.setInventorySlotContents( player.inventory.currentItem, null );

			// Create the data
			return new WirelessServerData( encKey );
		}

		return null;
	}

	@Override
	public void golemTick( final EntityGolemBase golem, final Object serverHandlerData, final IGolemHookSyncRegistry syncData )
	{
		// Has backpack?
		if( serverHandlerData instanceof WirelessServerData )
		{
			// Update clients
			syncData.updateSyncChar( this, this.wifiSyncID, ( ( (WirelessServerData)serverHandlerData ).isInRange ? SYNCFLAG_HAS_WIFI_IN_RANGE
																													: SYNCFLAG_HAS_WIFI_OUT_OF_RANGE ) );
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
		// Does the golem have a backpack?
		if( nbtTag.hasKey( NBTKEY_WIFI_KEY ) )
		{
			// Create the data
			return new WirelessServerData( nbtTag.getString( NBTKEY_WIFI_KEY ) );
		}

		// No wifi
		return null;
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

		// Push the matrix
		GL11.glPushMatrix();

		// Translate to the golem
		GL11.glTranslatef( (float)x, (float)y + 0.63f, (float)z );

		// Rotate with the golem
		float golemYaw = 270.0f;
		golemYaw -= ( ( golem.renderYawOffset * partialElaspsedTick ) + ( golem.prevRenderYawOffset * ( 1.0f - partialElaspsedTick ) ) );
		GL11.glRotatef( golemYaw, 0.0f, 1.0f, 0.0f );

		// Translate just behind the golem
		GL11.glTranslatef( -0.16f, 0.0f, 0.0f );

		// Scale
		GL11.glScalef( 0.8f, 0.8f, 0.8f );

		// Render
		this.addonModel.render( partialElaspsedTick, 0.0625f, ( (WirelessClientData)clientHandlerData ).isInRange );

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
		golem.advanced = true;
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
		if( clientHandlerData instanceof WirelessClientData )
		{
			// Update the data
			( (WirelessClientData)clientHandlerData ).isInRange = inRange;
			return clientHandlerData;
		}

		// Create new data
		return new WirelessClientData( inRange );
	}

	@Override
	public void writeEntityNBT( final EntityGolemBase golem, final Object serverHandlerData, final NBTTagCompound nbtTag )
	{
		// Does the golem have a backpack?
		if( serverHandlerData instanceof WirelessServerData )
		{
			// Write network key
			nbtTag.setString( NBTKEY_WIFI_KEY, ( (WirelessServerData)serverHandlerData ).encryptionKey );
		}
	}

}
