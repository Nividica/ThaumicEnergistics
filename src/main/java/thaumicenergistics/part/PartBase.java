package thaumicenergistics.part;

import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.api.networking.events.MENetworkBootingStatusChange;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.*;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;

import thaumicenergistics.integration.appeng.grid.GridUtil;
import thaumicenergistics.integration.appeng.grid.IThEGridHost;
import thaumicenergistics.integration.appeng.grid.ThEGridBlock;
import thaumicenergistics.integration.appeng.util.ThEActionSource;
import thaumicenergistics.item.ItemPartBase;
import thaumicenergistics.util.ForgeUtil;
import thaumicenergistics.util.IThEGridNodeBlock;
import thaumicenergistics.util.IThEOwnable;
import thaumicenergistics.util.ItemHandlerUtil;

import io.netty.buffer.ByteBuf;
import thaumicenergistics.util.inventory.IThEInvTile;

/**
 * @author BrockWS
 * @author Alex811
 */
public abstract class PartBase implements IPart, IThEGridHost, IUpgradeableHost, IActionHost, IPowerChannelState, IThEInvTile, IThEOwnable, IThEGridNodeBlock {

    protected ThEGridBlock gridBlock;
    protected IGridNode gridNode;
    protected IPartHost host;
    protected TileEntity hostTile;
    protected EntityPlayer owner;
    protected ItemPartBase item;
    protected int lightOpacity = -1;
    public AEPartLocation side;
    public IActionSource source;

    protected boolean isPowered;
    protected boolean isActive;

    public PartBase(ItemPartBase item) {
        this.item = item;
        this.source = new ThEActionSource(this);
    }

    public ItemStack getRepr() {
        return new ItemStack(this.item);
    }

    @Override
    public ItemStack getItemStack(PartItemStack type) {
        return getRepr();
    }

    public boolean canWork() {
        return false;
    }

    public double getIdlePowerUsage() {
        return 0;
    }

    @Override
    public DimensionalCoord getLocation() {
        if (this.hostTile != null && this.hostTile.hasWorld() && this.hostTile.getWorld().provider != null)
            return new DimensionalCoord(this.hostTile.getWorld(), this.hostTile.getPos());
        return null;
    }

