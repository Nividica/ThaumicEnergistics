package thaumicenergistics.common.registries;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.MinecraftForgeClient;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.render.*;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.tiles.TileArcaneAssembler;
import thaumicenergistics.common.tiles.TileGearBox;

/**
 * Manages ThE renderers.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class Renderers {
    public static final int PASS_OPAQUE = 0;
    public static final int PASS_ALPHA = 1;

    public static int currentRenderPass = 0;

    public static int EssentiaProviderRenderID, InfusionProviderRenderID;

    public static void registerRenderers() {
        // Get the next render ID
        Renderers.EssentiaProviderRenderID = RenderingRegistry.getNextAvailableRenderId();
        // Register the essentia provider renderer
        RenderingRegistry.registerBlockHandler(new RenderBlockEssentiaProvider());

        // Get the next render ID
        Renderers.InfusionProviderRenderID = RenderingRegistry.getNextAvailableRenderId();

        // Register the infusion provider renderer
        RenderingRegistry.registerBlockHandler(new RenderBlockInfusionProvider());

        // Are gearbox models enabled?
        if (!ThEApi.instance().config().disableGearboxModel()) {
            // Register the gearbox renderer
            ClientRegistry.bindTileEntitySpecialRenderer(TileGearBox.class, new RenderTileGearbox());

            // Register thaumium gearbox item renderer
            MinecraftForgeClient.registerItemRenderer(
                    ThEApi.instance().blocks().ThaumiumGearBox.getItem(),
                    new TileAsItemRenderer(new RenderTileGearbox(), new TileGearBox(true)));

            // Register iron gearbox item renderer
            MinecraftForgeClient.registerItemRenderer(
                    ThEApi.instance().blocks().IronGearBox.getItem(),
                    new TileAsItemRenderer(new RenderTileGearbox(), new TileGearBox(false)));
        }

        // Register the arcane assembler renderer
        ClientRegistry.bindTileEntitySpecialRenderer(TileArcaneAssembler.class, new RenderTileArcaneAssembler());

        // Register arcane assembler item renderer
        MinecraftForgeClient.registerItemRenderer(
                ThEApi.instance().blocks().ArcaneAssembler.getItem(),
                new TileAsItemRenderer(new RenderTileArcaneAssembler(), new TileArcaneAssembler()));

        // Register crafting aspect item renderer
        MinecraftForgeClient.registerItemRenderer(ItemEnum.CRAFTING_ASPECT.getItem(), new RendererItemCraftingAspect());
    }
}
