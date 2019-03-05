package thaumicenergistics.part;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartItemStack;

import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.item.part.ItemArcaneTerminal;
import thaumicenergistics.util.ItemHandlerUtil;
import thaumicenergistics.util.inventory.ThEInternalInventory;
import thaumicenergistics.util.inventory.ThEUpgradeInventory;

/**
 * @author BrockWS
 */
public class PartArcaneTerminal extends PartSharedTerminal {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/arcane_terminal/base"), // 0
            new ResourceLocation(ModGlobals.MOD_ID, "part/arcane_terminal/on"), // 1
            new ResourceLocation(ModGlobals.MOD_ID, "part/arcane_terminal/off"), // 2
            new ResourceLocation("appliedenergistics2", "part/display_status_has_channel"), // 3
            new ResourceLocation("appliedenergistics2", "part/display_status_on"), // 4
            new ResourceLocation("appliedenergistics2", "part/display_status_off") // 5
    };

    private static IPartModel MODEL_ON = new ThEPartModel(MODELS[0], MODELS[1], MODELS[4]);
    private static IPartModel MODEL_OFF = new ThEPartModel(MODELS[0], MODELS[2], MODELS[5]);
    private static IPartModel MODEL_HAS_CHANNEL = new ThEPartModel(MODELS[0], MODELS[1], MODELS[3]);

    public ThEInternalInventory craftingInventory;
    public ThEInternalInventory upgradeInventory;

    public PartArcaneTerminal(ItemArcaneTerminal item) {
        super(item);
        this.craftingInventory = new ThEInternalInventory("matrix", 15, 64);
        this.upgradeInventory = new ThEUpgradeInventory("upgrades", 1, 1, this.getItemStack(PartItemStack.NETWORK));
        this.getConfigManager().registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.getConfigManager().registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        this.getConfigManager().registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        if (name.equalsIgnoreCase("crafting"))
            return new InvWrapper(this.craftingInventory);
        if (name.equalsIgnoreCase("upgrades"))
            return new InvWrapper(this.upgradeInventory);
        return super.getInventoryByName(name);
    }

    @Override
    public void getDrops(List<ItemStack> list, boolean b) {
        super.getDrops(list, b);
        ItemHandlerUtil.getInventoryAsList(this.getInventoryByName("crafting"), list);
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        GuiHandler.openGUI(ModGUIs.ARCANE_TERMINAL, player, this.hostTile.getPos(), this.side);
        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("crafting", this.craftingInventory.serializeNBT());
        tag.setTag("upgrades", this.upgradeInventory.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("crafting"))
            this.craftingInventory.deserializeNBT(tag.getTagList("crafting", 10));
        if (tag.hasKey("upgrades"))
            this.upgradeInventory.deserializeNBT(tag.getTagList("upgrades", 10));
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isPowered())
            if (this.isActive())
                return MODEL_HAS_CHANNEL;
            else
                return MODEL_ON;
        return MODEL_OFF;
    }
}
