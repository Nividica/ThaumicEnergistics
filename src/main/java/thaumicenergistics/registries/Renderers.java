package thaumicenergistics.registries;

import thaumicenergistics.render.RenderBlockEssentiaProvider;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class Renderers
{
	public static final int PASS_OPAQUE = 0;
	public static final int PASS_ALPHA = 1;
	
	public static int currentRenderPass = 0;
	
	public static int EssentiaProviderRenderID;
	
	public static void registerRenderers()
	{
		// Get the next render ID
		Renderers.EssentiaProviderRenderID = RenderingRegistry.getNextAvailableRenderId();
		// Register the essentia provider renderer
		RenderingRegistry.registerBlockHandler( new RenderBlockEssentiaProvider() );
	}
}
