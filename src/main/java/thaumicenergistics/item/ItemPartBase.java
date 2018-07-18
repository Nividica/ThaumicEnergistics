package thaumicenergistics.item;

import thaumicenergistics.api.model.IThEModel;

import appeng.api.parts.IPartItem;

/**
 * @author BrockWS
 */
public abstract class ItemPartBase extends ItemBase implements IPartItem, IThEModel {

    public ItemPartBase(String id) {
        super(id);
    }
}
