package thaumicenergistics.container.part;

import java.util.Objects;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import appeng.api.AEApi;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.items.ItemsTC;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.ContainerBaseTerminal;
import thaumicenergistics.container.DummyContainer;
import thaumicenergistics.container.ICraftingContainer;
import thaumicenergistics.container.crafting.ContainerCraftAmountBridge;
import thaumicenergistics.container.slot.SlotArcaneMatrix;
import thaumicenergistics.container.slot.SlotArcaneResult;
import thaumicenergistics.container.slot.SlotUpgrade;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.integration.thaumcraft.TCCraftingManager;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.*;
import thaumicenergistics.part.PartBase;
import thaumicenergistics.part.PartSharedTerminal;
import thaumicenergistics.util.*;
import thaumicenergistics.util.inventory.ThEInternalInventory;

import com.google.common.collect.Lists;

/**
 * @author BrockWS
 * @author Alex811
 */
public class ContainerArcaneTerminal extends ContainerBaseTerminal implements IMEMonitorHandlerReceiver<IAEItemStack>, ICraftingContainer {

    public IRecipe recipe;

    protected PartSharedTerminal part;
    protected IItemStorageChannel channel;
    protected IMEMonitor<IAEItemStack> monitor;
    protected IInventory craftingResult;
    protected SlotArcaneResult resultSlot;


    public ContainerArcaneTerminal(EntityPlayer player, PartSharedTerminal part){
        super(player, part);
        this.part = part;

        // We use the client config manager on server as well to make sure the client is in sync
        this.clientConfigManager.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientConfigManager.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.clientConfigManager.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        this.clientConfigManager.registerSetting(Settings.SEARCH_MODE, ThEApi.instance().config().searchBoxMode());

        if (ForgeUtil.isServer()) {
            this.channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
            this.monitor = this.part.getInventory(this.channel);
            if (this.monitor != null) {
                this.monitor.addListener(this, null);
            }
        }

        this.addMatrixSlots(32, 36);
        this.addUpgradeSlots(177, 54);

        this.bindPlayerInventory(new PlayerMainInvWrapper(player.inventory), 0, 106);
        this.bindPlayerArmour(player, new PlayerArmorInvWrapper(player.inventory), 8, 19);
    }

    @Override
    public PartBase getPart() {
        return this.part;
    }

