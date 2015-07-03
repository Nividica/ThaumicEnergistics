package thaumicenergistics.parts;

import io.netty.buffer.ByteBuf;
import java.awt.Color;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.grid.IEssentiaWatcher;
import thaumicenergistics.grid.IEssentiaWatcherHost;
import thaumicenergistics.grid.IMEEssentiaMonitor;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper.AspectItemType;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.AEApi;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEColor;
import appeng.client.texture.CableBusTextures;
import appeng.core.localization.PlayerMessages;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaStorageMonitor
	extends AbstractAEPartBase
	implements IPartStorageMonitor, IEssentiaWatcherHost, IPowerChannelState
{
	/**
	 * All the data about what is being tracked.
	 * 
	 * @author Nividica
	 * 
	 */
	protected class TrackingInformation
	{
		/**
		 * Faux itemstack
		 */
		private IAEItemStack asItemStack;

		/**
		 * Aspect stack
		 */
		private AspectStack asAspectStack;

		public TrackingInformation()
		{
			// Create the faux itemstack
			ItemStack is = new ItemStack( new Item()
			{
				@Override
				public String getItemStackDisplayName( final ItemStack ignored_ )
				{
					return this.getUnlocalizedName();
				}

				@Override
				public String getUnlocalizedName()
				{
					return TrackingInformation.this.getAspectStack().getAspectName();
				}

				@Override
				public String getUnlocalizedName( final ItemStack ignored )
				{
					return this.getUnlocalizedName();
				}
			} );

			// Set the AE item stack
			this.asItemStack = AEApi.instance().storage().createItemStack( is );
		}

		/**
		 * Gets the aspect stack associated with the essentia.
		 * 
		 * @return
		 */
		public AspectStack getAspectStack()
		{
			return this.asAspectStack;
		}

		/**
		 * Gets an itemstack representing the essentia being tracked.
		 * Note: The only valid methods on the itemstack are the name accessors.
		 * 
		 * @return
		 */
		public IAEItemStack getItemStack()
		{
			return this.asItemStack;
		}

		/**
		 * Returns true if their is an essentia being tracked.
		 * 
		 * @return
		 */
		public boolean isValid()
		{
			return( this.asAspectStack != null );
		}

		/**
		 * Sets what is being tracked, or clears the data if null.
		 * 
		 * @param as
		 */
		public void setTracked( final AspectStack as )
		{
			if( as != null )
			{
				// Set aspect
				this.asAspectStack = as;
			}
			else
			{
				// Clear tracker
				this.asAspectStack = null;
			}
		}

		/**
		 * Updates the aspect amount.
		 * 
		 * @param aspectAmount
		 */
		public void updateTrackedAmount( final long aspectAmount )
		{
			// Ensure there is a valid tracker
			if( this.isValid() )
			{
				// Update amount
				this.asAspectStack.stackSize = aspectAmount;
			}
		}

	}

	/**
	 * How much power does the monitor require?
	 */
	private static final double IDLE_DRAIN = 0.0625;

	/**
	 * NBT Keys
	 */
	private static final String NBT_KEY_LOCKED = "Locked", NBT_KEY_TRACKED_ASPECT = "TrackedAspect";

	/**
	 * Watches the for changes in the essentia grid.
	 */
	private IEssentiaWatcher essentiaWatcher;

	/**
	 * True if the monitor is locked, and can not have its tracked essentia
	 * changed.
	 */
	private boolean monitorLocked = false;

	/**
	 * What is being tracked?
	 */
	protected final TrackingInformation trackedEssentia = new TrackingInformation();

	/**
	 * If true the cached render list needs to be updated.
	 */
	private boolean updateDisplayList = false;

	/**
	 * ID of the cached render list.
	 */
	private Integer cachedDisplayList = null;

	CableBusTextures darkCornerTexture;
	CableBusTextures lightCornerTexture;

	/**
	 * Constructor for conversion monitor.
	 * 
	 * @param subPart
	 */
	protected AEPartEssentiaStorageMonitor( final AEPartsEnum subPart )
	{
		super( subPart );

		this.lightCornerTexture = CableBusTextures.PartConversionMonitor_Colored;
		this.darkCornerTexture = CableBusTextures.PartConversionMonitor_Dark;
	}

	/**
	 * Default constructor
	 */
	public AEPartEssentiaStorageMonitor()
	{
		this( AEPartsEnum.EssentiaStorageMonitor );
	}

	/**
	 * Updates the watcher to the tracked essentia.
	 */
	private void configureWatcher()
	{
		if( this.essentiaWatcher != null )
		{
			// Clear any existing watched value
			this.essentiaWatcher.clear();

			// Is there an essentia being tracked?
			if( this.trackedEssentia.isValid() )
			{
				// Configure the watcher
				this.essentiaWatcher.add( this.trackedEssentia.getAspectStack().aspect );

				// Get the essentia monitor
				IMEEssentiaMonitor essMon = this.getGridBlock().getEssentiaMonitor();

				// Ensure there is a grid.
				if( essMon != null )
				{
					// Update the amount.
					this.updateTrackedEssentiaAmount( essMon );
				}
			}
		}
	}

	/**
	 * Returns true if the lock state was changed by this activation.
	 * 
	 * @param player
	 * @param heldItem
	 * @return
	 */
	private boolean didActivateChangeLockState( final EntityPlayer player, final ItemStack heldItem )
	{
		// Get the host tile entity
		TileEntity hte = this.getHostTile();

		// Is the item a wrench?
		if( !player.isSneaking() && Platform.isWrench( player, heldItem, hte.xCoord, hte.yCoord, hte.zCoord ) )
		{
			// Update the locked state
			this.monitorLocked = !this.monitorLocked;

			// Report to the player
			if( this.monitorLocked )
			{
				// Locked
				player.addChatMessage( PlayerMessages.isNowLocked.get() );
			}
			else
			{
				// Unlocked
				player.addChatMessage( PlayerMessages.isNowUnlocked.get() );
			}

			// Mark for sync & save
			this.markForUpdate();
			this.markForSave();

			return true;

		}

		return false;
	}

	/**
	 * Renders the aspect onto the screen.
	 * 
	 * @param tessellator
	 * @param aspect
	 */
	@SideOnly(Side.CLIENT)
	private void renderAspect( final Tessellator tessellator, final Aspect aspect )
	{
		// Get the aspect color
		Color aspectColor = new Color( aspect.getColor() );

		// Disable lighting
		GL11.glDisable( GL11.GL_LIGHTING );

		// Only draw if the image alpha is greater than the magic number
		GL11.glAlphaFunc( GL11.GL_GREATER, 0.004F );

		// Enable blending
		GL11.glEnable( GL11.GL_BLEND );

		// Specify the blending mode
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );

		// Set the color
		GL11.glColor4f( aspectColor.getRed() / 255.0F, aspectColor.getGreen() / 255.0F, aspectColor.getBlue() / 255.0F, 0.9F );
		tessellator.setColorRGBA_F( aspectColor.getRed() / 255.0F, aspectColor.getGreen() / 255.0F, aspectColor.getBlue() / 255.0F, 0.9F );

		// Center the aspect
		GL11.glTranslated( -0.20D, -0.25D, 0.0D );

		// Bind the aspect image
		Minecraft.getMinecraft().renderEngine.bindTexture( aspect.getImage() );

		// Add the vertex points
		double size = 0.38D;
		double zDepth = -0.265D;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV( 0.0D, size, zDepth, 0.0D, 1.0D ); // Bottom left
		tessellator.addVertexWithUV( size, size, zDepth, 1.0D, 1.0D ); // Bottom right
		tessellator.addVertexWithUV( size, 0.0D, zDepth, 1.0D, 0.0D ); // Top right
		tessellator.addVertexWithUV( 0.0D, 0.0D, zDepth, 0.0D, 0.0D ); // Top left

		// Draw!
		tessellator.draw();

		// Disable blending
		GL11.glDisable( GL11.GL_BLEND );

		// Enable lighting
		GL11.glEnable( GL11.GL_LIGHTING );
	}

	/**
	 * Renders the aspect and amount onto the screen.
	 * Note: Method originally from Applied Energistics 2,
	 * PartStorageMonitor.java
	 * 
	 * @param tessellator
	 * @param aspect
	 */
	@SideOnly(Side.CLIENT)
	private void renderScreen( final Tessellator tessellator, final AspectStack aspectStack )
	{
		// Push the OpenGL attributes
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

		// Get the side
		ForgeDirection side = this.getSide();

		// Adjust position based on cable side
		GL11.glTranslated( side.offsetX * 0.77, side.offsetY * 0.77, side.offsetZ * 0.77 );

		// Adjust scale and/or rotation based on cable side
		switch ( side )
		{
			case DOWN:
				GL11.glScalef( 1.0f, -1.0f, 1.0f );
				GL11.glRotatef( -90.0f, 1.0f, 0.0f, 0.0f );
				break;

			case EAST:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( -90.0f, 0.0f, 1.0f, 0.0f );
				break;

			case NORTH:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				break;

			case SOUTH:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( 180.0f, 0.0f, 1.0f, 0.0f );
				break;

			case UP:
				GL11.glScalef( 1.0f, -1.0f, 1.0f );
				GL11.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
				break;

			case WEST:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
				break;

			default:
				break;

		}

		// Push the OpenGL matrix
		GL11.glPushMatrix();

		try
		{
			// Calculate the brightness for the lightmap
			int brightness = 16 << 20 | 16 << 4;
			int brightnessComponent1 = brightness % 65536;
			int brightnessComponent2 = brightness / 65536;

			// Set the lightmap
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, brightnessComponent1 * 0.8F, brightnessComponent2 * 0.8F );

			// Render the aspect
			this.renderAspect( tessellator, aspectStack.aspect );
		}
		catch( Exception e )
		{
		}

		// Pop the OpenGL matrix
		GL11.glPopMatrix();

		// Move below the screen image
		GL11.glTranslatef( 0.0f, 0.14f, -0.24f );
		GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );

		// Convert the amount to a string
		final String renderedStackSize = ReadableNumberConverter.INSTANCE.toWideReadableForm( aspectStack.stackSize );

		// Get the font renderer
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

		// Adjust position based on string width
		GL11.glTranslatef( -0.5f * fr.getStringWidth( renderedStackSize ), 0.0f, -1.0f );

		// Render the string
		fr.drawString( renderedStackSize, 0, 0, 0 );

		// Pop the OpenGL attributes
		GL11.glPopAttrib();
	}

	/**
	 * Updates the tracked essentia amount.
	 * 
	 * @param essMonitor
	 */
	private void updateTrackedEssentiaAmount( final IMEEssentiaMonitor essMonitor )
	{
		// Ensure there is something to track
		if( !this.trackedEssentia.isValid() )
		{
			return;
		}

		// Reset the amount
		this.trackedEssentia.updateTrackedAmount( 0 );

		// Get the amount in the network
		long stored = essMonitor.getEssentiaAmount( this.trackedEssentia.getAspectStack().aspect );

		// Was there anything found?
		if( stored > 0 )
		{
			// Set the amount
			this.trackedEssentia.updateTrackedAmount( stored );
		}
	}

	/**
	 * Permission and activation checks.
	 * 
	 * @param player
	 * @return
	 */
	protected boolean activationCheck( final EntityPlayer player )
	{
		// Ignore fake players
		if( player instanceof FakePlayer )
		{
			return false;
		}

		// Is the monitor off?
		if( !this.isActive() )
		{
			return false;
		}

		// Does the player have permission to interact with this device?
		if( !Platform.hasPermissions( this.getLocation(), player ) )
		{
			return false;
		}

		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void finalize() throws Throwable
	{
		// Call super
		super.finalize();

		// Dealoc the cached render list
		if( this.cachedDisplayList != null )
		{
			GLAllocation.deleteDisplayLists( this.cachedDisplayList );
		}
	}

	/**
	 * Called when the monitor is right-clicked with an empty hand.
	 * Clears the tracker.
	 * 
	 * @return
	 */
	protected boolean onActivatedWithEmptyHand()
	{
		// Is the monitor locked?
		if( this.monitorLocked )
		{
			return false;
		}

		// Clear the tracker
		this.trackedEssentia.setTracked( null );

		// Update watcher
		this.configureWatcher();

		// Mark for sync & save
		this.markForUpdate();
		this.markForSave();

		return true;
	}

	/**
	 * Called when the monitor is right-clicked with an essentia container or
	 * label.
	 * Sets the tracker to the items contained aspect, or clears if the
	 * container is empty.
	 * 
	 * @param player
	 * @param heldItem
	 * @param itemType
	 * @return
	 */
	protected boolean onActivateWithAspectItem( final EntityPlayer player, final ItemStack heldItem, final AspectItemType itemType )
	{
		// Is the monitor locked?
		if( this.monitorLocked )
		{
			return false;
		}

		// Get the aspect
		Aspect heldAspect = EssentiaItemContainerHelper.INSTANCE.getFilterAspectFromItem( heldItem );

		// Ensure there is an aspect
		if( heldAspect == null )
		{
			// Empty container, clear the tracker
			return this.onActivatedWithEmptyHand();
		}

		// Create an aspect stack of size 0, and set it as the tracked aspect
		this.trackedEssentia.setTracked( new AspectStack( heldAspect, 0 ) );

		// Reconfigure the watcher
		this.configureWatcher();

		// Mark for sync & save
		this.markForUpdate();
		this.markForSave();

		return true;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	/**
	 * Collision boxes
	 */
	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		helper.addBox( 2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D );

		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];
	}

	/**
	 * What essentia gas is being displayed?
	 */
	@Override
	public IAEStack<?> getDisplayed()
	{
		if( this.trackedEssentia.isValid() )
		{
			return this.trackedEssentia.getItemStack();
		}

		return null;
	}

	/**
	 * Gets how much power the monitor requires.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return AEPartEssentiaStorageMonitor.IDLE_DRAIN;
	}

	/**
	 * Get the monitor brightness.
	 */
	@Override
	public int getLightLevel()
	{
		return( this.isActive() ? AbstractAEPartBase.ACTIVE_TERMINAL_LIGHT_LEVEL : 0 );
	}

	/**
	 * Is the monitor locked?
	 */
	@Override
	public boolean isLocked()
	{
		return this.monitorLocked;
	}

	/**
	 * Is the monitor on?
	 */
	@Override
	public boolean isPowered()
	{
		return this.isActive();
	}

	/**
	 * Called when a player right clicks the monitor.
	 */
	@Override
	public boolean onActivate( final EntityPlayer player, final Vec3 position )
	{

		// Ignore client side.
		if( EffectiveSide.isClientSide() )
		{
			return true;
		}

		// Permission and activation checks
		if( !activationCheck( player ) )
		{
			return false;
		}

		// Get the item the player is holding
		ItemStack heldItem = player.getCurrentEquippedItem();

		// Was the lock state changed?
		if( didActivateChangeLockState( player, heldItem ) )
		{
			return true;
		}

		// Is the players hand empty?
		if( heldItem == null )
		{
			return this.onActivatedWithEmptyHand();
		}

		// Get the type
		AspectItemType itemType = EssentiaItemContainerHelper.INSTANCE.getItemType( heldItem );

		// Is the item valid?
		if( itemType != AspectItemType.Invalid )
		{
			return this.onActivateWithAspectItem( player, heldItem, itemType );
		}

		return false;
	}

	/**
	 * Called by the watcher when the essentia amount changes.
	 */
	@Override
	public void onEssentiaChange( final Aspect aspect, final long storedAmount, final long changeAmount )
	{
		// Is there an essentia being tracked?
		if( !this.trackedEssentia.isValid() )
		{
			// Not tracking anything
			return;
		}

		// Update the amount
		this.trackedEssentia.updateTrackedAmount( storedAmount );

		// Mark for sync
		this.markForUpdate();
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read locked
		if( data.hasKey( AEPartEssentiaStorageMonitor.NBT_KEY_LOCKED ) )
		{
			this.monitorLocked = data.getBoolean( AEPartEssentiaStorageMonitor.NBT_KEY_LOCKED );
		}

		// Read tracked
		if( data.hasKey( AEPartEssentiaStorageMonitor.NBT_KEY_TRACKED_ASPECT ) )
		{
			// Read the aspect
			Aspect trackedAspect = Aspect.getAspect( data.getString( AEPartEssentiaStorageMonitor.NBT_KEY_TRACKED_ASPECT ) );

			// Set the tracker
			this.trackedEssentia.setTracked( new AspectStack( trackedAspect, 0 ) );
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean readFromStream( final ByteBuf stream ) throws IOException
	{
		boolean redraw = false;

		// Call super
		redraw |= super.readFromStream( stream );

		// Read locked state
		this.monitorLocked = stream.readBoolean();

		// Is there any tracking info to read?
		if( stream.readBoolean() )
		{
			// Read the tracked stack
			AspectStack as = AspectStack.loadAspectStackFromNBT( ByteBufUtils.readTag( stream ) );

			// Update the tracker
			this.trackedEssentia.setTracked( as );

			// Mark for screen redraw
			redraw |= this.updateDisplayList = true;
		}
		else
		{
			// Clear the tracker
			this.trackedEssentia.setTracked( null );

			// Mark for screen redraw
			redraw |= this.updateDisplayList = true;
		}

		return redraw;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderDynamic( final double x, final double y, final double z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		// Skip if nothing to draw
		if( ( !this.isActive() ) || ( !this.trackedEssentia.isValid() ) )
		{
			return;
		}

		// Does the cached display list need to be created?
		if( this.cachedDisplayList == null )
		{
			// Ask OpenGL for a display list
			this.cachedDisplayList = GLAllocation.generateDisplayLists( 1 );

			// Mark for update
			this.updateDisplayList = true;
		}

		// Push the OpenGL matrix
		GL11.glPushMatrix();

		// Move to the center of the monitor
		GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );

		// Does the display list need to be updated?
		if( this.updateDisplayList )
		{
			// Mark that it is being updated
			this.updateDisplayList = false;

			// Ask OpenGL to create a new compiled list, and run it when done.
			GL11.glNewList( this.cachedDisplayList, GL11.GL_COMPILE_AND_EXECUTE );

			// Add the screen render to the list
			this.renderScreen( Tessellator.instance, this.trackedEssentia.getAspectStack() );

			// End the list and run it
			GL11.glEndList();
		}
		else
		{
			// Run the cached list
			GL11.glCallList( this.cachedDisplayList );
		}

		// Pop the OpenGL matrix
		GL11.glPopMatrix();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		helper.setTexture( side, side, side, side, side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		// Face bounds
		helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );

		// Dark corners
		helper.setInvColor( AEColor.Transparent.blackVariant );
		helper.renderInventoryFace( this.darkCornerTexture.getIcon(), ForgeDirection.SOUTH, renderer );

		// Light corners
		helper.setInvColor( AEColor.Transparent.mediumVariant );
		helper.renderInventoryFace( this.lightCornerTexture.getIcon(), ForgeDirection.SOUTH, renderer );

		// Main face
		helper.setInvColor( AEColor.Transparent.whiteVariant );
		helper.renderInventoryFace( CableBusTextures.PartConversionMonitor_Bright.getIcon(), ForgeDirection.SOUTH, renderer );

		// Phial
		helper.setInvColor( AEColor.Black.blackVariant );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[0], ForgeDirection.SOUTH, renderer );

		// Cable lights
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderInventoryBusLights( helper, renderer );

	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator tessellator = Tessellator.instance;

		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		// Main block
		helper.setTexture( side, side, side, side, side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		// Light up if active
		if( this.isActive() )
		{
			Tessellator.instance.setBrightness( AbstractAEPartBase.ACTIVE_FACE_BRIGHTNESS );
		}

		// Dark corners
		tessellator.setColorOpaque_I( this.getHost().getColor().blackVariant );
		helper.renderFace( x, y, z, this.darkCornerTexture.getIcon(), ForgeDirection.SOUTH, renderer );

		// Light corners
		tessellator.setColorOpaque_I( this.getHost().getColor().mediumVariant );
		helper.renderFace( x, y, z, this.lightCornerTexture.getIcon(), ForgeDirection.SOUTH, renderer );

		// Main face
		tessellator.setColorOpaque_I( this.getHost().getColor().whiteVariant );
		helper.renderFace( x, y, z, CableBusTextures.PartConversionMonitor_Bright.getIcon(), ForgeDirection.SOUTH, renderer );

		// Phial
		if( !this.trackedEssentia.isValid() )
		{
			tessellator.setColorOpaque_I( this.getHost().getColor().mediumVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[0], ForgeDirection.SOUTH, renderer );
		}

		// Cable lights
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );

	}

	/**
	 * Needs dynamic renderer.
	 */
	@Override
	public boolean requireDynamicRender()
	{
		return true;
	}

	@Override
	public boolean showNetworkInfo( final MovingObjectPosition where )
	{
		return false;
	}

	/**
	 * Called when a new watcher is provided.
	 */
	@Override
	public void updateWatcher( final IEssentiaWatcher newWatcher )
	{
		// Set the watcher
		this.essentiaWatcher = newWatcher;

		// Configure it
		this.configureWatcher();
	}

	@Override
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		// Call super
		super.writeToNBT( data, saveType );

		// Write locked
		if( this.monitorLocked )
		{
			data.setBoolean( AEPartEssentiaStorageMonitor.NBT_KEY_LOCKED, this.monitorLocked );
		}

		// Write tracked data if valid
		if( this.trackedEssentia.isValid() )
		{
			// Write the aspect
			data.setString( AEPartEssentiaStorageMonitor.NBT_KEY_TRACKED_ASPECT, this.trackedEssentia.getAspectStack().getTag() );
		}
	}

	@Override
	public void writeToStream( final ByteBuf stream ) throws IOException
	{
		// Call super
		super.writeToStream( stream );

		// Write locked
		stream.writeBoolean( this.monitorLocked );

		// Write if valid
		stream.writeBoolean( this.trackedEssentia.isValid() );

		if( this.trackedEssentia.isValid() )
		{
			// Write the tracker
			ByteBufUtils.writeTag( stream, this.trackedEssentia.getAspectStack().writeToNBT( new NBTTagCompound() ) );
		}

	}
}
