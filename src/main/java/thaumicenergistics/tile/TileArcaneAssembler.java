package thaumicenergistics.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import thaumcraft.api.aura.AuraHelper;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.client.gui.IThEGuiTile;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.integration.appeng.grid.GridUtil;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketAssemblerGUIUpdate;
import thaumicenergistics.util.*;
import thaumicenergistics.util.inventory.IThEInvTile;
import thaumicenergistics.util.inventory.ThEInternalInventory;
import thaumicenergistics.util.inventory.ThEKnowledgeCoreInventory;
import thaumicenergistics.util.inventory.ThEUpgradeInventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Alex811
 */
public class TileArcaneAssembler extends TileNetwork implements IThESubscribable, IThEInvTile, IThEGuiTile, ICraftingProvider, IStorageMonitorable, IGridTickable {
    protected static final int BASE_STEP = 5;               // step to increase progress by / tick (not counting upgrades)
    private IItemStorageChannel channel;
    protected ThEInternalInventory coreInv;                 // contains Knowledge Core
    protected ThEUpgradeInventory upgradeInv;
    protected ThEInternalInventory craftingInv;             // what's being crafted
    protected int progress = 0;                             // crafting progress %
    protected HashMap<String, Boolean> aspectExists = new HashMap<>();
    protected boolean hasEnoughVis = true;

    public TileArcaneAssembler() {
        super();
        ItemStack assemblerItem = ThEApi.instance().blocks().arcaneAssembler().maybeStack(1).orElseThrow(RuntimeException::new);
        this.coreInv = new ThEKnowledgeCoreInventory("cores", 1, 1, assemblerItem);
        this.upgradeInv = new ThEUpgradeInventory("upgrades", 5, 1, assemblerItem);
        this.craftingInv = new ThEInternalInventory("crafting", 1, 64);
        this.channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(super.getUpdateTag());
    }