    @Override
    public boolean requireDynamicRender() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canConnectRedstone() {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        if (gridNode != null)
            this.gridNode.saveToNBT("part", nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (gridNode != null)
            this.gridNode.loadFromNBT("part", nbt);
    }

    protected int blockLight(int emit){
        if(this.lightOpacity >= 0)
            return (int) (emit * (this.lightOpacity / 255.0F));
        TileEntity te = this.getTile();
        return this.lightOpacity = 255 - te.getWorld().getBlockLightOpacity(te.getPos().offset(this.side.getFacing()));
    }

    @Override
    public int getLightLevel() {
        return this.blockLight(this.isPowered() ? 9 : 0);
    }

    @Override
    public boolean isLadder(EntityLivingBase entityLivingBase) {
        return false;
    }

    @Override
    public void onNeighborChanged(IBlockAccess iBlockAccess, BlockPos blockPos, BlockPos blockPos1) {
        this.host.markForUpdate();
    }

    @Override
    public int isProvidingStrongPower() {
        return 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return 0;
    }

    @Override
    public void writeToStream(ByteBuf buf) {
        buf.writeBoolean(this.isActive());
        buf.writeBoolean(this.isPowered());
    }

    @Override
    public boolean readFromStream(ByteBuf buf) {
        this.isActive = buf.readBoolean();
        this.isPowered = buf.readBoolean();
        return true;
    }

    @Override
    public IGridNode getGridNode() {
        return this.gridNode;
    }

    @Override
    public void onEntityCollision(Entity entity) {

    }

    @Override
    public void removeFromWorld() {
        if (this.gridNode != null)
            this.gridNode.destroy();
    }

    @Override
    public void addToWorld() {
        if (ForgeUtil.isClient())
            return;
        this.gridBlock = new ThEGridBlock(this);
        this.gridNode = AEApi.instance().grid().createGridNode(this.gridBlock);
        this.initGridNodeOwner();
        this.gridNode.updateState();
        //this.setPower(null); TODO
        BlockPos pos = this.gridBlock.getLocation().getPos();
        this.onNeighborChanged(null, pos, pos.offset(this.side.getFacing()));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return null;
    }

    @Override
    public void setPartHostInfo(AEPartLocation side, IPartHost host, TileEntity te) {
        this.side = side;
        this.host = host;
        this.hostTile = te;
        // TODO this.setPower(null);
    }

    @Override
    public boolean onActivate(EntityPlayer entityPlayer, EnumHand enumHand, Vec3d vec3d) {
        return false;
    }

    @Override
    public boolean onShiftActivate(EntityPlayer entityPlayer, EnumHand enumHand, Vec3d vec3d) {
        return false;
    }

    @Override
    public void getDrops(List<ItemStack> list, boolean b) {
        list.addAll(ItemHandlerUtil.getInventoryAsList(this.getInventoryByName("upgrades")));
    }

    @Override
    public float getCableConnectionLength(AECableType aeCableType) {
        return 3;
    }

    @Override
    public void randomDisplayTick(World world, BlockPos blockPos, Random random) {

    }

    @Override
    public void onPlacement(EntityPlayer player, EnumHand hand, ItemStack stack, AEPartLocation side) {
        this.setOwner(player);
    }

    @Override
    public void setOwner(EntityPlayer player) {
        this.owner = player;
    }

    @Override
    public EntityPlayer getOwner() {
        return this.owner;
    }

    @Override
    public boolean canBePlacedOn(BusSupport busSupport) {
        return busSupport == BusSupport.CABLE;
    }

    @Override
    public void getBoxes(IPartCollisionHelper box) {
        // Should be overridden and not used
        box.addBox(4, 4, 12, 12, 12, 14);
        box.addBox(6, 6, 11, 10, 10, 12);
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull AEPartLocation dir) {
        return this.gridNode;
    }

    @Override
    public ThEGridBlock getGridBlock() {
        return this.gridBlock;
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation dir) {
        return AECableType.GLASS;
    }

    @Override
    public void securityBreak() {
        if(this.getRepr().isEmpty() || this.getGridNode() == null) return;
        this.host.removePart(this.side, false);
        EnumFacing facing = this.side.getFacing();
        Vec3d offset = new Vec3d(facing.getXOffset(), facing.getYOffset(), facing.getZOffset());
        offset = offset.scale(.5);
        BlockPos pos = this.getTile().getPos();
        Vec3d posVec = new Vec3d(pos).add(.5, .5, .5).add(offset);
        World world = this.getTile().getWorld();
        world.playEvent(2001, pos, Block.getStateId(world.getBlockState(pos)));
        world.spawnEntity(new EntityItem(world, posVec.x, posVec.y, posVec.z, this.getRepr().copy()));
    }

    @Override
    public boolean isPowered() {
        return this.isPowered;
    }

    @Override
    public boolean isActive() {
        return this.gridNode != null ? this.gridNode.isActive() : this.isActive;
    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return this.gridNode;
    }

    @MENetworkEventSubscribe
    public final void updateBootStatus(MENetworkBootingStatusChange event) {
        this.host.markForUpdate();
    }

    @MENetworkEventSubscribe
    public void updatePowerStatus(MENetworkPowerStatusChange event) {
        try {
            this.isPowered = GridUtil.getEnergyGrid(this).isNetworkPowered();
            this.host.markForUpdate();
        } catch (GridAccessException e) {
            // should ignore?
            this.isPowered = false;
        }
    }

    @Override
    public void gridChanged() {

    }

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        return 0;
    }

    @Override
    public TileEntity getTile() {
        return this.hostTile;
    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        return null;
    }
}
