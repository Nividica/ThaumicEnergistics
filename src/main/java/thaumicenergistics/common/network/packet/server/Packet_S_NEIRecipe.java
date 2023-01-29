package thaumicenergistics.common.network.packet.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.tiles.TileMagicWorkbench;
import thaumicenergistics.common.container.ContainerInternalCrafting;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.integration.tc.ArcaneRecipeHelper;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.IPartitionList;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;

public class Packet_S_NEIRecipe extends ThEServerPacket {

    private ItemStack[][] recipe = null;
    private NBTTagCompound nbt = null;

    @Override
    protected void readData(ByteBuf stream) {
        setRecipe(ByteBufUtils.readTag(stream));
    }

    public void setRecipe(NBTTagCompound comp) {
        nbt = comp;
        if (comp != null) {
            this.recipe = new ItemStack[9][];
            for (int x = 0; x < this.recipe.length; x++) {
                final NBTTagList list = comp.getTagList("#" + x, 10);
                if (list.tagCount() > 0) {
                    this.recipe[x] = new ItemStack[list.tagCount()];
                    for (int y = 0; y < list.tagCount(); y++) {
                        this.recipe[x][y] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(y));
                    }
                }
            }
        }
    }

    /**
     * Allows subclasses to write data into the specified stream.
     *
     * @param stream
     */
    @Override
    protected void writeData(ByteBuf stream) {
        if (nbt != null) ByteBufUtils.writeTag(stream, nbt);
    }

    /**
     * Packet has been read and action can now take place.
     */
    @Override
    public void execute() {
        final EntityPlayerMP pmp = (EntityPlayerMP) player;
        final Container con = pmp.openContainer;

        if (con instanceof ContainerPartArcaneCraftingTerminal) {

            ContainerPartArcaneCraftingTerminal act = (ContainerPartArcaneCraftingTerminal) con;
            InventoryCrafting testInv = new InventoryCrafting(new ContainerInternalCrafting(), 3, 3);
            for (int x = 0; x < 9; x++) {
                if (this.recipe[x] != null && this.recipe[x].length > 0) {
                    testInv.setInventorySlotContents(x, this.recipe[x][0]);
                }
            }
            IRecipe r = Platform.findMatchingRecipe(testInv, player.worldObj);
            IArcaneRecipe arcaneRecipe = r == null
                    ? ArcaneRecipeHelper.INSTANCE.findMatchingArcaneResult(testInv, 0, 9, this.player)
                    : null;
            IGrid grid = act.getHostGrid();
            if (grid == null) return;
            final IStorageGrid inv = grid.getCache(IStorageGrid.class);
            final IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
            final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
            TileMagicWorkbench workbenchTile = ArcaneRecipeHelper.INSTANCE.createBridgeInventory(testInv, 0, 9);
            workbenchTile.setWorldObj(player.worldObj);
            if ((r != null || arcaneRecipe != null) && security != null
                    && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
                final ItemStack is = r != null ? r.getCraftingResult(testInv)
                        : arcaneRecipe.getCraftingResult(workbenchTile);
                if (is == null) return;
                final IMEMonitor<IAEItemStack> storage = inv.getItemInventory();
                final IItemList<IAEItemStack> all = storage.getStorageList();
                final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(act.getViewCells());
                final IInventory craftMatrix = (PartArcaneCraftingTerminal) act.getCraftingHost();
                final IInventory playerInventory = player.inventory;
                BaseActionSource as = new PlayerSource(player, act.terminal);
                for (int x = 0; x < 9; x++) {
                    final ItemStack patternItem = testInv.getStackInSlot(x);
                    ItemStack currentItem = craftMatrix.getStackInSlot(x);
                    if (currentItem != null) {
                        testInv.setInventorySlotContents(x, currentItem);
                        workbenchTile.setInventorySlotContents(x, currentItem);
                        final ItemStack newItemStack = r != null
                                ? (r.matches(testInv, pmp.worldObj) ? r.getCraftingResult(testInv) : null)
                                : (arcaneRecipe.matches(workbenchTile, pmp.worldObj, pmp)
                                        ? arcaneRecipe.getCraftingResult(workbenchTile)
                                        : null);
                        testInv.setInventorySlotContents(x, patternItem);
                        workbenchTile.setInventorySlotContents(x, patternItem);

                        if (newItemStack == null || !Platform.isSameItemPrecise(newItemStack, is)) {
                            final IAEItemStack in = AEItemStack.create(currentItem);
                            final IAEItemStack out = Platform.poweredInsert(energy, storage, in, as);
                            craftMatrix.setInventorySlotContents(x, out != null ? out.getItemStack() : null);
                            currentItem = craftMatrix.getStackInSlot(x);
                        }
                    }
                    if (patternItem != null && currentItem == null) {
                        ItemStack whichItem = r != null
                                ? Platform.extractItemsByRecipe(
                                        energy,
                                        as,
                                        storage,
                                        player.worldObj,
                                        r,
                                        is,
                                        testInv,
                                        patternItem,
                                        x,
                                        all,
                                        Actionable.MODULATE,
                                        filter)
                                : extractItemsByArcaneRecipe(
                                        energy,
                                        as,
                                        storage,
                                        player.worldObj,
                                        arcaneRecipe,
                                        is,
                                        workbenchTile,
                                        patternItem,
                                        x,
                                        all,
                                        Actionable.MODULATE,
                                        filter);
                        if (whichItem == null && playerInventory != null)
                            whichItem = extractItemFromPlayerInventory(player, patternItem);

                        craftMatrix.setInventorySlotContents(x, whichItem);
                    }
                    con.onCraftMatrixChanged(craftMatrix);
                }
            }
        }
    }

    ItemStack extractItemsByArcaneRecipe(final IEnergySource energySrc, final BaseActionSource mySrc,
            final IMEMonitor<IAEItemStack> src, final World w, final IArcaneRecipe r, final ItemStack output,
            final TileMagicWorkbench workbenchTile, final ItemStack providedTemplate, final int slot,
            final IItemList<IAEItemStack> items, final Actionable realForFake,
            final IPartitionList<IAEItemStack> filter) {
        if (energySrc.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.9) {
            if (providedTemplate == null) {
                return null;
            }

            final AEItemStack ae_req = AEItemStack.create(providedTemplate);
            ae_req.setStackSize(1);

            if (filter == null || filter.isListed(ae_req)) {
                final IAEItemStack ae_ext = src.extractItems(ae_req, realForFake, mySrc);
                if (ae_ext != null) {
                    final ItemStack extracted = ae_ext.getItemStack();
                    if (extracted != null) {
                        energySrc.extractAEPower(1, realForFake, PowerMultiplier.CONFIG);
                        return extracted;
                    }
                }
            }

            final boolean checkFuzzy = ae_req.isOre()
                    || providedTemplate.getItemDamage() == OreDictionary.WILDCARD_VALUE
                    || providedTemplate.hasTagCompound()
                    || providedTemplate.isItemStackDamageable();

            if (items != null && checkFuzzy) {
                for (final IAEItemStack x : items) {
                    final ItemStack sh = x.getItemStack();
                    if ((Platform.isSameItemType(providedTemplate, sh) || ae_req.sameOre(x))
                            && !Platform.isSameItem(sh, output)) { // Platform.isSameItemType( sh, providedTemplate )
                        final ItemStack cp = Platform.cloneItemStack(sh);
                        cp.stackSize = 1;
                        workbenchTile.setInventorySlotContents(slot, cp);
                        if (r.matches(workbenchTile, w, player)
                                && Platform.isSameItem(r.getCraftingResult(workbenchTile), output)) {
                            final IAEItemStack ax = x.copy();
                            ax.setStackSize(1);
                            if (filter == null || filter.isListed(ax)) {
                                final IAEItemStack ex = src.extractItems(ax, realForFake, mySrc);
                                if (ex != null) {
                                    energySrc.extractAEPower(1, realForFake, PowerMultiplier.CONFIG);
                                    return ex.getItemStack();
                                }
                            }
                        }
                        workbenchTile.setInventorySlotContents(slot, providedTemplate);
                    }
                }
            }
        }
        return null;
    }

    private ItemStack extractItemFromPlayerInventory(final EntityPlayer player, final ItemStack patternItem) {
        final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
        final AEItemStack request = AEItemStack.create(patternItem);
        final boolean checkFuzzy = request.isOre() || patternItem.getItemDamage() == OreDictionary.WILDCARD_VALUE
                || patternItem.hasTagCompound()
                || patternItem.isItemStackDamageable();
        return checkFuzzy ? ia.removeSimilarItems(1, patternItem, FuzzyMode.IGNORE_ALL, null)
                : ia.removeItems(1, patternItem, null);
    }
}