    @Override
    public void onAction(EntityPlayerMP player, PacketUIAction packet) {
        // TODO: Give inventoryInsert/inventoryExtract IEnergyGrid to extract power
        if (this.monitor == null)
            return;
        if (packet.action == ActionType.PICKUP_OR_SETDOWN) { // Normal lmb
            if (player.inventory.getItemStack().isEmpty() && packet.requestedStack != null) { // PICKUP
                IAEItemStack stack = (IAEItemStack) packet.requestedStack.copy();
                stack.setStackSize(stack.getDefinition().getMaxStackSize());
                stack = AEUtil.inventoryExtract(stack, this.monitor, this.part.source);

                if (stack != null)
                    player.inventory.setItemStack(stack.createItemStack());
                else
                    player.inventory.setItemStack(ItemStack.EMPTY);
                PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(player.inventory.getItemStack()));
            } else if (!player.inventory.getItemStack().isEmpty()) { // Set down
                IAEItemStack stack = this.channel.createStack(player.inventory.getItemStack());
                stack = AEUtil.inventoryInsert(stack, this.monitor, this.part.source);

                if (stack != null)
                    player.inventory.setItemStack(stack.createItemStack());
                else
                    player.inventory.setItemStack(ItemStack.EMPTY);
                PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(player.inventory.getItemStack()));
            }
        } else if (packet.action == ActionType.SPLIT_OR_PLACE_SINGLE) { // Normal rmb
            if (player.inventory.getItemStack().isEmpty() && packet.requestedStack != null) { // Grab half
                IAEItemStack stack = (IAEItemStack) packet.requestedStack.copy();
                stack.setStackSize(stack.getDefinition().getMaxStackSize()); // Cap it to max stack size
                stack = AEUtil.inventoryExtract(stack, this.monitor, this.part.source, Actionable.SIMULATE); // Double check how much we have available

                if (stack != null) {
                    long toPull = (long) Math.ceil((double) stack.getStackSize() / 2);
                    stack = AEUtil.inventoryExtract(stack.setStackSize(toPull), this.monitor, this.part.source);
                }

                if (stack != null)
                    player.inventory.setItemStack(stack.createItemStack());
                else
                    player.inventory.setItemStack(ItemStack.EMPTY);
                PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(player.inventory.getItemStack()));
            } else if (!player.inventory.getItemStack().isEmpty()) { // Drop single
                IAEItemStack stack = this.channel.createStack(player.inventory.getItemStack());
                Objects.requireNonNull(stack).setStackSize(1);
                stack = AEUtil.inventoryInsert(stack, this.monitor, this.part.source);
                if (stack == null) {
                    ItemStack stack2 = player.inventory.getItemStack();
                    stack2.setCount(stack2.getCount() - 1);
                    if (stack2.isEmpty())
                        player.inventory.setItemStack(ItemStack.EMPTY);
                    PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(player.inventory.getItemStack()));
                }
            }
        } else if ((packet.action == ActionType.SCROLL_UP || packet.action == ActionType.PICKUP_SINGLE) && packet.requestedStack instanceof IAEItemStack) { // Shift rmb
            ItemStack held = player.inventory.getItemStack();
            if (!held.isEmpty() && (held.getCount() >= held.getMaxStackSize() || !ForgeUtil.areItemStacksEqual(((IAEItemStack) packet.requestedStack).getDefinition(), held)))
                return;
            IAEItemStack stack = (IAEItemStack) packet.requestedStack.copy();
            stack.setStackSize(1);
            stack = AEUtil.inventoryExtract(stack, this.monitor, this.part.source);
            if (stack != null) {
                if (!held.isEmpty())
                    held.grow(1);
                else
                    held = stack.createItemStack();
            }
            player.inventory.setItemStack(held);
            PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(player.inventory.getItemStack()));
        } else if (packet.action == ActionType.SCROLL_DOWN && !player.inventory.getItemStack().isEmpty()) {
            ItemStack held = player.inventory.getItemStack();
            IAEItemStack is = this.channel.createStack(held);
            Objects.requireNonNull(is);
            is.setStackSize(1);
            is = AEUtil.inventoryInsert(is, this.monitor, this.part.source, Actionable.MODULATE);
            if (is != null) // Failed to insert one item
                return;
            if (held.getCount() > 1) {
                held.shrink(1);
            } else {
                held = ItemStack.EMPTY;
            }
            player.inventory.setItemStack(held);
            PacketHandler.sendToPlayer(player, new PacketInvHeldUpdate(player.inventory.getItemStack()));
        } else if (packet.action == ActionType.SHIFT_MOVE && packet.requestedStack instanceof IAEItemStack) {
            IAEItemStack stack = ((IAEItemStack) packet.requestedStack).copy();
            ItemStack is = stack.createItemStack();

            // Cap to max stack size
            stack.setStackSize(is.getMaxStackSize());
            is.setCount((int) stack.getStackSize());

            is = ForgeUtil.addStackToPlayerInventory(player, is, true);
            if (!is.isEmpty())
                stack.setStackSize(stack.getStackSize() - is.getCount());
            stack = AEUtil.inventoryExtract(stack, this.monitor, this.part.source);
            if (stack != null)
                ForgeUtil.addStackToPlayerInventory(player, stack.createItemStack(), false);
        } else if (packet.action == ActionType.AUTO_CRAFT) {
            if (!packet.requestedStack.isCraftable())
                return;
            GuiHandler.openGUI(ModGUIs.AE2_CRAFT_AMOUNT, player, this.part.getLocation().getPos(), this.part.side);
            if (player.openContainer instanceof ContainerCraftAmountBridge) {
                ContainerCraftAmountBridge cca = (ContainerCraftAmountBridge) player.openContainer;
                cca.getCraftingItem().putStack(packet.requestedStack.asItemStackRepresentation());
                cca.setItemToCraft((IAEItemStack) packet.requestedStack);
            }
        } else if (packet.action == ActionType.CLEAR_GRID) {
            clearCrafting();
        }
        this.onMatrixChanged();
    }

    protected void clearCrafting() {
        AEUtil.clearIntoMEInventory(this.getInventory("crafting"), this.monitor, this.part.source);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        if (ForgeUtil.isClient() || index < 0 || index > this.inventorySlots.size())
            return super.transferStackInSlot(playerIn, index);
        Slot slot = this.inventorySlots.get(index);
        if (slot.getHasStack() && !slot.getStack().isEmpty()) {
            IAEItemStack remaining = AEUtil.inventoryInsert(this.channel.createStack(slot.getStack()), this.monitor, this.part.source, Actionable.MODULATE);
            slot.putStack(remaining == null ? ItemStack.EMPTY : remaining.createItemStack());
            this.detectAndSendChanges();
        }

        return super.transferStackInSlot(playerIn, index);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.player instanceof IContainerListener)
            this.sendVisInfo((IContainerListener) this.player);
    }

    @Override
    public void handleJEITransfer(EntityPlayer player, NBTTagCompound tag) {
        NBTBase normal = tag.getTag("normal");
        NBTBase crystals = tag.getTag("crystal");

        boolean clearSuccess = AEUtil.clearIntoMEInventory(this.getInventory("crafting"), this.monitor, this.part.source);
        this.onMatrixChanged();
        if (!clearSuccess)
            return;

        handleJEITag(0, normal);
        handleJEITag(9, crystals);

        this.onMatrixChanged();
    }

    private void handleJEITag(int startAtSlot, NBTBase ingredientGroup){
        IItemHandler crafting = this.getInventory("crafting");
        IItemHandler playerInv = this.getInventory("player");

        if (ingredientGroup == null || ingredientGroup.isEmpty()) {
            // TODO: Probably check if its already in the slot
            return;
        }
        NBTTagList subs = (NBTTagList) ingredientGroup;
        for (int i = 0; i < subs.tagCount(); i++) {
            int slot = startAtSlot + i;
            NBTTagCompound ingredient = ((NBTTagList) subs.get(i)).getCompoundTagAt(0);
            ItemStack stack = new ItemStack(ingredient);
            if (stack.isEmpty()) continue;

            ThELog.debug("Adding {} for {}", stack.getDisplayName(), slot);
            IAEItemStack aeStack = this.channel.createStack(stack);
            if (aeStack == null) {
                ThELog.warn("Failed to create IAEItemStack for {}, report to developer!", stack.toString());
                continue;
            }
            IAEItemStack aeExtract = AEUtil.inventoryExtract(aeStack, this.monitor, this.part.source);
            if (aeExtract != null && aeExtract.getStackSize() > 0)
                crafting.insertItem(slot, aeExtract.createItemStack(), false);

            if (crafting.getStackInSlot(slot).getCount() >= stack.getCount()) // We managed to pull everything from the system
                continue;

            // Try pull from player
            ThELog.debug("Failed to pull item from ae inv, trying player inventory");
            stack.shrink(crafting.getStackInSlot(slot).getCount());

            ItemStack invExtract = ItemHandlerUtil.extract(playerInv, stack, false);
            if (!invExtract.isEmpty())
                crafting.insertItem(slot, invExtract, false);
        }
        ThELog.debug("Failed to find valid item");
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return true;
    }

    @Override
    public void postChange(IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change, IActionSource actionSource) {
        for (IContainerListener c : this.listeners) {
            this.sendInventory(c);
        }
    }

    @Override
    public void onListUpdate() {
        for (IContainerListener c : this.listeners) {
            this.sendInventory(c);
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        this.sendVisInfo(listener);
        this.sendInventory(listener);
        this.onMatrixChanged();
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    @Override
    public void onMatrixChanged() {
        if (ForgeUtil.isClient())
            return;
        this.craftingResult.setInventorySlotContents(0, ItemStack.EMPTY);
        this.detectAndSendChanges();
        IItemHandler matrix = this.getInventory("crafting");
        this.recipe = TCCraftingManager.findArcaneRecipe(matrix, this.player);
        if (this.recipe != null) {
            this.craftingResult.setInventorySlotContents(0, TCCraftingManager.getCraftingResult(this.getInventory("crafting"), (IArcaneRecipe) this.recipe));
            this.detectAndSendChanges();
            return;
        }
        InventoryCrafting inventory = new InventoryCrafting(new DummyContainer(), 3, 3);
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            inventory.setInventorySlotContents(i, matrix.getStackInSlot(i));
        }
        this.recipe = CraftingManager.findMatchingRecipe(inventory, this.player.world);
        if (this.recipe != null) {
            this.craftingResult.setInventorySlotContents(0, this.recipe.getCraftingResult(inventory));
            this.detectAndSendChanges();
        }
    }

    @Override
    public int tryCraft(int amount) {
        this.onMatrixChanged();
        if (this.recipe == null || ForgeUtil.isClient())
            return 0;
        float canCraft = amount;
        if (this.recipe instanceof IArcaneRecipe) {
            float visRequired = ((IArcaneRecipe) this.recipe).getVis() * (1f - this.getDiscount(this.player));
            canCraft = this.getWorldVis() / visRequired;
        }

        return Math.min(amount, (int) canCraft);
    }

    @Override
    public ItemStack onCraft(ItemStack toCraft) {
        if (toCraft.isEmpty())
            return ItemStack.EMPTY;
        IItemHandler crafting = this.getInventory("crafting");
        InventoryCrafting inv = this.getInvCrafting(crafting, this.recipe);
        ItemStack crafted = this.recipe.getCraftingResult(inv);
        int roomLeft = Math.min(crafted.getMaxStackSize(), toCraft.getCount() * crafted.getCount());
        int timesCrafted = 0;
        boolean craftAgain = true;
        do {
            roomLeft -= crafted.getCount();
            NonNullList<ItemStack> remaining = this.getRemaining(this.recipe, inv);

            for (int j = 0; j < remaining.size(); j++) {
                if (crafting.getStackInSlot(j).isEmpty()) // The slot is empty so ignore it
                    continue;
                ItemStack extract = crafting.extractItem(j, Integer.MAX_VALUE, false);
                if (!remaining.get(j).isEmpty()) { // We still have some remaining
                    crafting.insertItem(j, remaining.get(j), false);
                } else {
                    crafting.insertItem(j, this.getRefill(extract), false);
                }
            }
            if (this.getCurrentRequiredVis() > 0)
                TCUtil.drainVis(this.part.getTile().getWorld(),
                        this.part.getTile().getPos(),
                        this.getCurrentRequiredVis(),
                        this.getInventory("upgrades").getStackInSlot(0).isEmpty() ? 0 : 1);

            // Re-craft safety checks
            inv = this.getInvCrafting(crafting, this.recipe);

            if (!this.recipe.matches(inv, this.player.world)) // Check if we can craft again
                craftAgain = false;

            if (this.getWorldVis() < this.getCurrentRequiredVis())
                craftAgain = false;

            timesCrafted++;
        } while (roomLeft > 0 && roomLeft >= crafted.getCount() && craftAgain);
        crafted.setCount(timesCrafted * crafted.getCount());
        this.onMatrixChanged();
        this.detectAndSendChanges();
        if (crafted.getCount() > 0) {
            crafted.onCrafting(this.player.world, this.player, crafted.getCount());
            FMLCommonHandler.instance().firePlayerCraftingEvent(this.player, crafted, inv);

            if (this.recipe != null && !this.recipe.isDynamic()) {
                this.player.unlockRecipes(Lists.newArrayList(this.recipe));
            }
        }
        return crafted;
    }

    @Override
    public IItemHandler getInventory(String name) {
        switch (name.toLowerCase()) {
            case "crafting":
            case "upgrades":
                return this.part.getInventoryByName(name);
            case "result":
                return new InvWrapper(this.craftingResult);
            case "player":
                return new PlayerInvWrapper(this.player.inventory);
        }
        return null;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public BlockPos getPartPos() {
        return this.part.getLocation().getPos();
    }

    public AEPartLocation getPartSide() {
        return this.part.side;
    }

    @SuppressWarnings("SameParameterValue")
    protected void addMatrixSlots(int offsetX, int offsetY) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotArcaneMatrix(this, i * 3 + j, offsetX + (j * 18), offsetY + (i * 18)));
            }
        }
        offsetX += 104;
        for (int i = 0; i < 3; i++) { // Y
            for (int j = 0; j < 2; j++) { // X
                this.addSlotToContainer(new SlotArcaneMatrix(this, 9 + (i * 2 + j), offsetX + (j * 18), offsetY + (i * 18)));
            }
        }
        offsetX -= 104;
        this.craftingResult = new ThEInternalInventory("Result", 1, 64);
        this.addSlotToContainer(this.resultSlot = new SlotArcaneResult(this, this.player, 0, offsetX + 84, offsetY + 18));
        this.onMatrixChanged();
    }

    protected void addUpgradeSlots(int offsetX, int offsetY) {
        this.addSlotToContainer(new SlotUpgrade(this.getInventory("upgrades"), 0, offsetX, offsetY)/* {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return ThEApi.instance().items().upgradeArcane().isSameAs(stack);
            }
        }*/);
    }

    protected void sendVisInfo(IContainerListener listener) {
        if (ForgeUtil.isClient() || !(listener instanceof EntityPlayerMP))
            return;
        PacketHandler.sendToPlayer((EntityPlayerMP) this.player, new PacketVisUpdate(this.getWorldVis(), this.getCurrentRequiredVis(), this.getDiscount(this.player)));
    }

    protected float getWorldVis() {
        TileEntity te = this.part.getTile();
        float vis = AuraHelper.getVis(te.getWorld(), te.getPos());
        if (!this.getInventory("upgrades").getStackInSlot(0).isEmpty()) {
            vis += AuraHelper.getVis(te.getWorld(), te.getPos().add(-16, 0, -16));
            vis += AuraHelper.getVis(te.getWorld(), te.getPos().add(-16, 0, 0));
            vis += AuraHelper.getVis(te.getWorld(), te.getPos().add(-16, 0, 16));

            vis += AuraHelper.getVis(te.getWorld(), te.getPos().add(0, 0, -16));
            vis += AuraHelper.getVis(te.getWorld(), te.getPos().add(0, 0, 16));

            vis += AuraHelper.getVis(te.getWorld(), te.getPos().add(16, 0, -16));
            vis += AuraHelper.getVis(te.getWorld(), te.getPos().add(16, 0, 0));
            vis += AuraHelper.getVis(te.getWorld(), te.getPos().add(16, 0, 16));
        }
        return vis;
    }

    protected float getRequiredVis(IRecipe recipe, EntityPlayer player) {
        if (!(recipe instanceof IArcaneRecipe))
            return -1;
        return ((IArcaneRecipe) recipe).getVis() * (1f - this.getDiscount(player));
    }

    public float getCurrentRequiredVis(){
        return this.getRequiredVis(this.recipe, this.player);
    }

    protected float getDiscount(EntityPlayer player) {
        return TCCraftingManager.getDiscount(player);
    }

    private NonNullList<ItemStack> getRemaining(IRecipe recipe, InventoryCrafting inv) {
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(inv);
        AspectList crystals = this.recipe instanceof IArcaneRecipe ? ((IArcaneRecipe) this.recipe).getCrystals() : null;
        for (int i = 0; i < remaining.size(); i++) {
            if (i < 9) {
                boolean hasLeftover = !remaining.get(i).isEmpty();
                ItemStack existing = inv.getStackInSlot(i);
                if (existing.getCount() > 1) { // We had more than one
                    if (!hasLeftover)
                        existing.shrink(1);
                    remaining.set(i, existing);
                }
            } else {
                if (crystals == null || crystals.size() < 1) // We don't require crystals in this recipe
                    break;
                ItemStack crystalStack = inv.getStackInSlot(i);
                if (crystalStack.isEmpty())
                    continue;
                Aspect crystalAspect = TCUtil.getCrystalAspect(crystalStack);
                if (crystals.getAmount(crystalAspect) > 0) // We require X aspects in this recipe
                    crystalStack.shrink(crystals.getAmount(crystalAspect));
                if (crystalStack.getCount() > 0)
                    remaining.set(i, crystalStack);
            }
        }
        return remaining;
    }

    private ItemStack getRefill(ItemStack stack) {
        // TODO: Fuzzy selection
        IAEItemStack aeStack = this.monitor.extractItems(this.channel.createStack(stack), Actionable.SIMULATE, this.part.source);
        if (aeStack != null && aeStack.getStackSize() == stack.getCount()) {// Make sure we actually have enough to pull
           /* try {
                GridUtil.getEnergyGrid(this.part.getGridNode()).extractAEPower(1, Actionable.MODULATE, PowerMultiplier.CONFIG);
            } catch (GridAccessException ignored) {

            }*/
            return this.monitor.extractItems(aeStack, Actionable.MODULATE, this.part.source).createItemStack();
        }
        return ItemStack.EMPTY;
    }

    protected void sendInventory(IContainerListener listener) {
        if (ForgeUtil.isClient() || !(listener instanceof EntityPlayerMP) || this.monitor == null)
            return;
        IItemList<IAEItemStack> storage = this.monitor.getStorageList();
        PacketMEItemUpdate packet = new PacketMEItemUpdate();
        for (IAEItemStack stack : storage)
            packet.appendStack(stack);
        PacketHandler.sendToPlayer((EntityPlayerMP) listener, packet);
    }

    private InventoryCrafting getInvCrafting(IItemHandler handler, IRecipe recipe) {
        if (recipe instanceof IArcaneRecipe)
            return TCCraftingManager.getInvFromItemHandler(handler);
        InventoryCrafting inv = new InventoryCrafting(new DummyContainer(), 3, 3);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            inv.setInventorySlotContents(i, this.getInventory("crafting").getStackInSlot(i).copy());
        }
        return inv;
    }

    private boolean isCrystalRequired(IRecipe recipe, ItemStack stack) {
        if (!(recipe instanceof IArcaneRecipe) || stack.isEmpty() || !(stack.getItem() instanceof IEssentiaContainerItem) || stack.getItem() != ItemsTC.crystalEssence)
            return false;
        AspectList aspect = ((IEssentiaContainerItem) stack.getItem()).getAspects(stack);
        return ((IArcaneRecipe) recipe).getCrystals().getAmount(aspect.getAspects()[0]) > 0;
    }
}
