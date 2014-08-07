package thaumicenergistics.gui;

import appeng.parts.automation.UpgradeInventory;
import thaumicenergistics.container.slot.SlotNetworkTool;
import thaumicenergistics.texture.AEStateIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public abstract class GuiWithUpgradeSlots
	extends GuiContainer
{

	private static int upgradeU = AEStateIcons.UPGRADE_SLOT.getU();

	private static int upgradeV = AEStateIcons.UPGRADE_SLOT.getV();

	public GuiWithUpgradeSlots( Container container )
	{
		super( container );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer( float alpha, int mouseX, int mouseY )
	{
		Minecraft.getMinecraft().renderEngine.bindTexture( AEStateIcons.AE_STATES_TEXTURE );

		// Locate any upgrade or network slots
		for( int i = 0; i < this.inventorySlots.inventorySlots.size(); i++ )
		{
			// Get the slot
			Slot slot = (Slot)this.inventorySlots.inventorySlots.get( i );

			// Is it network or upgrade?
			if( ( slot instanceof SlotNetworkTool ) || ( slot.inventory instanceof UpgradeInventory ) )
			{
				// Draw background
				this.drawTexturedModalRect( this.guiLeft + slot.xDisplayPosition, this.guiTop + slot.yDisplayPosition, GuiWithUpgradeSlots.upgradeU,
					GuiWithUpgradeSlots.upgradeV, AEStateIcons.ICON_SIZE, AEStateIcons.ICON_SIZE );
			}
		}
	}

}
