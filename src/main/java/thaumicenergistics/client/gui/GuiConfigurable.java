package thaumicenergistics.client.gui;

import appeng.client.gui.widgets.GuiImgButton;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Mouse;
import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.ContainerBaseConfigurable;
import thaumicenergistics.integration.appeng.util.ThEConfigManager;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketSettingChange;
import thaumicenergistics.util.ThEUtil;

import java.io.IOException;

/**
 * @author Alex811
 */
public abstract class GuiConfigurable extends GuiBase {
    public GuiConfigurable(ContainerBase container) {
        super(container);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 1)
            for (final GuiButton btn : this.buttonList) {
                if (!btn.mousePressed(this.mc, mouseX, mouseY))
                    continue;
                super.mouseClicked(mouseX, mouseY, 0); // Make the code think we lmb the button, so actionPerformed() gets called
                return;
            }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof GuiImgButton) {
            GuiImgButton btn = (GuiImgButton) button;
            Enum currentValue = btn.getCurrentValue();
            Enum next = ThEUtil.rotateEnum(currentValue, btn.getSetting().getPossibleValues(), Mouse.isButtonDown(1));
            btn.set(next);
            if(!imgBtnActionOverride(btn, next))
                PacketHandler.sendToServer(new PacketSettingChange(btn.getSetting(), next));
        }
    }

    /**
     * Override to handle actions on a {@link GuiImgButton} differently.
     * @param btn the button to potentially override its action
     * @param next the new value of the button
     * @return true if you handled the action, to skip sending {@link PacketSettingChange} to the server
     */
    protected boolean imgBtnActionOverride(GuiImgButton btn, Enum next){
        return false;
    }

    public ThEConfigManager getConfigManager() {
        return ((ContainerBaseConfigurable) this.inventorySlots).getConfigManager();
    }
}
