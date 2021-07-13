package thaumicenergistics.client.gui.crafting;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;

import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketCraftRequest;
import thaumicenergistics.network.packets.PacketOpenGUI;
import thaumicenergistics.part.PartSharedTerminal;

/**
 * @author BrockWS
 */
public class GuiCraftAmountBridge extends GuiCraftAmount {

    private EntityPlayer player;
    private PartSharedTerminal part;
    private GuiNumberBox craftAmount;

    public GuiCraftAmountBridge(EntityPlayer player, PartSharedTerminal part) {
        super(player.inventory, part);
        this.player = player;
        this.part = part;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.craftAmount = ReflectionHelper.getPrivateValue(GuiCraftAmount.class, this, "amountToCraft");
        if (this.craftAmount == null)
            throw new RuntimeException("Failed to get private value amountToCraft");
        ItemStack icon = part.getRepr();
        if (!icon.isEmpty())
            this.buttonList.add(new GuiTabButton(this.guiLeft + 154, this.guiTop, icon, icon.getDisplayName(), this.itemRender));
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn.displayString.equals(GuiText.Next.getLocal()) || btn.displayString.equals(GuiText.Start.getLocal())) {
            PacketHandler.sendToServer(new PacketCraftRequest(Integer.parseInt(this.craftAmount.getText()), isShiftKeyDown()));
            return;
        }

        String name = part.getRepr().getDisplayName();
        if (btn instanceof GuiTabButton && ((GuiTabButton) btn).getMessage().equals(name)) {
            PacketHandler.sendToServer(new PacketOpenGUI(this.part.getGui(), this.part.getLocation().getPos(), this.part.side));
            return;
        }

        super.actionPerformed(btn);
    }
}
