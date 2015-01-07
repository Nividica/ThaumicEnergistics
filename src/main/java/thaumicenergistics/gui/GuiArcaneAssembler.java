package thaumicenergistics.gui;

import java.util.Dictionary;
import java.util.Hashtable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.container.ContainerArcaneAssembler;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.tileentities.TileArcaneAssembler;
import thaumicenergistics.util.GuiHelper;

public class GuiArcaneAssembler
	extends AbstractGuiBase
{
	/**
	 * Gui size.
	 */
	private static final int GUI_WIDTH = 211, GUI_HEIGHT = 197;

	/**
	 * Title position.
	 */
	private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

	/**
	 * Vis bar positions.
	 */
	private static final int VIS_EMPTY_Y = 87, VIS_FULL_Y = 202, VIS_BAR_HEIGHT = 16, VIS_BAR_WIDTH = 4;

	/**
	 * The player who is looking at the GUI.
	 */
	private EntityPlayer player;

	/**
	 * Title displayed on the GUI
	 */
	private String title;

	/**
	 * Map's an aspect to an X location.
	 */
	private Dictionary<Aspect, Integer> visBarPositionMap = new Hashtable<Aspect, Integer>();

	public GuiArcaneAssembler( final EntityPlayer player, final World world, final int X, final int Y, final int Z )
	{
		// Call super
		super( new ContainerArcaneAssembler( player, world, X, Y, Z ) );

		// Set the player
		this.player = player;

		// Set the GUI size
		this.xSize = GuiArcaneAssembler.GUI_WIDTH;
		this.ySize = GuiArcaneAssembler.GUI_HEIGHT;

		// Set the title
		this.title = StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".gui.arcane.assembler.title" );

		// Add the vis bar positions
		this.visBarPositionMap.put( Aspect.AIR, 41 );
		this.visBarPositionMap.put( Aspect.WATER, 59 );
		this.visBarPositionMap.put( Aspect.FIRE, 77 );
		this.visBarPositionMap.put( Aspect.ORDER, 95 );
		this.visBarPositionMap.put( Aspect.ENTROPY, 113 );
		this.visBarPositionMap.put( Aspect.EARTH, 131 );
	}

	private void drawVisBar( final Aspect aspect, final float percent )
	{
		// Calculate the height
		int height = (int)Math.floor( percent * GuiArcaneAssembler.VIS_BAR_HEIGHT );

		if( height > 0 )
		{
			// Calculate the X/U position
			int xuPos = this.visBarPositionMap.get( aspect );

			// Calculate the Y pos
			int yPos = GuiArcaneAssembler.VIS_EMPTY_Y + ( GuiArcaneAssembler.VIS_BAR_HEIGHT - height );

			// Calculate the v pos
			int vPos = GuiArcaneAssembler.VIS_FULL_Y + ( GuiArcaneAssembler.VIS_BAR_HEIGHT - height );

			// Draw the bar
			this.drawTexturedModalRect( this.guiLeft + xuPos, this.guiTop + yPos, xuPos, vPos, GuiArcaneAssembler.VIS_BAR_WIDTH, height );
		}
	}

	/**
	 * Draw background
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the workbench gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ARCANE_ASSEMBLER.getTexture() );

		// Draw the gui texture
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );

		// Get the aspect amounts
		AspectList storedVis = ( (ContainerArcaneAssembler)this.inventorySlots ).assembler.getStoredVis();

		// Draw the bars
		for( Aspect aspect : TileArcaneAssembler.PRIMALS )
		{
			float percent = storedVis.getAmount( aspect ) / (float)TileArcaneAssembler.MAX_STORED_VIS;
			this.drawVisBar( aspect, percent );
		}
	}

	/**
	 * Draw the foreground
	 */
	@Override
	public void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.title, GuiArcaneAssembler.TITLE_POS_X, GuiArcaneAssembler.TITLE_POS_Y, 0 );

		// Is the mouse over any of the bars?
		for( Aspect aspect : TileArcaneAssembler.PRIMALS )
		{
			int left = this.visBarPositionMap.get( aspect );
			if( GuiHelper.instance.isPointInGuiRegion( GuiArcaneAssembler.VIS_EMPTY_Y, left, GuiArcaneAssembler.VIS_BAR_HEIGHT,
				GuiArcaneAssembler.VIS_BAR_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
			{
				// Add the aspect name to the tooltip
				this.tooltip.add( StringUtils.capitalize( aspect.getTag() ) );

				// Add the amount
				int amount = ( (ContainerArcaneAssembler)this.inventorySlots ).assembler.getStoredVis().getAmount( aspect );
				this.tooltip.add( Integer.toString( amount ) + " / 100" );

				// Draw the tooltip
				this.drawTooltip( mouseX - this.guiLeft, mouseY - this.guiTop, true );

				// Stop searching
				break;
			}
		}
	}

}
