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

import appeng.api.parts.IPartModel;
import appeng.api.parts.PartItemStack;

import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.config.AESettings;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.item.part.ItemArcaneTerminal;
import thaumicenergistics.util.AEUtil;
import thaumicenergistics.util.ForgeUtil;
import thaumicenergistics.util.ItemHandlerUtil;
import thaumicenergistics.util.inventory.ThEInternalInventory;
import thaumicenergistics.util.inventory.ThEUpgradeInventory;

/**
 * @author BrockWS
 * @author Alex811
 */
public class PartArcaneTerminal extends PartSharedTerminal {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/arcane_terminal/base"), // 0
            new ResourceLocation(ModGlobals.MOD_ID, "part/arcane_terminal/on"), // 1
            new ResourceLocation(ModGlobals.MOD_ID, "part/arcane_terminal/off"), // 2
            new ResourceLocation(ModGlobals.MOD_ID_AE2, "part/display_status_has_channel"), // 3
            new ResourceLocation(ModGlobals.MOD_ID_AE2, "part/display_status_on"), // 4
            new ResourceLocation(ModGlobals.MOD_ID_AE2, "part/display_status_off") // 5
    };

    private static IPartModel MODEL_ON = new ThEPartModel(MODELS[0], MODELS[1], MODELS[4]);
    private static IPartModel MODEL_OFF = new ThEPartModel(MODELS[0], MODELS[2], MODELS[5]);
    private static IPartModel MODEL_HAS_CHANNEL = new ThEPartModel(MODELS[0], MODELS[1], MODELS[3]);

    protected ThEInternalInventory craftingInventory;
    protected ThEInternalInventory upgradeInventory;

    public PartArcaneTerminal(ItemArcaneTerminal item){
        this(item, ModGUIs.ARCANE_TERMINAL);
    }

    public PartArcaneTerminal(ItemArcaneTerminal item, ModGUIs gui) {
        super(item, gui);
        this.craftingInventory = new ThEInternalInventory("matrix", 15, 64);
        this.upgradeInventory = new ThEUpgradeInventory("upgrades", 1, 1, this.getItemStack(PartItemStack.NETWORK));
    }

    @Override
    protected AESettings.SUBJECT getAESettingSubject() {
        return AESettings.SUBJECT.ARCANE_TERMINAL;
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
        list.addAll(ItemHandlerUtil.getInventoryAsList(this.getInventoryByName("crafting")));
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        if ((player.isSneaking() && AEUtil.isWrench(player.getHeldItem(hand), player, this.getTile().getPos())))
            return false;

        if (ForgeUtil.isServer())
            GuiHandler.openGUI(this.getGui(), player, this.hostTile.getPos(), this.side);

        this.host.markForUpdate();
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
