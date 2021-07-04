package thaumicenergistics.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.tile.TileArcaneAssembler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * @author Alex811
 */
public class BlockArcaneAssembler extends BlockNetwork implements IThEModel{

    public BlockArcaneAssembler(String id) {
        super(id);
        this.blockSoundType = SoundType.GLASS;
        this.fullBlock = false;
        this.lightOpacity = 1;
        this.translucent = true;
        this.setHarvestLevel("pickaxe", 0);
        this.setHardness(2.2F);
    }

    @Override
    public void registerTileEntity() {
        super.registerTileEntity();
        GameRegistry.registerTileEntity(TileArcaneAssembler.class, Objects.requireNonNull(getRegistryName()));
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote || hand != EnumHand.MAIN_HAND)
            return !player.isSneaking();
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileArcaneAssembler) {
            TileArcaneAssembler arcAssembTE = (TileArcaneAssembler) te;
            arcAssembTE.openGUI(player);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileArcaneAssembler();
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory"));
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return (layer == BlockRenderLayer.TRANSLUCENT) || (layer == BlockRenderLayer.SOLID);
    }

    @Override
    @ParametersAreNonnullByDefault
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    @SuppressWarnings("deprecation")
    public boolean isTranslucent(IBlockState state) {
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        AxisAlignedBB boundingBox = super.getBoundingBox(state, source, pos);
        return boundingBox.shrink(0.0625);
    }

    @Override
    @ParametersAreNonnullByDefault
    @SuppressWarnings("deprecation")
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
