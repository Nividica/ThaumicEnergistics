package thaumicenergistics.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import thaumicenergistics.client.render.ThEModelLoader;

/**
 * Contains useful constant values
 *
 * @author BrockWS
 */
public class ModGlobals {

    public static final String MOD_ID = "thaumicenergistics";
    public static final String MOD_NAME = "Thaumic Energistics";
    public static final String MOD_VERSION = "alpha-1";
    public static final String MOD_DEPENDENCIES = "required-after:appliedenergistics2@[rv6-alpha-3,);required-after:thaumcraft@[6.1.BETA21,)";

    /**
     * Creative tab.
     */
    public static CreativeTabs CREATIVE_TAB = new CreativeTabs("ThaumicEnergistics") {
        // TODO: Tab Icon
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
    public static final ThEModelLoader MODEL_LOADER = new ThEModelLoader();
}
