package thaumicenergistics.common.items;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.parts.AEPartsEnum;
import thaumicenergistics.common.utils.ThELog;
import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base class for all ThE's AE2 cable parts.
 *
 * @author Nividica
 *
 */
public class ItemAEPart extends Item implements IPartItem, IItemGroup {

    /**
     * Constructor
     */
    public ItemAEPart() {
        // Undamageable
        this.setMaxDamage(0);

        // Has sub types
        this.setHasSubtypes(true);

        // Can be rendered on a cable.
        AEApi.instance().partHelper().setItemBusRenderer(this);

        // Register parts who can take an upgrade card.
        Map<Upgrades, Integer> possibleUpgradesList;
        for (AEPartsEnum part : AEPartsEnum.VALUES) {
            possibleUpgradesList = part.getUpgrades();

            for (Upgrades upgrade : possibleUpgradesList.keySet()) {
                upgrade.registerItem(
                        new ItemStack(this, 1, part.ordinal()),
                        possibleUpgradesList.get(upgrade).intValue());
            }
        }
    }

    @Override
    public IPart createPartFromItemStack(final ItemStack itemStack) {
        IPart newPart = null;

        // Get the part
        AEPartsEnum part = AEPartsEnum.getPartFromDamageValue(itemStack);

        // Attempt to create a new instance of the part
        try {
            newPart = part.createPartInstance(itemStack);
        } catch (Throwable e) {
            // Bad stuff, log the error.
            ThELog.error(e, "Unable to create cable-part from item: %s", itemStack.getDisplayName());
        }

        // Return the part
        return newPart;
    }

    @Override
    public EnumRarity getRarity(final ItemStack itemStack) {
        return EnumRarity.rare;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getSpriteNumber() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getSubItems(final Item item, final CreativeTabs tab,
            @SuppressWarnings("rawtypes") final List itemList) {
        // Get the number of parts
        int count = AEPartsEnum.VALUES.length;

        // Add each one to the list
        for (int i = 0; i < count; i++) {
            itemList.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public String getUnlocalizedGroupName(final Set<ItemStack> arg0, final ItemStack itemStack) {
        return AEPartsEnum.getPartFromDamageValue(itemStack).getGroupName();
    }

    @Override
    public String getUnlocalizedName() {
        return ThaumicEnergistics.MOD_ID + ".item.aeparts";
    }

    @Override
    public String getUnlocalizedName(final ItemStack itemStack) {
        return AEPartsEnum.getPartFromDamageValue(itemStack).getUnlocalizedName();
    }

    @Override
    public boolean onItemUse(final ItemStack itemStack, final EntityPlayer player, final World world, final int x,
            final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ) {
        // Can we place the item on the bus?
        return AEApi.instance().partHelper().placeBus(itemStack, x, y, z, side, player, world);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(final IIconRegister par1IconRegister) {}
}
