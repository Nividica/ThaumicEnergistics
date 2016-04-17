package thaumicenergistics.common.parts;

import java.util.ArrayList;
import java.util.List;
import appeng.api.config.Settings;
import appeng.api.config.ViewItems;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.ICraftingIssuerHost;
import thaumicenergistics.client.gui.GuiEssentiaCellTerminal;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.container.ContainerEssentiaCellTerminalBase;
import thaumicenergistics.common.container.ContainerEssentiaTerminal;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper.AspectItemType;
import thaumicenergistics.common.inventory.TheInternalInventory;
import thaumicenergistics.common.network.packet.server.Packet_S_ChangeGui;
import thaumicenergistics.common.registries.EnumCache;
import thaumicenergistics.common.storage.AspectStackComparator.AspectStackComparatorMode;
import thaumicenergistics.common.utils.EffectiveSide;

/**
 * Allows a player to extract/deposit essentia from the network.
 *
 * @author Nividica
 *
 */
public class PartEssentiaTerminal
	extends ThERotateablePart
	implements ICraftingIssuerHost
{

	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 0.5D;

	/**
	 * NBT Keys
	 */
	private static final String NBT_KEY_SORT_MODE = "sortMode", NBT_KEY_INVENTORY = "slots", NBT_KEY_VIEW_MODE = "ViewMode";

	/**
	 * Default sorting mode for the terminal.
	 */
	private static final AspectStackComparatorMode DEFAULT_SORT_MODE = AspectStackComparatorMode.MODE_ALPHABETIC;

	/**
	 * Default view mode for the terminal.
	 */
	private static final ViewItems DEFAULT_VIEW_MODE = ViewItems.ALL;

	/**
	 * List of currently opened containers.
	 */
	private List<ContainerEssentiaTerminal> listeners = new ArrayList<ContainerEssentiaTerminal>();

	/**
	 * The sorting mode used to display aspects.
	 */
	private AspectStackComparatorMode sortMode = DEFAULT_SORT_MODE;

	/**
	 * The viewing mode used to display aspects.
	 */
	private ViewItems viewMode = DEFAULT_VIEW_MODE;

	/**
	 * The selected aspect in the GUI.
	 * Only stored while the part is loaded.
	 */
	public Aspect selectedAspect = null;

	private TheInternalInventory inventory = new TheInternalInventory( ThaumicEnergistics.MOD_ID + ".part.aspect.terminal", 2, 64 )
	{
		@Override
		public boolean isItemValidForSlot( final int slotId, final ItemStack itemStack )
		{
			// Get the type
			AspectItemType iType = EssentiaItemContainerHelper.INSTANCE.getItemType( itemStack );

			// True if jar or jar label
			return ( iType == AspectItemType.EssentiaContainer ) || ( iType == AspectItemType.JarLabel );
		}
	};

	public PartEssentiaTerminal()
	{
		super( AEPartsEnum.EssentiaTerminal );
	}

	/**
	 * Informs all open containers to update their respective clients
	 * that the mode has changed.
	 */
	private void notifyListenersOfModeChanged()
	{
		for( ContainerEssentiaTerminal listener : this.listeners )
		{
			listener.onModeChanged( this.sortMode, this.viewMode );
		}
	}

	public void addListener( final ContainerEssentiaCellTerminalBase container )
	{
		if( container instanceof ContainerEssentiaTerminal )
		{
			this.listeners.add( (ContainerEssentiaTerminal)container );
		}
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		helper.addBox( 2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D );

		helper.addBox( 4.0D, 4.0D, 13.0D, 12.0D, 12.0D, 14.0D );

		helper.addBox( 5.0D, 5.0D, 12.0D, 11.0D, 11.0D, 13.0D );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[3];
	}

	@Override
	public Object getClientGuiElement( final EntityPlayer player )
	{
		return GuiEssentiaCellTerminal.NewEssentiaTerminalGui( this, player );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return null;
	}

	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{
		// Inventory is saved when wrenched.
		if( wrenched )
		{
			return;
		}

		// Loop over inventory
		for( int slotIndex = 0; slotIndex < 2; slotIndex++ )
		{
			// Get the stack at this index
			ItemStack slotStack = this.inventory.getStackInSlot( slotIndex );

			// Did we get anything?
			if( slotStack != null )
			{
				// Add to drops
				drops.add( slotStack );
			}
		}
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		// Ignored
		return null;
	}

	@Override
	public ItemStack getIcon()
	{
		return this.associatedItem;
	}

	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return PartEssentiaTerminal.IDLE_POWER_DRAIN;
	}

	public TheInternalInventory getInventory()
	{
		return this.inventory;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return this.getGridBlock().getItemMonitor();
	}

	/**
	 * Light level based on if terminal is active.
	 */
	@Override
	public int getLightLevel()
	{
		return( this.isActive() ? ThEPartBase.ACTIVE_TERMINAL_LIGHT_LEVEL : 0 );
	}

	@Override
	public Object getServerGuiElement( final EntityPlayer player )
	{
		return new ContainerEssentiaTerminal( this, player );
	}

	/**
	 * Gets the current sorting mode
	 *
	 * @return
	 */
	public AspectStackComparatorMode getSortingMode()
	{
		return this.sortMode;
	}

	/**
	 * Gets the view mode.
	 *
	 * @return
	 */
	public ViewItems getViewMode()
	{
		return this.viewMode;
	}

	@Override
	public void launchGUI( final EntityPlayer player )
	{
		TileEntity host = this.getHostTile();

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Launch the gui
			ThEGuiHandler.launchGui( this, player, host.getWorldObj(), host.xCoord, host.yCoord, host.zCoord );
		}
		else
		{
			// Ask the server to change the GUI
			Packet_S_ChangeGui.sendGuiChangeToPart( this, player, host.getWorldObj(), host.xCoord, host.yCoord, host.zCoord );
		}
	}

	/**
	 * Called when a player has changed sorting modes.
	 *
	 * @param sortMode
	 */
	public void onClientRequestSortingModeChange( final boolean backwards )
	{
		// Change the sorting mode
		if( backwards )
		{
			this.sortMode = this.sortMode.previousMode();
		}
		else
		{
			this.sortMode = this.sortMode.nextMode();
		}

		// Update clients
		this.notifyListenersOfModeChanged();

		// Mark that we need saving
		this.markForSave();

	}

	/**
	 * Called when a player has changed viewing modes.
	 */
	public void onClientRequestViewModeChange( final boolean backwards )
	{
		// Change the view mode
		this.viewMode = Platform.rotateEnum( this.viewMode, backwards, Settings.VIEW_MODE.getPossibleValues() );

		// Update clients
		this.notifyListenersOfModeChanged();

		// Mark for save
		this.markForSave();
	}

	/**
	 * Called to read our saved state
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read the sorting mode
		if( data.hasKey( NBT_KEY_SORT_MODE ) )
		{
			this.sortMode = AspectStackComparatorMode.VALUES[data.getInteger( NBT_KEY_SORT_MODE )];
		}

		// Read inventory
		if( data.hasKey( PartEssentiaTerminal.NBT_KEY_INVENTORY ) )
		{
			this.inventory.readFromNBT( data, PartEssentiaTerminal.NBT_KEY_INVENTORY );
		}

		// Read view mode
		if( data.hasKey( NBT_KEY_VIEW_MODE ) )
		{
			this.viewMode = EnumCache.AE_VIEW_ITEMS[data.getInteger( NBT_KEY_VIEW_MODE )];
		}
	}

	public void removeListener( final ContainerEssentiaTerminal containerAspectTerminal )
	{
		this.listeners.remove( containerAspectTerminal );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[3];

		helper.setTexture( side );
		helper.setBounds( 4.0F, 4.0F, 13.0F, 12.0F, 12.0F, 14.0F );
		helper.renderInventoryBox( renderer );

		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[4], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		ts.setBrightness( 0xD000D0 );

		helper.setInvColor( 0xFFFFFF );

		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[4], ForgeDirection.SOUTH, renderer );

		helper.setBounds( 3.0F, 3.0F, 15.0F, 13.0F, 13.0F, 16.0F );

		helper.setInvColor( AEColor.Transparent.blackVariant );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[0], ForgeDirection.SOUTH, renderer );

		helper.setInvColor( AEColor.Transparent.mediumVariant );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );

		helper.setInvColor( AEColor.Transparent.whiteVariant );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

		helper.setBounds( 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 13.0F );
		this.renderInventoryBusLights( helper, renderer );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[3];

		// Cable connector
		helper.setTexture( side );
		helper.setBounds( 4.0F, 4.0F, 13.0F, 12.0F, 12.0F, 14.0F );
		helper.renderBlock( x, y, z, renderer );

		// Mid-block
		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[4], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		// Light up if active
		if( this.isActive() )
		{
			Tessellator.instance.setBrightness( ThEPartBase.ACTIVE_FACE_BRIGHTNESS );
		}

		// Rotate the face texture
		this.rotateRenderer( renderer, false );

		// Base face
		ts.setColorOpaque_I( 0xFFFFFF );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[4], ForgeDirection.SOUTH, renderer );

		// Dark colored face
		helper.setBounds( 3.0F, 3.0F, 15.0F, 13.0F, 13.0F, 16.0F );
		ts.setColorOpaque_I( this.getHost().getColor().blackVariant );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[0], ForgeDirection.SOUTH, renderer );

		// Standard colored face
		ts.setColorOpaque_I( this.getHost().getColor().mediumVariant );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );

		// Light colored face
		ts.setColorOpaque_I( this.getHost().getColor().whiteVariant );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

		// Reset rotation
		this.rotateRenderer( renderer, true );

		// Cable lights
		helper.setBounds( 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 13.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );

	}

	/**
	 * Called to save our state
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		// Call super
		super.writeToNBT( data, saveType );

		// Only write NBT data if saving, or wrenched.
		if( ( saveType != PartItemStack.World ) && ( saveType != PartItemStack.Wrench ) )
		{
			return;
		}

		// Write the sorting mode
		if( this.sortMode != DEFAULT_SORT_MODE )
		{
			data.setInteger( NBT_KEY_SORT_MODE, this.sortMode.ordinal() );
		}

		// Write inventory
		if( !this.inventory.isEmpty() )
		{
			this.inventory.writeToNBT( data, NBT_KEY_INVENTORY );
		}

		// Write view mode
		if( this.viewMode != DEFAULT_VIEW_MODE )
		{
			data.setInteger( NBT_KEY_VIEW_MODE, this.viewMode.ordinal() );
		}
	}

}
