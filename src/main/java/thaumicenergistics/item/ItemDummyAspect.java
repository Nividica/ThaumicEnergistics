package thaumicenergistics.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import net.minecraftforge.client.model.ModelLoader;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.client.render.DummyAspectItemModel;
import thaumicenergistics.client.render.DummyAspectRenderer;
import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.client.render.ThEModelLoader;
import thaumicenergistics.init.ModGlobals;

/**
 * A dummy item that uses the aspect icon for its icon
 * Used by AEEssentiaStack
 * <p>
 * TODO: Render item based on Aspect Image + Colour
 *
 * @author BrockWS
 */
public class ItemDummyAspect extends ItemBase implements IThEModel {

    public ItemDummyAspect() {
        super("dummy_aspect", false);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        // TODO: Probably set name to aspect name
        //this.setTileEntityItemStackRenderer(DummyAspectRenderer.INSTANCE);
    }

    public void setAspect(ItemStack stack, Aspect aspect) {
        if (aspect != null) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("aspect", aspect.getTag());
            stack.setTagCompound(tag);
        } else {
            stack.setTagCompound(null);
        }
    }

    public Aspect getAspect(ItemStack stack) {
        return stack.hasTagCompound() ? Aspect.getAspect(stack.getTagCompound().getString("aspect")) : null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (this.getAspect(stack) != null) {
            ItemDummyAspect item = (ItemDummyAspect) stack.getItem();
            return item.getAspect(stack).getName();
        }
        return "[ERROR] Report to mod dev";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        // Hide from create tabs
    }

    @Override
    public void initModel() {
        this.setTileEntityItemStackRenderer(DummyAspectRenderer.INSTANCE);
        //ThEModelLoader.MODEL_LOADER.addModel("models/item/dummy_aspect", new DummyAspectItemModel());
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":dummy_aspect", "inventory"));
        //ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }
}
