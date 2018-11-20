package thaumicenergistics.item;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;

import thaumicenergistics.api.IThEUpgrade;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public class ItemMaterial extends ItemBase implements IThEModel {

    public ItemMaterial(String id) {
        super(id);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        Optional<IThEUpgrade> optional = ThEApi.instance().upgrades().getUpgrade(stack);
        optional.ifPresent(upgrade -> {
            upgrade.getSupported().forEach((stack1, integer) -> {
                tooltip.add(stack1.getDisplayName() + " (" + integer + ")");
            });
        });

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public void initModel() {
        Preconditions.checkNotNull(this.getRegistryName());
        Preconditions.checkNotNull(this.getRegistryName().getResourcePath());

        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":material/" + this.getRegistryName().getResourcePath(), "inventory"));
    }
}
