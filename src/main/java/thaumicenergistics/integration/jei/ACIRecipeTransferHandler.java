package thaumicenergistics.integration.jei;

import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import thaumicenergistics.container.part.ContainerArcaneInscriber;

/**
 * @author Alex811
 */
public class ACIRecipeTransferHandler<C extends ContainerArcaneInscriber> extends ACTRecipeTransferHandler<C> implements IRecipeTransferHandler<C> {
    public ACIRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        super(helper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<C> getContainerClass() {
        return (Class<C>) ContainerArcaneInscriber.class;
    }
}
