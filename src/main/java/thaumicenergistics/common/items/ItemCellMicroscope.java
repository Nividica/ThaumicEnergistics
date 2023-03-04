package thaumicenergistics.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import thaumcraft.api.research.ScanResult;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.relics.ItemThaumometer;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketScannedToServer;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumicenergistics.common.registries.ThEStrings;
import appeng.api.AEApi;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.items.storage.ItemBasicStorageCell;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCellMicroscope extends ItemThaumometer {

    public ItemCellMicroscope() {
        super();
    }

    ItemStack startCell = null;

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerIcon("thaumcraft:blank");
    }

    @Override
    public String getUnlocalizedName() {
        return ThEStrings.Item_Cell_Microscope.getUnlocalized();
    }

    @Override
    public String getUnlocalizedName(final ItemStack itemStack) {
        return this.getUnlocalizedName();
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemstack) {
        return 50;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer p) {
        if (world.isRemote) {
            ItemStack cell = this.doScan(stack, world, p, 0);
            if (cell != null) {
                this.startCell = cell;
            }
        }

        p.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer p, int count) {
        if (p.worldObj.isRemote
                && p.getCommandSenderName() == Minecraft.getMinecraft().thePlayer.getCommandSenderName()) {

            ItemStack cell = this.doScan(stack, p.worldObj, p, count);

            if (cell != null && cell.equals(this.startCell)) {

                if (count <= 5) {
                    this.startCell = null;
                    p.stopUsingItem();

                    IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell()
                            .getCellInventory(cell, null, StorageChannel.ITEMS);
                    IItemList<IAEItemStack> itemList = inv
                            .getAvailableItems(AEApi.instance().storage().createItemList());

                    for (final IAEItemStack i : itemList) {
                        ScanResult sr = new ScanResult(
                                (byte) 1,
                                Item.getIdFromItem(i.getItem()),
                                i.getItemDamage(),
                                (Entity) null,
                                "");
                        if (ScanManager.isValidScanTarget(p, sr, "@")) {

                            ScanManager.completeScan(p, sr, "@");
                            PacketHandler.INSTANCE.sendToServer(new PacketScannedToServer(sr, p, "@"));
                        }
                    }
                }

                if (count % 2 == 0) {
                    p.worldObj.playSound(
                            p.posX,
                            p.posY,
                            p.posZ,
                            "thaumcraft:cameraticks",
                            0.2F,
                            0.45F + ((float) count / 50) + p.worldObj.rand.nextFloat() * 0.1F,
                            false);
                }
            } else {
                this.startCell = null;
            }
        }

    }

    private ItemStack doScan(ItemStack stack, World world, EntityPlayer p, int count) {

        Entity pointedEntity = EntityUtils.getPointedEntity(p.worldObj, p, 0.5D, 10.0D, 0.0F, true);

        if (pointedEntity != null) {
            if (pointedEntity instanceof EntityItem) {
                if (((EntityItem) pointedEntity).getEntityItem().getItem() instanceof ItemBasicStorageCell) {
                    MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(p.worldObj, p, true);
                    try {
                        Thaumcraft.proxy.blockRunes(
                                world,
                                (double) mop.blockX,
                                (double) mop.blockY + 0.50D,
                                (double) mop.blockZ,
                                0.3F,
                                0.3F,
                                0.7F + world.rand.nextFloat() * 0.7F,
                                15,
                                -0.03F);
                    } catch (Exception e) {}

                    ItemStack cell = (((EntityItem) pointedEntity).getEntityItem());
                    return cell;
                }
            }
        }

        return null;

    }

    public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer,
            int par4) {
        super.onPlayerStoppedUsing(par1ItemStack, par2World, par3EntityPlayer, par4);
        this.startCell = null;
    }
}
