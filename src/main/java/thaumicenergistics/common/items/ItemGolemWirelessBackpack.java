package thaumicenergistics.common.items;

import appeng.api.features.INetworkEncodable;
import appeng.core.localization.GuiText;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.registries.ThEStrings;

/**
 * Gives golems access to the AE network.
 *
 * @author Nividica
 *
 */
public class ItemGolemWirelessBackpack extends Item implements INetworkEncodable {
    public static enum BackpackSkins {
        Thaumium("Thaum"),
        Stone("Stone"),
        Straw("Straw"),
        GreatWood("Wood"),
        Flesh("Flesh"),
        Clay("Clay"),
        Iron("Iron"),
        Tallow("Tallow"),
        Gold("Gold"),
        Diamond("Diamond");

        /**
         * Array of skins.
         */
        public static final BackpackSkins VALUES[] = BackpackSkins.values();

        /**
         * Skin texture.
         */
        private ResourceLocation texture = null;

        /**
         * Texture ID of the skin
         */
        private final String texID;

        /**
         * Private constructor
         *
         * @param ID
         */
        private BackpackSkins(final String ID) {
            this.texID = ID;
        }

        /**
         * Gets the texture for this skin.
         *
         * @return
         */
        public ResourceLocation getTextureLocation() {
            if (this.texture == null) {
                this.texture = new ResourceLocation(
                        ThaumicEnergistics.MOD_ID, "textures/models/golemBackpack/" + this.texID + ".png");
            }
            return this.texture;
        }
    }

    private static final String NBT_ENCRYPTION_KEY = "wifiKey";

    public ItemGolemWirelessBackpack() {
        // Set the texture
        this.setTextureName(ThaumicEnergistics.MOD_ID + ":golem.wifi.backpack");

        // Set non stacking
        this.setMaxStackSize(1);

        // Set non-damageable
        this.setMaxDamage(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(
            final ItemStack stack, final EntityPlayer player, final List list, final boolean advancedInfo) {
        if (stack.hasTagCompound()) {
            list.add(GuiText.Linked.getLocal());
        } else {
            list.add(GuiText.Unlinked.getLocal());
        }
    }

    @Override
    public String getEncryptionKey(final ItemStack stack) {
        if (stack.hasTagCompound() && stack.stackTagCompound.hasKey(NBT_ENCRYPTION_KEY)) {
            return stack.stackTagCompound.getString(NBT_ENCRYPTION_KEY);
        }
        return null;
    }

    @Override
    public String getUnlocalizedName() {
        return ThEStrings.Item_Golem_Wifi_Backpack.getUnlocalized();
    }

    @Override
    public String getUnlocalizedName(final ItemStack itemStack) {
        return this.getUnlocalizedName();
    }

    @Override
    public void setEncryptionKey(final ItemStack stack, final String encKey, final String name) {
        NBTTagCompound tag;
        if (!stack.hasTagCompound()) {
            tag = new NBTTagCompound();
            stack.stackTagCompound = tag;
        } else {
            tag = stack.stackTagCompound;
        }
        tag.setString(NBT_ENCRYPTION_KEY, encKey);
    }
}
