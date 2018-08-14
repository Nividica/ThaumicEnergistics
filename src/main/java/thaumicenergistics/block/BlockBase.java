package thaumicenergistics.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public abstract class BlockBase extends Block {

    public BlockBase(String id) {
        this(id, Material.IRON);
    }

    public BlockBase(String id, Material material) {
        super(material);
        this.setRegistryName(id);
        this.setUnlocalizedName(ModGlobals.MOD_ID + "." + id);
        this.setCreativeTab(ModGlobals.CREATIVE_TAB);
        this.setHardness(1f);
    }

    public void registerTileEntity() {}
}
