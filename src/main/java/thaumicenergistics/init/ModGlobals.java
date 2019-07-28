package thaumicenergistics.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import thaumicenergistics.api.ThEApi;

/**
 * Contains useful constant values
 *
 * @author BrockWS
 */
public class ModGlobals {

    public static final String MOD_ID = "thaumicenergistics";
    public static final String MOD_NAME = "Thaumic Energistics";
    public static final String MOD_VERSION = "2.2.2";
    public static final String MOD_DEPENDENCIES = "required-after:appliedenergistics2@[rv6-stable-6,);" +
            "required-after:thaumcraft@[6.1.BETA26,);" +
            "after:thaumicjei";

    /**
     * Creative tab.
     */
    public static CreativeTabs CREATIVE_TAB = new CreativeTabs("ThaumicEnergistics") {

        @Override
        public ItemStack createIcon() {
            ItemStack icon = ThEApi.instance().items().essentiaCell1k().maybeStack(1).orElse(ItemStack.EMPTY);
            if (icon.isEmpty())
                throw new NullPointerException("Unable to use essentiaCell1k for creative tab!");
            return icon;
        }
    };

    public static final String RESEARCH_CATEGORY = ModGlobals.MOD_ID.toUpperCase();

    public static final String MOD_ID_AE2 = "appliedenergistics2";
}
