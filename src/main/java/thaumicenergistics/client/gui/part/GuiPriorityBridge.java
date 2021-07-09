package thaumicenergistics.client.gui.part;

import appeng.client.gui.implementations.GuiPriority;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketOpenGUI;
import thaumicenergistics.part.PartEssentiaStorageBus;

import java.io.IOException;

/**
 * @author Alex811
 */
public class GuiPriorityBridge extends GuiPriority {

    private EntityPlayer player;
    private PartEssentiaStorageBus part;
    private GuiNumberBox priority;

    public GuiPriorityBridge(EntityPlayer player, PartEssentiaStorageBus part) {
        super(player.inventory, part);
        this.player = player;
        this.part = part;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.priority = ReflectionHelper.getPrivateValue(GuiPriority.class, this, "priority");
        if (this.priority == null)
            throw new RuntimeException("Failed to get private value priority");
        ItemStack icon = part.getRepr();
        if (!icon.isEmpty())
            this.buttonList.add(new GuiTabButton(this.guiLeft + 154, this.guiTop, icon, icon.getDisplayName(), this.itemRender));
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        String name = part.getRepr().getDisplayName();
        if (btn instanceof GuiTabButton && ((GuiTabButton) btn).getMessage().equals(name)) {
            PacketHandler.sendToServer(new PacketOpenGUI(ModGUIs.ESSENTIA_STORAGE_BUS, this.part.getLocation().getPos(), this.part.side));
            return;
        }

        super.actionPerformed(btn);
    }
}
