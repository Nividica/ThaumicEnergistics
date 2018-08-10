package thaumicenergistics.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * Contains useful constant values
 *
 * @author BrockWS
 */
public class ModGlobals {

    public static final String MOD_ID = "thaumicenergistics";
    public static final String MOD_NAME = "Thaumic Energistics";
    public static final String MOD_VERSION = "1.2.0.0";
    public static final String MOD_DEPENDENCIES = "required-after:appliedenergistics2@[rv6-alpha-2,);required-after:thaumcraft@[6.1.BETA17,)";

    /**
     * Creative tab.
     */
    public static CreativeTabs CREATIVE_TAB = new CreativeTabs("ThaumicEnergistics") {

        @Override
        public ItemStack getIconItemStack() {
            return new ItemStack(Items.DIAMOND);
        }

        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(Items.DIAMOND);
        }
    };

    public static final String RESEARCH_CATEGORY = ModGlobals.MOD_ID;
}
