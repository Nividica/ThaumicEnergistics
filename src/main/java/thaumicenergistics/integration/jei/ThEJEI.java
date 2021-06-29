package thaumicenergistics.integration.jei;

import com.google.common.base.Strings;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import thaumicenergistics.api.IThEItems;
import thaumicenergistics.api.ThEApi;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author BrockWS
 * @author Alex811
 */
@JEIPlugin
public class ThEJEI implements IModPlugin {
    private static IJeiRuntime runtime;

    @Override
    @ParametersAreNonnullByDefault
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime){
        runtime = jeiRuntime;
    }

    public static String getSearchText(){
        return Strings.nullToEmpty(runtime.getIngredientFilter().getFilterText());
    }

    public static void setSearchText(String searchText){
        runtime.getIngredientFilter().setFilterText(Strings.nullToEmpty(searchText));
    }

    @Override
    public void register(IModRegistry registry) {
        IRecipeTransferHandlerHelper rthh = registry.getJeiHelpers().recipeTransferHandlerHelper();
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        IThEItems items = ThEApi.instance().items();

        items.arcaneTerminal().maybeStack(1).ifPresent(stack -> registerWorkbenchCatalyst(registry, new ACTRecipeTransferHandler<>(rthh), stack));
        items.arcaneInscriber().maybeStack(1).ifPresent(stack -> registerWorkbenchCatalyst(registry, new ACIRecipeTransferHandler<>(rthh), stack));

        items.dummyAspect().maybeStack(1).ifPresent(blacklist::addIngredientToBlacklist);
    }

    public void registerWorkbenchCatalyst(IModRegistry registry, IRecipeTransferHandler<? extends Container> handler, ItemStack stack){
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(handler, VanillaRecipeCategoryUid.CRAFTING);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(handler, "THAUMCRAFT_ARCANE_WORKBENCH");
        registry.addRecipeCatalyst(stack, "THAUMCRAFT_ARCANE_WORKBENCH");
    }
}
