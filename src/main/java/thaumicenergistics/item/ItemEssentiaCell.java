package thaumicenergistics.item;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IItemList;
import appeng.core.localization.GuiText;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.api.EssentiaStack;
import thaumicenergistics.api.model.IThEModel;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.AEEssentiaStack;

/**
 * @author BrockWS
 */
public class ItemEssentiaCell extends ItemBase implements IStorageCell<IAEEssentiaStack>, IThEModel {

    private String size;
    private int bytes;
    private int types;

    public ItemEssentiaCell(String size, int bytes, int types) {
        super("essentia_cell_" + size);

        this.size = size;
        this.bytes = bytes;
        this.types = types;

        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(false);
        this.setCreativeTab(ModGlobals.CREATIVE_TAB);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        IMEInventoryHandler<IAEEssentiaStack> inventory = AEApi.instance().registries().cell().getCellInventory(stack, null, getChannel());
        inventory.injectItems(AEEssentiaStack.fromEssentiaStack(new EssentiaStack(Aspect.FIRE, 1000)), Actionable.MODULATE, null);
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell().getCellInventory(stack, null, getChannel());

        if (inventory instanceof ICellInventoryHandler) {
            final ICellInventoryHandler handler = (ICellInventoryHandler) inventory;
            final ICellInventory cellInventory = handler.getCellInv();

            if (cellInventory != null) {
                tooltip.add(cellInventory.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal());

                tooltip.add(cellInventory.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalItemTypes() + ' ' + GuiText.Types.getLocal());

                if (handler.isPreformatted()) {
                    final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded).getLocal();

                    if (handler.isFuzzy()) {
                        tooltip.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Fuzzy.getLocal());
                    } else {
                        tooltip.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());
                    }
                }

                // TODO: Temporary util Essentia Terminal is added
                IItemList<IAEEssentiaStack> list = inventory.getAvailableItems(this.getChannel().createList());

                for (IAEEssentiaStack s : list) {
                    tooltip.add(s.getAspect().getName() + " : " + s.getStackSize());
                }
            }
        }

    }

    @Override
    public int getBytes(ItemStack itemStack) {
        return this.bytes;
    }

    @Override
    public int getBytesPerType(ItemStack itemStack) {
        return 8;
    }

    @Override
    public int getTotalTypes(ItemStack itemStack) {
        return this.types;
    }

    @Override
    public boolean isBlackListed(ItemStack itemStack, IAEEssentiaStack iaeEssentiaStack) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack itemStack) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 1;
    }

    @Override
    public IStorageChannel getChannel() {
        return AEApi.instance().storage().getStorageChannel(IEssentiaStorageChannel.class);
    }

    @Override
    public boolean isEditable(ItemStack itemStack) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(ItemStack itemStack) {
        return new CellUpgrades(itemStack, 0);
    }

    @Override
    public IItemHandler getConfigInventory(ItemStack itemStack) {
        return new CellConfig(itemStack);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack itemStack) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack itemStack, FuzzyMode fuzzyMode) {

    }

    @Override
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":cell/essentia_cell_" + this.size, "inventory"));
    }
}