    @Override
    @MethodsReturnNonnullByDefault
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("cores", this.coreInv.serializeNBT());
        tag.setTag("upgrades", this.upgradeInv.serializeNBT());
        tag.setTag("crafting", this.craftingInv.serializeNBT());
        if (ForgeUtil.isClient())
            return super.writeToNBT(tag);
        super.writeToNBT(tag);
        return tag;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("cores")){
            this.coreInv.deserializeNBT(tag.getTagList("cores", 10));
        }
        if (tag.hasKey("upgrades")){
            this.upgradeInv.deserializeNBT(tag.getTagList("upgrades", 10));
        }
        if (tag.hasKey("crafting")){
            this.craftingInv.deserializeNBT(tag.getTagList("crafting", 10));
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }

    @Override
    public void openGUI(EntityPlayer player) {
        GuiHandler.openGUI(this.getGUI(), player, this.getPos());
    }

    @Override
    public ModGUIs getGUI() {
        return ModGUIs.ARCANE_ASSEMBLER;
    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        switch(name){
            case "cores": return new InvWrapper(this.coreInv);
            case "upgrades": return new InvWrapper(this.upgradeInv);
        }
        return null;
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        if(!this.isActive()) return;
        final ItemStack knowledgeCore = coreInv.getStackInSlot(0);
        KnowledgeCoreUtil.recipeStreamOf(knowledgeCore)
                .map(recipe -> KnowledgeCoreUtil.getAEPattern(recipe, world))
                .forEach(AEPattern -> {
                    AEPattern.setPriority(0);
                    craftingTracker.addCraftingOption(this, AEPattern);
                });
    }

    /**
     * Begins the crafting process if we have everything and takes out the ingredients
     * @see #tickingRequest(IGridNode, int)
     */
    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        ItemStack result = patternDetails.getOutputs()[0].createItemStack();
        KnowledgeCoreUtil.Recipe recipe = KnowledgeCoreUtil.getRecipe(this.coreInv.getStackInSlot(0), result);
        if(recipe == null) return false;
        // Check vis
        this.hasEnoughVis = this.getWorldVis() >= recipe.getVisCost();
        // Simulate removing aspects
        aspectExists = new HashMap<>();
        IMEMonitor<IAEItemStack> inventory = this.getInventory(this.channel);
        ArrayList<ItemStack> aspects = new ArrayList<>();
        AtomicBoolean missingAspect = new AtomicBoolean(false);
        recipe.getIngredientPart(true).forEach(aspect -> {
            if(!aspect.isEmpty()){
                IAEItemStack canExtractAmount = AEUtil.inventoryExtract(this.channel.createStack(aspect), inventory, this.src, Actionable.SIMULATE);
                String aspectName = Objects.requireNonNull(TCUtil.getCrystalAspect(aspect)).getTag();
                this.aspectExists.put(aspectName, canExtractAmount != null && canExtractAmount.getStackSize() == aspect.getCount());
                if(this.aspectExists.get(aspectName)) aspects.add(aspect);
                else missingAspect.set(true);
            }
        });
        if(!this.hasEnoughVis || missingAspect.get()){
            notifySubs(player -> PacketHandler.sendToPlayer((EntityPlayerMP) player, new PacketAssemblerGUIUpdate(this))); // update client side, to show details in the GUI
            return false; // we don't have the ingredients, tell AE2 we can't craft
        }
        aspectExists = new HashMap<>(); // we have what we need, clear this, since we're not trying to find the aspects anymore
        notifySubs(player -> PacketHandler.sendToPlayer((EntityPlayerMP) player, new PacketAssemblerGUIUpdate(this))); // update client side, to show details in the GUI
        // Craft
        aspects.forEach(aspect -> AEUtil.inventoryExtract(this.channel.createStack(aspect), inventory, this.src));
        if(recipe.getVisCost() > 0){
            final ItemStack visRangeUpgrade = ThEApi.instance().items().upgradeArcane().maybeStack(1).orElseThrow(RuntimeException::new);
            TCUtil.drainVis(this.getWorld(), this.getPos(), recipe.getVisCost(), this.upgradeInv.getUpgrades(visRangeUpgrade));
        }
        this.progress = 0;
        this.craftingInv.setInventorySlotContents(0, result);
        return true;
    }

    @Override
    public boolean isBusy() {
        return !this.craftingInv.getStackInSlot(0).isEmpty();
    }

    @MENetworkEventSubscribe
    public void onChannelChange(MENetworkChannelsChanged event){
        init();
    }

    @MENetworkEventSubscribe
    public void onBootChange(MENetworkPowerStatusChange event){
        if(this.isActive()) init();
    }

    public void init(){
        this.markDirty();
        if(ForgeUtil.isServer()) {
            this.getActionableNode().getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.getActionableNode())); // update ME system available patterns
            this.channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
            ((ITickManager) this.gridNode.getGrid().getCache(ITickManager.class)).wakeDevice(this.gridNode); // wake up, necessary for AE ticks to start again when reloading the world
        }
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        try {
            return GridUtil.getStorageGrid(this).getInventory(channel);
        } catch (GridAccessException e) {
            // Ignored
        }
        return null;
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(ThEApi.instance().config().tickTimeArcaneAssemblerMin(), ThEApi.instance().config().tickTimeArcaneAssemblerMax(), !this.isBusy(), false);
    }

    /**
     * Ticks an existing crafting job
     * @see #pushPattern(ICraftingPatternDetails, InventoryCrafting)
     */
    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        if(!this.isActive()) return TickRateModulation.SLEEP;
        if(!this.craftingInv.getStackInSlot(0).isEmpty()){
            if(this.progress == 0) this.markDirty();
            this.progress += getStep();
            if(progress >= 100){
                AEUtil.inventoryInsert(this.channel.createStack(this.craftingInv.getStackInSlot(0)), this.getInventory(this.channel), this.src);
                this.craftingInv.removeStackFromSlot(0);
                this.markDirty();
            }
            return TickRateModulation.URGENT;
        }
        return TickRateModulation.SLOWER;
    }

    public HashMap<String, Boolean> getAspectExists() {
        return this.aspectExists;
    }

    public void setAspectExists(HashMap<String, Boolean> aspectExists){
        if(ForgeUtil.isClient())
            this.aspectExists = aspectExists;
    }

    public boolean getHasEnoughVis(){
        return this.hasEnoughVis;
    }

    public void setHasEnoughVis(boolean hasEnoughVis){
        this.hasEnoughVis = hasEnoughVis;
    }

    protected int getStep(){
        AtomicInteger step = new AtomicInteger(BASE_STEP);
        AEApi.instance().definitions().materials().cardSpeed().maybeStack(1).ifPresent(cardSpeed -> step.set((int) (BASE_STEP + Math.pow(3, this.upgradeInv.getUpgrades(cardSpeed)))));
        return step.get();
    }

    protected float getWorldVis() {
        return ThEApi.instance().items().upgradeArcane().maybeStack(1).map(visRangeUpgrade -> {
            float vis = AuraHelper.getVis(this.getWorld(), this.getPos());
            if(this.upgradeInv.getUpgrades(visRangeUpgrade) > 0){
                vis += AuraHelper.getVis(this.getWorld(), this.getPos().add(-16, 0, -16));
                vis += AuraHelper.getVis(this.getWorld(), this.getPos().add(-16, 0, 0));
                vis += AuraHelper.getVis(this.getWorld(), this.getPos().add(-16, 0, 16));

                vis += AuraHelper.getVis(this.getWorld(), this.getPos().add(0, 0, -16));
                vis += AuraHelper.getVis(this.getWorld(), this.getPos().add(0, 0, 16));

                vis += AuraHelper.getVis(this.getWorld(), this.getPos().add(16, 0, -16));
                vis += AuraHelper.getVis(this.getWorld(), this.getPos().add(16, 0, 0));
                vis += AuraHelper.getVis(this.getWorld(), this.getPos().add(16, 0, 16));
            }
            return vis;
        }).orElse(AuraHelper.getVis(this.getWorld(), this.getPos()));
    }

    public ThEInternalInventory getCraftingInv() {
        return this.craftingInv;
    }

    @Override
    public void getDrops(World world, BlockPos blockPos, List<ItemStack> list) {
        super.getDrops(world, blockPos, list);
        this.coreInv.iterator().forEachRemaining(list::add);
        this.upgradeInv.iterator().forEachRemaining(list::add);
    }
}
