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

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketCraftRequest;
import thaumicenergistics.network.packets.PacketOpenGUI;
import thaumicenergistics.part.PartArcaneTerminal;

/**
 * @author BrockWS
 */
public class GuiCraftAmountBridge extends GuiCraftAmount {

    private EntityPlayer player;
    private PartArcaneTerminal part;
    private GuiNumberBox craftAmount;

    public GuiCraftAmountBridge(EntityPlayer player, PartArcaneTerminal part) {
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
        ItemStack icon = ThEApi.instance().items().arcaneTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
        if (!icon.isEmpty())
            this.buttonList.add(new GuiTabButton(this.guiLeft + 154, this.guiTop, icon, icon.getDisplayName(), this.itemRender));
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn.displayString.equals(GuiText.Next.getLocal()) || btn.displayString.equals(GuiText.Start.getLocal())) {
            PacketHandler.sendToServer(new PacketCraftRequest(Integer.parseInt(this.craftAmount.getText()), isShiftKeyDown()));
            return;
        }

        String name = ThEApi.instance().lang().itemArcaneTerminal().getLocalizedKey();
        if (btn instanceof GuiTabButton && ((GuiTabButton) btn).getMessage().equals(name)) {
            PacketHandler.sendToServer(new PacketOpenGUI(ModGUIs.ARCANE_TERMINAL, this.part.getLocation().getPos(), this.part.side));
            return;
        }

        super.actionPerformed(btn);
    }
}
