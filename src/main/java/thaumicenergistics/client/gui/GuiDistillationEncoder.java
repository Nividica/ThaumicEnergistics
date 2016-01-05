package thaumicenergistics.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigItems;
import thaumicenergistics.api.storage.IInventoryUpdateReceiver;
import thaumicenergistics.client.gui.abstraction.ThEBaseGui;
import thaumicenergistics.client.gui.buttons.GuiButtonEncodePattern;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.container.ContainerDistillationEncoder;
import thaumicenergistics.common.items.ItemCraftingAspect;
import thaumicenergistics.common.network.packet.server.Packet_S_DistillationEncoder;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.utils.ThEUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDistillationEncoder
	extends ThEBaseGui
	implements IInventoryUpdateReceiver
{
	/**
	 * Gui size.
	 */
	private static final int GUI_WIDTH = 176, GUI_HEIGHT = 234;

	/**
	 * Position of the title string.
	 */
	private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

	/**
	 * Position of the encode button
	 */
	private static final int BUTTON_ENCODE_POS_X = 146, BUTTON_ENCODE_POS_Y = 94;

	/**
	 * Half of the size of a standard item
	 */
	private static final int ITEM_HALF_SIZE = 8;

	/**
	 * Scale to draw the Thaumometer at.
	 */
	private static final float THAUMOMETER_SCALE = 2.8f;

	/**
	 * Title of the gui.
	 */
	private final String title;

	/**
	 * The GUI's container.
	 */
	private final ContainerDistillationEncoder deContainer;

	/**
	 * Thaumcraft's thaumometer
	 */
	private final ItemStack thaumometer = new ItemStack( ConfigItems.itemThaumometer );

	/**
	 * Particles
	 */
	private final ArrayList<GuiParticleAnimator> particles = new ArrayList<GuiParticleAnimator>();

	/**
	 * The encode button.
	 */
	private GuiButtonEncodePattern buttonEncode;

	/**
	 * Hash code of the last itemstack.
	 */
	private int lastSourceItemHash = 0;

	/**
	 * Set true when the source item may have been changed.
	 */
	private boolean sourceItemDirty = false;

	/**
	 * Set false the first time the GUI is drawn.
	 */
	private boolean firstDraw = true;

	public GuiDistillationEncoder( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Call super
		super( new ContainerDistillationEncoder( player, world, x, y, z ) );

		// Set the title
		this.title = ThEStrings.Block_DistillationEncoder.getLocalized();

		// Set the GUI size
		this.xSize = GUI_WIDTH;
		this.ySize = GUI_HEIGHT;

		// Set the container
		this.deContainer = (ContainerDistillationEncoder)this.inventorySlots;
		this.deContainer.slotUpdateReceiver = this;

	}

	/**
	 * Checks for item changes to the source item.
	 */
	private void checkSourceItem()
	{
		// Clear the dirty bit
		this.sourceItemDirty = false;

		// Get the source item
		ItemStack sourceItem = this.deContainer.slotSourceItem.getDisplayStack();

		// Is there a stack?
		if( sourceItem == null )
		{
			// Reset hash
			this.lastSourceItemHash = 0;
			return;
		}

		// Compare to last hash
		int hash = sourceItem.hashCode();
		if( hash == this.lastSourceItemHash )
		{
			// Same item
			return;
		}
		this.lastSourceItemHash = hash;

		// Clear any existing particles
		this.particles.clear();

		// Check each slot
		boolean isItemScanned = false;
		for( Slot slot : this.deContainer.slotSourceAspects )
		{
			// Does the slot have a stack?
			if( slot.getHasStack() )
			{
				// Get the aspect for that stack
				Aspect aspect = ItemCraftingAspect.getAspect( slot.getStack() );
				if( aspect != null )
				{
					// Found something
					isItemScanned = true;

					// Create the animator
					GuiParticleAnimator gpa = new GuiParticleAnimator( this.deContainer.slotSourceItem.xDisplayPosition,
									this.deContainer.slotSourceItem.yDisplayPosition,
									slot.xDisplayPosition,
									slot.yDisplayPosition,
									0.3f,
									EnumGuiParticles.Orb );

					// Set FPS
					gpa.framesPerSecond = 30;

					// Set the color
					float[] argb = ThEGuiHelper.INSTANCE.convertPackedColorToARGBf( aspect.getColor() );
					gpa.setColor( argb[1], argb[2], argb[3] );

					// Add to the list
					this.particles.add( gpa );
				}
			}
			else
			{
				break;
			}
		}

		if( isItemScanned )
		{
			// Play the on sound
			ThEUtils.playClientSound( null, "thaumcraft:hhon" );
		}
		else
		{
			// Play the off sound
			ThEUtils.playClientSound( null, "thaumcraft:hhoff" );
		}

	}

	/**
	 * Draw the background.
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the encoder gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.DISTILLATION_ENCODER.getTexture() );

		// Draw the gui texture
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );

		// Calculate the position and rotation of the thaumometer
		float th_PosX = this.guiLeft + ContainerDistillationEncoder.SLOT_SOURCE_ITEM_POS_X + ITEM_HALF_SIZE;
		float th_PosY = this.guiTop + ContainerDistillationEncoder.SLOT_SOURCE_ITEM_POS_Y - 0.25f + ITEM_HALF_SIZE;
		float th_Rotation = ( ( System.currentTimeMillis() % 36000 ) * 0.02f );
		float th_ScaleOffset = (float)Math.sin( th_Rotation * 0.15f ) * 0.1f;

		// Disable depth testing and push the matrix
		GL11.glDisable( GL11.GL_DEPTH_TEST );
		GL11.glPushMatrix();

		// Translate
		GL11.glTranslatef( th_PosX, th_PosY, 0.0F );

		// Scale
		GL11.glScalef( THAUMOMETER_SCALE + th_ScaleOffset, THAUMOMETER_SCALE + th_ScaleOffset, 1.0f );

		// Rotate
		GL11.glRotatef( th_Rotation, 0.0f, 0.0f, 1.0f );

		// Draw thaumometer
		GuiScreen.itemRender.renderItemAndEffectIntoGUI( this.fontRendererObj, this.mc.getTextureManager(),
			this.thaumometer, -ITEM_HALF_SIZE, -ITEM_HALF_SIZE );

		// Restore
		GL11.glPopMatrix();
		GL11.glEnable( GL11.GL_DEPTH_TEST );
	}

	/**
	 * Draw the foreground.
	 */
	@Override
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.title, TITLE_POS_X, TITLE_POS_Y, 0 );

		// Check the source item
		if( this.sourceItemDirty && !this.firstDraw )
		{
			this.checkSourceItem();
		}

		// Any particles?
		if( this.particles.size() > 0 )
		{
			// Prep
			EnumGuiParticles.Orb.prepareDraw();

			// Draw each
			for( Iterator<GuiParticleAnimator> iterator = this.particles.iterator(); iterator.hasNext(); )
			{
				if( !iterator.next().draw( this, false ) )
				{
					// Remove if done.
					iterator.remove();
				}
			}

			// Finish
			EnumGuiParticles.finishDraw();
		}

		this.firstDraw = false;

	}

	@Override
	protected void mouseClicked( final int mouseX, final int mouseY, final int mouseButton )
	{
		// Call super
		super.mouseClicked( mouseX, mouseY, mouseButton );

		// Was the mouse clicked over a widget?
		for( Slot slot : this.deContainer.slotSourceAspects )
		{
			if( slot.getHasStack() && ThEGuiHelper.INSTANCE.isPointInGuiRegion(
				slot.yDisplayPosition, slot.xDisplayPosition,
				18, 18, mouseX, mouseY,
				this.guiLeft, this.guiTop ) )
			{
				// Play clicky sound
				ThEUtils.playClientSound( null, "gui.button.press" );
				break;
			}
		}
	}

	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
		if( button == this.buttonEncode )
		{
			Packet_S_DistillationEncoder.sendEncodePattern(
							( (ContainerDistillationEncoder)this.inventorySlots ).getPlayer() );
		}
	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		// Create the encode button
		this.buttonEncode = new GuiButtonEncodePattern( 0, BUTTON_ENCODE_POS_X + this.guiLeft, BUTTON_ENCODE_POS_Y + this.guiTop,
						AEStateIconsEnum.STANDARD_ICON_SIZE,
						AEStateIconsEnum.STANDARD_ICON_SIZE );
		this.buttonList.add( this.buttonEncode );

		// Reset flags
		this.sourceItemDirty = false;
		this.firstDraw = true;
	}

	@Override
	public void onInventoryChanged( final IInventory sourceInventory )
	{
		if( !this.sourceItemDirty && ( sourceInventory == this.deContainer.slotSourceItem.inventory ) )
		{
			this.sourceItemDirty = true;
		}
	}
}
