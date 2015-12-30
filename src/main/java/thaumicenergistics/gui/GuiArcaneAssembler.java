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
import thaumicenergistics.container.ContainerArcaneAssembler;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.tileentities.TileArcaneAssembler;
import thaumicenergistics.util.GuiHelper;

public class GuiArcaneAssembler
	extends AbstractGuiBase
{
	/**
	 * Gui size.
	 */
	private static final int FULL_GUI_WIDTH = 247, MAIN_GUI_WIDTH = 176, UPGRADE_GUI_WIDTH = 234 - MAIN_GUI_WIDTH, GUI_HEIGHT = 197,
					UPGRADE_GUI_HEIGHT = 104;

	/**
	 * Title position.
	 */
	private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

	/**
	 * Vis bar positions.
	 */
	private static final int VIS_EMPTY_Y = 87, VIS_FULL_Y = 202, VIS_BAR_HEIGHT = 16, VIS_BAR_WIDTH = 4;

	/**
	 * Title displayed on the GUI
	 */
	private String title;

	/**
	 * Map's an aspect to an X location.
	 */
	private Dictionary<Aspect, Integer> visBarPositionMap = new Hashtable<Aspect, Integer>();

	/**
	 * True if the player is holding a network tool.
	 */
	private boolean hasNetworkTool = false;

	/**
	 * Fake aspect used to simplify progress bar draw calls
	 */
	private Aspect progressAspect = Aspect.MECHANISM;

	public GuiArcaneAssembler( final EntityPlayer player, final World world, final int X, final int Y, final int Z )
	{
		// Call super
		super( new ContainerArcaneAssembler( player, world, X, Y, Z ) );

		this.hasNetworkTool = ( (ContainerArcaneAssembler)this.inventorySlots ).hasNetworkTool();

		// Set the GUI size
		this.xSize = ( this.hasNetworkTool ? GuiArcaneAssembler.FULL_GUI_WIDTH : GuiArcaneAssembler.MAIN_GUI_WIDTH +
						GuiArcaneAssembler.UPGRADE_GUI_WIDTH );
		this.ySize = GuiArcaneAssembler.GUI_HEIGHT;

		// Set the title
		this.title = ThEStrings.Gui_TitleArcaneAssembler.getLocalized();

		// Add the vis bar positions
		this.visBarPositionMap.put( Aspect.AIR, 41 );
		this.visBarPositionMap.put( Aspect.WATER, 59 );
		this.visBarPositionMap.put( Aspect.FIRE, 77 );
		this.visBarPositionMap.put( Aspect.ORDER, 95 );
		this.visBarPositionMap.put( Aspect.ENTROPY, 113 );
		this.visBarPositionMap.put( Aspect.EARTH, 131 );
		this.visBarPositionMap.put( this.progressAspect, 149 );
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
		if( this.hasNetworkTool )
		{
			// Draw the full gui
			this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );
		}
		else
		{
			// Draw main body
			this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, GuiArcaneAssembler.MAIN_GUI_WIDTH, this.ySize );

			// Draw upgrades
			this.drawTexturedModalRect( this.guiLeft + GuiArcaneAssembler.MAIN_GUI_WIDTH, this.guiTop, GuiArcaneAssembler.MAIN_GUI_WIDTH, 0,
				GuiArcaneAssembler.UPGRADE_GUI_WIDTH, GuiArcaneAssembler.UPGRADE_GUI_HEIGHT );
		}

		// Get the aspect amounts
		AspectList storedVis = ( (ContainerArcaneAssembler)this.inventorySlots ).assembler.getStoredVis();

		// Draw the bars
		for( Aspect aspect : TileArcaneAssembler.PRIMALS )
		{
			float percent = storedVis.getAmount( aspect ) / (float)TileArcaneAssembler.MAX_STORED_CVIS;
			this.drawVisBar( aspect, percent );
		}

		// Draw crafting percent
		this.drawVisBar( this.progressAspect, ( (ContainerArcaneAssembler)this.inventorySlots ).assembler.getPercentComplete() );

		// Call super
		super.drawAEToolAndUpgradeSlots( alpha, mouseX, mouseY );
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
			if( GuiHelper.INSTANCE.isPointInGuiRegion( GuiArcaneAssembler.VIS_EMPTY_Y, left, GuiArcaneAssembler.VIS_BAR_HEIGHT,
				GuiArcaneAssembler.VIS_BAR_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
			{
				// Add the aspect name to the tooltip
				this.tooltip.add( GuiHelper.INSTANCE.getAspectChatColor( aspect ) + StringUtils.capitalize( aspect.getTag() ) );

				// Add the amount
				int amount = ( (ContainerArcaneAssembler)this.inventorySlots ).assembler.getStoredVis().getAmount( aspect );
				this.tooltip.add( Float.toString( amount / 10.0F ) + " / 150" );

				// Add the discount
				float discount = ( (ContainerArcaneAssembler)this.inventorySlots ).assembler.getVisDiscountForAspect( aspect );
				this.tooltip.add( String.format( "%.0f%% %s", ( discount * 100.0F ), StatCollector.translateToLocal( "tc.vis.costavg" ) ) );

				// Stop searching
				break;
			}
		}
	}

}
